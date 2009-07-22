package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayActions;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.File;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replay parser to produce a {@link Replay} java object from a binary replay file.
 * 
 * @author Andras Belicza
 */
public class BinRepParser {
	
	/**
	 * For testing purposes only.
	 * @param arguments
	 */
	public static void main( final String[] arguments ) {
		final String[] replayNames = new String[] { "w:/rep/4 - hack.rep" };
		
		for ( final String replayName : replayNames ) {
			final Replay replay = parseReplay( new File( replayName ), true, false );
			if ( replay != null )
				replay.replayHeader.printHeaderInformation( new PrintWriter( System.out ) );
			else
				System.out.println( "Could not parse " + replayName + "!" );
			System.out.println();
		}
	}
	
	/** Size of the header section */
	public static final int HEADER_SIZE = 0x279;
	
	/**
	 * Wrapper class to build the game chat.
	 * @author Andras Belicza
	 */
	private static class GameChatWrapper {
		
		/** <code>StringBuilder</code> for the game chat.  */
		public final StringBuilder          gameChatBuilder;
		/** Map from the player IDs to their name.         */
		public final Map< Integer, String > playerIndexNameMap;
		/** Message buffer to be used to extract messages. */
		public final byte[]                 messageBuffer = new byte[ 80 ];
		
		/**
		 * Creates a new GameChatWrapper.
		 */
		public GameChatWrapper( final String[] playerNames, final int[] playerIds ) {
			gameChatBuilder = new StringBuilder();
			
			playerIndexNameMap = new HashMap< Integer, String >();
			for ( int i = 0; i < playerNames.length; i++ )
				if ( playerNames[ i ] != null && playerIds[ i ] != 0xff ) // Computers are listed with playerId values of 0xff.
					playerIndexNameMap.put( i, playerNames[ i ] );
		}
	}
	
	/**
	 * Parses a binary replay file.
	 * 
	 * @param replayFile           replay file to be parsed
	 * @param parseCommandsSection tells if player actions have to be parsed from the commands section 
	 * @param gameChatOnly         tells if only game chat is desired but not other actions
	 * @return a {@link Replay} object describing the replay; or <code>null</code> if replay cannot be parsed 
	 */
	@SuppressWarnings("unchecked")
	public static Replay parseReplay( final File replayFile, final boolean parseCommandsSection, final boolean gameChatOnly ) {
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
			
			replayHeader.gameSpeed   = headerBuffer.getShort( 0x3a );
			replayHeader.gameType    = headerBuffer.getShort( 0x3c );
			replayHeader.gameSubType = headerBuffer.getShort( 0x3e );
			
			replayHeader.creatorName = getZeroPaddedString( headerData, 0x48, 24 );
			
			replayHeader.mapName     = getZeroPaddedString( headerData, 0x61, 26 );
			
			replayHeader.playerRecords = Arrays.copyOfRange( headerData, 0xa1, 0xa1 + 432 );
			for ( int i = 0; i < replayHeader.playerColors.length; i++ )
				replayHeader.playerColors[ i ] = headerBuffer.getInt( 0x251 + i * 4 );
			replayHeader.playerSpotIndices = Arrays.copyOfRange( headerData, 0x271, 0x271 + 8 );
			
			// Derived data from player records:
			for ( int i = 0; i < 12; i++ ) {
				final String playerName = getZeroPaddedString( replayHeader.playerRecords, i * 36 + 11, 25 );
				if ( playerName.length() > 0 )
					replayHeader.playerNames[ i ] = playerName;
				replayHeader.playerRaces[ i ] = replayHeader.playerRecords[ i * 36 + 9 ];
				replayHeader.playerIds  [ i ] = replayHeader.playerRecords[ i * 36 + 4 ] & 0xff;
			}
			
			if ( !parseCommandsSection )
				return new Replay( replayHeader, null, null );
			
			// Player commands length section
			final int playerCommandsLength = Integer.reverseBytes( ByteBuffer.wrap( unpacker.unpackSection( 4 ) ).getInt() );
			
			// Player commands section
			final ByteBuffer commandsBuffer = ByteBuffer.wrap( unpacker.unpackSection( playerCommandsLength ) );
			commandsBuffer.order( ByteOrder.LITTLE_ENDIAN );
			
			final List< Action >[] playerActionLists;
			final GameChatWrapper  gameChatWrapper;
			if ( gameChatOnly ) {
				gameChatWrapper   = new GameChatWrapper( replayHeader.playerNames, replayHeader.playerIds );
				playerActionLists = null;
			}
			else {
				gameChatWrapper   = null;
				playerActionLists = new ArrayList[ replayHeader.playerNames.length ]; // This will be indexed by playerId!
				for ( int i = 0; i < playerActionLists.length; i++ )
					playerActionLists[ i ] = new ArrayList< Action >();
			}
			
			while ( commandsBuffer.position() < playerCommandsLength ) {
				final int frame               = commandsBuffer.getInt();
				int       commandBlocksLength = commandsBuffer.get() & 0xff;
				final int commandBlocksEndPos = commandsBuffer.position() + commandBlocksLength;
				
				while ( commandsBuffer.position() < commandBlocksEndPos ) {
					final int playerId = commandsBuffer.get() & 0xff;
					final Action action = readNextAction( frame, commandsBuffer, commandBlocksEndPos, gameChatWrapper );
					if ( action != null ) {
						replayHeader.playerIdActionsCounts[ playerId ]++; // If playerId is outside the index range, throw the implicit exception and fail to parse replay, else it may contain incorrect actions which may lead to false hack reports!
						if ( playerActionLists != null )
							playerActionLists[ playerId ].add( action );
					}
				}
			}
			
			if ( gameChatWrapper != null )
				return new Replay( replayHeader, null, gameChatWrapper.gameChatBuilder.toString() );
			
			// Now create the ReplayActions object
			final Map< String, List< Action > > playerNameActionListMap = new HashMap< String, List< Action > >();
			for ( int i = 0; i < replayHeader.playerNames.length; i++ )
				if ( replayHeader.playerNames[ i ] != null )
					if ( replayHeader.playerIds[ i ] != 0xff )  // Computers are listed with playerId values of 0xff, but no actions are recorded from them.
						playerNameActionListMap.put( replayHeader.playerNames[ i ], playerActionLists[ replayHeader.playerIds[ i ] ] );
			final ReplayActions replayActions = new ReplayActions( playerNameActionListMap );
			
			/*// Map data length section
			final int mapDataLength = Integer.reverseBytes( ByteBuffer.wrap( unpacker.unpackSection( 4 ) ).getInt() );
			
			// Map data section
			final ByteBuffer mapDataBuffer = ByteBuffer.wrap( unpacker.unpackSection( mapDataLength ) );
			mapDataBuffer.order( ByteOrder.LITTLE_ENDIAN );
			
			final byte[] sectionNameBuffer = new byte[ 4 ];
			// Name of the unit section in the map data replay section.
			final String UNIT_SECTION_NAME = "UNIT";
			while ( mapDataBuffer.position() < mapDataLength ) {
				mapDataBuffer.get( sectionNameBuffer );
				final String sectionName   = new String( sectionNameBuffer, "US-ASCII" );
				final int    sectionLength = mapDataBuffer.getInt();
				final int    sectionEndPos = mapDataBuffer.position() + sectionLength;
				
				if ( sectionName.equals( UNIT_SECTION_NAME ) ) {
					//System.out.println( sectionName + ", length=" + sectionLength );
					while ( mapDataBuffer.position() < sectionEndPos ) {
						final int unitRecordEndPos = mapDataBuffer.position() + 36; // unit record length is 36 bytes
						final int unitId = mapDataBuffer.getInt();
						mapDataBuffer.position( mapDataBuffer.position() + 4 ); // We skip the x and y coordinates
						final int unitType = mapDataBuffer.getInt();
						//System.out.println( unitId + " - " + unitType );
						mapDataBuffer.position( unitRecordEndPos );
					}
					break; // We only needed the unit section
				}
				
				if ( mapDataBuffer.position() < sectionEndPos )
					mapDataBuffer.position( sectionEndPos );
			}
			if ( mapDataBuffer.position() < mapDataLength ) // We might have skipped some parts of map data, so we position to the end
				mapDataBuffer.position( mapDataLength );*/
			
			return new Replay( replayHeader, replayActions, null );
		}
		catch ( final Exception e ) {
			return null;
		}
		finally {
			if ( unpacker != null )
				unpacker.close();
		}
	}
	
	/**
	 * Returns a string from a "C" style buffer array.<br>
	 * That means we take the bytes of a string form a buffer until we find a 0x00 terminating character.
	 * @param data   data to read from
	 * @param offset offset to read from
	 * @param length max length of the string
	 * @return the zero padded string read from the buffer
	 */
	private static String getZeroPaddedString( final byte[] data, final int offset, final int length ) {
		String string = new String( data, offset, length );
		
		int firstNullCharPos = string.indexOf( 0x00 );
		if ( firstNullCharPos >= 0 )
			string = string.substring( 0, firstNullCharPos );
		
		return string;
	}
	
	/**
	 * Reads the next action in the commands buffer.<br>
	 * Only parses actions which are important in hack detection.
	 * @param frame               frame of the action
	 * @param commandsBuffer      commands buffer to be read from
	 * @param commandBlocksEndPos end position of the current command blocks
	 * @param gameChatWrapper     game chat wrapper to be used if game chat is desired
	 * @return the next action object
	 */
	private static Action readNextAction( final int frame, final ByteBuffer commandsBuffer, final int commandBlocksEndPos, final GameChatWrapper gameChatWrapper ) {
		final byte blockId  = commandsBuffer.get();
		
		Action action    = null;
		int    skipBytes = 0;
		
		switch ( blockId ) {
			case (byte) 0x09 :   // Select units
			case (byte) 0x0a :   // Shift select units
			case (byte) 0x0b : { // Shift deselect units
				int unitsCount = commandsBuffer.get() & 0xff;
				final StringBuilder parametersBuilder = new StringBuilder();
				for ( ; unitsCount > 0; unitsCount-- ) {
					parametersBuilder.append( commandsBuffer.getShort() );
					if ( unitsCount > 1 )
						parametersBuilder.append( ',' );
				}
				// TODO: determine unit name indices
				action = new Action( frame, parametersBuilder.toString(), blockId );
				break;
			}
			case (byte) 0x0c : { // Build
				/*final byte  type   = */commandsBuffer.get();
				final short posX   = commandsBuffer.getShort();
				final short posY   = commandsBuffer.getShort();
				final short unitId = commandsBuffer.getShort();
				
				action = new Action( frame, "(" + posX + "," + posY + ")," + Action.UNIT_ID_NAME_MAP.get( unitId ), blockId, Action.UNIT_NAME_INDEX_UNKNOWN, unitId );
				break;
			}
			case (byte) 0x0d : { // Vision
				final byte data1 = commandsBuffer.get();
				final byte data2 = commandsBuffer.get();
				action = new Action( frame, convertToHexString( data1, data2 ), blockId );
				break;
			}
			case (byte) 0x0e : { // Ally
				final byte data1 = commandsBuffer.get();
				final byte data2 = commandsBuffer.get();
				final byte data3 = commandsBuffer.get();
				final byte data4 = commandsBuffer.get();
				action = new Action( frame, convertToHexString( data1, data2, data3, data4 ), blockId );
				break;
			}
			case (byte) 0x0f : { // Change game speed
				final byte speed = commandsBuffer.get();
				action = new Action( frame, Action.GAME_SPEED_MAP.get( speed ), blockId );
				break;
			}
			case (byte) 0x13 : { // Hotkey
				final byte type = commandsBuffer.get();
				action = new Action( frame, ( type == (byte) 0x00 ? Action.HOTKEY_ACTION_PARAM_NAME_ASSIGN : Action.HOTKEY_ACTION_PARAM_NAME_SELECT ) + "," + commandsBuffer.get(), blockId );
				break;
			}
			case (byte) 0x14 : { // Move
				final short posX   = commandsBuffer.getShort();
				final short posY   = commandsBuffer.getShort();
				/*final short unitId = */commandsBuffer.getShort(); // Move to (posX;posY) if this is 0xffff, or move to this unit if it's a valid unit id (if it's not 0xffff)
				skipBytes = 3;
				action = new Action( frame, posX + "," + posY, Action.ACTION_NAME_INDEX_MOVE );
				break;
			}
			case (byte) 0x15 : { // Attack/Right Click/Cast Magic/Use ability
				final short posX   = commandsBuffer.getShort();
				final short posY   = commandsBuffer.getShort();
				/*final short unitId = */commandsBuffer.getShort(); // (posX;posY) if this is 0xffff, or target this unit if it's a valid unit id (if it's not 0xffff)
				commandsBuffer.getShort(); // Unknown
				final byte type    = commandsBuffer.get();
				
				byte actionNameIndex = Action.ACTION_NAME_INDEX_UNKNOWN;
				if ( type == (byte) 0x00 || type == (byte) 0x06 ) // Move with right click or Move by click move icon
					actionNameIndex = Action.ACTION_NAME_INDEX_MOVE;
				else if ( type == (byte) 0x09 || type == (byte) 0x4f || type == (byte) 0x50 ) // Gather
					actionNameIndex = Action.ACTION_NAME_INDEX_GATHER;
				else if ( type == (byte) 0x0e ) // Attack move
					actionNameIndex = Action.ACTION_NAME_INDEX_ATTACK_MOVE;
				
				/*final byte type2 = */commandsBuffer.get(); // Type2: 0x00 for normal attack, 0x01 for shift attack
				action = new Action( frame, posX + "," + posY, actionNameIndex, type, Action.UNIT_NAME_INDEX_UNKNOWN, Action.BUILDING_NAME_INDEX_NON_BUILDING );
				break;
			}
			case (byte) 0x1f : { // Train
				final short unitId = commandsBuffer.getShort();
				action = new Action( frame, Action.UNIT_ID_NAME_MAP.get( unitId ), blockId, unitId, Action.BUILDING_NAME_INDEX_NON_BUILDING );
				break;
			}
			case (byte) 0x20 : { // Cancel train
				skipBytes = 2;
				action = new Action( frame, "", blockId );
				break;
			}
			case (byte) 0x23 : { // Hatch
				final short unitId = commandsBuffer.getShort();
				action = new Action( frame, Action.UNIT_ID_NAME_MAP.get( unitId ), blockId, unitId, Action.BUILDING_NAME_INDEX_NON_BUILDING );
				break;
			}
			case (byte) 0x30 : { // Research
				final byte researchId = commandsBuffer.get();
				action = new Action( frame, Action.RESEARCH_ID_NAME_MAP.get( researchId ), blockId );
				break;
			}
			case (byte) 0x32 : { // Upgrade
				final byte upgradeId = commandsBuffer.get();
				action = new Action( frame, Action.UPGRADE_ID_NAME_MAP.get( upgradeId ), blockId );
				break;
			}
			case (byte) 0x1e :   // Return chargo
			case (byte) 0x21 :   // Cloack
			case (byte) 0x22 :   // Decloack
			case (byte) 0x25 :   // Unsiege
			case (byte) 0x26 :   // Siege
			case (byte) 0x28 :   // Unload all
			case (byte) 0x2b :   // Hold position
			case (byte) 0x2c :   // Burrow
			case (byte) 0x2d :   // Unburrow
			case (byte) 0x1a : { // Stop
				final byte data = commandsBuffer.get();
				final String params = blockId == 0x1a || blockId == (byte) 0x1e || blockId == (byte) 0x28  || blockId == (byte) 0x2b ? ( data == 0x00 ? "Instant" : "Queued" ) : "";
				action = new Action( frame, params, blockId );
				break;
			}
			case (byte) 0x35 : {  // Morph
				final short unitId = commandsBuffer.getShort();
				action = new Action( frame, Action.UNIT_ID_NAME_MAP.get( unitId ), blockId, unitId, unitId );
				break;
			}
			case (byte) 0x57 : {  // Leave game
				final byte reason = commandsBuffer.get();
				action = new Action( frame, reason == (byte) 0x01 ? "Quit" : ( reason == (byte) 0x06 ? "Dropped" : "" ), blockId );
				break;
			}
			case (byte) 0x29 : { // Unload
				skipBytes = 2;
				action = new Action( frame, "", blockId );
				break;
			}
			case (byte) 0x58 : { // Minimap ping
				final short posX = commandsBuffer.getShort();
				final short posY = commandsBuffer.getShort();
				action = new Action( frame, "(" + posX + "," + posY + ")", blockId );
				break;
			}
			case (byte) 0x12 :   // Use Cheat
			case (byte) 0x2f : { // Lift
				final byte data1 = commandsBuffer.get();
				final byte data2 = commandsBuffer.get();
				final byte data3 = commandsBuffer.get();
				final byte data4 = commandsBuffer.get();
				action = new Action( frame, convertToHexString( data1, data2, data3, data4 ), blockId );
				break;
			}
			case (byte) 0x18 :   // Cancel
			case (byte) 0x19 :   // Cancel hatch
			case (byte) 0x27 :   // Build interceptor/scarab
			case (byte) 0x2a :   // Merge archon
			case (byte) 0x2e :   // Cancel nuke
			case (byte) 0x31 :   // Cancel research
			case (byte) 0x36 :   // Stim
			case (byte) 0x5a : { // Merge dark archon
				// No additional data
				action = new Action( frame, "", blockId );
				break;
			}
			case (byte) 0x5c : { // Game Chat (as of 1.16)
				if ( gameChatWrapper == null )
					skipBytes = 81;  // 1 byte for player index, and 80 bytes of message characters
				else {
					if ( gameChatWrapper.gameChatBuilder.length() > 0 )
						gameChatWrapper.gameChatBuilder.append( "\r\n" );
					ReplayHeader.formatFrames( frame, gameChatWrapper.gameChatBuilder, false );
					gameChatWrapper.gameChatBuilder.append( " - " ).append( gameChatWrapper.playerIndexNameMap.get( commandsBuffer.get() & 0xff ) );
					commandsBuffer.get( gameChatWrapper.messageBuffer );
					gameChatWrapper.gameChatBuilder.append( ": " ).append( getZeroPaddedString( gameChatWrapper.messageBuffer, 0, gameChatWrapper.messageBuffer.length ) );
				}
				break;
			}
			default: { // We don't know how to handle actions, we have to skip the whole time frame which means we might lose some actions!
				skipBytes = commandBlocksEndPos - commandsBuffer.position();
				break;
			}
		}
		
		if ( skipBytes > 0 )
			commandsBuffer.position( commandsBuffer.position() + skipBytes );
		
		if ( blockId == (byte) 0x5c ) // Game chat is not a "real" action
			return null;
		
		if ( action == null )
			action = new Action( frame, "", Action.ACTION_NAME_INDEX_UNKNOWN );
		
		return action;
	}
	
	/**
	 * Converts bytes to hex string separating bytes with spaces. 
	 * @return the bytes converted to string separated with spaces
	 */
	private static String convertToHexString( final byte... data ) {
    	final StringBuilder sb = new StringBuilder( data.length * 2 );
    	
    	for ( int i = 0; i < data.length; ) {
    	    sb.append( Integer.toHexString( ( data[ i ] >> 4 ) & 0x0f ).toUpperCase() );
    	    sb.append( Integer.toHexString( data[ i ] & 0x0f ).toUpperCase() );
    	    
    	    if ( ++i < data.length )
    	    	sb.append( ' ' );
    	}
    	
    	return sb.toString();
	}
	
}
