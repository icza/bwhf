package hu.belicza.andras.bwhfagent.view;


/**
 * Manual scan tab.
 * 
 * @author Andras Belicza
 */
public class ManualScanTab extends LoggedTab {
	
	/** Log file name for autoscan. */
	private static final String LOG_FILE_NAME = "manual_scan.log";
	
	/**
	 * Creates a new AutoscanTab.
	 */
	public ManualScanTab() {
		super( "Manual scan", LOG_FILE_NAME );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		super.buildGUI();
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
