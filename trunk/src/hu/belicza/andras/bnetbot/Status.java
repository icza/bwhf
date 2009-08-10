package hu.belicza.andras.bnetbot;

/**
 * Enumeration type representing the status of the bot.
 * 
 * @author Andras Belicza
 */
public enum Status {
	
	/** The bot is not connected to any battle.net server. */
	DISCONNECTED,
	/** The bot is connected, but not logged in.           */
	CONNECTED,
	/** The bot is logged in.                              */
	LOGGED_IN
	
}
