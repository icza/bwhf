package hu.belicza.andras.hackerdb;

/**
 * Constants to be used if a client communicates with the server.
 * 
 * @author Andras Belicza
 */
public class ServerApiConsts {
	
	/** Name of the operation request parameter.       */
	public static final String REQUEST_PARAMETER_NAME_OPERATION   = "op";
	/** Name of the key request parameter.             */
	public static final String REQUEST_PARAMETER_NAME_KEY         = "key";
	/** Name of the player name request parameter.     */
	public static final String REQUEST_PARAMETER_NAME_PLAYER      = "pln";
	/** Name of the gateway request parameter.         */
	public static final String REQUEST_PARAMETER_NAME_GATEWAY     = "gat";
	/** Name of the game engine request parameter.     */
	public static final String REQUEST_PARAMETER_NAME_GAME_ENGINE = "gen";
	/** Name of the map name request parameter.        */
	public static final String REQUEST_PARAMETER_NAME_MAP_NAME    = "mpn";
	/** Name of the filters present request parameter. */
	public static final String REQUEST_PARAMETER_FILTERS_PRESENT  = "fpr";
	
	/** List hackers operation value.            */
	public static final String OPERATION_LIST   = "lst";
	/** Check authorization key operation value. */
	public static final String OPERATION_CHECK  = "chk";
	/** Report hackers operation value.          */
	public static final String OPERATION_REPORT = "rep";
	
	/** Message to be sent back to the client if his/her report was accepted and processed. */
	public static final String REPORT_ACCEPTED_MESSAGE = "OK";
	
	/** String representations of gateways. Only the index should is sent over. */
	public static final String[] GATEWAYS     = new String[] { "USEast", "USWest", "Europe", "Asia", "iCCup", "Other" };
	/** String representations of game engines. Only the index is be sent over. */
	public static final String[] GAME_ENGINES = new String[] { "Starcraft", "Broodwar" };
	
	/** Filter name for name.              */
	public static final String FILTER_NAME_NAME              = "nam";
	/** Filter name for gateway.           */
	public static final String FILTER_NAME_GATEWAY           = "gat";
	/** Filter name for game engine.       */
	public static final String FILTER_NAME_GAME_ENGINE       = "gen";
	/** Filter name for game engine.       */
	public static final String FILTER_NAME_MAP_NAME          = "mpn";
	/** Filter name for min report count.  */
	public static final String FILTER_NAME_MIN_REPORT_COUNT  = "mrc";
	/** Filter name for reported with key. */
	public static final String FILTER_NAME_REPORTED_WITH_KEY = "rwk";
	/** Filter name for page.              */
	public static final String FILTER_NAME_PAGE              = "pag";
	/** Filter name for page size.         */
	public static final String FILTER_NAME_PAGE_SIZE         = "pgs";
	/** Filter name for sort by.           */
	public static final String FILTER_NAME_SORT_BY           = "sby";
	/** Filter name for ascendant sorting. */
	public static final String FILTER_NAME_ASCENDANT_SORTING = "ast";
	/** Filter name for step direction.    */
	public static final String FILTER_NAME_STEP_DIRECTION    = "std";
	
	/** Sort by value for name.            */
	public static final String SORT_BY_VALUE_NAME            = "snm";
	/** Sort by value for gateway.         */
	public static final String SORT_BY_VALUE_GATEWAY         = "sgt";
	/** Sort by value for report count.    */
	public static final String SORT_BY_VALUE_REPORT_COUNT    = "src";
	
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_FIRST          = "First";
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_PREVIOUS       = "Prev";
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_NEXT           = "Next";
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_LAST           = "Last";
	
}
