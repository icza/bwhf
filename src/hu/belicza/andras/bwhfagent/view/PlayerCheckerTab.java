package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.textrecognition.CharDef;
import hu.belicza.andras.bwhfagent.view.textrecognition.TextRecognizer;
import hu.belicza.andras.hackerdb.ServerApiConsts;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import swingwt.awt.BorderLayout;
import swingwt.awt.GridBagConstraints;
import swingwt.awt.GridBagLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.JTextField;

/**
 * Player checker tab.<br>
 * This tab provides the functionality to check players in the game lobby if they have been reported before.
 * 
 * @author Andras Belicza
 */
public class PlayerCheckerTab extends LoggedTab {
	
	/** Text of the update now button.           */
	private static final String UPDATE_NOW_BUTTON_TEXT      = "Update now";
	/** Name of the bwhf hacker list cache file. */
	private static final String HACKER_LIST_CACHE_FILE_NAME = "BWHF_hacker_list_cache.txt";
	/** THe hacker list directory.               */
	private static final File   HACKER_LIST_DIRECTORY       = new File( Consts.HACKER_LIST_DIRECTORY_NAME );
	/** The hacker list cache file.              */
	private static final File   HACKER_LIST_CACHE_FILE      = new File( Consts.HACKER_LIST_DIRECTORY_NAME, HACKER_LIST_CACHE_FILE_NAME );
	
	/** Reference to the settings panel. */
	private JPanel settingsPanel;
	
	/** Checkbox to enable/disable the autoscan.                  */
	protected final JCheckBox  playerCheckerEnabledCheckBox       = new JCheckBox( "Enable checking players in the game lobby when pressing the 'Print Screen' key", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_PLAYER_CHECKER_ENABLED ) ) );
	/** Combo box to set the hacker list update interval.         */
	private   final JComboBox  hackerListUpdateIntervalComboBox   = new JComboBox( new Object[] { 1, 2, 6, 12, 24, 48 } );
	/** Label to display the last update time of the hacker list. */
	private   final JLabel     hackerListLastUpdatedLabel         = new JLabel();
	/** Button to update hacker list now.                         */
	private   final JButton    updateNowButton                    = new JButton( UPDATE_NOW_BUTTON_TEXT );
	/** Checkbox to include custom player list.                   */
	private   final JCheckBox  includeCustomPlayerListCheckBox    = new JCheckBox( "Include this custom player list:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_INCLUDE_CUSTOM_PLAYER_LIST ) ) );
	/** File of the custom player list.                           */
	private   final JTextField customPlayerListFileTextField      = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_CUSTOM_PLAYER_LIST_FILE ) );
	/** Button to reload custom player list.                      */
	private   final JButton    reloadButton                       = new JButton( "Reload" );
	/** Checkbox to enable/disable the autoscan.                  */
	private   final JCheckBox  deleteGameLobbyScreenshotsCheckBox = new JCheckBox( "Delete game lobby screenshots after checking players", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS ) ) );
	
	/** BWHF hackers mapped to their gateways.   */
	private final Map< Integer, Set< String > > gatewayBwhfHackerSetMap   = new HashMap< Integer, Set< String > >();
	/** Custom players mapped to their gateways. */
	private final Map< Integer, Set< String > > gatewayCustomPlayerSetMap = new HashMap< Integer, Set< String > >();
	
	/**
	 * Creates a new PlayerCheckerTab.
	 */
	public PlayerCheckerTab() {
		super( "Player checker", "player_checker.log" );
		
		buildGUI();
		
		hackerListUpdateIntervalComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_HACKER_LIST_UPDATE_INTERVAL ) ) );
		
		refreshLastUpdatedLabel();
		
		reloadPlayerList( HACKER_LIST_CACHE_FILE, gatewayBwhfHackerSetMap );
		includeCustomPlayerListCheckBox.doClick();
		
		// Load the CharDef class to avoid delays later on
		CharDef.class.toString();
	}
	
	@Override
	protected void buildGUI() {
		final GridBagLayout      gridBagLayout = new GridBagLayout();
		final GridBagConstraints constraints   = new GridBagConstraints();
		settingsPanel = new JPanel( gridBagLayout );
		settingsPanel.setBorder( BorderFactory.createTitledBorder( "Settings:" ) );
		
		JPanel  wrapperPanel;
		JLabel  label;
		JButton button;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperPanel = Utils.wrapInPanel( playerCheckerEnabledCheckBox );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Automatically update hacker list in every " );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( hackerListUpdateIntervalComboBox, constraints );
		settingsPanel.add( hackerListUpdateIntervalComboBox );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		label = new JLabel( " hours." );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Hacker list last updated at:" );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		gridBagLayout.setConstraints( hackerListLastUpdatedLabel, constraints );
		settingsPanel.add( hackerListLastUpdatedLabel );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		// In SwingWT Button text cannot be changed if a mnemonic has been assigned
		//updateNowButton.setMnemonic( ( updateNowButton.getText().charAt( 0 ) ) );
		updateNowButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				updateHackerList();
			}
		} );
		gridBagLayout.setConstraints( updateNowButton, constraints );
		settingsPanel.add( updateNowButton );
		
		constraints.gridwidth = 1;
		includeCustomPlayerListCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				reloadButton.setEnabled( includeCustomPlayerListCheckBox.isSelected() );
				if ( reloadButton.isEnabled() )
					reloadButton.doClick();
			}
		} );
		gridBagLayout.setConstraints( includeCustomPlayerListCheckBox, constraints );
		settingsPanel.add( includeCustomPlayerListCheckBox );
		gridBagLayout.setConstraints( customPlayerListFileTextField, constraints );
		settingsPanel.add( customPlayerListFileTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperPanel = new JPanel( new BorderLayout() );
		button = Utils.createFileChooserButton( getContent(), customPlayerListFileTextField, JFileChooser.FILES_ONLY, Utils.SWING_TEXT_FILE_FILTER, new String[][] { new String[] { "*.txt", "*.*" }, new String[] { "Text files (*.txt)", "All files (*.*)" } }, new Runnable() {
			public void run() {
				final File selectedFile = new File( customPlayerListFileTextField.getText() );
				if ( selectedFile.getAbsolutePath().equals( new File( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() ).getAbsolutePath() ) )
					customPlayerListFileTextField.setText( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() );
			}
		} );
		wrapperPanel.add( button, BorderLayout.WEST );
		reloadButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				reloadPlayerList( new File( customPlayerListFileTextField.getText() ), gatewayCustomPlayerSetMap );
			}
		} );
		wrapperPanel.add( reloadButton, BorderLayout.CENTER );
		button = new JButton( "Edit" );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.editFile( customPlayerListFileTextField.getText() );
			}
		} );
		wrapperPanel.add( button, BorderLayout.EAST );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( deleteGameLobbyScreenshotsCheckBox, constraints );
		settingsPanel.add( deleteGameLobbyScreenshotsCheckBox );
		
		contentBox.add( Utils.wrapInPanel( settingsPanel ) );
		
		super.buildGUI();
	}
	
	/**
	 * Check players from the provided screenshot files.
	 * @param screenshotFiles screenshot files to be used to obtain player names
	 * @return the remaining files that were not deleted
	 */
	public synchronized File[] checkPlayers( final File[] screenshotFiles ) {
		final List< File > remainedScreenshotFileList = new ArrayList< File >( screenshotFiles.length );
		
		for ( final File screenshotFile : screenshotFiles ) {
			BufferedImage image = null;
			try {
				image = ImageIO.read( screenshotFile );
			}
			catch ( final IOException ie ) {
			}
			
			if ( image == null || !TextRecognizer.isGameLobbyScreenshot( image ) )
				remainedScreenshotFileList.add( screenshotFile );
			else {
				logMessage( "", false ); // Prints 2 empty lines
				logMessage( "Game lobby screenshot detected, proceeding to check..." );
				
				final int gateway = MainFrame.getInstance().autoscanTab.gatewayComboBox.getSelectedIndex() - 1;
				
				final String[] playerNames = TextRecognizer.readPlayerNamesFromGameLobbyImage( image );
				
				for ( int i = 0; i < playerNames.length; i++ ) {
					final String playerName = playerNames[ i ]; 
					if ( playerName != null ) {
						final boolean exactMatch = playerName.indexOf( 'I' ) < 0 && playerName.indexOf( 'l' ) < 0;
						final String loweredPlayerName = playerName.toLowerCase();
						
						Set< String > playerNameSet;
						if ( ( playerNameSet = gatewayBwhfHackerSetMap.get( gateway ) ) != null && gatewayBwhfHackerSetMap.get( gateway ).contains( loweredPlayerName ) ) {
							logMessage( "Found " + ( exactMatch ? "" : "possible " ) + "hacker player in game lobby: " + playerName );
							Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, exactMatch ? "hacker_at_slot.wav" : "possible_hacker_at_slot.wav" ), true );
							Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, (i+1) + ".wav" ), true );
						}
						else if ( ( playerNameSet = gatewayCustomPlayerSetMap.get( gateway ) ) != null && playerNameSet.contains( loweredPlayerName ) ) {
							logMessage( "Found " + ( exactMatch ? "" : "possible " ) + "custom listed player in game lobby: " + playerName );
							Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, exactMatch ? "custom_at_slot.wav" : "possible_custom_at_slot.wav" ), true );
							Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, (i+1) + ".wav" ), true );
						}
					}
				}
				logMessage( "Player check finished." );
				
				if ( deleteGameLobbyScreenshotsCheckBox.isSelected() )
					screenshotFile.delete();
				else
					remainedScreenshotFileList.add( screenshotFile );
			}
		}
		
		return remainedScreenshotFileList.toArray( new File[ remainedScreenshotFileList.size() ] );
	}
	
	/**
	 * Updates the local cache of the hacker list.
	 */
	private void updateHackerList() {
		if ( !updateNowButton.isEnabled() )
			return;
		
		updateNowButton.setEnabled( false );
		
		updateNowButton.setText( "Updating..." );
		settingsPanel.getParent().validate();
		
		new NormalThread() {
			@Override
			public void run() {
				BufferedReader input  = null;
				PrintWriter    output = null;
				try {
					logMessage( "", false ); // Prints 2 empty lines
					logMessage( "Updating hacker list..." );
					
					if ( HACKER_LIST_DIRECTORY.exists() && HACKER_LIST_DIRECTORY.isFile() ) {
						logMessage( "Error: hacker list cache directory exists and is a file: " + HACKER_LIST_DIRECTORY.getAbsolutePath() + " (You have to delete it first!)" );
						throw new Exception();
					}
					
					if ( !HACKER_LIST_DIRECTORY.exists() && !HACKER_LIST_DIRECTORY.mkdirs() ) {
						logMessage( "Error: could not create hacker list cache directory: " + HACKER_LIST_DIRECTORY.getAbsolutePath() );
						throw new Exception();
					}
					
					input = new BufferedReader( new InputStreamReader( new URL( Consts.BWHF_HACKER_DATA_BASE_SERVER_URL + "?" + ServerApiConsts.REQUEST_PARAMETER_NAME_OPERATION + "=" + ServerApiConsts.OPERATION_DOWNLOAD ).openStream() ) );
					final File tempCacheFile = new File( Consts.HACKER_LIST_DIRECTORY_NAME, HACKER_LIST_CACHE_FILE_NAME + ".dl" );
					output = new PrintWriter( tempCacheFile );
					
					String line;
					while ( ( line = input.readLine() ) != null )
						output.println( line );
					output.flush();
					output.close();
					
					if ( HACKER_LIST_CACHE_FILE.exists() && !HACKER_LIST_CACHE_FILE.delete() ) {
						logMessage( "Error: could not delete old hacker list cache file: " + HACKER_LIST_CACHE_FILE.getAbsolutePath() );
						throw new Exception();
					}
					
					if ( !tempCacheFile.renameTo( HACKER_LIST_CACHE_FILE ) ) {
						logMessage( "Error: could not rename '" + tempCacheFile.getAbsolutePath() + "' to '" + HACKER_LIST_CACHE_FILE.getAbsolutePath() + "'" );
						throw new Exception();
					}
					
					refreshLastUpdatedLabel();
					
					logMessage( "Update succeeded." );
					
					updateNowButton.setText( UPDATE_NOW_BUTTON_TEXT );
					
					reloadPlayerList( HACKER_LIST_CACHE_FILE, gatewayBwhfHackerSetMap );
				}
				catch ( final Exception e ) {
					logMessage( "Update failed!" );
					e.printStackTrace();
					updateNowButton.setText( UPDATE_NOW_BUTTON_TEXT + " (update failed!)" );
				}
				finally {
					if ( output != null )
						output.close();
					if ( input != null )
						try { input.close(); } catch ( final IOException ie ) { ie.printStackTrace(); }
					updateNowButton.setEnabled( true );
					settingsPanel.getParent().validate();
				}
			}
		}.start();
	}
	
	/**
	 * Reloads player lists from a file.
	 * @param file               file containing the list
	 * @param gatewayPlayerSetMap map to be stored the list to
	 */
	private synchronized void reloadPlayerList( final File file, final Map< Integer, Set< String > > gatewayPlayerSetMap ) {
		gatewayPlayerSetMap.clear();
		
		logMessage( "", false ); // Prints 2 empty lines
		logMessage( "Reloading player list from file: '" + file.getAbsolutePath() + "'..." );
		
		BufferedReader input = null;
		int skippedLinesCount = 0;
		int playersCount      = 0;
		try {
			input = new BufferedReader( new FileReader( file ) );
			
			String line;
			Integer lastGateway = null;
			Set< String > playerSet = null;
			while ( ( line = input.readLine() ) != null ) {
				try {
					final int commaIndex = line.indexOf( ',' );
					if ( commaIndex == line.length() - 1 )
						throw new Exception(); // Missing player name
					
					final Integer gateway  = Integer.valueOf( line.substring( 0, commaIndex ) );
					
					if ( !gateway.equals( lastGateway ) ) {
						playerSet = gatewayPlayerSetMap.get( gateway );
						if ( playerSet == null )
							gatewayPlayerSetMap.put( gateway, playerSet = new HashSet< String >() );
					}
					
					playerSet.add( line.substring( commaIndex + 1 ).toLowerCase() );
					playersCount++;
					lastGateway = gateway;
				}
				catch ( final Exception e ) {
					skippedLinesCount++;
				}
			}
			
			if ( skippedLinesCount > 0 )
				logMessage( "Skipped " + skippedLinesCount + " line" + ( skippedLinesCount == 1 ? "." : "s." ) );
			logMessage( "Successfully reloaded " + playersCount + " player" + ( playersCount == 1 ? "" : "s" ) + " from file: " + file.getAbsolutePath() );
		}
		catch ( final Exception e ) {
			logMessage( "Error: failed to load list from file: " + file.getAbsolutePath() );
		}
		finally {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) { ie.printStackTrace(); }
		}
	}
	
	/**
	 * Refreshes the last updated label.
	 */
	private void refreshLastUpdatedLabel() {
		final long lastUpdated = HACKER_LIST_CACHE_FILE.lastModified();
		hackerListLastUpdatedLabel.setText( lastUpdated > 0l ? DATE_FORMAT.format( new Date( lastUpdated ) ) : "&lt;never&gt;" );
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_PLAYER_CHECKER_ENABLED       , Boolean.toString( playerCheckerEnabledCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HACKER_LIST_UPDATE_INTERVAL  , Integer.toString( hackerListUpdateIntervalComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_INCLUDE_CUSTOM_PLAYER_LIST   , Boolean.toString( includeCustomPlayerListCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CUSTOM_PLAYER_LIST_FILE      , customPlayerListFileTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS, Boolean.toString( deleteGameLobbyScreenshotsCheckBox.isSelected() ) );
	}
	
}
