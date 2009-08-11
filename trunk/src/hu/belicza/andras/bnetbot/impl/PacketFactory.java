package hu.belicza.andras.bnetbot.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import hu.belicza.andras.bnetbot.BnetPacket;

/**
 * Factory class to create Battle.net packets.
 * 
 * @author Andras Belicza
 */
public class PacketFactory {
	
	/** Platform id to send to the bnet server.  */
	private static final String PLATFORM_ID  = "IX86";
	/** Game id to send to the bnet server.      */
	private static final String GAME_ID      = "D2DV"; // Diablo 2, not expansion
	/** Country code to send to the bnet server. */
	private static final String COUNTRY_CODE = "CAN";
	/** Country to send to the bnet server.      */
	private static final String COUNTRY      = "canada";
	
	/**
	 * Creates and returns a protocol packet indicating binary protocol.
	 * @return a protocol packet indicating binary protocol
	 */
	public static BnetPacket createBinaryProtocolPacket() {
		return new BnetPacket( new byte[] { 0x01 } ) ;
	}
	
	/**
	 * Creates and returns a protocol packet indicating binary protocol.
	 * @return a protocol packet indicating binary protocol
	 */
	public static BnetPacket createAuthorizationInfoPacket() {
		final ByteBuffer byteBuffer = ByteBuffer.allocate( 100 ); // TODO: revise capacity
		byteBuffer.order( ByteOrder.LITTLE_ENDIAN );
		
		addStringToBuffer( PLATFORM_ID, byteBuffer );
		
		addStringToBuffer( GAME_ID, byteBuffer );
		byteBuffer.putInt( 0x00 ); // Game version, TODO: revise version
		
		// Non-essential data
		byteBuffer.putInt( 0x00 ); // Product language
		byteBuffer.putInt( 0x00 ); // Local IP
		byteBuffer.putInt( 0x00 ); // Time zone
		byteBuffer.putInt( 0x00 ); // Local ID
		byteBuffer.putInt( 0x00 ); // Language ID
		
		addCStringToBuffer( COUNTRY_CODE, byteBuffer );
		addCStringToBuffer( COUNTRY     , byteBuffer );
		
		return new BnetPacket( BnetPacket.ID_AUTHORIZATION_INFO, byteBuffer.array() );
	}
	
	/**
	 * Adds a string to a buffer. Terminates it with a null-code character.
	 * @param s      string to be added
	 * @param buffer buffer to add to
	 */
	private static void addStringToBuffer( final String s, final ByteBuffer buffer ) {
		for ( int i = s.length() - 1; i >= 0; i-- )
			buffer.put( (byte) s.charAt( i ) );
	}
	
	/**
	 * Adds a string to a buffer. Terminates it with a null-code character.
	 * @param s      string to be added
	 * @param buffer buffer to add to
	 */
	private static void addCStringToBuffer( final String s, final ByteBuffer buffer ) {
		for ( int i = s.length() - 1; i >= 0; i-- )
			buffer.put( (byte) s.charAt( i ) );
		buffer.put( (byte) 0x00 );
	}
	
}
