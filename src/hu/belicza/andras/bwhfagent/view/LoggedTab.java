package hu.belicza.andras.bwhfagent.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * A tab with log and log control capabilities.
 * 
 * @author Belicza Andras
 */
public abstract class LoggedTab extends Tab {
	
	/** Date format to create timestamps for logging. */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	/** Log text area.        */
	private final JTextArea logTextArea        = new JTextArea( 10, 20 );
	/** Scroll Lock checkbox. */
	private final JCheckBox scrollLockCheckBox = new JCheckBox( "Scroll Lock" );
	
	/**
	 * Creates a new LoggedTab.
	 * @param title title of the tab
	 */
	public LoggedTab( final String title ) {
		super( title );
	}
	
	/**
	 * Builds the GUI of the tab.<br>
	 * Builds the log panel and adds it to the content box.
	 */
	protected void buildGUI() {
		final JPanel logPanel = new JPanel( new BorderLayout() );
		
		logTextArea.setEditable( false );
		logTextArea.setLineWrap( true );
		logTextArea.setWrapStyleWord( true );
		logPanel.add( new JScrollPane( logTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS ), BorderLayout.CENTER );
		
		final JPanel controlPanel = new JPanel();
		controlPanel.add( scrollLockCheckBox );
		final JButton clearLogButton = new JButton( "Clear" );
		clearLogButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent ae ) {
				logTextArea.setText( "" );
			}
		} );
		controlPanel.add( clearLogButton );
		logPanel.add( controlPanel, BorderLayout.SOUTH );
		
		logPanel.setBorder( BorderFactory.createTitledBorder( "Log" ) );
		contentBox.add( logPanel );
	}
	
	/**
	 * Logs a message.
	 * @param message message to be logged
	 */
	protected void logMessage( final String message ) {
		logTextArea.append( DATE_FORMAT.format( new Date() ) + " - " + message + "\n" );
		if ( !scrollLockCheckBox.isSelected() )
			logTextArea.setCaretPosition( logTextArea.getDocument().getLength() );
	}
	
}
