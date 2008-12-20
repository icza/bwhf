package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.ReplayActions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replay actions parser to produce a {@link ReplayActions} java object from a string or a text file
 * containing the actions of the replay.
 * 
 * @author Andras Belicza
 */
public class ReplayActionsParser {
	
	/**
	 * Parses the BWChart-exported text actions of a replay and returns the parsed replay actions object.
	 * 
	 * @param replayActions string containing the actions of a replay in the BWChart export format
	 * @return the parsed replay actions
	 * @throws ParseException thrown if parsing fails
	 */
	public static ReplayActions parseBWChartExportString( final String replayActions ) throws ParseException {
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
				
				// Here comes just a separator character (space or a tab) and another tab.
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
		
		return new ReplayActions( playerNameActionListMap );
	}
	
	/**
	 * Parses the BWChart-exported text actions of a replay and returns the parsed replay.
	 * 
	 * @param replayExportFile a text file containing the actions of a replay in the BWChart export format
	 * @return the parsed replay actions
	 * @throws ParseException thrown if parsing fails
	 */
	public static ReplayActions parseBWChartExportFile( final File replayExportFile ) throws ParseException {
		final Map< String, List< Action > > playerNameActionListMap = new HashMap< String, List< Action > >();
		
		LineNumberReader reader = null;
		try {
			
			reader = new LineNumberReader( new FileReader( replayExportFile ) );
			
			// Buffer of a new action
			int    iteration;
			String playerName;
			String name;
			String parameters;
			String unitIds;
			
			String line;
			int    toIndex;
			while ( ( line = reader.readLine() ) != null ) {
				int fromIndex = 0;
				iteration = Integer.parseInt( line.substring( fromIndex, toIndex = line.indexOf( '\t', fromIndex ) ) );
				
				fromIndex = toIndex + 1;
				playerName = line.substring( fromIndex, toIndex = line.indexOf( '\t', fromIndex ) );
				
				fromIndex = toIndex + 1;
				name = line.substring( fromIndex, toIndex = line.indexOf( '\t', fromIndex ) );
				
				fromIndex = toIndex + 1;
				parameters = line.substring( fromIndex, toIndex = line.indexOf( '\t', fromIndex ) ).trim();
				
				// Here comes just a separator character (space or a tab) and another tab.
				fromIndex = toIndex + 1;
				toIndex = line.indexOf( '\t', fromIndex );
				
				fromIndex = toIndex + 1;
				unitIds = line.substring( fromIndex );
				
				addActionToPlayerMap( playerName, new Action( iteration, name, parameters, unitIds ), playerNameActionListMap );
			}
			
		}
		catch ( final FileNotFoundException fnfe ) {
			fnfe.printStackTrace();
			throw new ParseException();
		}
		catch ( final IOException ie ) {
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
		
		return new ReplayActions( playerNameActionListMap );
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
