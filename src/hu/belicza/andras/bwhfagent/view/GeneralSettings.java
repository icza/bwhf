package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * General settings tab.
 * 
 * @author Andras Belicza
 */
public class GeneralSettings extends Tab {
	
	/** Text of the check updates button. */
	private static final String CHECK_UPDATES_BUTTON_TEXT = "Check now";
	
	/** Checkbox to tell whether check for updates automatically on startup. */
	private final JCheckBox checkUpdatesOnStartupCheckBox      = new JCheckBox( "Check for updates on startup", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP ) ) );
	/** Check for updates button.                                            */
	private final JButton checkUpdatesButton                   = new JButton( CHECK_UPDATES_BUTTON_TEXT );
	/** Checkbox to tell whether check for updates automatically on startup. */
	private final JCheckBox skipLatterActionsOfHackersCheckBox = new JCheckBox( "During a replay scan if a player is found hacking, skip scanning his latter actions", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS ) ) );
	
	/**
	 * Creates a new GeneralSettings.
	 */
	public GeneralSettings() {
		super( "General settings" );
		
		buildGUI();
		
		if ( checkUpdatesOnStartupCheckBox.isSelected() )
			checkUpdates();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final JPanel panel = new JPanel();
		panel.add( checkUpdatesOnStartupCheckBox );
		checkUpdatesButton.setMnemonic( ( checkUpdatesButton.getText().charAt( 0 ) ) );
		checkUpdatesButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				checkUpdates();
			}
		} );
		panel.add( checkUpdatesButton );
		contentBox.add( panel );
		
		contentBox.add( Utils.wrapInPanel( skipLatterActionsOfHackersCheckBox ) );
		// To consume the remaining space:
		contentBox.add( new JPanel( new BorderLayout() ) );
	}
	
	/**
	 * Checks for updates.
	 */
	private void checkUpdates() {
		checkUpdatesButton.setEnabled( false );
		
		checkUpdatesButton.setText( "Checking..." );
		
		new Thread() {
			@Override
			public void run() {
				try {
					final String versionString = Utils.readVersionStringFromStream( new URL( Consts.LATEST_STABLE_VERSION_TEXT_URL ).openStream() );
					if ( versionString == null )
						throw new Exception();
					
					if ( versionString.equals( Utils.getMainFrame().applicationVersion ) )
						checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (no new version)" );
					else {
						if ( JOptionPane.showConfirmDialog( Utils.getMainFrame(), "A newer version of " + Consts.APPLICATION_NAME + " is available.\nWould you like to visit the home page to download it?", "New version available!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE ) == JOptionPane.YES_OPTION )
							Utils.showURLInBrowser( Consts.HOME_PAGE_URL );
						checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (new version available!)" );
					}
				} catch ( final Exception e ) {
					checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT + " (check failed!)" );
				}
				finally {
					checkUpdatesButton.setEnabled( true );
				}
			}
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CHECK_UPDATES_ON_STARTUP      , Boolean.toString( checkUpdatesOnStartupCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS, Boolean.toString( skipLatterActionsOfHackersCheckBox.isSelected() ) );
	}
	
}
