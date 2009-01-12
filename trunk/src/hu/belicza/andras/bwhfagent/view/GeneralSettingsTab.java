package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.net.URL;

import swingwt.awt.BorderLayout;
import swingwt.awt.Dimension;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JLabel;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.JSlider;

/**
 * General settings tab.
 * 
 * @author Andras Belicza
 */
public class GeneralSettingsTab extends Tab {
	
	/** Text of the check updates button. */
	private static final String CHECK_UPDATES_BUTTON_TEXT = "Check now";
	
	/** Checkbox to tell whether check for updates automatically on startup. */
	private final JCheckBox checkUpdatesOnStartupCheckBox      = new JCheckBox( "Check for updates on startup", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP ) ) );
	/** Check for updates button.                                            */
	private final JButton   checkUpdatesButton                 = new JButton( CHECK_UPDATES_BUTTON_TEXT );
	/** Checkbox to tell whether check for updates automatically on startup. */
	public  final JCheckBox skipLatterActionsOfHackersCheckBox = new JCheckBox( "During a replay scan if a player is found hacking, skip scanning his latter actions", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS ) ) );
	/** Slider to set the sound volume.                                      */
	public  final JSlider   soundVolumeSlider                  = new JSlider( 0, 100, Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_SOUND_VOLUME ) ) );
	
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
		JPanel panel = new JPanel();
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
		
		panel = new JPanel(); 
		panel.add( new JLabel( "Sound volume:" ) );
		soundVolumeSlider.setLabelTable( soundVolumeSlider.createStandardLabels( 10 ) );
		soundVolumeSlider.setPreferredSize( new Dimension( 300, 45 ) );
		panel.add( soundVolumeSlider );
		contentBox.add( panel );
		
		// To consume the remaining space:
		contentBox.add( new JPanel( new BorderLayout() ) );
		
	}
	
	/**
	 * Checks for updates.
	 */
	private void checkUpdates() {
		checkUpdatesButton.setEnabled( false );
		
		checkUpdatesButton.setText( "Checking..." );
		
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
					Utils.repackMainFrameDueToButtonChange( checkUpdatesButton );
				}
			}
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP      , Boolean.toString( checkUpdatesOnStartupCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS, Boolean.toString( skipLatterActionsOfHackersCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SOUND_VOLUME                  , Integer.toString( soundVolumeSlider.getValue() ) );
	}
	
}
