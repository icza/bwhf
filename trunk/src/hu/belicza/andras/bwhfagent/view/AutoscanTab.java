package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.hackerdb.ServerApiConsts;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.JTextField;
import swingwtx.swing.filechooser.FileFilter;

/**
 * Autoscan tab.
 * 
 * @author Andras Belicza
 */
public class AutoscanTab extends LoggedTab {
	
	/** Log file name for autoscan.                 */
	private static final String LOG_FILE_NAME                         = "autoscan.log";
	/** Time between checking for new replay in ms. */
	private static final long   TIME_BETWEEN_CHECKS_FOR_NEW_REPLAY_MS = 2000l;
	/** Text of the check key button.               */
	private static final String CHECK_KEY_BUTTON_TEXT                 = "Check key";
	
	/** Checkbox to enable/disable the autoscan.                                */
	private   final JCheckBox  autoscanEnabledCheckBox          = new JCheckBox( "Autoscan enabled", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTOSCAN_ENABLED ) ) );
	/** Checkbox to enable/disable autosaving hacker reps.                      */
	private   final JCheckBox  saveHackerRepsCheckBox           = new JCheckBox( "Save hacker replays to folder:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SAVE_HACKER_REPS ) ) );
	/** Save hacker replays to this folder.                                     */
	private   final JTextField hackerRepsDestinationTextField   = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_HACKER_REPS_DESTINATION ) );
	/** Checkbox to enable/disable autosaving all reps.                         */
	private   final JCheckBox  saveAllRepsCheckBox              = new JCheckBox( "Save all replays to folder:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SAVE_ALL_REPS ) ) );
	/** Save hacker replays to this folder.                                     */
	private   final JTextField allRepsDestinationTextField      = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_ALL_REPS_DESTINATION ) );
	/** Checkbox to use short names for autosaved replays.                      */
	private   final JCheckBox  useShortNamesForAutosaveCheckBox = new JCheckBox( "Use short names for autosaved replays (short format: 'YYMMDD hhmmss.rep', default: 'YYYY-MM-DD hh-mm-ss LastRep.rep' )", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_USE_SHORT_NAMES_FOR_AUTOSAVE ) ) );
	/** Checkbox to enable/disable playing sound if found hacks.                */
	private   final JCheckBox  playSoundCheckBox                = new JCheckBox( "Play sound alert if found hackers", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_PLAY_SOUND_IF_FOUND_HACKERS ) ) );
	/** Checkbox to enable/disable bringing main frame to front if found hacks. */
	private   final JCheckBox  bringToFrontCheckBox             = new JCheckBox( "Bring Agent to front if found hackers", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_BRING_TO_FRONT ) ) );
	/** Checkbox to enable/disable reporting hackers.                           */
	private   final JCheckBox  reportHackersCheckBox            = new JCheckBox( "Report hackers to the central hacker database with Battle.net gateway:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_REPORT_HACKERS ) ) );
	/** Combobox to select the gateway of the user.                             */
	protected final JComboBox  gatewayComboBox                  = new JComboBox();
	
	/** Authorization key to report hackers.      */
	private String           authorizationKey = Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTHORIZATION_KEY );
	/** Result of the last key check.             */
	private volatile Boolean lastKeyCheckResult;
	/** Tells whether a key check is in progress. */
	private volatile boolean keyCheckInProgress;
	
	/**
	 * Creates a new AutoscanTab.
	 */
	public AutoscanTab() {
		super( "Autoscan", IconResourceManager.ICON_AUTOSCAN, LOG_FILE_NAME );
		
		gatewayComboBox.addItem( "<select your gateway>" );
		for ( final String gateway : ServerApiConsts.GATEWAYS )
			gatewayComboBox.addItem( gateway );
		
		buildGUI();
		
		gatewayComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_GATEWAY ) ) );
		
		startAutoscanner();
	}
	
	@Override
	protected void buildGUI() {
		final GridBagLayout      gridBagLayout = new GridBagLayout();
		final GridBagConstraints constraints   = new GridBagConstraints();
		final JPanel             settingsPanel = new JPanel( gridBagLayout );
		settingsPanel.setBorder( BorderFactory.createTitledBorder( "Settings:" ) );
		
		JButton button;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		JPanel wrapperPanel = Utils.wrapInPanel( autoscanEnabledCheckBox );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( saveHackerRepsCheckBox, constraints );
		settingsPanel.add( saveHackerRepsCheckBox );
		gridBagLayout.setConstraints( hackerRepsDestinationTextField, constraints );
		settingsPanel.add( hackerRepsDestinationTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( hackerRepsDestinationTextField, JFileChooser.DIRECTORIES_ONLY, null, null, null );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( saveAllRepsCheckBox, constraints );
		settingsPanel.add( saveAllRepsCheckBox );
		gridBagLayout.setConstraints( allRepsDestinationTextField, constraints );
		settingsPanel.add( allRepsDestinationTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( allRepsDestinationTextField, JFileChooser.DIRECTORIES_ONLY, null, null, null );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( useShortNamesForAutosaveCheckBox, constraints );
		settingsPanel.add( useShortNamesForAutosaveCheckBox );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( playSoundCheckBox, constraints );
		settingsPanel.add( playSoundCheckBox );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( bringToFrontCheckBox, constraints );
		settingsPanel.add( bringToFrontCheckBox );
		
		constraints.gridwidth = 2;
		final JButton checkKeyButton = new JButton( CHECK_KEY_BUTTON_TEXT, IconResourceManager.ICON_SERVER_CONNECT );
		JPanel panel = new JPanel( new BorderLayout() );
		reportHackersCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( reportHackersCheckBox.isSelected() ) {
					reportHackersCheckBox.setSelected( false );
					if ( gatewayComboBox.getSelectedIndex() == 0 ) {
						JOptionPane.showMessageDialog( getContent(), "Please select your gateway corresponding to where you play in order to report hackers!", "Error", JOptionPane.ERROR_MESSAGE );
						return;
					}
					reportHackersCheckBox.setEnabled( false );
					gatewayComboBox.setEnabled( false );
					new NormalThread() {
						public void run() {
							checkKeyButton.doClick();
							
							while ( keyCheckInProgress )
								try { sleep( 10l ); } catch ( final InterruptedException ie ) {}
							
							if ( lastKeyCheckResult == null || !lastKeyCheckResult ) {
								reportHackersCheckBox.setSelected( false );
								JOptionPane.showMessageDialog( getContent(), "You need a valid authorization key to report hackers!\n" + ( lastKeyCheckResult == null ? "Failed to check the authorization key." : "The supplied authorization key is invalid."), "Error", JOptionPane.ERROR_MESSAGE );
							}
							else
								reportHackersCheckBox.setSelected( true );
							
							gatewayComboBox.setEnabled( true );
							reportHackersCheckBox.setEnabled( true );
						}
					}.start();
				}
			}
		} );
		panel.add( reportHackersCheckBox, BorderLayout.WEST );
		panel.add( gatewayComboBox, BorderLayout.CENTER );
		gridBagLayout.setConstraints( panel, constraints );
		settingsPanel.add( panel );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		panel = new JPanel( new BorderLayout() );
		button = new JButton( "Change key...", IconResourceManager.ICON_KEY );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final Object newAuthorizationKeyObject = JOptionPane.showInputDialog( getContent(), "Enter your authorization key:", "Changing key", JOptionPane.QUESTION_MESSAGE, null, null, authorizationKey );
				if ( newAuthorizationKeyObject != null ) {
					authorizationKey = ( (String) newAuthorizationKeyObject ).trim();
					checkKeyButton.doClick();
				}
			}
		} );
		panel.add( button, BorderLayout.CENTER );
		checkKeyButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( keyCheckInProgress ) // If a key check is already in progress, we have nothing to do.
					return;
				
				keyCheckInProgress = true;
				checkKeyButton.setEnabled( false );
				checkKeyButton.setText( "Checking..." );
				new NormalThread() {
					public void run() {
						lastKeyCheckResult = Utils.checkAuthorizationKey( authorizationKey );
						checkKeyButton.setText( CHECK_KEY_BUTTON_TEXT + ( lastKeyCheckResult == null ? " (check failed!)" : ( lastKeyCheckResult ? " (valid)" : " (invalid)" ) ) );
						checkKeyButton.setEnabled( true );
						settingsPanel.getParent().validate();
						keyCheckInProgress = false;
					}
				}.start();
			}
		} );
		panel.add( checkKeyButton, BorderLayout.EAST );
		gridBagLayout.setConstraints( panel, constraints );
		settingsPanel.add( panel );
		
		contentBox.add( Utils.wrapInPanel( settingsPanel ) );
		
		super.buildGUI();
	}
	
	/**
	 * Calls {@link Utils#createFileChooserButton(swingwt.awt.Component, JTextField, int, FileFilter, String[][], Runnable)} with the scroll pane of the tab.
	 */
	private JButton createFileChooserButton( final JTextField targetTextField, final int fileSelectionMode, final FileFilter choosableFileFilter, final String[][] choosableFileFilterWT, final Runnable taskOnApprove ) {
		return Utils.createFileChooserButton( getContent(), targetTextField, fileSelectionMode, choosableFileFilter, choosableFileFilterWT, taskOnApprove );
	}
	
	/**
	 * Starts the autoscanner.
	 */
	private void startAutoscanner() {
		new NormalThread() {
			
			@Override
			public void run() {
				final JTextField starcraftFolderTextField           = MainFrame.getInstance().generalSettingsTab.starcraftFolderTextField;
				final JCheckBox  skipLatterActionsOfHackersCheckBox = MainFrame.getInstance().generalSettingsTab.skipLatterActionsOfHackersCheckBox;
				
				long lastModifiedOfLastChecked = new File( starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ).lastModified(); // Last modified time of the LastReplay.rep that was checked lastly; this will be 0l if 'LastRepay.rep' does not yet exist.
				while ( true ) {
					try {
						final File lastReplayFile      = new File( starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME );
						long newLastReplayLastModified = lastReplayFile.lastModified();
						
						if ( newLastReplayLastModified != lastModifiedOfLastChecked ) {
							sleep( 1500l ); // Wait a little for Starcraft to finish saving replay...
							// lastModified property changes when replay saving finishes, so we query it again:
							newLastReplayLastModified = lastReplayFile.lastModified();
							lastModifiedOfLastChecked = newLastReplayLastModified;
							
							if ( MainFrame.getInstance().playersNetworkTab.autoSendInfoAboutLastReplayCheckBox.isSelected() )
								new NormalThread() { // We send it in a new Thread to not block the autoscan process
									@Override
									public void run() {
										MainFrame.getInstance().playersNetworkTab.sendFileInfo( lastReplayFile, gatewayComboBox.getSelectedIndex() > 0 ? gatewayComboBox.getSelectedIndex() - 1 : null );
									}
								}.start();
							
							if ( autoscanEnabledCheckBox.isSelected() ) {
								final String autosavedFileName = useShortNamesForAutosaveCheckBox.isSelected() ? Utils.SHORT_DATE_FORMAT.format( new Date() ) + ".rep" : Utils.DATE_FORMAT.format( new Date() ) + " LastRep.rep";
								if ( saveAllRepsCheckBox.isSelected() )
									Utils.copyFile( lastReplayFile, new File( allRepsDestinationTextField.getText() ), autosavedFileName );
								
								logMessage( "LastReplay.rep was modified - proceeding to scan..." );
								
								List< HackDescription > hackDescriptionList = null; 
								final Replay replay = BinRepParser.parseReplay( lastReplayFile, true, false );
								if ( replay != null )
									hackDescriptionList = ReplayScanner.scanReplayForHacks( replay, skipLatterActionsOfHackersCheckBox.isSelected() );
								
								if ( hackDescriptionList == null )
									logMessage( "Could not scan LastReplay.rep!" );
								else
									if ( !hackDescriptionList.isEmpty() ) {
										if ( bringToFrontCheckBox.isSelected() ) {
											MainFrame.getInstance().selectTab( AutoscanTab.this );
											MainFrame.getInstance().toFront();
										}
										
										final Set< String > hackerSet = new HashSet< String >();
										logMessage( "Found " + hackDescriptionList.size() + " hack" + (hackDescriptionList.size() == 1 ? "" : "s" ) + " in LastReplay.rep:" );
										for ( final HackDescription hackDescription : hackDescriptionList ) {
											logMessage( "\t" + hackDescription.description, false );
											hackerSet.add( hackDescription.playerName );
										}
										
										if ( saveHackerRepsCheckBox.isSelected() )
											Utils.copyFile( lastReplayFile, new File( hackerRepsDestinationTextField.getText() ), autosavedFileName );
										
										if ( playSoundCheckBox.isSelected() ) {
											final ReplayHeader replayHeader = replay.replayHeader;
											for ( final String hacker : hackerSet ) {
												Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, "hacker.wav" ), true );
												final int playerIndex = replayHeader.getPlayerIndexByName( hacker );
												if ( playerIndex >= 0 ) {
													if ( replayHeader.playerColors[ playerIndex ] < 8 )
														Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, ReplayHeader.IN_GAME_COLOR_NAMES[ replayHeader.playerColors[ playerIndex ] ] + ".wav" ), true );
													else
														Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, "unique.wav" ), true );
													if ( replayHeader.playerRaces[ playerIndex ] < ReplayHeader.RACE_NAMES.length )
														Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, ReplayHeader.RACE_NAMES[ replayHeader.playerRaces[ playerIndex ] ].toLowerCase() + ".wav" ), true );
												}
											}
										}
										
										if ( reportHackersCheckBox.isSelected() ) {
											if ( gatewayComboBox.getSelectedIndex() == 0 )
												logMessage( "Error! Hacker report was not sent due to no gateway is selected!" );
											else {
												logMessage( "Sending hacker report..." );
												final Set< String > playerNameSet = new HashSet< String >( 8 );
												for ( final HackDescription hackDescription : hackDescriptionList )
													playerNameSet.add( hackDescription.playerName );
												
												final String message = Utils.sendHackerReport( authorizationKey, gatewayComboBox.getSelectedIndex() - 1, replay.replayHeader.gameEngine & 0xff, replay.replayHeader.mapName, playerNameSet );
												if ( message == null )
													logMessage( "Sending hacker report succeeded." );
												else
													logMessage( "Sending hacker report failed: " + message );
											}
										}
									}
									else
										logMessage( "Found no hacks in 'LastReplay.rep.'" );
							}
						}
						
						sleep( TIME_BETWEEN_CHECKS_FOR_NEW_REPLAY_MS );
					}
					catch ( final InterruptedException ie ) {
					}
				}
			}
			
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTOSCAN_ENABLED            , Boolean.toString( autoscanEnabledCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SAVE_HACKER_REPS            , Boolean.toString( saveHackerRepsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HACKER_REPS_DESTINATION     , hackerRepsDestinationTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SAVE_ALL_REPS               , Boolean.toString( saveAllRepsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ALL_REPS_DESTINATION        , allRepsDestinationTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_USE_SHORT_NAMES_FOR_AUTOSAVE, Boolean.toString( useShortNamesForAutosaveCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_PLAY_SOUND_IF_FOUND_HACKERS , Boolean.toString( playSoundCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_BRING_TO_FRONT              , Boolean.toString( bringToFrontCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_REPORT_HACKERS              , Boolean.toString( reportHackersCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_GATEWAY                     , Integer.toString( gatewayComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTHORIZATION_KEY           , authorizationKey );
	}
	
}
