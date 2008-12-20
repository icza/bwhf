package hu.belicza.andras.bwhf.model;

/**
 * Class modelling the actions of a player.
 * 
 * @author Andras Belicza
 */
public class PlayerActions {
	
	public final String   name;
	public final Action[] actions;
	
	public PlayerActions( final String name, final Action[] actions ) {
		this.name    = name;
		this.actions = actions;
	}
	
}
