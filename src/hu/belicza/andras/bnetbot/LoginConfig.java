package hu.belicza.andras.bnetbot;

/**
 * Class holding the data required for logging in to a battle.net server.
 * 
 * @author Andras Belicza
 */
public class LoginConfig {
	
	/** URLs of the battle.net gateways. */
	private static final String[] GATEWAY_URLS = { "useast.battle.net", "uswest.battle.net", "europe.battle.net", "asia.battle.net", "sc.theabyss.ru" };
	
	/** URL of the battle.net server.  */
	public final String serverUrl;
	/** Port of the battle.net server. */
	public final int    serverPort = 6112;
	
	/**
	 * Creates a new LoginConfig.
	 * @param gatway gateway to connect to (valid gateway: <code>ServerApiConsts.GATEWAYS</code>)
	 */
	public LoginConfig( final int gateway ) {
		if ( gateway < 0 || gateway >= GATEWAY_URLS.length )
			throw new IllegalArgumentException( "Invalid gateway specified!" );
		serverUrl = GATEWAY_URLS[ gateway ];
	}
	
	/**
	 * Creates a new LoginConfig.
	 * @param gatewayUrl URL of the gateway to connect to
	 */
	public LoginConfig( final String serverUrl ) {
		this.serverUrl = serverUrl;
	}
	
}
