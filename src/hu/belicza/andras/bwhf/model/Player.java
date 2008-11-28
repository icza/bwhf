package hu.belicza.andras.bwhf.model;

/**
 * Class modelling a player.
 * 
 * @author Andras Belicza
 */
public class Player {
	
	public final String   name;
	public final Action[] actions;
	
	public Player( final String name, final Action[] actions ) {
		this.name    = name;
		this.actions = actions;
	}
	
}
