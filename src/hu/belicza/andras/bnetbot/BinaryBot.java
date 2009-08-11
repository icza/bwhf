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
	 * Sends packets according to the Bnet protocol to login a user specified by the <code>loginConfig</code> object.
	 * @param loginConfig login config holding all the data required to login
	 * @return null if login was successfull; the error message otherwise
	 * @throws IllegalStateException if the bot is not in the connected state
	 */
	String login( final LoginConfig loginConfig ) throws IllegalStateException;
	
	/**
	 * Sends a binary packet to the battle.net server.
	 * @param packet packet to be sent
	 * @return true if packet sent successfully; false otherwise (disconnected)
	 * @throws IllegalStateException if the bot is not connected
	 */
	boolean sendPacket( final BnetPacket packet ) throws IllegalStateException;
	
	/**
	 * Reads a binary packet from the battle.net server.
	 * @return the read binary packet; or <code>null</code> if the server disconnected
	 * @throws IllegalStateException if the bot is not connected
	 */
	BnetPacket readPacket() throws IllegalStateException;
	
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
