package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Replay parser to produce a {@link Replay} java object from a binary replay file.
 * 
 * @author Andras Belicza
 */
public class BinRepParser {
	
	/**
	 * For temporary testing purposes only. 
	 * @param arguments
	 */
	public static void main( final String[] arguments ) {
		parseReplay( new File( "w:/sample/nonhack rain/0288 CrT HoP coT StT sOP MP.rep" ) );
		System.out.println();
		parseReplay( new File( "w:/sample/Dakota hacks.rep" ) );
		System.out.println();
		parseReplay( new File( "w:/sample/00135_wrx-sti_mc - hack.rep" ) );
	}
	
	private static final byte[] REPLAY_ID = new byte[] { (byte) 0x72, (byte) 0x65, (byte) 0x52, (byte) 0x53 };
	
	/**
	 * Parses a binary replay file and returns a {@link Replay} object describing it.
	 * @param replayFile replay file to be parsed
	 * @return a {@link Replay} object describing the replay
	 */
	public static Replay parseReplay( final File replayFile ) {
		BinReplayUnpacker unpacker = null;
		try {
			unpacker = new BinReplayUnpacker( replayFile );
			
			final byte[] dWordBuffer = new byte[ 4 ];
			int offset;
			
			final byte[] replayId = unpacker.unpackSection( REPLAY_ID.length );
			for ( int i = REPLAY_ID.length - 1; i >= 0; i-- ) {
				if ( replayId[ i ] != REPLAY_ID[ i ] )
					return null;  // Not a replay file
			}
			
			final byte[] headerData = unpacker.unpackSection( ReplayHeader.HEADER_SIZE );
			offset = 0;
			final ReplayHeader replayHeader = new ReplayHeader();
			replayHeader.gameEngine = headerData[ offset++ ];
			
			System.arraycopy( headerData, offset, dWordBuffer, 0, dWordBuffer.length ); offset += dWordBuffer.length;
			
			System.out.println( dWordBuffer[ 0 ] + " " +dWordBuffer[ 1 ] + " " + dWordBuffer[ 2 ] + " " + dWordBuffer[ 3 ] + " "  );
			replayHeader.gameFrames = ByteBuffer.wrap( dWordBuffer ).getInt();
			System.out.println( replayHeader.gameFrames );
			
			offset += replayHeader.padding0.length;
			
			System.arraycopy( headerData, offset, dWordBuffer, 0, dWordBuffer.length ); offset += dWordBuffer.length;
			replayHeader.saveTime = ByteBuffer.wrap( dWordBuffer ).getInt();
			
			offset += replayHeader.padding1.length;
			
			System.arraycopy( headerData, offset, replayHeader.gameName, 0, replayId.length ); offset += replayHeader.gameName.length;
			System.out.println( new String( replayHeader.gameName ) );
			
			return new Replay( replayHeader, null );
		}
		catch ( final Exception e ) {
			e.printStackTrace();
			return null;
		}
		finally {
			if ( unpacker != null )
				unpacker.close();
		}
	}
	
}
