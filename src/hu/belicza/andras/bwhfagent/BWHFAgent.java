package hu.belicza.andras.bwhfagent;

import hu.belicza.andras.bwhfagent.view.MainFrame;
import hu.belicza.andras.bwhfagent.view.Utils;
import swingwtx.swing.SwingWTUtils;

/**
 * The main class of BWHFAgent.
 * 
 * @author Andras Belicza
 */
public class BWHFAgent {
	
	/**
	 * Entry point of the program.<br>
	 * Calls the agent starter respectively to the OS we're running on.
	 * 
	 * @param arguments used to take arguments from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		SwingWTUtils.setShowSwingWTInfoOnStartup( false );
		
		if ( SwingWTUtils.isMacOSX() ) {
			SwingWTUtils.initialiseMacOSX( new Runnable() {
				public void run() {
					startAgent( arguments );
				}
			} );
		}
		else
			startAgent( arguments );
	}
	
	/**
	 * Starts the agent.<br>
	 * Creates the main frame.
	 * 
	 * @param arguments used to take arguments from the running environment - not used here
	 */
	private static void startAgent( final String[] arguments ) {
		final String applicationVersion = Utils.readVersionStringFromStream( BWHFAgent.class.getResourceAsStream( Consts.VERSION_RESOURCE_NAME ) );
		
		// No need of changing to system LAF with SwingWT, it already uses native components
		/*try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( final Exception e  ) {
		}*/
	    
		new MainFrame( applicationVersion == null ? "" : applicationVersion, arguments );
	}
	
}
