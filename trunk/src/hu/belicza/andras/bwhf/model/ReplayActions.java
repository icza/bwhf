package hu.belicza.andras.bwhf.model;

import java.util.List;
import java.util.Map;

/**
 * Class modelling the actions of a replay.
 * 
 * @author Andras Belicza
 */
public class ReplayActions {
	
	/** The players action map. The key is the player name, the value is the player's action list. */
	public final Map< String, List< Action > > playerNameActionListMap;
	
	/** Players of the replay. */
	public final PlayerActions[] players;
	
	/**
	 * Creates a new Replay.
	 * 
	 * @param playerNameActionListMap map containint the actions of the players of the replay
	 */
	public ReplayActions( final Map< String, List< Action > >playerNameActionListMap ) {
		this.playerNameActionListMap = playerNameActionListMap;
		
		players = new PlayerActions[ playerNameActionListMap.size() ];
		
		int i = 0;
		for ( final Map.Entry< String, List< Action > > playerNameActionListEntry : playerNameActionListMap.entrySet() ) {
			final String         playerName       = playerNameActionListEntry.getKey();
			final List< Action > playerActionList = playerNameActionListEntry.getValue();
			final Action[]       playerActions    = playerActionList.toArray( new Action[ playerActionList.size() ] );
			
			players[ i++ ] = new PlayerActions( playerName, playerActions );
		}
	}
	
}
