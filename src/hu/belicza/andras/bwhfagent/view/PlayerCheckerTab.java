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
import swingwt.awt.Font;
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
	
	/** Time between checking if there's need to update the local hacker list cache. */
	private static final long   TIME_BETWEEN_CHECKS_FOR_NEED_OF_UPDATE = 10000l;
	/** Ms in an hour.                                                               */
	private static final long   MS_IN_AN_HOUR                          = 60*60*1000l;
	
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
	
	/** Checkbox to enable/disable the autoscan.                       */
	protected final JCheckBox  playerCheckerEnabledCheckBox       = new JCheckBox( "Enable checking players in the game lobby when pressing the 'Print Screen' key", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_PLAYER_CHECKER_ENABLED ) ) );
	/** Combo box to set the hacker list update interval.              */
	private   final JComboBox  hackerListUpdateIntervalComboBox   = new JComboBox( new Object[] { 1, 2, 6, 12, 24, 48 } );
	/** Label to display the last update time of the hacker list.      */
	private   final JLabel     hackerListLastUpdatedLabel         = new JLabel();
	/** Button to update hacker list now.                              */
	private   final JButton    updateNowButton                    = new JButton( UPDATE_NOW_BUTTON_TEXT, IconResourceManager.ICON_DATABASE_REFRESH );
	/** Checkbox to include custom player list.                        */
	private   final JCheckBox  includeCustomPlayerListCheckBox    = new JCheckBox( "Include this custom player list:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_INCLUDE_CUSTOM_PLAYER_LIST ) ) );
	/** File of the custom player list.                                */
	private   final JTextField customPlayerListFileTextField      = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_CUSTOM_PLAYER_LIST_FILE ) );
	/** Button to reload custom player list.                           */
	private   final JButton    reloadButton                       = new JButton( "Reload", IconResourceManager.ICON_ARROW_REFRESH );
	/** Checkbox to enable/disable saying "clean" if no hackers found. */
	private   final JCheckBox  sayCleanCheckBox                   = new JCheckBox( "Say \"clean\" if no hackers found", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SAY_CLEAN ) ) );
	/** Checkbox to enable/disable the autoscan.                       */
	private   final JCheckBox  deleteGameLobbyScreenshotsCheckBox = new JCheckBox( "Delete game lobby screenshots after checking players", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS ) ) );
	/** Checkbox to enable/disable the autoscan.                       */
	private   final JCheckBox  echoRecognizedPlayerNamesCheckBox  = new JCheckBox( "Echo recognized player names in the log below", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_ECHO_RECOGNIZED_PLAYER_NAMES ) ) );
	
	/** BWHF hackers mapped to their gateways.   */
	private final Map< Integer, Set< String > > gatewayBwhfHackerSetMap   = new HashMap< Integer, Set< String > >();
	/** Custom players mapped to their gateways. */
	private final Map< Integer, Set< String > > gatewayCustomPlayerSetMap = new HashMap< Integer, Set< String > >();
	
	/** Last update time. */
	private volatile long lastUpdateTime = HACKER_LIST_CACHE_FILE.lastModified(); // If local cache doesn't exist yet, this returns 0l, and will update immediatelly if player checker is enabled.
	
	/**
	 * Creates a new PlayerCheckerTab.
	 */
	public PlayerCheckerTab() {
		super( "Player checker", IconResourceManager.ICON_PLAYER_CHECKER, "player_checker.log" );
		
		buildGUI();
		
		hackerListUpdateIntervalComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_HACKER_LIST_UPDATE_INTERVAL ) ) );
		
		refreshLastUpdatedLabel();
		
		reloadPlayerList( HACKER_LIST_CACHE_FILE, gatewayBwhfHackerSetMap );
		includeCustomPlayerListCheckBox.doClick();
		
		// Load the CharDef class (and the definitions from file) to avoid delays later on
		CharDef.class.toString();
		
		startCacheAutoUpdater();
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
		label = new JLabel( "Automatically update hacker list cache in every " );
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
		label = new JLabel( "Hacker list cache last updated at:" );
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
				if ( !includeCustomPlayerListCheckBox.isSelected() || customPlayerListFileTextField.getText().length() == 0 )
					gatewayCustomPlayerSetMap.clear();
				else
					reloadPlayerList( new File( customPlayerListFileTextField.getText() ), gatewayCustomPlayerSetMap );
			}
		} );
		wrapperPanel.add( reloadButton, BorderLayout.CENTER );
		button = new JButton( "Edit", IconResourceManager.ICON_EDIT );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.editFile( customPlayerListFileTextField.getText() );
			}
		} );
		wrapperPanel.add( button, BorderLayout.EAST );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( sayCleanCheckBox, constraints );
		settingsPanel.add( sayCleanCheckBox );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( deleteGameLobbyScreenshotsCheckBox, constraints );
		settingsPanel.add( deleteGameLobbyScreenshotsCheckBox );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( echoRecognizedPlayerNamesCheckBox, constraints );
		settingsPanel.add( echoRecognizedPlayerNamesCheckBox );
		
		contentBox.add( Utils.wrapInPanel( settingsPanel ) );
		
		final JLabel noteLabel = new JLabel( "Note that you will only be notified of players reported with the gateway set on your autoscan tab! (Always keep it synchronized with the gateway you play on.)" );
		noteLabel.setFont( new Font( "Default", Font.BOLD, 9 ) );
		contentBox.add( Utils.wrapInPanel( noteLabel ) );
		
		super.buildGUI();
	}
	
	/**
	 * Check players from the provided screenshot files.
	 * @param screenshotFiles screenshot files to be used to obtain player names
	 * @return the remaining files that were not deleted
	 */
	public synchronized File[] checkPlayers( final File[] screenshotFiles ) {
		final List< File > remainedScreenshotFileList = new ArrayList< File >( screenshotFiles.length );
		
		final int gateway = MainFrame.getInstance().autoscanTab.gatewayComboBox.getSelectedIndex() - 1;
		final Set< String > bwhfHackerSet       = gatewayBwhfHackerSetMap  .get( gateway );
		final Set< String > customPlayerNameSet = gatewayCustomPlayerSetMap.get( gateway );
		
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
				logMessage( "", false ); // Prints an empty line
				logMessage( "Game lobby screenshot detected, proceeding to check..." );
				
				final String[] playerNames = TextRecognizer.readPlayerNamesFromGameLobbyImage( image );
				
				if ( echoRecognizedPlayerNamesCheckBox.isSelected() ) {
					final StringBuilder playerNamesBuilder = new StringBuilder();
					for ( final String playerName : playerNames )
						if ( playerName != null ) {
							if ( playerNamesBuilder.length() > 0 )
								playerNamesBuilder.append( ", " );
							playerNamesBuilder.append( playerName );
						}
					logMessage( "Recognized player names in game lobby: " + playerNamesBuilder.toString() );
				}
				
				boolean foundHacker = false;
				for ( int i = 0; i < playerNames.length; i++ ) {
					final String playerName = playerNames[ i ];
					
					if ( playerName != null ) {
						final String[] playerNamePermutations = generatePlayerNamePermutations( playerName );
						final boolean  exactMatch             = playerNamePermutations.length == 1;
						
						// First check if the player is a hacker
						boolean isHacker = false;
						for ( final String playerNamePermutation : playerNamePermutations )
							if ( bwhfHackerSet != null && bwhfHackerSet.contains( playerNamePermutation ) ) {
								foundHacker = true;
								isHacker = true;
								logMessage( "Found " + ( exactMatch ? "" : "possible " ) + "hacker player in game lobby: " + playerName );
								Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, exactMatch ? "hacker_at_slot.wav" : "possible_hacker_at_slot.wav" ), true );
								Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, (i+1) + ".wav" ), true );
								break;
							}
						
						if ( !isHacker ) for ( final String playerNamePermutation : playerNamePermutations )
							if ( customPlayerNameSet != null && customPlayerNameSet.contains( playerNamePermutation ) ) {
								foundHacker = true;
								logMessage( "Found " + ( exactMatch ? "" : "possible " ) + "custom listed player in game lobby: " + playerName );
								Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, exactMatch ? "custom_at_slot.wav" : "possible_custom_at_slot.wav" ), true );
								Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, (i+1) + ".wav" ), true );
								break;
							}
					}
				}
				if ( !foundHacker && sayCleanCheckBox.isSelected() )
					Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, "clean.wav" ), true );
				
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
	 * Generates possible player name permutations by varying between 'i' and 'l' where 'l' is detected.<br>
	 * If no 'l' is found in the player name, then a 1-length array is returned with the original player name.
	 * The returned array contains lowercased player names.<br>
	 * The method only generates permutations for the first 5 'l's because player names can be up 15 (or something) chars,
	 * and if all is 'l', it could define a really big amount of permutations.
	 * @param playerName player name to be permutated
	 * @return an array of possible player name permutations
	 */
	private static String[] generatePlayerNamePermutations( final String playerName ) {
		int lsCount = 0;
		for ( int i = playerName.length() - 1; i >= 0; i-- )
			if ( playerName.charAt( i ) == 'l' )
				lsCount++;
		
		if ( lsCount == 0 )
			return new String[] { playerName.toLowerCase() };
		
		if ( lsCount > 5 )
			lsCount = 5;
		
		// Let's find the indices of 'l's
		final int[] lsIndices = new int[ lsCount ];
		int counter = 0;
		for ( int i = playerName.length() - 1; i >= 0 && counter < lsCount; i-- )
			if ( playerName.charAt( i ) == 'l' )
				lsIndices[ counter++ ] = i;
		
		final String[] playerNamePermutations = new String[ 1 << lsCount ];
		final StringBuilder playerNamePermutationBuilder = new StringBuilder();
		
		// Now we build the permutations
		for ( int permutation = playerNamePermutations.length - 1; permutation >= 0; permutation-- ) {
			playerNamePermutationBuilder.setLength( 0 );
			playerNamePermutationBuilder.append( playerName );
			
			// bit 0 means 'l', bit 1 means 'i'
			int bitPos  = lsCount - 1;
			int bitMask = playerNamePermutations.length >> 1;
			for ( ; bitPos >= 0; bitPos--, bitMask >>= 1 )
				playerNamePermutationBuilder.setCharAt( lsIndices[ bitPos ], ( permutation & bitMask ) > 0 ? 'i' : 'l' );
			
			playerNamePermutations[ permutation ] = playerNamePermutationBuilder.toString().toLowerCase();
		}
		
		return playerNamePermutations;
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
					logMessage( "", false ); // Prints an empty line
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
					
					// Local hacker list cache was modified, we set the last update time
					lastUpdateTime = HACKER_LIST_CACHE_FILE.lastModified();
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
		
		logMessage( "", false ); // Prints an empty line
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
			logMessage( "Successfully reloaded " + playersCount + " player" + ( playersCount == 1 ? "" : "s" ) + " from file: '" + file.getAbsolutePath() + "'" );
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
	
	/**
	 * Starts the local hacker list cache auto updater. 
	 */
	private void startCacheAutoUpdater() {
		new NormalThread() {
			
			@Override
			public void run() {
				while ( true ) {
					try {
						if ( playerCheckerEnabledCheckBox.isSelected() && new Date().getTime() > lastUpdateTime + MS_IN_AN_HOUR * (Integer) hackerListUpdateIntervalComboBox.getSelectedItem() ) {
							// Regarding whether update succeeded or not, we set the last update time (we don't want to retry in every cycle if server is down for example)
							lastUpdateTime = new Date().getTime();
							updateHackerList();
						}
						
						sleep( TIME_BETWEEN_CHECKS_FOR_NEED_OF_UPDATE );
					}
					catch ( final InterruptedException ie ) {
					}
				}
			}
			
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_PLAYER_CHECKER_ENABLED       , Boolean.toString( playerCheckerEnabledCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HACKER_LIST_UPDATE_INTERVAL  , Integer.toString( hackerListUpdateIntervalComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_INCLUDE_CUSTOM_PLAYER_LIST   , Boolean.toString( includeCustomPlayerListCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CUSTOM_PLAYER_LIST_FILE      , customPlayerListFileTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SAY_CLEAN                    , Boolean.toString( sayCleanCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS, Boolean.toString( deleteGameLobbyScreenshotsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ECHO_RECOGNIZED_PLAYER_NAMES , Boolean.toString( echoRecognizedPlayerNamesCheckBox.isSelected() ) );
	}
	
}
