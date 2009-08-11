package hu.belicza.andras.bwhfagent.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import hu.belicza.andras.bnetbot.LoginConfig;
import hu.belicza.andras.bnetbot.TextBot;
import hu.belicza.andras.bnetbot.impl.TextBotImpl;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.JTextArea;


/**
 * Bot tab.
 * 
 * @author Andras Belicza
 */
public class BotTab extends Tab {
	
	/** Date format to create timestamps for messages. */
	private static final DateFormat DATE_FORMAT  = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	/** Text area to display bnet and chat messages. */
	private final JTextArea messagesTextArea = new JTextArea();
	
	/** Reference to the underlaying bot. */
	private TextBot textBot;
	
	/**
	 * Creates a new ChartsTab.
	 */
	public BotTab() {
		super( "Bot", IconResourceManager.ICON_CHARTS );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final JButton connectButton = new JButton( "Connect" );
		connectButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				textBot = new TextBotImpl();
				
				final LoginConfig loginConfig = new LoginConfig( 3, "someuser", "somepass" );
				appendMessage( "Connecting to " + loginConfig.serverUrl + ":" + loginConfig.serverPort + "..." );
				final String result = textBot.login( loginConfig );
				appendMessage( result == null ? "Connected." : result );
				textBot.disconnect();
			}
		} );
		contentBox.add( Utils.wrapInPanel( connectButton ) );
		
		contentBox.add( new JScrollPane( messagesTextArea ) );
	}
	
	/**
	 * Appends a message to the messages text area.
	 * @param message message to be appended
	 */
	private void appendMessage( final String message ) {
		messagesTextArea.append( DATE_FORMAT.format( new Date() ) + ": " + message + LoggedTab.LINE_SEPARATOR );
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
