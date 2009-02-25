package hu.belicza.andras.bwhf.model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
	
	public static final char[] RACE_CHARACTERS = { 'Z', 'T', 'P' };
	
	public static final String[] IN_GAME_COLOR_NAMES = {
		"red", "blue", "teal", "purple", "orange", "brown", "white", "yellow",
		"green", "pale yellow", "tan", "aqua", "pale green", "blueish gray", "pale yellow", "cyan"
	};
	
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
	public String[] playerNames        = new String[ 12 ];
	public byte[]   playerRaces        = new byte[ 12 ];
	public int[]    playerIds          = new int[ 12 ];
	
	// Calculated data when parsing the replay
	public int[]    playerIdActionsCounts = new int[ 12 ];
	
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
	
	/**
	 * Returns the description of a player specified by his/her name.<br>
	 * The description contains the name, race and APM of the player in the following format:<br>
	 * <code>player_name (R), actions: xxx, APM: yyy</code>
	 * @param playerName name of the player being queried
	 * @return the description of the player
	 */
	public String getPlayerDescription( final String playerName ) {
		final int playerIndex = getPlayerIndexByName( playerName );
		if ( playerIndex < 0 )
			return null;
		
		final Integer apm = playerIdActionsCounts[ playerIds[ playerIndex ] ] * 60 / getDurationSeconds();
		return playerNames[ playerIndex ] + " (" + RACE_CHARACTERS[ playerRaces[ playerIndex ] ] + "), actions: " + playerIdActionsCounts[ playerIds[ playerIndex ] ] + ", APM: " + apm;
	}
	
	/**
	 * Returns the index of a player specified by his/her name.
	 * @param playerName name of player to be searched
	 * @return the index of a player specified by his/her name; or -1 if player name not found
	 */
	public int getPlayerIndexByName( final String playerName ) {
		for ( int i = 0; i < playerNames.length; i++ )
			if ( playerNames[ i ] != null && playerNames[ i ].equals( playerName ) )
				return i;
		return -1;
	}
	
	/**
	 * Prints the replay header information into the specified output writer.
	 * @param output output print writer to be used
	 */
	public void printHeaderInformation( final PrintWriter output ) {
		output.println( "Game engine: " + getGameEngineString() );
		output.println( "Duration: " + getDurationString() );
		output.println( "Saved on: " + saveTime );
		output.println( "Game name: " + gameName );
		output.println( "Map size: " + getMapSize() );
		output.println( "Creator name: " + creatorName );
		output.println( "Map name: " + mapName );
		output.println( "Players: " );
		
		final int seconds = getDurationSeconds();
		// We want to sort players by the number of their actions (which is basically sorting by APM)
		final List< Object[] > playerDescriptionList = new ArrayList< Object[] >( 12 );
		for ( int i = 0; i < playerNames.length; i++ )
			if ( playerNames[ i ] != null ) {
				String colorName;
				try {
					colorName = IN_GAME_COLOR_NAMES[ playerColors[ i ] ];
				}
				catch ( final Exception e ) {
					colorName = "<unknown>";
				}
				final String playerDescription = "    " + playerNames[ i ] + " (" + RACE_CHARACTERS[ playerRaces[ i ] ] 
				                + "), color: " + colorName + ", actions: " + playerIdActionsCounts[ playerIds[ i ] ] 
				                + ", APM: " + ( playerIdActionsCounts[ playerIds[ i ] ] * 60 / seconds );
				playerDescriptionList.add( new Object[] { playerIdActionsCounts[ playerIds[ i ] ], playerDescription } );
			}
		
		Collections.sort( playerDescriptionList, new Comparator< Object[] >() {
			public int compare( final Object[] object1, final Object[] object2 ) {
				return -( (Integer) object1[ 0 ] ).compareTo( (Integer) object2[ 0 ] );
			}
		} );
		
		for ( final Object[] playerDescription : playerDescriptionList )
			output.println( (String) playerDescription[ 1 ] );
		
		output.flush();
	}
	
}
