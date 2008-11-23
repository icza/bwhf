package hu.belicza.andras.bwhfagent.view;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * The main frame of BWHFAgent.
 * 
 * @author Andras Belicza
 */
public class MainFrame extends JFrame {
	
	public static final String APPLICATION_NAME      = "BWHF Agent";
	public static final String VERSION_RESOURCE_NAME = "../current_version.txt";
	public static final String APPLICATION_AUTHOR    = "András Belicza";
	
	public MainFrame() {
		String applicationVersion = "";
		try {
			final BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( VERSION_RESOURCE_NAME ) ) );
			applicationVersion = bufferedReader.readLine();
			bufferedReader.close();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		
		setTitle( APPLICATION_NAME + " " + applicationVersion + " by " + APPLICATION_AUTHOR );
		
		buildGUI();
		
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		
		pack();
		setLocation( 100, 100 );
		setVisible( true );
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
