package hu.belicza.andras.bnetbot;

/**
 * The interface of a binary Battle.net bot.<br>
 * This interface provides methods to send binary packets and to receive binary responses.
 * 
 * @author Andras Belicza
 */
public interface BinaryBot {
	
	/**
	 * Tells if the bot is in connected status.
	 * @return true if the bot is connected
	 */
	boolean isConnected();
	
	/**
	 * Tells if the bot is in logged in status.
	 * @return true if the bot is logged in
	 */
	boolean isLoggedIn();
	
	/**
	 * Tries to connect to a battle.net server specified by the loginConfig object.
	 * @param loginConfig login config holding all the data required to login
	 * @return null if connecting succeeded; an error message otherwise
	 * @throws IllegalStateException if the bot is already connected
	 */
	String connect( final LoginConfig loginConfig ) throws IllegalStateException;
	
	/**
	 * Sends a binary packet to the battle.net server.
	 * @param packet packet to be sent
	 */
	void sendPacket( final BnetPacket packet );
	
	/**
	 * Disconnects the bot.
	 */
	void disconnect();
	
	/**
	 * Registers a status change listener.
	 * @param statusChangeListener status change listener to be registered
	 */
	void registerStatusChangeListener( final StatusChangeListener statusChangeListener );
	
	/**
	 * Removes a status change listener.
	 * @param statusChangeListener status change listener to be removed
	 */
	void removeStatusChangeListener( final StatusChangeListener statusChangeListener );
	
}
