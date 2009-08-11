package hu.belicza.andras.bnetbot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Formatter;

/**
 * Class representing a binary packet sent to and received from a Battle.net server.
 *  
 * @author Andras Belicza
 */
public class BnetPacket {
	
	/** Empty message to keep the conneciton alive. */
	public static final byte SID_NULL       = (byte) 0x00;
	/** Authorization info id.                      */
	public static final byte SID_AUTH_INFO  = (byte) 0x50;
	/** Authorization result.                       */
	public static final byte SID_AUTH_CHECK = (byte) 0x51;
    
	/** Packet identifier.         */
	public final byte   id;
	
	/** Data buffer of the packet. */
	public final byte[] data;
	
	/**
	 * Creates a new BnetPacket.
	 * @param data data buffer of the packet.
	 */
	public BnetPacket( final byte[] data ) {
		this.id   = (byte) -1;
		this.data = data;
	}
	
	/**
	 * Creates a new BnetPacket.
	 * @param data data buffer of the packet.
	 */
	public BnetPacket( final byte id, final byte[] data ) {
		this.id   = id;
		this.data = data;
	}
	
	/**
	 * Returns a byte buffer wrapper of the data array.<br>
	 * The byte order of the returned wrapper is changed to LITTLE_ENDIAN.
	 * 
	 * @return a byte buffer wrapper of the data array
	 */
	public ByteBuffer getByteBufferWrapper() {
		final ByteBuffer byteBufferWrapper = ByteBuffer.wrap( data );
		byteBufferWrapper.order( ByteOrder.LITTLE_ENDIAN );
		return byteBufferWrapper;
	}
	
	/**
	 * Produces a nice debug representation such as:<br>
	 * <pre>
	 * Packet id=0x50, length: 47
	 * 0x00: | 00 00 00 00 49 58 38 36  44 32 44 56 00 00 00 00 | ````IX86 D2DV````
	 * 0x10: | 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 | ```````` ````````
	 * 0x20: | 00 00 00 00 43 41 4e 00  63 61 6e 61 64 61 00 00 | ````CAN` canada`
	 * </pre>
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		
		builder.append( "Packet id=0x" ).append( Integer.toHexString( id & 0xff ) ).append( ", length: " ).append( data.length );
		
		for ( int i = 0; i < data.length; i++ ) {
			if ( ( i & 0x0f ) == 0 )
				builder.append( new Formatter().format( "\n0x%02x: | ", i ).toString() );
			
			builder.append( new Formatter().format( "%02x ", data[ i ] & 0xff ).toString() );
			if ( ( i & 0x0f ) == 0x07 )
				builder.append( ' ' );
			
			if ( ( i & 0x0f ) == 0x0f || i == data.length - 1 ) {
				if ( i == data.length - 1 ) // Not full line
					for ( int j = 0x0f - ( i & 0x0f ) - 1; j >= 0; j-- ) {
						builder.append( "   " );
						if ( ( j & 0x0f ) == 0x07 )
							builder.append( ' ' );
					}
				
				builder.append( "| " );
				for ( int j = i & 0xfff0; j <= i; j++ ) {
					builder.append( (char) ( data[ j ] &0xff ) );
					if ( ( j & 0x0f ) == 0x07 )
						builder.append( ' ' );
				}
			}
		}
		
		return builder.toString();
	}
	
}
