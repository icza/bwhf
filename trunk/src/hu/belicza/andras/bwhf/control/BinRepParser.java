package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;

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
		//final String[] replays = new String[] { "w:/sample/nonhack rain/0288 CrT HoP coT StT sOP MP.rep", "w:/sample/Dakota hacks.rep", "w:/sample/00135_wrx-sti_mc - hack.rep" };
		final String[] replayNames = new String[] { "w:/Dakota hacks.rep", "w:/9369 NiT thT LT DP gP DT sZ.rep", "w:/9745 SuT TCT wNT NeP DoZ DZ.rep" };
		
		for ( final String replayName : replayNames ) {
			final Replay replay = parseReplay( new File( replayName ) );
			if ( replay != null )
				displayReplayInformation( replay );
			else
				System.out.println( "Could not parse " + replayName + "!" );
			System.out.println();
		}
	}
	
	/** Size of the header section */
	public static final int HEADER_SIZE = 0x279;
	
	/**
	 * Parses a binary replay file and returns a {@link Replay} object describing it.
	 * @param replayFile replay file to be parsed
	 * @return a {@link Replay} object describing the replay
	 */
	public static Replay parseReplay( final File replayFile ) {
		BinReplayUnpacker unpacker = null;
		try {
			unpacker = new BinReplayUnpacker( replayFile );
			
			// Replay ID section
			if ( Integer.reverseBytes( ByteBuffer.wrap( unpacker.unpackSection( 4 ) ).getInt() ) != 0x53526572 )
				return null;  // Not a replay file
			
			// Replay header section
			final byte[] headerData = unpacker.unpackSection( HEADER_SIZE );
			final ByteBuffer headerBuffer = ByteBuffer.wrap( headerData );
			
			final ReplayHeader replayHeader = new ReplayHeader();
			replayHeader.gameEngine  = headerData[ 0x00 ];
			
			replayHeader.gameFrames  = Integer.reverseBytes( headerBuffer.getInt( 0x01 ) );
			replayHeader.saveTime    = new Date( Integer.reverseBytes( headerBuffer.getInt( 0x08 ) ) * 1000l );
			
			replayHeader.gameName    = getZeroPaddedString( headerData, 0x18, 28 );
			
			replayHeader.mapWidth    = Short.reverseBytes( headerBuffer.getShort( 0x34 ) );
			replayHeader.mapHeight   = Short.reverseBytes( headerBuffer.getShort( 0x36 ) );
			
			replayHeader.creatorName = getZeroPaddedString( headerData, 0x48, 24 );
			
			replayHeader.mapName     = getZeroPaddedString( headerData, 0x61, 26 );
			
			for ( int i = 0; i < 12; i++ ) {
				final String playerName = getZeroPaddedString( headerData, 0xa1 + i * 36 + 11, 26 );
				if ( playerName.length() > 0 )
					replayHeader.playerNames[ i ] = playerName;
			}
			
			// Player commands length section
			final int playerCommandsLength = Integer.reverseBytes( ByteBuffer.wrap( unpacker.unpackSection( 4 ) ).getInt() );
			System.out.println( playerCommandsLength );
			
			// Player commands section
			final byte[] playerCommandsData = unpacker.unpackSection( playerCommandsLength );
			
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
	
	private static String getZeroPaddedString( final byte[] data, final int offset, final int length ) {
		String string = new String( data, offset, length );
		
		int firstNullCharPos = string.indexOf( 0x00 );
		if ( firstNullCharPos >= 0 )
			string = string.substring( 0, firstNullCharPos );
		
		return string;
	}
	
	/**
	 * Displays the replay header informations.
	 * @param replay replay to be displayed informations about
	 */
	public static void displayReplayInformation( final Replay replay ) {
		final ReplayHeader replayHeader = replay.replayHeader;
		
		System.out.println( "Game engine:  " + replayHeader.getGameEngineString () );
		System.out.println( "Duration:     " + replayHeader.getDurationString   () );
		System.out.println( "Save time:    " + replayHeader.saveTime               );
		System.out.println( "Game name:    " + replayHeader.gameName               );
		System.out.println( "Map size:     " + replayHeader.getMapSize          () );
		System.out.println( "Creator name: " + replayHeader.creatorName            );
		System.out.println( "Map name:     " + replayHeader.mapName                );
		System.out.println( "Players:      " + replayHeader.getPlayerNamesString() );
	}
	
}
