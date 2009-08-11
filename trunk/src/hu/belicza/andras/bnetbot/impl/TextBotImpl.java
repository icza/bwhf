package hu.belicza.andras.bnetbot.impl;

import hu.belicza.andras.bnetbot.BinaryBot;
import hu.belicza.andras.bnetbot.LoginConfig;
import hu.belicza.andras.bnetbot.Status;
import hu.belicza.andras.bnetbot.StatusChangeListener;
import hu.belicza.andras.bnetbot.TextBot;

/**
 * Default implementation of the text bot.
 * 
 * @author Andras Belicza
 */
public class TextBotImpl implements TextBot, StatusChangeListener {
	
	/** Status manager of this bot. */
	private StatusManager statusManager = new StatusManager();
	
	/** Reference to the binary bot that we're enhancing. */
	private BinaryBot binaryBot = new BinaryBotImpl();
	
	/**
	 * Creates a new TextBotImpl.
	 */
	public TextBotImpl() {
		binaryBot.registerStatusChangeListener( this );
	}
	
	/**
	 * @see TextBot#disconnect()
	 */
	public void disconnect() {
		binaryBot.disconnect();
	}
	
	/**
	 * @see TextBot#executeCommand(String)
	 */
	public String executeCommand( final String command ) {
		return null;
	}
	
	/**
	 * @see TextBot#isLoggedIn()
	 */
	public boolean isLoggedIn() {
		return statusManager.getStatus() == Status.LOGGED_IN;
	}
	
	/**
	 * @see TextBot#login(LoginConfig)
	 */
	public String login( final LoginConfig loginConfig ) {
		if ( statusManager.getStatus() == Status.LOGGED_IN )
			throw new IllegalStateException( "Illegal state! Already logged in!" );
		
		final String connectResult = binaryBot.connect( loginConfig );
		if ( connectResult == null ) {
			return binaryBot.login( loginConfig );
		}
		else
			return connectResult;
	}
	
	/**
	 * @see TextBot#registerStatusChangeListener(StatusChangeListener)
	 */
	public void registerStatusChangeListener( final StatusChangeListener statusChangeListener ) {
		statusManager.registerStatusChangeListener( statusChangeListener );
	}
	
	/**
	 * @see TextBot#removeStatusChangeListener(StatusChangeListener)
	 */
	public void removeStatusChangeListener( final StatusChangeListener statusChangeListener ) {
		statusManager.removeStatusChangeListener( statusChangeListener );
	}
	
	/**
	 * Called when the status of the underlying binary bot changes.
	 * @see StatusChangeListener#statusChanged(Status)
	 */
	public void statusChanged( final Status status ) {
		switch ( status ) {
		case DISCONNECTED : statusManager.setStatus( Status.DISCONNECTED ); break;
		case LOGGED_IN    : statusManager.setStatus( Status.LOGGED_IN    );
		}
	}
	
}
