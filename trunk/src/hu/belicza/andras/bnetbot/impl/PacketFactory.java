package hu.belicza.andras.bnetbot.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import hu.belicza.andras.bnetbot.BnetPacket;
import hu.belicza.andras.bnetbot.LoginConfig;

/**
 * Factory class to create Battle.net packets.
 * 
 * @author Andras Belicza
 */
public class PacketFactory {
	
	/**
	 * Creates and returns a protocol packet indicating binary protocol.
	 * @return a protocol packet indicating binary protocol
	 */
	public static BnetPacket createBinaryProtocolPacket() {
		return new BnetPacket( new byte[] { 0x01 } ) ;
	}
	
	/**
	 * Creates and returns a protocol packet indicating binary protocol.
	 * @param loginConfig login config holding all the data required to login
	 * @return a protocol packet indicating binary protocol
	 */
	public static BnetPacket createAuthorizationInfoPacket( final LoginConfig loginConfig ) {
		final ByteBuffer byteBuffer = ByteBuffer.allocate( 64 ); // TODO: revise capacity
		byteBuffer.order( ByteOrder.LITTLE_ENDIAN );
		
		byteBuffer.putInt( 0x00 ); // Protocol ID
		addStringToBuffer( loginConfig.platformId, byteBuffer );
		addStringToBuffer( loginConfig.productId , byteBuffer );
		byteBuffer.putInt( 0x00 ); // Version byte TODO: determine its value
		
		// Non-essential data
		byteBuffer.putInt( 0x00 ); // Product language
		byteBuffer.putInt( 0x00 ); // Local IP for NAT compatibility
		byteBuffer.putInt( 0x00 ); // Timezone bias
		byteBuffer.putInt( 0x00 ); // Locale ID
		byteBuffer.putInt( 0x00 ); // Language ID
		
		addCStringToBuffer( loginConfig.countryCode, byteBuffer );
		addCStringToBuffer( loginConfig.country    , byteBuffer );
		
		return new BnetPacket( BnetPacket.SID_AUTH_INFO, Arrays.copyOf( byteBuffer.array(), byteBuffer.position() ) );
	}
	
	/**
	 * Adds a string to a buffer. Terminates it with a null-code character.
	 * @param s      string to be added
	 * @param buffer buffer to add to
	 */
	private static void addStringToBuffer( final String s, final ByteBuffer buffer ) {
		for ( int i = 0; i < s.length(); i++ )
			buffer.put( (byte) s.charAt( i ) );
	}
	
	/**
	 * Adds a string to a buffer. Terminates it with a null-code character.
	 * @param s      string to be added
	 * @param buffer buffer to add to
	 */
	private static void addCStringToBuffer( final String s, final ByteBuffer buffer ) {
		for ( int i = 0; i < s.length(); i++ )
			buffer.put( (byte) s.charAt( i ) );
		buffer.put( (byte) 0x00 );
	}
	
	/**
	 * Reads a null terminated string from a buffer.
	 * @param buffer buffer to add to
	 * @reutrn the null terminated string read from the buffer without the terminating null character
	 */
	public static String readCStringFromBuffer( final ByteBuffer buffer ) {
		final StringBuilder builder = new StringBuilder();
		int readChar;
		while ( ( readChar = buffer.get() ) != 0x00 )
			builder.append( (char) readChar );
		
		return builder.toString();
	}
	
}
