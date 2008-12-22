package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
			headerBuffer.order( ByteOrder.LITTLE_ENDIAN );
			
			final ReplayHeader replayHeader = new ReplayHeader();
			replayHeader.gameEngine  = headerData[ 0x00 ];
			
			replayHeader.gameFrames  = headerBuffer.getInt( 0x01 );
			replayHeader.saveTime    = new Date( headerBuffer.getInt( 0x08 ) * 1000l );
			
			replayHeader.gameName    = getZeroPaddedString( headerData, 0x18, 28 );
			
			replayHeader.mapWidth    = headerBuffer.getShort( 0x34 );
			replayHeader.mapHeight   = headerBuffer.getShort( 0x36 );
			
			replayHeader.creatorName = getZeroPaddedString( headerData, 0x48, 24 );
			
			replayHeader.mapName     = getZeroPaddedString( headerData, 0x61, 26 );
			
			for ( int i = 0; i < 12; i++ ) {
				final String playerName = getZeroPaddedString( headerData, 0xa1 + i * 36 + 11, 26 );
				if ( playerName.length() > 0 )
					replayHeader.playerNames[ i ] = playerName;
			}
			
			for ( int i = 0; i < replayHeader.playerColors.length; i++ )
				replayHeader.playerColors[ i ] = headerBuffer.getInt( 0x251 + i * 4 );
			replayHeader.playerSpotIndices = Arrays.copyOfRange( headerData, 0x271, 0x271 + 8 );
			
			// Player commands length section
			final int playerCommandsLength = Integer.reverseBytes( ByteBuffer.wrap( unpacker.unpackSection( 4 ) ).getInt() );
			System.out.println( playerCommandsLength );
			
			// Player commands section
			final ByteBuffer playerCommandsBuffer = ByteBuffer.wrap( unpacker.unpackSection( playerCommandsLength ) );
			playerCommandsBuffer.order( ByteOrder.LITTLE_ENDIAN );
			final List< String > pl = new ArrayList< String >();
			for ( final String n : replayHeader.playerNames )
				if ( n!= null )
					pl.add( n );
			do {
				//frame += playerCommandsBuffer.getInt(); // frames past
				final int frame = playerCommandsBuffer.getInt();
				final int commandBlocksLength = playerCommandsBuffer.get() & 0xff;
				final int playerId            = playerCommandsBuffer.get() & 0xff;
				final int blockId             = playerCommandsBuffer.get() & 0xff;
				
				//System.out.println( "Command blocks length: " + commandBlocksLength );
				System.out.println( frame + " Player ID: " + pl.get( playerId ) );
				//System.out.println( "Block ID: " + blockId );
				
				playerCommandsBuffer.position( playerCommandsBuffer.position() + commandBlocksLength - 2 );
			} while ( Math.random() < 2 );
			
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
