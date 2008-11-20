package hu.belicza.andras.bwhfagent.view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JTextArea;

/**
 * Autoscan tab.
 * 
 * @author Andras Belicza
 */
public class AutoscanTab extends Tab {
	
	/** Log text area. */
	private final JTextArea logTextArea = new JTextArea();
	
	/**
	 * Creates a new AutoscanTab.
	 */
	public AutoscanTab() {
		super( "Autoscan" );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final Box box = Box.createVerticalBox();
		
		logTextArea.setEditable( false );
		logTextArea.setBorder( BorderFactory.createTitledBorder( "Log" ) );
		box.add( logTextArea );
		
		setContent( box );
	}
	
}
