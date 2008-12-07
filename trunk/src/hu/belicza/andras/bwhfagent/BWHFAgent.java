package hu.belicza.andras.bwhfagent;

import hu.belicza.andras.bwhfagent.view.MainFrame;
import hu.belicza.andras.bwhfagent.view.Utils;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
		
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( final Exception e  ) {
		}
	    
		new MainFrame( applicationVersion == null ? "" : applicationVersion );
	}
	
}
