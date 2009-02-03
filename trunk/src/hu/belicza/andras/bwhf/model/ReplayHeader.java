package hu.belicza.andras.bwhf.model;

import java.util.Date;

/**
 * Class modeling the header of a replay.
 * 
 * @author Andras Belicza
 */
public class ReplayHeader {
	
	// Constants for the header field values
	
	public static final byte GAME_ENGINE_STARCRAFT = (byte) 0x00;
	public static final byte GAME_ENGINE_BROODWAR  = (byte) 0x01;
	
	public static final byte RACE_ZERG    = (byte) 0x00;
	public static final byte RACE_TERRAN  = (byte) 0x01;
	public static final byte RACE_PROTOSS = (byte) 0x02;
	
	// Header fields
	
	public byte     gameEngine;
	public int      gameFrames;
	public Date     saveTime;
	public String   gameName;
	public short    mapWidth;
	public short    mapHeight;
	public String   creatorName;
	public String   mapName;
	public byte[]   playerRecords = new byte[ 432 ]; // 12 player records, 12*36 bytes
	public int[]    playerColors  = new int[ 8 ]; // Player spot color index (ABGR?)
	public byte[]   playerSpotIndices = new byte[ 8 ]; 
	
	// Derived data from player records:
	public String[] playerNames = new String[ 12 ];
	public byte[]   playerRaces = new byte[ 12 ];
	public int[]    playerIds   = new int[ 12 ];
	
	/**
	 * Converts the specified amount of frames to seconds.
	 * @return the specified amount of frames in seconds
	 */
	public static int convertFramesToSeconds( final int frames ) {
		return frames * 42 / 1000; 
	}
	
	/**
	 * Returns the game duration in seconds.
	 * @return the game duration in seconds
	 */
	public int getDurationSeconds() {
		return convertFramesToSeconds( gameFrames );
	}
	
	/**
	 * Converts the specified amount of frames to a human friendly time format.
	 * @param frames amount of frames to be formatted
	 * @param formatBuilder builder to be used to append the output to
	 */
	public static void formatFrames( final int frames, final StringBuilder formatBuilder ) {
		int seconds = convertFramesToSeconds( frames );
		
		final int hours = seconds / 3600;
		if ( hours > 0 )
			formatBuilder.append( hours ).append( ':' );
		
		seconds %= 3600;
		final int minutes = seconds / 60;
		if ( hours > 0 && minutes < 10 )
			formatBuilder.append( 0 );
		formatBuilder.append( minutes ).append( ':' );
		
		seconds %= 60;
		if ( seconds < 10 )
			formatBuilder.append( 0 );
		formatBuilder.append( seconds );
	}
	
	/**
	 * Returns the duration as a human friendly string.
	 * @return the duration as a human friendly string
	 */
	public String getDurationString() {
		final StringBuilder formatBuilder = new StringBuilder();
		formatFrames( gameFrames, formatBuilder );
		return formatBuilder.toString();
	}
	
	/**
	 * Returns the game engine as a string.<br>
	 * This is either the string <code>"Starcraft"</code> or <code>"Broodwar"</code>.
	 * @return the game engine as a string
	 */
	public String getGameEngineString() {
		return gameEngine == GAME_ENGINE_BROODWAR ? "Broodwar" : "Starcraft";
	}
	
	/**
	 * Returns the map size as a string.
	 * @return the map size as a string
	 */
	public String getMapSize() {
		return mapWidth + "x" + mapHeight;
	}
	
	/**
	 * Returns the string listing the player names (comma separated).
	 * @return the string listing the player names (comma separated)
	 */
	public String getPlayerNamesString() {
		final StringBuilder playerNamesBuilder = new StringBuilder();
		
		for ( final String playerName : playerNames )
			if ( playerName != null ) {
				if ( playerNamesBuilder.length() > 0 )
					playerNamesBuilder.append( ", " );
				playerNamesBuilder.append( playerName );
			}
		
		return playerNamesBuilder.toString();
	}
	
}
