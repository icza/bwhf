package hu.belicza.andras.bwhf.model;

/**
 * Class modelling the actions of a player.
 * 
 * @author Andras Belicza
 */
public class PlayerActions {
	
	/** Name of the player.    */
	public final String   playerName;
	/** Actions of the player. */
	public final Action[] actions;
	
	/**
	 * Creates a new PlayerActions.
	 * @param playerName name of the player
	 * @param actions    actions of the player
	 */
	public PlayerActions( final String playerName, final Action[] actions ) {
		this.playerName    = playerName;
		this.actions = actions;
	}
	
}
