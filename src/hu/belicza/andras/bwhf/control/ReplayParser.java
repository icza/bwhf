package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.Replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Replay parser to produce a {@link Replay} java object from a string or a text file
 * from the actions of the replay.
 * 
 * @author Andras Belicza
 */
public class ReplayParser {
	
	/**
	 * Parses the BWChart-exported text actions of a replay and returns the parsed replay.
	 * 
	 * @param replayActions
	 * @return the parsed replay
	 * @throws ParseException thrown if parsing fails
	 */
	public static Replay parseBWChartExportString( final String replayActions ) throws ParseException {
		final Map< String, List< Action > > playerNameActionListMap = new HashMap< String, List< Action > >();
		
		int lineCounter =  0;
		int fromIndex   =  0;
		int toIndex     = -1;
		
		// Buffer of a new action
		int    iteration;
		String playerName;
		String name;
		String parameters;
		String unitIds;
		
		final int replayActionsLength = replayActions.length() - 2; // minus 2-char-long line end
		try {
			while ( toIndex < replayActionsLength ) {
				lineCounter++;
				fromIndex = toIndex + 1;  // new line character (would be "\r\n" on IE, but the 2nd char will be trimmed, on Firefox there is only '\n'!)
				iteration = Integer.parseInt( replayActions.substring( fromIndex, toIndex = replayActions.indexOf( '\t', fromIndex ) ) );
				
				fromIndex = toIndex + 1;
				playerName = replayActions.substring( fromIndex, toIndex = replayActions.indexOf( '\t', fromIndex ) );
				
				fromIndex = toIndex + 1;
				name = replayActions.substring( fromIndex, toIndex = replayActions.indexOf( '\t', fromIndex ) );
				
				fromIndex = toIndex + 1;
				parameters = replayActions.substring( fromIndex, toIndex = replayActions.indexOf( '\t', fromIndex ) ).trim();
				
				// Here comes just a separator space and another tab.
				fromIndex = toIndex + 1;
				toIndex = replayActions.indexOf( '\t', fromIndex );
				
				fromIndex = toIndex + 1;
				unitIds = replayActions.substring( fromIndex, toIndex = replayActions.indexOf( '\n', fromIndex ) );
				
				addActionToPlayerMap( playerName, new Action( iteration, name, parameters, unitIds ), playerNameActionListMap );
			}
		}
		catch ( final Exception e ) {
			throw new ParseException( lineCounter );
		}
		
		return new Replay( playerNameActionListMap );
	}
	
	/**
	 * Parses the BWChart-exported text actions of a replay and returns the parsed replay.
	 * 
	 * @param replayActions
	 * @return the parsed replay
	 * @throws ParseException thrown if parsing fails
	 */
	public static Replay parseBWChartExportFile( final File replayExportFile ) throws ParseException {
		final Map< String, List< Action > > playerNameActionListMap = new HashMap< String, List< Action > >();
		
		LineNumberReader reader = null;
		try {
			
			reader = new LineNumberReader( new FileReader( replayExportFile ) );
			
			String line;
			
			// Buffer of a new action
			int    iteration;
			String playerName;
			String name;
			String parameters;
			String unitIds;
			
			while ( ( line = reader.readLine() ) != null ) {
				final StringTokenizer lineTokenizer = new StringTokenizer( line, "\t" );
				
				iteration  = Integer.parseInt( lineTokenizer.nextToken() );
				playerName = lineTokenizer.nextToken();
				name       = lineTokenizer.nextToken();
				parameters = lineTokenizer.nextToken();
				unitIds    = lineTokenizer.nextToken();
				
				addActionToPlayerMap( playerName, new Action( iteration, name, parameters, unitIds ), playerNameActionListMap );
			}
			
		} catch ( final FileNotFoundException fnfe ) {
			throw new ParseException();
		} catch ( final IOException ie ) {
			throw new ParseException();
		}
		catch ( final Exception e ) {
			if ( reader != null )
				throw new ParseException( reader.getLineNumber() );
			else
				throw new ParseException();
		}
		finally {
			if ( reader != null )
				try { reader.close(); } catch ( final Exception e ) {}
		}
		
		return new Replay( playerNameActionListMap );
	}
	
	/**
	 * Adds an action of a player to the players map.
	 * 
	 * @param playerName              name of the player of the action
	 * @param action                  action to be added
	 * @param playerNameActionListMap map of players; mapping from the player's name to the player's action list
	 */
	private static void addActionToPlayerMap( final String playerName, final Action action, final Map< String, List< Action > > playerNameActionListMap ) {
		List< Action > playerActionList = playerNameActionListMap.get( playerName );
		if ( playerActionList == null ) {
			playerActionList = new ArrayList< Action >();
			playerNameActionListMap.put( playerName, playerActionList );
		}
		
		playerActionList.add( action );
	}
	
}
