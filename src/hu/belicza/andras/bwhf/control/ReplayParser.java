package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.Replay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Andras Belicza
 */
public class ReplayParser {
	
	/**
	 * Parses the BWChart-exported text actions of a replay and returns the parsed replay.
	 * 
	 * @param replayActions
	 * @return the parsed replay
	 * @throws Exception thrown if parsing fails
	 */
	public static Replay parseBWChartReplayActions( final String replayActions ) throws Exception {
		final Map< String, List< Action > > playerNameActionListMap = new HashMap< String, List< Action > >();
		List< Action > playerActionList;
		
		int lineCounter =  0;
		int fromIndex   =  0;
		int toIndex     = -1;
		
		// Buffer of a new action
		String playerName;
		int    iteration;
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
				
				playerActionList = playerNameActionListMap.get( playerName );
				if ( playerActionList == null ) {
					playerActionList = new ArrayList< Action >();
					playerNameActionListMap.put( playerName, playerActionList );
				}
				
				playerActionList.add( new Action( iteration, name, parameters, unitIds ) );
			}
		}
		catch ( final Exception e ) {
			throw new Exception( "Parse error in line " + lineCounter );
		}
		
		return new Replay( playerNameActionListMap );
	}
	
}
