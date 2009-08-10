package hu.belicza.andras.bnetbot;

/**
 * Class holding the data required for logging in to a battle.net server.
 * 
 * @author Andras Belicza
 */
public class LoginConfig {
	
	/** URLs of the battle.net gateways. */
	private static final String[] GATEWAY_URLS = { "useast.battle.net", "uswest.battle.net", "europe.battle.net", "asia.battle.net", "sc.theabyss.ru" };
	
	/** URL of the battle.net server.       */
	public final String serverUrl;
	/** Port of the battle.net server.      */
	public final int    serverPort;
	/** User name to login with.            */
	public final String userName;
	/** Password of the user to login with. */
	public final String password;
	
	/**
	 * Creates a new LoginConfig.
	 * @param gatway   gateway to connect to (valid gateway: <code>ServerApiConsts.GATEWAYS</code>)
	 * @param userName user name to login with
	 * @param password password of the user to login with
	 */
	public LoginConfig( final int gateway, final String userName, final String password ) {
		this( gateway < 0 || gateway >= GATEWAY_URLS.length ? null : GATEWAY_URLS[ gateway ], 6112, userName, password );
	}
	
	/**
	 * Creates a new LoginConfig.
	 * @param gatewayUrl URL of the gateway to connect to
	 * @param port       of the gateway to connect to
	 * @param userName   user name to login with
	 * @param password   password of the account to login with
	 */
	public LoginConfig( final String serverUrl, final int serverPort, final String userName, final String password ) {
		if ( serverUrl == null )
			throw new IllegalArgumentException( "Invalid gateway specified!" );
		
		this.serverUrl  = serverUrl;
		this.serverPort = serverPort;
		this.userName   = userName;
		this.password   = password;
	}
	
}
