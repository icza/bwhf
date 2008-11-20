package hu.belicza.andras.bwhfagent;

import hu.belicza.andras.bwhfagent.view.MainFrame;

import javax.swing.JFrame;

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
		new MainFrame();
	}
	
}
