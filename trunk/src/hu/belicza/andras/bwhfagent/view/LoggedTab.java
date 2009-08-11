package hu.belicza.andras.bwhfagent.view;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import swingwt.awt.BorderLayout;
import swingwt.awt.Font;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Icon;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JPanel;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.JTextArea;

/**
 * A tab with log and log control capabilities.
 * 
 * @author Belicza Andras
 */
public abstract class LoggedTab extends Tab {
	
	/** Date format to create timestamps for logging. */
	public    static final DateFormat DATE_FORMAT          = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	/** The default system dependent line separator.  */
	public    static final String     LINE_SEPARATOR       = System.getProperty( "line.separator", "\r\n" );
	/** Limit of messages before auto-clearing.       */
	private   static final int        MESSAGES_COUNT_LIMIT = 2000;
	
	/** Log text area.        */
	private final JTextArea logTextArea        = new JTextArea( 5, 10 );
	/** Scroll Lock checkbox. */
	private final JCheckBox scrollLockCheckBox = new JCheckBox( "Scroll Lock" );
	
	/** Number of messages in the log text area. */
	private int messagesCount = 0;
	
	private final String logFileName;
	/** Log file writer. */
	private FileWriter   logFileWriter = null;
	
	/**
	 * Creates a new LoggedTab.
	 * @param title       title of the tab
	 * @param icon        icon of the tab
	 * @param logFileName name of the log file
	 */
	public LoggedTab( final String title, final Icon icon, final String logFileName ) {
		super( title, icon );
		this.logFileName = logFileName;
		
		try {
			logFileWriter = new FileWriter( logFileName, true );
		} catch ( final IOException ie ) {
		}
	}
	
	/**
	 * Builds the GUI of the tab.<br>
	 * Builds the log panel and adds it to the content box.
	 */
	protected void buildGUI() {
		final JPanel logPanel = new JPanel( new BorderLayout() );
		logPanel.setMaximumSize( Utils.getMaxDimension() );
		
		logTextArea.setEditable( false );
		logTextArea.setLineWrap( true );
		logTextArea.setWrapStyleWord( true );
		logTextArea.setTabSize( 4 );
		logTextArea.setFont( new Font( "default", Font.PLAIN, 10 ) );
		logPanel.add( new JScrollPane( logTextArea ), BorderLayout.CENTER );
		
		final JPanel controlPanel = Utils.createWrapperPanel();
		controlPanel.add( scrollLockCheckBox );
		final JButton clearLogButton = new JButton( "Clear" );
		clearLogButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent ae ) {
				logTextArea.setText( "" );
				messagesCount = 0;
			}
		} );
		controlPanel.add( clearLogButton );
		final JButton viewEntireLogButton = new JButton( "View entire log", IconResourceManager.ICON_VIEW_LOG );
		viewEntireLogButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.editFile( logFileName );
			}
		} );
		controlPanel.add( viewEntireLogButton );
		logPanel.add( controlPanel, BorderLayout.SOUTH );
		
		logPanel.setBorder( BorderFactory.createTitledBorder( "This content is also saved to file '" + logFileName + "'." ) );
		contentBox.add( logPanel );
	}
	
	/**
	 * Logs a message.
	 * @param message message to be logged
	 */
	protected void logMessage( final String message ) {
		logMessage( message, true );
	}
	
	/**
	 * Logs a message.
	 * @param message message to be logged
	 * @param appendTimeStamp tells whether timestamp should be appended to the message
	 */
	protected void logMessage( final String message, final boolean appendTimeStamp ) {
		if ( messagesCount++ == MESSAGES_COUNT_LIMIT ) {
			logTextArea.setText( "    ....auto-clearing log text area (full log is still available)..." + LINE_SEPARATOR );
			messagesCount = 0;
		}
		
		// In SwingWT tabs are not working, so we replace them.
		final String formattedMessage = ( appendTimeStamp ? DATE_FORMAT.format( new Date() ) + " - " : "" ) + message.replace( "\t", "    " ) + LINE_SEPARATOR;
		
		final int caretPosition = logTextArea.getCaretPosition();
		logTextArea.append( formattedMessage );
		
		// SwingWT scrolls automatically
		if ( scrollLockCheckBox.isSelected() )
			logTextArea.setCaretPosition( caretPosition );
		
		if ( logFileWriter != null )
			try {
				logFileWriter.write( formattedMessage );
				logFileWriter.flush();
			} catch ( IOException ie ) {
			}
	}
	
}
