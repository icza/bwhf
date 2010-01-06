package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.net.URL;

import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.Dimension;
import swingwt.awt.Font;
import swingwt.awt.GridBagConstraints;
import swingwt.awt.GridBagLayout;
import swingwt.awt.GridLayout;
import swingwt.awt.Rectangle;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.KeyEvent;
import swingwt.awt.event.KeyListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JFrame;
import swingwtx.swing.JLabel;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.JSlider;
import swingwtx.swing.JTextField;

/**
 * General settings tab.
 * 
 * @author Andras Belicza
 */
public class GeneralSettingsTab extends Tab {
	
	/** Text of the check updates button. */
	private static final String CHECK_UPDATES_BUTTON_TEXT = "Check now";
	
	/** Reference to the main frame. */
	private final MainFrame mainFrame = MainFrame.getInstance();
	
	/** Checkbox to tell whether check for updates automatically on startup. */
	private   final JCheckBox  checkUpdatesOnStartupCheckBox      = new JCheckBox( "Check for updates on startup", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP ) ) );
	/** Check for updates button.                                            */
	private   final JButton    checkUpdatesButton                 = new JButton( CHECK_UPDATES_BUTTON_TEXT, IconResourceManager.ICON_SERVER_CONNECT );
	/** Starcraft directory.                                                 */
	protected final JTextField starcraftFolderTextField           = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_STARCRAFT_FOLDER ) );
	/** Start folder when selecting replay flies.                            */
	protected final JTextField replayStartFolderTextField         = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_REPLAY_START_FOLDER ) );
	/** Default folder for replay lists.                                     */
	protected final JTextField defaultReplayListsFolderTextField  = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_DEFAULT_REPLAY_LISTS_FOLDER ) );
	/** Replay list to be loaded on startup.                                 */
	protected final JTextField replayListToLoadOnStartupTextField = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_REPLAY_LIST_TO_LOAD_ON_STARTUP ) );
	/** Program to view/edit files.                                          */
	protected final JTextField editorProgramTextField             = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_EDITOR_PROGRAM ) );
	/** Checkbox to tell whether check for updates automatically on startup. */
	protected final JCheckBox  skipLatterActionsOfHackersCheckBox = new JCheckBox( "Short scan (during a replay scan if a player is found hacking, skip scanning his latter actions)", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS ) ) );
	/** Slider to set the sound volume.                                      */
	protected final JSlider    soundVolumeSlider                  = new JSlider( 0, 100, Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_SOUND_VOLUME ) ) );
	/** Checkbox to enable system tray icon.                                 */
	protected final JCheckBox  enableSystemTrayIconCheckBox       = new JCheckBox( "Enable system tray icon", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_ENABLE_SYSTEM_TRAY_ICON ) ) );
	/** Checkbox to always minimize to tray.                                 */
	protected final JCheckBox  alwaysMinimizeToTrayCheckBox       = new JCheckBox( "Always minimize to tray", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_ALWAYS_MINIMIZE_TO_TRAY ) ) );
	/** Checkbox to always minimize to tray.                                 */
	protected final JCheckBox  startAgentMinimizedToTrayCheckBox  = new JCheckBox( "Start agent minimized to tray", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_START_AGENT_MINIMIZED_TO_TRAY ) ) );
	/** Checkbox to save window position.                                    */
	protected final JCheckBox  saveWindowPositionCheckBox         = new JCheckBox( "Save window position", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SAVE_WINDOW_POSITION ) ) );
	
	/**
	 * Creates a new GeneralSettings.
	 */
	public GeneralSettingsTab() {
		super( "General settings", IconResourceManager.ICON_GENERAL_SETTINGS );
		
		final File defaultReplayListsFolder = new File( Consts.DEFAULT_REPLAY_LISTS_FOLDER );
		if ( !defaultReplayListsFolder.exists() )
			defaultReplayListsFolder.mkdir();
		
		buildGUI();
		checkStarcraftFolder();
		
		if ( checkUpdatesOnStartupCheckBox.isSelected() ) // Start thread delayed in new thread because main frame might not yet (or at all) be visible
			new Thread() {
				@Override
				public void run() {
					try {
						sleep( 2000l );
						checkUpdatesButton.doClick();
					} catch ( final InterruptedException ie ) {
						ie.printStackTrace();
					}
				}
			}.start();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		JPanel titledPanel;
		
		JPanel panel = Utils.createWrapperPanel();
		panel.add( checkUpdatesOnStartupCheckBox );
		// In SwingWT Button text cannot be changed if a mnemonic has been assigned
		//checkUpdatesButton.setMnemonic( ( checkUpdatesButton.getText().charAt( 0 ) ) );
		checkUpdatesButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				checkUpdates();
			}
		} );
		panel.add( checkUpdatesButton );
		contentBox.add( panel );
		
		final GridBagLayout      gridBagLayout = new GridBagLayout();
		final GridBagConstraints constraints   = new GridBagConstraints();
		panel = new JPanel( gridBagLayout );
		panel.setBorder( BorderFactory.createTitledBorder( "Folder and editor settings:" ) );
		
		JLabel  label;
		JButton button;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridwidth = 1;
		label = new JLabel( "Starcraft directory:", JLabel.LEFT );
		gridBagLayout.setConstraints( label, constraints );
		panel.add( label );
		// This is a workaround becase SwingWT does not implement DocumentListener correctly :S
		starcraftFolderTextField.addKeyListener( new KeyListener() {
			public void keyPressed( final KeyEvent event ) {
				checkStarcraftFolder();
			}
			public void keyReleased( final KeyEvent event ) {
				checkStarcraftFolder();
			}
			public void keyTyped( final KeyEvent event ) {
				checkStarcraftFolder();
			}
		} );
		gridBagLayout.setConstraints( starcraftFolderTextField, constraints );
		panel.add( starcraftFolderTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = Utils.createFileChooserButton( getContent(), starcraftFolderTextField, JFileChooser.DIRECTORIES_ONLY, null, null, new Runnable() {
			public void run() {
				checkStarcraftFolder();
			}
		} );
		gridBagLayout.setConstraints( button, constraints );
		panel.add( button );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Start folder when selecting replay files:", JLabel.LEFT );
		gridBagLayout.setConstraints( label, constraints );
		panel.add( label );
		gridBagLayout.setConstraints( replayStartFolderTextField, constraints );
		panel.add( replayStartFolderTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = Utils.createFileChooserButton( getContent(), replayStartFolderTextField, JFileChooser.DIRECTORIES_ONLY, null, null, new Runnable() {
			public void run() {
				// If one of Starcraft's folder was selected, we replace its path to be relative so it will work if the product is copied/moved to another directory
				final File selectedFolder = new File( replayStartFolderTextField.getText() );
				
				if ( selectedFolder.getAbsolutePath().equals( new File( starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ).getAbsolutePath() ) )
					replayStartFolderTextField.setText( Consts.STARCRAFT_REPLAY_FOLDER );
				else if ( selectedFolder.getParentFile() != null && selectedFolder.getParentFile().getAbsolutePath().equals( new File( starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ).getAbsolutePath() ) )
					replayStartFolderTextField.setText( Consts.STARCRAFT_REPLAY_FOLDER + "/" + selectedFolder.getName() );
			}
		} );
		gridBagLayout.setConstraints( button, constraints );
		panel.add( button );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Default replay lists folder:", JLabel.LEFT );
		gridBagLayout.setConstraints( label, constraints );
		panel.add( label );
		gridBagLayout.setConstraints( defaultReplayListsFolderTextField, constraints );
		panel.add( defaultReplayListsFolderTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = Utils.createFileChooserButton( getContent(), defaultReplayListsFolderTextField, JFileChooser.DIRECTORIES_ONLY, null, null, new Runnable() {
			public void run() {
				// If the default folder was selected, we replace its path to be relative so it will work if the product is copied/moved to another directory
				final File selectedFolder = new File( defaultReplayListsFolderTextField.getText() );
				if ( selectedFolder.getAbsolutePath().equals( new File( Consts.DEFAULT_REPLAY_LISTS_FOLDER ).getAbsolutePath() ) )
					defaultReplayListsFolderTextField.setText( Consts.DEFAULT_REPLAY_LISTS_FOLDER );
			}
		} );
		gridBagLayout.setConstraints( button, constraints );
		panel.add( button );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Replay list to load on startup:", JLabel.LEFT );
		gridBagLayout.setConstraints( label, constraints );
		panel.add( label );
		gridBagLayout.setConstraints( replayListToLoadOnStartupTextField, constraints );
		panel.add( replayListToLoadOnStartupTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = Utils.createFileChooserButton( getContent(), replayListToLoadOnStartupTextField, JFileChooser.FILES_ONLY, Utils.SWING_TEXT_FILE_FILTER , new String[][] { { "*.txt", "*.*" }, { "Text files (*.txt)", "All files (*.*)" } }, new Runnable() {
			public void run() {
				// If the selected replay list file is in the default replay list folder, we replace its path to be relative so it will work if the product is copied/moved to another directory
				final File selectedFile = new File( replayListToLoadOnStartupTextField.getText() );
				
				if ( selectedFile.getParentFile() != null && selectedFile.getParentFile().getAbsolutePath().equals( new File( Consts.DEFAULT_REPLAY_LISTS_FOLDER ).getAbsolutePath() ) )
					replayListToLoadOnStartupTextField.setText( Consts.DEFAULT_REPLAY_LISTS_FOLDER + "/" + selectedFile.getName() );
			}
		} );
		gridBagLayout.setConstraints( button, constraints );
		panel.add( button );
		
		label = new JLabel( "(Note that if you select a big list with thousands of replays, it might significantly slow down the startup of BWHF Agent.)" );
		label.setFont( new Font( "Default", Font.ITALIC, 9 ) );
		final JPanel wrapperPanel = Utils.wrapInPanel( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		panel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Program to view/edit files:", JLabel.LEFT );
		gridBagLayout.setConstraints( label, constraints );
		panel.add( label );
		gridBagLayout.setConstraints( editorProgramTextField, constraints );
		panel.add( editorProgramTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = Utils.createFileChooserButton( getContent(), editorProgramTextField, JFileChooser.FILES_ONLY, null, null, null );
		gridBagLayout.setConstraints( button, constraints );
		panel.add( button );
		
		final Box contentBox2 = Box.createVerticalBox();
		contentBox2.add( panel );
		
		panel = new JPanel( new GridLayout( 3, 1 ) );
		enableSystemTrayIconCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( enableSystemTrayIconCheckBox.isSelected() )
					mainFrame.installSystemTrayIcon();
				else
					mainFrame.removeSystemTrayIcon();
				alwaysMinimizeToTrayCheckBox.setEnabled( enableSystemTrayIconCheckBox.isSelected() );
				startAgentMinimizedToTrayCheckBox.setEnabled( enableSystemTrayIconCheckBox.isSelected() );
				mainFrame.minimizeToTrayButton.setEnabled( enableSystemTrayIconCheckBox.isSelected() );
			}
		} );
		panel.add( enableSystemTrayIconCheckBox );
		panel.add( alwaysMinimizeToTrayCheckBox );
		panel.add( startAgentMinimizedToTrayCheckBox );
		// Initialize system tray related checkboxes
		enableSystemTrayIconCheckBox.doClick(); // This does not change the state of the checkbox
		titledPanel = Utils.wrapInPanel( panel );
		titledPanel.setBorder( BorderFactory.createTitledBorder( "Tray icon settings:" ) );
		contentBox2.add( titledPanel );
		
		final Box miscBox = Box.createVerticalBox();
		panel = Utils.createWrapperPanel(); 
		panel.add( new JLabel( "Sound volume:" ) );
		soundVolumeSlider.setLabelTable( soundVolumeSlider.createStandardLabels( 10 ) );
		soundVolumeSlider.setPreferredSize( new Dimension( 300, 45 ) );
		panel.add( soundVolumeSlider );
		final JButton testSoundButton = new JButton( "Play test sound", IconResourceManager.ICON_SOUND );
		testSoundButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.playWavFile( new File( Consts.SOUNDS_DIRECTORY_NAME, "hacker.wav" ), false );
			}
		} );
		panel.add( testSoundButton );
		miscBox.add( panel );
		miscBox.add( saveWindowPositionCheckBox );
		miscBox.add( skipLatterActionsOfHackersCheckBox );
		titledPanel = Utils.wrapInPanel( miscBox );
		titledPanel.setBorder( BorderFactory.createTitledBorder( "Miscellaneous settings:" ) );
		contentBox2.add( titledPanel );
		
		contentBox.add( Utils.wrapInPanel( contentBox2 ) );
		
		// To consume the remaining space:
		contentBox.add( new JPanel( new BorderLayout() ) );
	}
	
	/**
	 * Checks for updates.
	 */
	private void checkUpdates() {
		if ( !checkUpdatesButton.isEnabled() )
			return;
		
		checkUpdatesButton.setEnabled( false );
		
		checkUpdatesButton.setText( "Checking..." );
		checkUpdatesButton.getParent().validate();
		
		new NormalThread( "Update checker" ) {
			@Override
			public void run() {
				try {
					final String versionString = Utils.readVersionStringFromStream( new URL( Consts.LATEST_STABLE_VERSION_TEXT_URL ).openStream() );
					if ( versionString == null )
						throw new Exception();
					
					if ( versionString.equals( MainFrame.getInstance().applicationVersion ) )
						checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (no new version)" );
					else {
						mainFrame.stayInWonderland();
						if ( JOptionPane.showConfirmDialog( null, "A newer version of " + Consts.APPLICATION_NAME + " is available.\nWould you like to visit the home page to download it?", "New version available!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE ) == JOptionPane.YES_OPTION ) // Parent is not the mainFrame, because it might not be yet (or at all) visible
							Utils.showURLInBrowser( Consts.HOME_PAGE_URL );
						checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (new version available!)" );
					}
				} catch ( final Exception e ) {
					checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (check failed!)" );
				}
				finally {
					checkUpdatesButton.setEnabled( true );
					checkUpdatesButton.getParent().validate();
				}
			}
		}.start();
	}
	
	/**
	 * Checks if starcraft folder is set correctly.
	 */
	private void checkStarcraftFolder() {
		if ( new File( starcraftFolderTextField.getText(), Consts.STARCRAFT_EXECUTABLE_FILE_NAME ).exists() ) {
			mainFrame.startScButton.setEnabled( true );
			mainFrame.starcraftFolderStatusLabel.setText( "Starcraft directory is set correctly." );
			mainFrame.starcraftFolderStatusLabel.setForeground( Color.GREEN.darker() );
		}
		else {
			mainFrame.startScButton.setEnabled( false );
			mainFrame.starcraftFolderStatusLabel.setText( "Cannot find '" + Consts.STARCRAFT_EXECUTABLE_FILE_NAME + "'!" );
			mainFrame.starcraftFolderStatusLabel.setForeground( Color.RED );
		}
	}
	
	/**
	 * Returns the start folder for opening replays.
	 * @return the start folder for opening replays
	 */
	public File getReplayStartFolder() {
		final File replayStartFolder = new File( replayStartFolderTextField.getText() );
		return replayStartFolder.isAbsolute() ? replayStartFolder : new File( starcraftFolderTextField.getText(), replayStartFolderTextField.getText() );
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP      , Boolean.toString( checkUpdatesOnStartupCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_STARCRAFT_FOLDER              , starcraftFolderTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_REPLAY_START_FOLDER           , replayStartFolderTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DEFAULT_REPLAY_LISTS_FOLDER   , defaultReplayListsFolderTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_REPLAY_LIST_TO_LOAD_ON_STARTUP, replayListToLoadOnStartupTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_EDITOR_PROGRAM                , editorProgramTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS, Boolean.toString( skipLatterActionsOfHackersCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SOUND_VOLUME                  , Integer.toString( soundVolumeSlider.getValue() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ENABLE_SYSTEM_TRAY_ICON       , Boolean.toString( enableSystemTrayIconCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ALWAYS_MINIMIZE_TO_TRAY       , Boolean.toString( alwaysMinimizeToTrayCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_START_AGENT_MINIMIZED_TO_TRAY , Boolean.toString( startAgentMinimizedToTrayCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SAVE_WINDOW_POSITION          , Boolean.toString( saveWindowPositionCheckBox.isSelected() ) );
		if ( saveWindowPositionCheckBox.isSelected() )
			if ( mainFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH )
				Utils.settingsProperties.setProperty( Consts.PROPERTY_WINDOW_POSITION, "maximized" );
			else {
				final Rectangle bounds = mainFrame.getBounds();
				Utils.settingsProperties.setProperty( Consts.PROPERTY_WINDOW_POSITION, bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height );
			}
	}
	
}
