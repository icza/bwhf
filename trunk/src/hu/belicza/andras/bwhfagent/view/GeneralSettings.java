package hu.belicza.andras.bwhfagent.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	private final JCheckBox autoCheckUpdatesCheckBox = new JCheckBox( "Check for updates on startup", true );
	/** Check for updates button.                                            */
	private final JButton checkUpdatesButton         = new JButton( CHECK_UPDATES_BUTTON_TEXT );
	
	/**
	 * Creates a new GeneralSettings.
	 */
	public GeneralSettings() {
		super( "General settings" );
		
		buildGUI();
	}
	
	private void buildGUI() {
		final JPanel panel = new JPanel();
		panel.add( autoCheckUpdatesCheckBox );
		checkUpdatesButton.setMnemonic( ( checkUpdatesButton.getText().charAt( 0 ) ) );
		checkUpdatesButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				checkUpdates();
			}
		} );
		panel.add( checkUpdatesButton );
		contentBox.add( panel );
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
				try { sleep(5000l); } catch ( final Exception e ) {}
				
				checkUpdatesButton.setText( CHECK_UPDATES_BUTTON_TEXT );
				checkUpdatesButton.setEnabled( true );
			}
		}.start();
	}
	
}
