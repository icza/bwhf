package hu.belicza.andras.bwhf.model;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	
	public static final String[] GAME_ENGINE_NAMES = {
		"Starcraft", "Broodwar"
	};
	
	public static final byte RACE_ZERG    = (byte) 0x00;
	public static final byte RACE_TERRAN  = (byte) 0x01;
	public static final byte RACE_PROTOSS = (byte) 0x02;
	
	public static final String[] RACE_NAMES = {
		"Zerg", "Terran", "Protoss"
	};
	
	public static final char[] RACE_CHARACTERS = { 'Z', 'T', 'P' };
	
	public static final String[] IN_GAME_COLOR_NAMES = {
		"red", "blue", "teal", "purple", "orange", "brown", "white", "yellow",
		"green", "pale yellow", "tan", "aqua", "pale green", "blueish gray", "pale yellow", "cyan"
	};
	
	public static final short GAME_TYPE_SINGLE_PLAYER = 0x02; // or MELEE?
	//public static final short GAME_TYPE_MELEE         = 0x02;
	public static final short GAME_TYPE_FFA           = 0x03; // Free for all
	public static final short GAME_TYPE_ONE_ON_ONE    = 0x04;
	public static final short GAME_TYPE_CTF           = 0x05; // Capture the flag
	public static final short GAME_TYPE_GREED         = 0x06;
	public static final short GAME_TYPE_SLAUGHTER     = 0x07;
	public static final short GAME_TYPE_SUDDEN_DEATH  = 0x08;
	public static final short GAME_TYPE_UMS           = 0x0a; // Use map settings
	public static final short GAME_TYPE_TEAM_MELEE    = 0x0b;
	public static final short GAME_TYPE_TEAM_FFA      = 0x0c;
	public static final short GAME_TYPE_TEAM_CTF      = 0x0d;
	public static final short GAME_TYPE_TVB           = 0x0f;
	
	// Header fields
	
	public byte     gameEngine;
	public int      gameFrames;
	public Date     saveTime;
	public String   gameName;
	public short    mapWidth;
	public short    mapHeight;
	public short    gameSpeed;
	public short    gameType;
	public String   creatorName;
	public String   mapName;
	public byte[]   playerRecords     = new byte[ 432 ]; // 12 player records, 12*36 bytes
	public int[]    playerColors      = new int[ 8 ]; // Player spot color index (ABGR?)
	public byte[]   playerSpotIndices = new byte[ 8 ]; 
	
	// Derived data from player records:
	public String[] playerNames       = new String[ 12 ];
	public byte[]   playerRaces       = new byte[ 12 ];
	public int[]    playerIds         = new int[ 12 ];
	
	// Calculated data when parsing the replay
	public int[]     playerIdActionsCounts = new int[ 12 ];
	
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
		output.println( "Version: " + guessVersionFromDate() );
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
	
	/** Names of the public Starcraft Broodwar versions. */
	public static final String[] VERSION_NAMES = {
		"1.0", "1.07", "1.08", "1.08b", "1.09", "1.09b", "1.10", "1.11", "1.11b",
		"1.12", "1.12b", "1.13", "1.13b", "1.13c", "1.13d", "1.13e", "1.13f", "1.14",
		"1.15", "1.15.1", "1.15.2", "1.15.3", "1.16", "1.16.1 or higher"
	};
	
	/** Starcraft release dates and version names. Source: ftp.blizzard.com/pub/broodwar/patches/PC */
	public static final long[] VERSION_RELEASE_DATES = new long[ VERSION_NAMES.length ];
	static {
		final String[] versionReleaseDateStrings = {
			"1998-01-01", "1999-11-02", "2001-05-18", "2001-05-20", "2002-02-06", "2002-02-25",
			"2003-10-14", "2004-04-29", "2004-06-01", "2005-02-17", "2005-02-24", "2005-06-30",
			"2005-08-12", "2005-08-22", "2005-09-06", "2005-09-12", "2006-04-21", "2006-08-01",
			"2007-05-15", "2007-08-20", "2008-01-16", "2008-09-11", "2008-11-25", "2009-01-21",
		};
		try {
			final DateFormat SDF = new SimpleDateFormat( "yyyy-MM-dd" );
			for ( int i = 0; i < versionReleaseDateStrings.length; i++ )
				VERSION_RELEASE_DATES[ i ] = SDF.parse( versionReleaseDateStrings[ i ] ).getTime();
		}
		catch ( final ParseException pe ) {
			pe.printStackTrace();
		}
	}
	
	/**
	 * Guesses the replay Starcraft version from the save date.
	 * @return the guessed version string
	 */
	public String guessVersionFromDate() {
		final long saveTime_ = saveTime.getTime();
		
		for ( int i = VERSION_RELEASE_DATES.length - 1; i >= 0; i-- )
			if ( saveTime_ > VERSION_RELEASE_DATES[ i ] )
				return VERSION_NAMES[ i ];
		
		return "<unknown>";
	}
	
}
