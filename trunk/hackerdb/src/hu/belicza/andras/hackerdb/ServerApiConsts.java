package hu.belicza.andras.hackerdb;

/**
 * Constants to be used if a client communicates with the server.
 * 
 * @author Andras Belicza
 */
public class ServerApiConsts {
	
	/** Name of the operation request parameter.       */
	public static final String REQUEST_PARAMETER_NAME_OPERATION        = "op";
	/** Name of the key request parameter.             */
	public static final String REQUEST_PARAMETER_NAME_KEY              = "key";
	/** Name of the player request parameter.          */
	public static final String REQUEST_PARAMETER_NAME_PLAYER           = "pln";
	/** Name of the gateway request parameter.         */
	public static final String REQUEST_PARAMETER_NAME_GATEWAY          = "gat";
	/** Name of the game engine request parameter.     */
	public static final String REQUEST_PARAMETER_NAME_GAME_ENGINE      = "gen";
	/** Name of the map name request parameter.        */
	public static final String REQUEST_PARAMETER_NAME_MAP_NAME         = "mpn";
	/** Name of the map name request parameter.        */
	public static final String REQUEST_PARAMETER_NAME_AGENT_VERSION    = "agv";
	/** Name of the filters present request parameter. */
	public static final String REQUEST_PARAMETER_NAME_FILTERS_PRESENT  = "fpr";
	/** Name of the filters present request parameter. */
	public static final String REQUEST_PARAMETER_NAME_REPLAY_MD5       = "md5";
	/** Name of the filters present request parameter. */
	public static final String REQUEST_PARAMETER_NAME_REPLAY_SAVE_TIME = "rst";
	
	/** List hackers operation value.            */
	public static final String OPERATION_LIST       = "lst";
	/** Check authorization key operation value. */
	public static final String OPERATION_CHECK      = "chk";
	/** Report hackers operation value.          */
	public static final String OPERATION_REPORT     = "rep";
	/** Report hackers operation value.          */
	public static final String OPERATION_STATISTICS = "sta";
	/** Download hacker list operation value.    */
	public static final String OPERATION_DOWNLOAD   = "dhl";
	
	/** Message to be sent back to the client if his/her report was accepted and processed. */
	public static final String REPORT_ACCEPTED_MESSAGE = "OK";
	
	/** String representations of gateways. Only the index is sent over.     */
	public static final String[] GATEWAYS     = new String[] { "USEast", "USWest", "Europe", "Asia", "iCCup", "Other" };
	/** String representations of game engines. Only the index is sent over. */
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
	/** Sort by value for first reported.  */
	public static final String SORT_BY_VALUE_FIRST_REPORTED  = "sfr";
	/** Sort by value for last reported.   */
	public static final String SORT_BY_VALUE_LAST_REPORTED   = "slr";
	/** Sort by value for hacking period.  */
	public static final String SORT_BY_VALUE_HACKING_PERIOD  = "shp";
	
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_FIRST          = "First";
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_PREVIOUS       = "Prev";
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_NEXT           = "Next";
	/** Step direction value for first.    */
	public static final String STEP_DIRECTION_LAST           = "Last";
	
	
	// ***********************************************
	// *********** PLAYERS' NETWORK ******************
	// ***********************************************
	
	/** Name of the operation request parameter.          */
	public static final String PN_REQUEST_PARAM_NAME_OPERATION     = "op";
	/** Name of the entity request parameter.             */
	public static final String PN_REQUEST_PARAM_NAME_ENTITY        = "what";
	/** Name of the page request parameter.               */
	public static final String PN_REQUEST_PARAM_NAME_PAGE          = "page";
	/** Name of the entity id request parameter.          */
	public static final String PN_REQUEST_PARAM_NAME_ENTITY_ID     = "id";
	/** Name of the sorting index request parameter.      */
	public static final String PN_REQUEST_PARAM_NAME_SORTING_INDEX = "sortidx";
	/** Name of the descendant sorting request parameter. */
	public static final String PN_REQUEST_PARAM_NAME_SORTING_DESC  = "sortdesc";
	/** Name of the player 1 request parameter.           */
	public static final String PN_REQUEST_PARAM_NAME_PLAYER1       = "player1";
	/** Name of the player 2 request parameter.           */
	public static final String PN_REQUEST_PARAM_NAME_PLAYER2       = "player2";
	/** Name of the include akas request parameter.       */
	public static final String PN_REQUEST_PARAM_NAME_INCLUDE_AKAS  = "includeakas";
	/** Name of the name filter request parameter.        */
	public static final String PN_REQUEST_PARAM_NAME_NAME_FILTER   = "namefilter";
	
	/** Send game info operation value.   */
	public static final String PN_OPERATION_SEND    = "send";
	/** List entity operation value.      */
	public static final String PN_OPERATION_LIST    = "list";
	/** Detail an entity operation value. */
	public static final String PN_OPERATION_DETAILS = "details";
	
	/** Game entity value.   */
	public static final String ENTITY_GAME   = "game";
	/** Player entity value. */
	public static final String ENTITY_PLAYER = "player";
	/** Aka entity value.    */
	public static final String ENTITY_AKA    = "aka";
	
	/** Game param name for engine.        */
	public static final String GAME_PARAM_ENGINE         = "engine";
	/** Game param name for frames.        */
	public static final String GAME_PARAM_FRAMES         = "frames";
	/** Game param name for save time.     */
	public static final String GAME_PARAM_SAVE_TIME      = "saveTime";
	/** Game param name for name.          */
	public static final String GAME_PARAM_NAME           = "name";
	/** Game param name for map width.     */
	public static final String GAME_PARAM_MAP_WIDTH      = "mapWidth";
	/** Game param name for map height.    */
	public static final String GAME_PARAM_MAP_HEIGHT     = "mapHeight";
	/** Game param name for speed.         */
	public static final String GAME_PARAM_SPEED          = "speed";
	/** Game param name for type.          */
	public static final String GAME_PARAM_TYPE           = "type";
	/** Game param name for sub type.      */
	public static final String GAME_PARAM_SUB_TYPE       = "subType";
	/** Game param name for creator name.  */
	public static final String GAME_PARAM_CREATOR_NAME   = "creatorName";
	/** Game param name for map name.      */
	public static final String GAME_PARAM_MAP_NAME       = "mapName";
	/** Game param name for replay md5.    */
	public static final String GAME_PARAM_REPLAY_MD5     = "replayMd5";
	/** Game param name for agent version. */
	public static final String GAME_PARAM_AGENT_VERSION  = "agentVersion";
	/** Game param name for agent version. */
	public static final String GAME_PARAM_GATEWAY        = "gateway";
	/** Game param name for player name.   */
	public static final String GAME_PARAM_PLAYER_NAME    = "playerName";
	/** Game param name for player actions.*/
	public static final String GAME_PARAM_PLAYER_ACTIONS = "playerActions";
	/** Game param name for player race.   */
	public static final String GAME_PARAM_PLAYER_RACE    = "playerRace";
	/** Game param name for player color.  */
	public static final String GAME_PARAM_PLAYER_COLOR   = "playerColor";
	
}
