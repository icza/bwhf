package hu.belicza.andras.bnetbot.impl;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import hu.belicza.andras.bnetbot.BinaryBot;
import hu.belicza.andras.bnetbot.BnetPacket;
import hu.belicza.andras.bnetbot.LoginConfig;
import hu.belicza.andras.bnetbot.Status;
import hu.belicza.andras.bnetbot.StatusChangeListener;

/**
 * Default implementation of the binary bot.
 * 
 * @author Andras Belicza
 */
public class BinaryBotImpl implements BinaryBot {
	
	/** Status manager of this bot. */
	private StatusManager statusManager = new StatusManager();
	
	/** The socket of the connection to the battle.net server. */
	private Socket socket;
	
	/**
	 * @see BinaryBot#connect(LoginConfig)
	 */
	public String connect( final LoginConfig loginConfig ) {
		if ( statusManager.getStatus() != Status.DISCONNECTED )
			throw new IllegalStateException( "Illegal state! Already connected!" );
		
		try {
			socket = new Socket( loginConfig.serverUrl, loginConfig.serverPort );
			statusManager.setStatus( Status.CONNECTED );
		} catch ( final UnknownHostException uhe) {
			return "Unknown host: " + loginConfig.serverUrl;
		} catch ( final IOException ie ) {
			return "Cannot connect to: " + loginConfig.serverUrl + ":" + loginConfig.serverPort;
		}
		
		return null;
	}
	
	/**
	 * @see BinaryBot#disconnect()
	 */
	public void disconnect() {
		if ( socket != null ) {
			try { socket.close(); } catch ( final IOException ie ) {}
			socket = null;
		}
		
		statusManager.setStatus( Status.DISCONNECTED );
	}
	
	/**
	 * @see BinaryBot#isConnected()
	 */
	public boolean isConnected() {
		return statusManager.getStatus() != Status.DISCONNECTED;
	}
	
	/**
	 * @see BinaryBot#isLoggedIn()
	 */
	public boolean isLoggedIn() {
		return statusManager.getStatus() == Status.LOGGED_IN;
	}
	
	/**
	 * @see BinaryBot#registerStatusChangeListener(StatusChangeListener)
	 */
	public void registerStatusChangeListener( final StatusChangeListener statusChangeListener ) {
		statusManager.registerStatusChangeListener( statusChangeListener );
	}
	
	/**
	 * @see BinaryBot#removeStatusChangeListener(StatusChangeListener)
	 */
	public void removeStatusChangeListener( final StatusChangeListener statusChangeListener) {
		statusManager.removeStatusChangeListener( statusChangeListener );
	}
	
	/**
	 * @see BinaryBot#sendPacket(BnetPacket)
	 */
	public void sendPacket( final BnetPacket packet ) {
	}
	
}
