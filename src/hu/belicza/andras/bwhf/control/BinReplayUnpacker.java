package hu.belicza.andras.bwhf.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * A class to unpack a binary compressed replay file.<br>
 * The algorithm comes from JCA's bwreplib.<br>
 * Java port and optimization for Java environment by Andras Belicza.
 * 
 * See also: http://www.cs.utsa.edu/~wagner/laws/Abytes.html
 * 
 * @author Andras Belicza
 */
public class BinReplayUnpacker {
	
	/** Size of int. */
	private static final int INT_SIZE = 4;
	
	private static final byte[] OFF_507120 = { // length = 0x40
			(byte) 0x02, (byte) 0x04, (byte) 0x04, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06,
			(byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07,
			(byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07, (byte) 0x07,
			(byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08
	};
	
	private static final byte[] OFF_507160 = { // length = 0x40, com1
			(byte) 0x03, (byte) 0x0D, (byte) 0x05, (byte) 0x19, (byte) 0x09, (byte) 0x11, (byte) 0x01, (byte) 0x3E, (byte) 0x1E, (byte) 0x2E, (byte) 0x0E, (byte) 0x36, (byte) 0x16, (byte) 0x26, (byte) 0x06, (byte) 0x3A,
			(byte) 0x1A, (byte) 0x2A, (byte) 0x0A, (byte) 0x32, (byte) 0x12, (byte) 0x22, (byte) 0x42, (byte) 0x02, (byte) 0x7C, (byte) 0x3C, (byte) 0x5C, (byte) 0x1C, (byte) 0x6C, (byte) 0x2C, (byte) 0x4C, (byte) 0x0C,
			(byte) 0x74, (byte) 0x34, (byte) 0x54, (byte) 0x14, (byte) 0x64, (byte) 0x24, (byte) 0x44, (byte) 0x04, (byte) 0x78, (byte) 0x38, (byte) 0x58, (byte) 0x18, (byte) 0x68, (byte) 0x28, (byte) 0x48, (byte) 0x08,
			(byte) 0xF0, (byte) 0x70, (byte) 0xB0, (byte) 0x30, (byte) 0xD0, (byte) 0x50, (byte) 0x90, (byte) 0x10, (byte) 0xE0, (byte) 0x60, (byte) 0xA0, (byte) 0x20, (byte) 0xC0, (byte) 0x40, (byte) 0x80, (byte) 0x00
	};

	private static final byte[] OFF_5071A0 = { // length = 0x10
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08
	};
	
	private static final byte[] OFF_5071B0 = { // length = 0x20
			(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x07, (byte) 0x00,
			(byte) 0x08, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x0E, (byte) 0x00, (byte) 0x16, (byte) 0x00, (byte) 0x26, (byte) 0x00, (byte) 0x46, (byte) 0x00, (byte) 0x86, (byte) 0x00, (byte) 0x06, (byte) 0x01
	};
	
	private static final byte[] OFF_5071D0 = { // length = 0x10
			(byte) 0x03, (byte) 0x02, (byte) 0x03, (byte) 0x03, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x05, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x07, (byte) 0x07
	};
	
	private static final byte[] OFF_5071E0 = { // length = 0x10, com1
			(byte) 0x05, (byte) 0x03, (byte) 0x01, (byte) 0x06, (byte) 0x0A, (byte) 0x02, (byte) 0x0C, (byte) 0x14, (byte) 0x04, (byte) 0x18, (byte) 0x08, (byte) 0x30, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x00,
	};
	
	
	/** Input stream of the replay fie. */
	private final FileInputStream input;
	
	/** Buffer to be used to read int numbers.          */
	private final byte[] intBuffer;
	/** Buffer to be used in several section unpacking. */
	private final byte[] buffer;
	/** Esi struct used in several section unpacking.   */
	private final Esi    esi = new Esi();
	
	/**
	 * Creates a new BinReplayUnpacker.
	 * @param replayFile replay file to be unpacked
	 * @throws Exception if the specified file is a directory, or is a file but does not exist or if it is not a replay file (based on its size)
	 */
	public BinReplayUnpacker( final File replayFile ) throws Exception {
		if ( !replayFile.exists() || replayFile.isDirectory() || replayFile.length() < BinRepParser.HEADER_SIZE + 8 ) // Not enough data for id, header and commands length
			throw new Exception( "Not a replay file!" );
		
		input = new FileInputStream( replayFile );
		
		intBuffer = new byte[ INT_SIZE ];
		buffer    = new byte[ 0x2000 ];
	}
	
	private static class ReplayEnc {
		byte[] src;
		int    m04;
		byte[] m08;
		int    m0C;
		int    m10;
		int    m14;
	}
	
	private static class Esi {
		int       m00;
		int       m04;
		int       m08;
		int       m0C;
		int       m10;
		int       m14;
		int       m18;
		int       m1C;
		int       m20;
		ReplayEnc m24;
		int       m28;
		int       m2C;
		byte[]    m30   = new byte[ 0x1000 ];
		byte[]    m1030 = new byte[ 0x1000 ];
		byte[]    m2030 = new byte[ 0x0204 ];
		byte[]    m2234 = new byte[ 0x0800 ];
		byte[]    m2A34 = new byte[ 0x0100 ];
		byte[]    m2B34 = new byte[ 0x0100 ];
		byte[]    m2C34 = new byte[ 0x0100 ];
		byte[]    m2D34 = new byte[ 0x0180 ];
		byte[]    m2EB4 = new byte[ 0x0100 ];
		byte[]    m2FB4 = new byte[ 0x0100 ];
		byte[]    m30B4 = new byte[ 0x0040 ];
		byte[]    m30F4 = new byte[ 0x0010 ];
		byte[]    m3104 = new byte[ 0x0010 ];
		byte[]    m3114 = new byte[ 0x0020 ];
		
		public void init() {
			 m2C = m28 = m20 = m1C = m18 = m14 = m10 = m0C = m08 = m04 = m00 = 0;
			 m24 = null;
			 Arrays.fill( m30  , (byte) 0x00 );
			 Arrays.fill( m1030, (byte) 0x00 );
			 Arrays.fill( m2030, (byte) 0x00 );
			 Arrays.fill( m2234, (byte) 0x00 );
			 Arrays.fill( m2A34, (byte) 0x00 );
			 Arrays.fill( m2B34, (byte) 0x00 );
			 Arrays.fill( m2C34, (byte) 0x00 );
			 Arrays.fill( m2D34, (byte) 0x00 );
			 Arrays.fill( m2EB4, (byte) 0x00 );
			 Arrays.fill( m2FB4, (byte) 0x00 );
			 Arrays.fill( m30B4, (byte) 0x00 );
			 Arrays.fill( m30F4, (byte) 0x00 );
			 Arrays.fill( m3104, (byte) 0x00 );
			 Arrays.fill( m3114, (byte) 0x00 );
		}
	}
	
	/**
	 * Unpacks a seciton and returns a byte array of the unpacked data.
	 * @return a byte array of the unpacked data
	 * @throws Exception thrown if size is zero, if I/O error occurs or there's not enough data
	 */
	public synchronized byte[] unpackSection( final int size ) throws Exception {
		if ( size == 0 )
			throw new Exception();
		
		/*final int check = */readIntFromStream();
		final int count = readIntFromStream();
		
		final ReplayEnc rep = new ReplayEnc();
		
		int length, n, len = 0, m1C, m20 = 0;
		final byte[] result = new byte[ size ];
		int resultOffset = 0;
		
		esi.init();
		
		for ( n = 0, m1C = 0; n < count; n++, m1C += buffer.length, m20 += len ) {
			length = readIntFromStream();
			if ( length > size - m20 )
				throw new Exception();
			if ( input.read( result, resultOffset, length ) != length )
				throw new Exception();
			
			if ( length == Math.min( size - m1C, buffer.length ) )
				continue;
			
			rep.src = Arrays.copyOfRange( result, resultOffset, resultOffset + length );
			rep.m04 = 0;
			rep.m08 = buffer;
			rep.m0C = 0;
			rep.m10 = length;
			rep.m14 = buffer.length;
			
			if ( unpackRepSection( esi, rep ) == 0 && rep.m0C <= buffer.length )
				len = rep.m0C;
			else
				len = 0;
			
			if ( len == 0 || len > size )
				throw new Exception();
			
			System.arraycopy( buffer, 0, result, resultOffset, len );
		}
		
		return result;
	}
	
	private int unpackRepSection( final Esi esi, final ReplayEnc rep ) {
		esi.m24 = rep;
		esi.m1C = 0x800;
		esi.m20 = esi28( esi.m2234, esi.m1C, esi.m24 );
		if ( esi.m20 <= 4 )
			return 3;
		esi.m04 = rep.src[ 0 ] & 0xff;
		esi.m0C = rep.src[ 1 ] & 0xff;
		esi.m14 = rep.src[ 2 ] & 0xff;
		esi.m18 = 0;
		esi.m1C = 3;
		if ( esi.m0C < 4 || esi.m0C > 6 )
			return 1;
		esi.m10 = ( 1 << esi.m0C ) - 1;   // 2^n - 1
		
		if ( esi.m04 != 0 )
			return 2;
		
		System.arraycopy( OFF_5071D0, 0, esi.m30F4, 0, OFF_5071D0.length );
		com1( OFF_5071E0.length, esi.m30F4, OFF_5071E0, esi.m2B34 );
		System.arraycopy( OFF_5071A0, 0, esi.m3104, 0, OFF_5071A0.length );
		System.arraycopy( OFF_5071B0, 0, esi.m3114, 0, OFF_5071B0.length );
		System.arraycopy( OFF_507120, 0, esi.m30B4, 0, OFF_507120.length );
		com1( OFF_507160.length, esi.m30B4, OFF_507160, esi.m2A34 );
		unpackRepChunk( esi );
		
		return 0;
	}
	
	private void com1( final int strlen, final byte[] src, final byte[] str, final byte[] dst ) {
		int x,y;
		for ( int n = strlen - 1 ; n >= 0; n-- )
			for ( x = str[ n ] & 0xff, y = 1 << ( src[ n ] & 0xff ); x < 0x100; x += y )
				dst[ x ] = (byte) n;
	}
	
	private int unpackRepChunk( final Esi esi ) {
		int tmp, len;
		
		esi.m08 = 0x1000;
		do {
			len = function1( esi );
			if ( len >= 0x305 )
				break;
			if ( len >= 0x100 ) { // decode region of length len -0xFE
				len -= 0xFE;
				tmp = function2( esi, len );
				if ( tmp == 0 ) {
					len = 0x306;
					break;
				}
				for ( ; len > 0; esi.m08++, len-- )
					try {
						esi.m30[ esi.m08 ] = esi.m30[ esi.m08 - tmp ];
					}
					catch ( final ArrayIndexOutOfBoundsException aioobe ) {
						// In C there is no index checking, so the next buffer in the sturct is overwritten (maybe this is intended). 
						esi.m1030[ esi.m08 - esi.m30.length ] = esi.m08 - tmp < esi.m30.length ? esi.m30[ esi.m08 - tmp ] : esi.m1030[ esi.m08 - tmp - esi.m30.length ];
					}
			}
			else { // just copy the character
				try {
					esi.m30[ esi.m08 ] = (byte) len;
				}
				catch ( final ArrayIndexOutOfBoundsException aioobe ) {
					// In C there is no index checking, so the next buffer in the sturct is overwritten (maybe this is intended). 
					esi.m1030[ esi.m08 - esi.m30.length ] = (byte) len;
				}
				esi.m08++;
			}
			if ( esi.m08 < 0x2000 )
				continue;
			esi2C( esi.m1030, 0x1000, esi.m24 );
			System.arraycopy( esi.m1030, 0, esi.m30, 0, esi.m08 - 0x1000 );
			esi.m08 -= 0x1000;
		} while ( true );
		esi2C( esi.m1030, esi.m08 - 0x1000, esi.m24 );
		
		return len;
	}
	
	private int function1( final Esi esi ) {
		int x, result;
		
		// myesi.m14 is odd
		if ( ( 1 & esi.m14 ) != 0 ) {
			if ( common( esi, 1 ) )
				return 0x306;
			result = esi.m2B34[ esi.m14 & 0xff ] & 0xff;
			if ( common( esi, esi.m30F4[ result ] & 0xff ) )
				return 0x306;
			if ( esi.m3104[ result ] != (byte) 0 ) {
				x = ( ( 1 << ( esi.m3104[ result ] & 0xff ) ) - 1 ) & esi.m14;
				if ( common( esi, esi.m3104[ result ] & 0xff ) && ( result + x ) != 0x10E )
					return 0x306;
				// TODO: examine this from closer, low-high byte orders...
				//result =  Short.reverseBytes( ByteBuffer.wrap( esi.m3114, 2*result, 2 ).getShort() );
				result =  ( ( esi.m3114[ 2 * result + 1 ] & 0xff ) << 8 ) | ( esi.m3114[ 2 * result ] & 0xff ); // memcpy(&result, &myesi->m3114[2*result], 2);
				result += x;
			}
			return result + 0x100;
		}
		// myesi->m14 is even
		if ( common( esi, 1 ) )
			return 0x306;
		if ( esi.m04 == 0 ) {
			result = esi.m14 & 0xff;
			if ( common( esi, 8 ) )
				return 0x306;
		return result;
		}
		if ( esi.m14 == 0 ) {
			if ( common( esi, 8 ) )
		    	return 0x306;
		    result = esi.m2EB4[ esi.m14 & 0xff ] & 0xff;
		}
		else {
			result = esi.m2C34[ esi.m14 & 0xff ] & 0xff;
			if ( result == 0xFF ) {
				if ( ( esi.m14 & 0x3F ) == 0 ) {
					if ( common( esi, 6 ) )
						return 0x306;
					result = esi.m2C34[ esi.m14 & 0x7F ] & 0xff;
				}
				else {
					if ( common( esi, 4 ) )
						return 0x306;
					result = esi.m2D34[ esi.m14 & 0xFF ] & 0xff;
				}
			}
		}
		if ( common( esi, esi.m2FB4 [result ] & 0xff ) )
			return 0x306;
		return result;
	}
	
	private int function2( final Esi esi, final int len ) {
		int tmp;
		
		tmp = esi.m2A34[ esi.m14 & 0xff ] & 0xff;
		if ( common( esi, esi.m30B4[ tmp ] & 0xff ) )
			return 0;
		if ( len != 2 ) {
			tmp <<= esi.m0C & 0xff;
			tmp |= esi.m14 & esi.m10;
			if ( common( esi, esi.m0C ) )
				return 0;
		}
		else {
			tmp <<= 2;
			tmp |= esi.m14 & 3;
			if ( common( esi, 2 ) )
				return 0;
		}   /* A38 */
		
		return tmp + 1;
	}
	
	private boolean common( final Esi esi, int count ) {
		int tmp;
		
		if ( esi.m18 < count ) {
			esi.m14 >>>= esi.m18 & 0xff;
			if ( esi.m1C == esi.m20 ) {
				esi.m20 = esi28( esi.m2234, 0x800, esi.m24 );
				if ( esi.m20 == 0 )
					return true;
				else
					esi.m1C = 0;
			}
			tmp = esi.m2234[ esi.m1C ] & 0xff;
			tmp <<= 8;
			esi.m1C++;
			tmp |= esi.m14;
			esi.m14 = tmp;
			tmp >>>= ( count - ( esi.m18  & 0xff ) );
			esi.m14 = tmp;
			esi.m18 += ( 8 - count );
		}
		else {
			esi.m18 -= count;
			esi.m14 >>>= count & 0xff;
		}
		
		return false;
	}
	
	private int esi28( final byte[] dst, int len, final ReplayEnc rep ) {
		len = Math.min( rep.m10 - rep.m04, len );
		System.arraycopy( rep.src, rep.m04, dst, 0, len );
		rep.m04 += len;
		return len;
	}
	
	private void esi2C( final byte[] src, final int len, final ReplayEnc rep ) {
		if ( rep.m0C + len <= rep.m14 )
			System.arraycopy( src, 0, rep.m08, rep.m0C, len );
		rep.m0C += len;
	}
	
	/**
	 * Reads an int from the file.
	 * @return the int read from the file
	 * @throws Exception if I/O error occurs or there's not enough data
	 */
	private int readIntFromStream() throws Exception {
		if ( input.read( intBuffer ) != INT_SIZE )
			throw new Exception();
		return Integer.reverseBytes( ByteBuffer.wrap( intBuffer ).getInt() );
	}
	
	/**
	 * Closes the replay file input stream if it's not null.
	 */
	public void close() {
		if ( input != null )
			try {
				input.close();
			} catch ( final IOException ie ) {
			}
	}
	
}
