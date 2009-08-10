package hu.belicza.andras.bnetbot;

/**
 * The interface of a text Battle.net bot.<br>
 * This interface provides methods to send text commands and to receive text responses.
 * 
 * @author Andras Belicza
 */
public interface TextBot {
	
	/**
	 * Tells if the bot is in logged in status.
	 * @return true if the bot is logged in
	 */
	boolean isLoggedIn();
	
	/**
	 * Tries to login to a battle.net server specified by the <code>loginConfig</code> object.
	 * @param loginConfig login config holding all the data required to login
	 * @return null if login was successfull; the error message otherwise
	 * @throws IllegalStateException if the bot is already logged in
	 */
	String login( final LoginConfig loginConfig ) throws IllegalStateException;
	
	/**
	 * Executes a command and returns its result.
	 * @param command command to be executed
	 * @return the result of the command
	 * @throws IllegalStateException if the bot is not connected
	 */
	String executeCommand( final String command ) throws IllegalStateException;
	
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
