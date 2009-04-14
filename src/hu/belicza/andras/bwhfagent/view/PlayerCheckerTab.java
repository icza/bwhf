package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.hackerdb.ServerApiConsts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Date;

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
import swingwtx.swing.filechooser.FileFilter;

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
	private static final File HACKER_LIST_DIRECTORY         = new File( Consts.HACKER_LIST_DIRECTORY_NAME );
	/** The hacker list cache file.              */
	private static final File   HACKER_LIST_CACHE_FILE      = new File( Consts.HACKER_LIST_DIRECTORY_NAME, HACKER_LIST_CACHE_FILE_NAME );
	
	/** Reference to the settings panel. */
	private JPanel settingsPanel;
	
	/** Checkbox to enable/disable the autoscan.                  */
	private final JCheckBox  playerCheckerEnabledCheckBox       = new JCheckBox( "Enable checking players in the game lobby when pressing the 'Print Screen' key", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_PLAYER_CHECKER_ENABLED ) ) );
	/** Combo box to set the hacker list update interval.         */
	private final JComboBox  hackerListUpdateIntervalComboBox   = new JComboBox( new Object[] { 1, 2, 6, 12, 24, 48 } );
	/** Label to display the last update time of the hacker list. */
	private final JLabel     hackerListLastUpdatedLabel         = new JLabel();
	/** Button to update hacker list now.                         */
	private final JButton    updateNowButton                    = new JButton( UPDATE_NOW_BUTTON_TEXT );
	/** Checkbox to enable/disable the autoscan.                  */
	private final JCheckBox  deleteGameLobbyScreenshotsCheckBox = new JCheckBox( "Delete game lobby screenshots after checking players", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS ) ) );
	/** Checkbox to include extra player list.                    */
	private final JCheckBox  includeExtraPlayerListCheckBox     = new JCheckBox( "Include this extra player list:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS ) ) );
	/** File of hte extra player list.                            */
	private final JTextField extraPlayerListFileTextField       = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_EXTRA_PLAYER_LIST_FILE ) );
	
	/**
	 * Creates a new PlayerCheckerTab.
	 */
	public PlayerCheckerTab() {
		super( "Player checker", "player_checker.log" );
		
		buildGUI();
		
		hackerListUpdateIntervalComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_HACKER_LIST_UPDATE_INTERVAL ) ) );
		
		refreshLastUpdatedLabel();
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
		gridBagLayout.setConstraints( includeExtraPlayerListCheckBox, constraints );
		settingsPanel.add( includeExtraPlayerListCheckBox );
		gridBagLayout.setConstraints( extraPlayerListFileTextField, constraints );
		settingsPanel.add( extraPlayerListFileTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperPanel = new JPanel( new BorderLayout() );
		button = Utils.createFileChooserButton( getContent(), extraPlayerListFileTextField, JFileChooser.FILES_ONLY, new FileFilter() {
			@Override
			public boolean accept( final File file ) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith( ".txt" );
			}
			@Override
			public String getDescription() {
				return "Text files (*.txt)";
			}
		}, new String[][] { new String[] { "*.txt", "*.*" }, new String[] { "Text files (*.txt)", "All files (*.*)" } }, new Runnable() {
			public void run() {
				final File selectedFile = new File( extraPlayerListFileTextField.getText() );
				if ( selectedFile.getAbsolutePath().equals( new File( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() ).getAbsolutePath() ) )
					extraPlayerListFileTextField.setText( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() );
			}
		} );
		wrapperPanel.add( button, BorderLayout.WEST );
		button = new JButton( "Reload" );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				reloadLists();
			}
		} );
		wrapperPanel.add( button, BorderLayout.CENTER );
		button = new JButton( "Edit" );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.editFile( extraPlayerListFileTextField.getText() );
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
					logMessage( "\n", false ); // Prints 2 empty lines
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
					
					reloadLists();
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
	 * Reloads player lists from files.
	 */
	private synchronized void reloadLists() {
		refreshLastUpdatedLabel();
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
		Utils.settingsProperties.setProperty( Consts.PROPERTY_INCLUDE_EXTRA_PLAYER_LIST    , Boolean.toString( includeExtraPlayerListCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_EXTRA_PLAYER_LIST_FILE       , extraPlayerListFileTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS, Boolean.toString( deleteGameLobbyScreenshotsCheckBox.isSelected() ) );
	}
	
}
