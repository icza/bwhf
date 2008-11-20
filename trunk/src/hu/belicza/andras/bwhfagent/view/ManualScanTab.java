package hu.belicza.andras.bwhfagent.view;

import javax.swing.Box;

/**
 * Manual scan tab.
 * 
 * @author Andras Belicza
 */
public class ManualScanTab extends Tab {
	
	/**
	 * Creates a new AutoscanTab.
	 */
	public ManualScanTab() {
		super( "Manual scan" );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final Box box = Box.createVerticalBox();
		setContent( box );
	}
	
}
