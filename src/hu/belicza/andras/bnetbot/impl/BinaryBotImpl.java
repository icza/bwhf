package hu.belicza.andras.bnetbot.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	/** The socket of the connection to the battle.net server.    */
	private Socket        socket;
	/** Input stream of the connection to the battle.net server.  */
	private InputStream   inputStream;
	/** Output stream of the connection to the battle.net server. */
	private OutputStream  outputStream;
	
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
	 * @see BinaryBot#connect(LoginConfig)
	 */
	public String connect( final LoginConfig loginConfig ) {
		if ( statusManager.getStatus() != Status.DISCONNECTED )
			throw new IllegalStateException( "Illegal state! Already connected!" );
		
		try {
			socket       = new Socket( loginConfig.serverUrl, loginConfig.serverPort );
			inputStream  = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			statusManager.setStatus( Status.CONNECTED );
		} catch ( final UnknownHostException uhe) {
			return "Unknown host: " + loginConfig.serverUrl;
		} catch ( final IOException ie ) {
			return "Cannot connect to: " + loginConfig.serverUrl + ":" + loginConfig.serverPort;
		}
		
		return null;
	}
	
	/**
	 * @see BinaryBot#login(LoginConfig)
	 */
	public String login( final LoginConfig loginConfig ) {
		if ( statusManager.getStatus() != Status.CONNECTED )
			throw new IllegalStateException( "Illegal state! Must be connected and not logged in!" );
		
		sendPacket( PacketFactory.createBinaryProtocolPacket() );
		sendPacket( PacketFactory.createAuthorizationInfoPacket() );
		
		return null;
	}
	
	/**
	 * @see BinaryBot#sendPacket(BnetPacket)
	 */
	public boolean sendPacket( final BnetPacket packet ) {
		if ( statusManager.getStatus() == Status.DISCONNECTED )
			throw new IllegalStateException( "Not connected!" );
		
		try {
			outputStream.write( packet.data );
			outputStream.flush();
			return true;
		} catch ( final IOException ie ) {
			// Disconnected from battle.net
			disconnect();
			return false;
		}
	}
	
	/**
	 * @see BinaryBot#readPacket()
	 */
	public BnetPacket readPacket() {
		if ( statusManager.getStatus() == Status.DISCONNECTED )
			throw new IllegalStateException( "Not connected!" );
		
		try {
			final int packetStartByte = inputStream.read();
			if ( packetStartByte == -1 )
				throw new IOException( "Connection lost!" );
			
			final byte packetId = (byte) inputStream.read();
			int packetLength = inputStream.read() & 0xff;
			packetLength    += ( inputStream.read() & 0xff ) << 8;
			
			if ( packetStartByte != 0xff )
				throw new IOException( "Invalid packet start byte (0x" + Integer.toHexString( packetStartByte ) + " )!" );
			
			final byte[] packetData = new byte[ packetLength - 4 ];
			final int    bytesRead  = inputStream.read( packetData );
			if ( bytesRead < packetData.length )
				throw new IOException( "Broken packet!" );
			
			return new BnetPacket( packetId, packetData );
		} catch ( final IOException ie ) {
			// Disconnected from battle.net
			disconnect();
			return null;
		}
	}
	
	/**
	 * @see BinaryBot#disconnect()
	 */
	public void disconnect() {
		if ( socket != null ) {
			try { socket.close(); } catch ( final IOException ie ) {}
			outputStream = null;
			inputStream  = null;
			socket       = null;
		}
		
		statusManager.setStatus( Status.DISCONNECTED );
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
	
}
