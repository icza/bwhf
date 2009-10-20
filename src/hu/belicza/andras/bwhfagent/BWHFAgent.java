package hu.belicza.andras.bwhfagent;

import hu.belicza.andras.bwhfagent.view.MainFrame;
import hu.belicza.andras.bwhfagent.view.Utils;

import java.awt.SplashScreen;

import javax.swing.UIManager;

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
		startAgent( arguments );
	}
	
	/**
	 * Starts the agent.<br>
	 * Creates the main frame and hides the splash screen.
	 * 
	 * @param arguments used to take arguments from the running environment - not used here
	 */
	private static void startAgent( final String[] arguments ) {
		final String applicationVersion = Utils.readVersionStringFromStream( BWHFAgent.class.getResourceAsStream( Consts.VERSION_RESOURCE_NAME ) );
		
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( final Exception e  ) {
		}
	    
		new MainFrame( applicationVersion == null ? "" : applicationVersion, arguments );
		
		// Since the main frame is not an AWT or Swing window, splash screen will not disappear by itself. Make it disappear.
		final SplashScreen splashScreen = SplashScreen.getSplashScreen();
		if ( splashScreen != null )
			splashScreen.close();
	}
	
}
