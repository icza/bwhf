package hu.belicza.andras.bwhfagent;

import hu.belicza.andras.bwhfagent.view.MainFrame;
import hu.belicza.andras.bwhfagent.view.Utils;
import swingwtx.swing.JFrame;
import swingwtx.swing.SwingWTUtils;

/**
 * The main class of BWHFAgent.
 * 
 * @author Andras Belicza
 */
@SuppressWarnings("serial")
public class BWHFAgent extends JFrame {
	
	/**
	 * Entry point of the program.<br>
	 * Creates the main frame.
	 * 
	 * @param arguments used to take arguments from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		final String applicationVersion = Utils.readVersionStringFromStream( BWHFAgent.class.getResourceAsStream( Consts.VERSION_RESOURCE_NAME ) );
		
		// No need of changing to system LAF with SwingWT, it already uses native components
		/*try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( final Exception e  ) {
		}*/
		SwingWTUtils.setShowSwingWTInfoOnStartup( false );
	    
		new MainFrame( applicationVersion == null ? "" : applicationVersion );
	}
	
}
