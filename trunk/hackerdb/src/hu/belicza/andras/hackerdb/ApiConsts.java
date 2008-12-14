package hu.belicza.andras.hackerdb;

/**
 * Constants to be used if a client communicates with the server.
 * 
 * @author Andras Belicza
 */
public class ApiConsts {
	
	/** Name of the operation request parameter.   */
	public static final String REQUEST_PARAMETER_NAME_OPERATION = "op";
	/** Name of the key request parameter.         */
	public static final String REQUEST_PARAMETER_NAME_KEY       = "key";
	/** Name of the player name request parameter. */
	public static final String REQUEST_PARAMETER_NAME_PLAYER    = "pln";
	/** Name of the gateway request parameter.     */
	public static final String REQUEST_PARAMETER_NAME_GATEWAY   = "gat";
	
	/** List hackers operation value.            */
	public static final String OPERATION_LIST   = "lst";
	/** Check authorization key operation value. */
	public static final String OPERATION_CHECK  = "chk";
	/** Report hackers operation value.          */
	public static final String OPERATION_REPORT = "rep";
	
	/** String representations of gateways. Only the index should be sent over. */
	public static final String[] GATEWAYS = new String[] { "USEast", "USWest", "Europe", "Asia", "iCCup", "Other" };
	
}
