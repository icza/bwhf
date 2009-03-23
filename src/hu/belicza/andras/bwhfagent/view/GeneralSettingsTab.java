package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.net.URL;

import swingwt.awt.BorderLayout;
import swingwt.awt.Dimension;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JFileChooser;
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
	
	/** Checkbox to tell whether check for updates automatically on startup. */
	private   final JCheckBox  checkUpdatesOnStartupCheckBox      = new JCheckBox( "Check for updates on startup", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP ) ) );
	/** Check for updates button.                                            */
	private   final JButton    checkUpdatesButton                 = new JButton( CHECK_UPDATES_BUTTON_TEXT );
	/** Checkbox to tell whether check for updates automatically on startup. */
	protected final JCheckBox  skipLatterActionsOfHackersCheckBox = new JCheckBox( "During a replay scan if a player is found hacking, skip scanning his latter actions", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS ) ) );
	/** Slider to set the sound volume.                                      */
	protected final JSlider    soundVolumeSlider                  = new JSlider( 0, 100, Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_SOUND_VOLUME ) ) );
	/** Start folder when selecting replay flies.                            */
	protected final JTextField replayStartFolderTextField         = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_REPLAY_START_FOLDER ) );
	/** Checkbox to enable system tray icon.                                 */
	protected final JCheckBox  enableSystemTrayIconCheckBox       = new JCheckBox( "Enable system tray icon", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_ENABLE_SYSTEM_TRAY_ICON ) ) );
	/** Checkbox to always minimize to tray.                                 */
	protected final JCheckBox  alwaysMinimizeToTrayCheckBox       = new JCheckBox( "Always minimize to tray", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_ALWAYS_MINIMIZE_TO_TRAY ) ) );
	/** Checkbox to always minimize to tray.                                 */
	protected final JCheckBox  startAgentMinimizedToTrayCheckBox  = new JCheckBox( "Start agent minimized to tray", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_START_AGENT_MINIMIZED_TO_TRAY ) ) );
	
	/**
	 * Creates a new GeneralSettings.
	 */
	public GeneralSettingsTab() {
		super( "General settings" );
		
		buildGUI();
		
		if ( checkUpdatesOnStartupCheckBox.isSelected() )
			checkUpdatesButton.doClick();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
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
		
		contentBox.add( Utils.wrapInPanel( skipLatterActionsOfHackersCheckBox ) );
		
		panel = Utils.createWrapperPanel(); 
		panel.add( new JLabel( "Sound volume:" ) );
		soundVolumeSlider.setLabelTable( soundVolumeSlider.createStandardLabels( 10 ) );
		soundVolumeSlider.setPreferredSize( new Dimension( 300, 45 ) );
		panel.add( soundVolumeSlider );
		contentBox.add( panel );
		
		panel = Utils.createWrapperPanel();
		panel.add( new JLabel( "Start folder when selecting replay files:" ) );
		panel.add( replayStartFolderTextField );
		final JButton button = Utils.createFileChooserButton( getContent(), replayStartFolderTextField, JFileChooser.DIRECTORIES_ONLY, null, null, new Runnable() {
			public void run() {
				// If one of Starcraft's folder was selected, we replace its path to be relative so it will work if the product is copied/moved to another directory
				final File selectedFolder = new File( replayStartFolderTextField.getText() );
				
				if ( selectedFolder.equals( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ) ) )
					replayStartFolderTextField.setText( Consts.STARCRAFT_REPLAY_FOLDER );
				else if ( selectedFolder.getParentFile() != null && selectedFolder.getParentFile().equals( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ) ) )
					replayStartFolderTextField.setText( Consts.STARCRAFT_REPLAY_FOLDER + "/" + selectedFolder.getName() );
			}
		} );
		panel.add( button );
		contentBox.add( panel );
		
		final ActionListener systemTrayIconEnablerCheckBoxActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( enableSystemTrayIconCheckBox.isSelected() )
					MainFrame.getInstance().installSystemTrayIcon();
				else
					MainFrame.getInstance().removeSystemTrayIcon();
				alwaysMinimizeToTrayCheckBox.setEnabled( enableSystemTrayIconCheckBox.isSelected() );
				startAgentMinimizedToTrayCheckBox.setEnabled( enableSystemTrayIconCheckBox.isSelected() );
				MainFrame.getInstance().minimizeToTrayButton.setEnabled( enableSystemTrayIconCheckBox.isSelected() );
			}
		};
		enableSystemTrayIconCheckBox.addActionListener( systemTrayIconEnablerCheckBoxActionListener );
		contentBox.add( Utils.wrapInPanel( enableSystemTrayIconCheckBox ) );
		
		contentBox.add( Utils.wrapInPanel( alwaysMinimizeToTrayCheckBox ) );
		
		contentBox.add( Utils.wrapInPanel( startAgentMinimizedToTrayCheckBox ) );
		
		// Initialize system tray related checkboxes
		systemTrayIconEnablerCheckBoxActionListener.actionPerformed( null );
		
		// To consume the remaining space:
		contentBox.add( new JPanel( new BorderLayout() ) );
	}
	
	/**
	 * Checks for updates.
	 */
	private void checkUpdates() {
		checkUpdatesButton.setEnabled( false );
		
		checkUpdatesButton.setText( "Checking..." );
		checkUpdatesButton.getParent().doLayout();
		
		new NormalThread() {
			@Override
			public void run() {
				try {
					final String versionString = Utils.readVersionStringFromStream( new URL( Consts.LATEST_STABLE_VERSION_TEXT_URL ).openStream() );
					if ( versionString == null )
						throw new Exception();
					
					if ( versionString.equals( MainFrame.getInstance().applicationVersion ) )
						checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (no new version)" );
					else {
						if ( JOptionPane.showConfirmDialog( MainFrame.getInstance(), "A newer version of " + Consts.APPLICATION_NAME + " is available.\nWould you like to visit the home page to download it?", "New version available!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE ) == JOptionPane.YES_OPTION )
							Utils.showURLInBrowser( Consts.HOME_PAGE_URL );
						checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (new version available!)" );
					}
				} catch ( final Exception e ) {
					checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (check failed!)" );
				}
				finally {
					checkUpdatesButton.setEnabled( true );
					checkUpdatesButton.getParent().doLayout();
				}
			}
		}.start();
	}
	
	/**
	 * Returns the start folder for opening replays.
	 * @return the start folder for opening replays
	 */
	public File getReplayStartFolder() {
		final File replayStartFolder = new File( replayStartFolderTextField.getText() );
		return replayStartFolder.isAbsolute() ? replayStartFolder : new File( MainFrame.getInstance().starcraftFolderTextField.getText(), replayStartFolderTextField.getText() );
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP      , Boolean.toString( checkUpdatesOnStartupCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS, Boolean.toString( skipLatterActionsOfHackersCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SOUND_VOLUME                  , Integer.toString( soundVolumeSlider.getValue() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_REPLAY_START_FOLDER           , replayStartFolderTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ENABLE_SYSTEM_TRAY_ICON       , Boolean.toString( enableSystemTrayIconCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ALWAYS_MINIMIZE_TO_TRAY       , Boolean.toString( alwaysMinimizeToTrayCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_START_AGENT_MINIMIZED_TO_TRAY , Boolean.toString( startAgentMinimizedToTrayCheckBox.isSelected() ) );
	}
	
}
