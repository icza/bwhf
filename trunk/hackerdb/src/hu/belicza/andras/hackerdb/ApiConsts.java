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
	
	/** Filter name for name.             */
	public static final String FILTER_NAME_NAME               = "nam";
	/** Filter name for no all gateways.  */
	public static final String FILTER_NAME_NO_ALL_GATEWAYS    = "nag";
	/** Filter name for gateway.          */
	public static final String FILTER_NAME_GATEWAY            = "gat";
	/** Filter name for min report count. */
	public static final String FILTER_NAME_MIN_REPORT_COUNT   = "mrc";
	/** Filter name for page.             */
	public static final String FILTER_NAME_PAGE               = "pag";
	/** Filter name for page size.        */
	public static final String FILTER_NAME_PAGE_SIZE          = "pgs";
	/** Filter name for sort by.          */
	public static final String FILTER_NAME_SORT_BY            = "sby";
	/** Filter name for sort by.          */
	public static final String FILTER_NAME_DESCENDANT_SORTING = "dst";
	
	/** Sort by value for name.           */
	public static final String SORT_BY_VALUE_NAME             = "snm";
	/** Sort by value for gateway.           */
	public static final String SORT_BY_VALUE_GATEWAY          = "sgt";
	/** Sort by value for report count.   */
	public static final String SORT_BY_VALUE_REPORT_COUNT     = "src";
	
}
