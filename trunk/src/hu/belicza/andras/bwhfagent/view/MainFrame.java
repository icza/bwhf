package hu.belicza.andras.bwhfagent.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * The main frame of BWHFAgent.
 * 
 * @author Andras Belicza
 */
public class MainFrame extends JFrame {
	
	public static final String APPLICATION_NAME    = "BWHF Agent";
	public static final String APPLICATION_VERSION = "0.1 2008-11-20";
	public static final String APPLICATION_AUTHOR  = "András Belicza";
	
	public MainFrame() {
		super( APPLICATION_NAME + " " + APPLICATION_VERSION + " by " + APPLICATION_AUTHOR );
		
		buildGUI();
		
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		
		pack();
		setVisible( true );
		setLocation( 100, 100 );
	}
	
	/**
	 * Builds the graphical user interface.
	 */
	private void buildGUI() {
		final JTabbedPane tabbedPane = new JTabbedPane();
		
		final Tab[] tabs = new Tab[] { new AutoscanTab(), new ManualScanTab() };
		
		for ( int tabIndex = 0; tabIndex < tabs.length; tabIndex++ ) {
			final Tab tab = tabs[ tabIndex ];
			final char mnemonicChar = Integer.toString( tabIndex + 1 ).charAt( 0 );
			tabbedPane.add( mnemonicChar + " " + tab.getTitle(), tab.getScrollPane() );
			tabbedPane.setMnemonicAt( tabIndex, mnemonicChar );
		}
		
		getContentPane().add( tabbedPane, BorderLayout.CENTER );
	}
	
}
