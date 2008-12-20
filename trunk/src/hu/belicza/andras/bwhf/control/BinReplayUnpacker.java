package hu.belicza.andras.bwhf.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A class to unpack a binary compressed replay file.<br>
 * This algorithm is the Java traversion of JCA's bwreplib.<br>
 * Optimized for Java environment by Andras Belicza.
 * 
 * @author Andras Belicza
 */
public class BinReplayUnpacker {
	
	/** Size of int. */
	private static final int INT_SIZE = 4;
	
	/** Input stream of the replay fie. */
	private final FileInputStream input;
	
	/** Buffer to be used to read int numbers.      */
	private final byte[] intBuffer;
	/** Buffer to be used during section unpacking. */
	private final byte[] buffer;
	
	/**
	 * Creates a new BinReplayUnpacker.
	 * @param replayFile replay file to be unpacked
	 * @throws Exception if the specified file is a directory, or is a file but does not exist or if it is not a replay file (based on its size)
	 */
	public BinReplayUnpacker( final File replayFile ) throws Exception {
		if ( !replayFile.exists() || replayFile.isDirectory() || replayFile.length() < 641 ) // Not enough data for id, header and commands length
			throw new Exception( "Not a replay file!" );
		
		input = new FileInputStream( replayFile );
		
		intBuffer = new byte[ INT_SIZE ];
		buffer    = new byte[ 2000 ];
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
	}
	
	/**
	 * Unpacks a seciton and returns a byte array of the unpacked data.
	 * @return a byte array of the unpacked data
	 * @throws Exception thrown if size is zero, if I/O error occurs or there's not enough data
	 */
	public synchronized byte[] unpackSection( final int size ) throws Exception {
		if ( size == 0 )
			throw new Exception();
		
		final int check = readIntFromStream();
		final int count = readIntFromStream();
		
		final ReplayEnc rep = new ReplayEnc();
		
		int length, n, len = 0, m1C, m20=0;
		final byte[] result = new byte[ size ];
		int resultOffset = 0;
		
		for ( n = 0, m1C=0; n < count; n++, m1C += buffer.length, m20 += len ) {
			length = readIntFromStream();
			if ( length > size - m20 )
				throw new Exception();
			if ( input.read( result, resultOffset, length ) != length )
				throw new Exception();
			
			if ( length == Math.min( size - m1C, buffer.length ) )
				continue;
			
			rep.src = result;
			rep.m04 = 0;
			rep.m08 = buffer;
			rep.m0C = 0;
			rep.m10 = length;
			rep.m14 = buffer.length;
			
			if ( unpackRepSection( rep ) == 0 && rep.m0C <= buffer.length )
				len = rep.m0C;
			else
				len = 0;
			
			if ( len == 0 || len > size )
				throw new Exception();
			
			System.arraycopy( buffer, 0, result, resultOffset, len );
		}
		
		return result;
	}
	
	private int unpackRepSection( final ReplayEnc rep ) {
		return 0;
	}
	
	/**
	 * Reads an int from the file.
	 * @return the int read from the file
	 * @throws Exception if I/O error occurs or there's not enough data
	 */
	private int readIntFromStream() throws Exception {
		if ( input.read( intBuffer ) != INT_SIZE )
			throw new Exception();
		return ByteBuffer.wrap( intBuffer ).getInt();
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
