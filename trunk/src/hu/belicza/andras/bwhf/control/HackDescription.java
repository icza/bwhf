package hu.belicza.andras.bwhf.control;

/**
 * Wrapper class for the result of a hack event.
 * 
 * @author Andras Belicza
 */
public class HackDescription {
	
	/** Name of the player who was hacking. */
	public final String playerName;
	/** Description of the hack.            */
	public final String description;
	
	/**
	 * Creates a new HackDescription.
	 * @param playerName  name of the player who was hacking
	 * @param description description of the hack
	 */
	public HackDescription( final String playerName, final String description ) {
		this.playerName  = playerName;
		this.description = description;
	}
	
}
