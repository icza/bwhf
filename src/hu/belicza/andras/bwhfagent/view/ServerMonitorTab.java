package hu.belicza.andras.bwhfagent.view;

/**
 * Server monitor tab.
 * 
 * @author Andras Belicza
 */
public class ServerMonitorTab extends Tab {
	
	/** Name of the server list file. */
	private static final String SERVER_LIST_FILE_NAME = "server_list.txt";
	
	/**
	 * Creates a new ServerMOnitorTab.
	 */
	public ServerMonitorTab() {
		super( "Server monitor", IconResourceManager.ICON_SERVER_MONITOR );
		
		buildGUI();
	}
	
	/**
	 * Builds the graphical user interface of the tab.
	 */
	protected void buildGUI() {
		
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
