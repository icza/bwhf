package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * Servlet to serve hacker list related requests.<br>
 * These include:
 * <ul>
 * 	<li>Sending parts of the hacker list.
 * 	<li>Performing validation of authorization keys.
 * 	<li>Processing reports of hackers.
 * </ul>
 * 
 * @author Andras Belicza
 */
public class HackerDbServlet extends HttpServlet {
	
	/** URL of the hacker database. */
	private static final String DATABASE_URL = "jdbc:hsqldb:hsql://localhost/hackers";
	
	/** Date format to be used to format output dates. */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
	
	/** Max players in a report. */
	private static final int MAX_PLAYERS_IN_REPORT = 8;
	
	/** Gateway colors.                                              */
	private static final String[] GATEWAY_COLORS            = new String[] { "ff5050", "00ffff", "00ff00", "ffff00", "000000", "ffffff" };
	/** Gateway foreground colors.                                   */
	private static final String[] GATEWAY_FOREGROUND_COLORS = new String[] { "000000", "000000", "000000", "000000", "ffffff", "000000" };
	/** Styles used for different gateways in the hacker list table. */
	private static final String[] GATEWAY_STYLES;
	static {
		GATEWAY_STYLES = new String[ GATEWAY_COLORS.length ];
		for ( int i = 0; i < GATEWAY_STYLES.length; i++ )
			GATEWAY_STYLES[ i ] = "background:#" + GATEWAY_COLORS[ i ] + ";color:#" + GATEWAY_FOREGROUND_COLORS[ i ] + ";";
	}
	
	/** Style for unknown gateway.                                   */
	private static final String   UNKNOWN_GATEWAY_STYLE = "background:#f080f0;color:#000000;";
	
	/** Id to be used in the form. */
	private static final String FORM_ID = "fid";
	
	/** Default value of the min report count filter. */
	private static final int FILTER_DEFAULT_MIN_REPORT_COUNT = 1;
	/** Default value of the page filter.             */
	private static final int FILTER_DEFAULT_PAGE             = 1;
	/** Default value of the page size filter.        */
	private static final int FILTER_DEFAULT_PAGE_SIZE        = 25;
	
	/** Default value of ascendant sorting for the different sorting by values. */
	private static final Map< String, Boolean > FILTER_DEFAULT_ASCENDANT_SORTING_MAP = new HashMap< String, Boolean >();
	static {
		FILTER_DEFAULT_ASCENDANT_SORTING_MAP.put( SORT_BY_VALUE_NAME          , true  );
		FILTER_DEFAULT_ASCENDANT_SORTING_MAP.put( SORT_BY_VALUE_GATEWAY       , true  );
		FILTER_DEFAULT_ASCENDANT_SORTING_MAP.put( SORT_BY_VALUE_REPORT_COUNT  , false );
		FILTER_DEFAULT_ASCENDANT_SORTING_MAP.put( SORT_BY_VALUE_FIRST_REPORTED, false );
		FILTER_DEFAULT_ASCENDANT_SORTING_MAP.put( SORT_BY_VALUE_LAST_REPORTED , false );
	}
	
	/** Data source to provide pooled connections to the hacker database. */
	private static final BasicDataSource dataSource = new BasicDataSource();
	static {
		dataSource.setDriverClassName( "org.hsqldb.jdbcDriver" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );
		dataSource.setUrl( DATABASE_URL );
	}
	
	@Override
	public void doPost( final HttpServletRequest request, final HttpServletResponse response ) {
		doGet( request, response );
	}
	
	@Override
	public void doGet( final HttpServletRequest request, final HttpServletResponse response ) {
		setNoCache( response );
		
		try {
			String operation = request.getParameter( REQUEST_PARAMETER_NAME_OPERATION );
			
			if ( operation == null )
				operation = OPERATION_LIST;
			
			if ( operation.equals( OPERATION_LIST ) ) {
				
				final FiltersWrapper filtersWrapper = new FiltersWrapper();
				// Parse parameters
				filtersWrapper.name              = getStringParamValue( request, FILTER_NAME_NAME );
				filtersWrapper.gateways          = new boolean[ GATEWAYS.length ];
				for ( int i = 0; i < filtersWrapper.gateways.length; i++ )
					filtersWrapper.gateways[ i ] = request.getParameter( REQUEST_PARAMETER_FILTERS_PRESENT ) == null || request.getParameter( FILTER_NAME_GATEWAY + i ) != null;
				filtersWrapper.gameEngines       = new boolean[ GAME_ENGINES.length ];
				for ( int i = 0; i < filtersWrapper.gameEngines.length; i++ )
					filtersWrapper.gameEngines[ i ] = request.getParameter( REQUEST_PARAMETER_FILTERS_PRESENT ) == null || request.getParameter( FILTER_NAME_GAME_ENGINE + i ) != null;
				filtersWrapper.mapName           = getStringParamValue( request, FILTER_NAME_MAP_NAME );
				filtersWrapper.minReportCount    = getIntParamValue( request, FILTER_NAME_MIN_REPORT_COUNT, FILTER_DEFAULT_MIN_REPORT_COUNT );
				filtersWrapper.reportedWithKey   = getStringParamValue( request, FILTER_NAME_REPORTED_WITH_KEY );
				filtersWrapper.sortByValue       = getStringParamValue( request, FILTER_NAME_SORT_BY );
				if ( filtersWrapper.sortByValue.length() == 0 )
					filtersWrapper.sortByValue   = SORT_BY_VALUE_LAST_REPORTED;
				filtersWrapper.ascendantSorting  = request.getParameter( FILTER_NAME_ASCENDANT_SORTING ) == null ? FILTER_DEFAULT_ASCENDANT_SORTING_MAP.get( filtersWrapper.sortByValue ) : request.getParameter( FILTER_NAME_ASCENDANT_SORTING ).equalsIgnoreCase( "true" );
				filtersWrapper.page              = getIntParamValue( request, FILTER_NAME_PAGE, FILTER_DEFAULT_PAGE );
				filtersWrapper.pageSize          = getIntParamValue( request, FILTER_NAME_PAGE_SIZE, FILTER_DEFAULT_PAGE_SIZE );
				filtersWrapper.stepDirection     = request.getParameter( FILTER_NAME_STEP_DIRECTION );
				
				serveHackerList( filtersWrapper, response );
				
			} else if ( operation.equals( OPERATION_CHECK ) ) {
				
				// Check the validity of an authorization key
				final String key = request.getParameter( REQUEST_PARAMETER_NAME_KEY );
				if ( key == null )
					throw new BadRequestException();
				
				sendBackPlainMessage( Boolean.toString( checkKey( key ) ), response );
				
			} else if ( operation.equals( OPERATION_REPORT ) ) {
				
				final String key = request.getParameter( REQUEST_PARAMETER_NAME_KEY );
				if ( key == null )
					throw new BadRequestException();
				
				int gatewayIndex = 0;
				try {
					gatewayIndex = Integer.parseInt( request.getParameter( REQUEST_PARAMETER_NAME_GATEWAY ) );
				}
				catch ( final Exception e ) {
					throw new BadRequestException();
				}
				if ( gatewayIndex < 0 || gatewayIndex >= GATEWAYS.length )
					throw new BadRequestException();
				
				int gameEngine = 0;
				try {
					gameEngine = Integer.parseInt( request.getParameter( REQUEST_PARAMETER_NAME_GAME_ENGINE ) );
				}
				catch ( final Exception e ) {
					throw new BadRequestException();
				}
				
				String mapName = request.getParameter( REQUEST_PARAMETER_NAME_MAP_NAME );
				if ( mapName == null )
					throw new BadRequestException();
				mapName = mapName.toLowerCase(); // We store lowercased version to search fast without case sensitivity
				
				String agentVersion = request.getParameter( REQUEST_PARAMETER_NAME_AGENT_VERSION );
				if ( agentVersion == null )
					throw new BadRequestException();
				
				final List< String > playerNameList = new ArrayList< String >( MAX_PLAYERS_IN_REPORT );
				String playerName;
				for ( int i = 0; i <= MAX_PLAYERS_IN_REPORT && ( playerName = request.getParameter( REQUEST_PARAMETER_NAME_PLAYER + i ) ) != null; i++ )
					playerNameList.add( playerName.toLowerCase() ); // We handle player names all lowercased!
				if ( playerNameList.isEmpty() )
					throw new BadRequestException();
				
				sendBackPlainMessage( handleReport( key, gatewayIndex, gameEngine, mapName, agentVersion, playerNameList.toArray( new String[ playerNameList.size() ] ), request.getRemoteAddr() ), response );
				
			} else if ( operation.equals( OPERATION_STATISTICS ) ) {
				
				serveStatistics( response );
				
			} else if ( operation.equals( OPERATION_DOWNLOAD ) ) {
				
				serveDownload( request, response );
				
			}
		}
		catch ( final BadRequestException bre ) {
			sendBackErrorMessage( response );
		}
	}
	
	/**
	 * Returns the value of a request parameter.
	 * @param request   http request object
	 * @param paramName name of the parameter whose value is to be returned
	 * @return the value of a request parameter or an empty string if the parameter does not have a value
	 */
	private String getStringParamValue( final HttpServletRequest request, final String paramName ) {
		final String paramValue = request.getParameter( paramName );
		return paramValue == null ? "" : paramValue;
	}
	
	/**
	 * Returns the integer value parsed from a request parameter.
	 * @param request   http request object
	 * @param paramName name of the parameter whose value is to be returned
	 * @param defaultValue
	 * @return the value of a request parameter or <code>defaultValue</code> if the parameter does not have a value or it does not contain a valid integer
	 */
	private int getIntParamValue( final HttpServletRequest request, final String paramName, final int defaultValue ) {
		try {
			return Integer.parseInt( request.getParameter( paramName ) );
		}
		catch ( final Exception e ) {
			return defaultValue;
		}
	}
	
	/**
	 * Serves a part of the hacker list.
	 * @param filtersWrapper filters wrapper holding the filter parameters
	 * @param response       the http response
	 */
	private void serveHackerList( final FiltersWrapper filtersWrapper, final HttpServletResponse response ) {
		setNoCache( response );
		response.setContentType( "text/html" );
		
		Connection        connection = null;
		PreparedStatement statement  = null;
		ResultSet         resultSet  = null;
		
		PrintWriter outputWriter = null;
		try {
			
			connection = dataSource.getConnection();
			
			statement = buildHackersQueryStatement( filtersWrapper, true, connection );
			resultSet = statement.executeQuery();
			int matchingRecordsCount = 0;
			if ( resultSet.next() )
				matchingRecordsCount = resultSet.getInt( 1 );
			else
				throw new SQLException( "Could not count matching hackers." );
			resultSet.close();
			statement.close();
			
			if ( filtersWrapper.pageSize < 1 )
				filtersWrapper.pageSize = 1;
			final int pagesCount = ( (matchingRecordsCount-1) / filtersWrapper.pageSize ) + 1;
			if ( filtersWrapper.stepDirection != null ) {
				if ( filtersWrapper.stepDirection.equals( STEP_DIRECTION_FIRST ) )
					filtersWrapper.page = 1;
				else if ( filtersWrapper.stepDirection.equals( STEP_DIRECTION_PREVIOUS ) )
					filtersWrapper.page--;
				else if ( filtersWrapper.stepDirection.equals( STEP_DIRECTION_NEXT ) )
					filtersWrapper.page++;
				else if ( filtersWrapper.stepDirection.equals( STEP_DIRECTION_LAST ) )
					filtersWrapper.page = pagesCount;
			}
			if ( filtersWrapper.page < 1 )
				filtersWrapper.page = 1;
			if ( filtersWrapper.page > pagesCount )
				filtersWrapper.page = pagesCount;
			
			outputWriter = response.getWriter();
			
			outputWriter.println( "<html><head><title>BWHF Hacker database</title>" );
			outputWriter.println( "<link rel='shortcut icon' href='favicon.ico' type='image/x-icon'><style>" );
			for ( int i = 0; i < GATEWAY_STYLES.length; i++ )
				outputWriter.println( ".gat" + i + " {" + GATEWAY_STYLES[ i ] + "}" );
			outputWriter.println( ".gatUn {" + UNKNOWN_GATEWAY_STYLE + "}" );
			outputWriter.println( ".sortCol {cursor:pointer;}</style></head><body style='background:#f7f7f7'><center>" );
			
			// Header section
			outputWriter.println( "<h2>BWHF Hacker database</h2>" );
			outputWriter.println( "<p><table border=0><tr><td><a href='http://code.google.com/p/bwhf'>BWHF Agent home page</a><td>&nbsp;&nbsp;<a href='hackers?" + REQUEST_PARAMETER_NAME_OPERATION + "=" + OPERATION_STATISTICS + "'>Statistics</a><sup style='color:red;background:yellow;font-size:65%'>NEW!</sup>&nbsp;&nbsp;<td><a href='http://code.google.com/p/bwhf/wiki/OnlineHackerDatabase'>Help about this page (filters, sorting)</a></table></p>" );
			//outputWriter.println( "<table border=0><tr><td><a href='http://code.google.com/p/bwhf'>BWHF Agent home page</a><td>&nbsp;&nbsp;<a href='http://code.google.com/p/bwhf/wiki/OnlineHackerDatabase'>Help about this page (filters, sorting)</a></table>" );
			
			// Controls section
			outputWriter.println( "<form id='" + FORM_ID + "' action='hackers' method='POST'>" );
			outputWriter.println( "<input name='" + REQUEST_PARAMETER_FILTERS_PRESENT + "' type=hidden value='yes'>" ); // We might use default values if this is not present (for example gateways and game engines). 
			
			// Filters
			outputWriter.println( "<b>Filters</b> <button type=button onclick=\"" + getJavaScriptForResetFilters( filtersWrapper ) + "\">Reset filters</button><table border=1>" );
			outputWriter.println( "<tr><th>Hacker name:<td><input name='" + FILTER_NAME_NAME + "' type=text value='" + filtersWrapper.name + "' style='width:100%'>" );
			// Render gateways here
			outputWriter.print  ( "<tr><th>Gateways:<td>" );
			for ( int i = 0; i < GATEWAYS.length; i++ )
				outputWriter.println( "<input name='" + FILTER_NAME_GATEWAY + i + "' type=checkbox " + ( filtersWrapper.gateways[ i ] ? "checked" : "" ) + ">" + GATEWAYS[ i ] );
			// Render game engines here
			outputWriter.print  ( "<tr><th>Game engines:<td>" );
			for ( int i = 0; i < GAME_ENGINES.length; i++ )
				outputWriter.println( "<input name='" + FILTER_NAME_GAME_ENGINE + i + "' type=checkbox " + ( filtersWrapper.gameEngines[ i ] ? "checked" : "" ) + ">" + GAME_ENGINES[ i ] );
			outputWriter.println( "<tr><th>Map name:<td><input name='" + FILTER_NAME_MAP_NAME + "' type=text value='" + filtersWrapper.mapName + "' style='width:100%'>" );
			outputWriter.println( "<tr><th>Min report count:<td><input name='" + FILTER_NAME_MIN_REPORT_COUNT +"' type=text value='" + filtersWrapper.minReportCount + "' style='width:100%'>" );
			outputWriter.println( "<tr><th>Reported with key:<td><input name='" + FILTER_NAME_REPORTED_WITH_KEY +"' type=text value='" + filtersWrapper.reportedWithKey + "' style='width:100%'>" );
			outputWriter.println( "</table>" );
			outputWriter.println( "<p><input type=submit value='Go / Refresh'></p>" );
			
			outputWriter.println( "<p><b>Hackers matching the filters: " + matchingRecordsCount + "</b></p>" );
			
			// Sorting
			outputWriter.println( "<input name='" + FILTER_NAME_SORT_BY + "' type=hidden value='" + filtersWrapper.sortByValue + "'>" );
			outputWriter.println( "<input name='" + FILTER_NAME_ASCENDANT_SORTING + "' type=hidden value='" + filtersWrapper.ascendantSorting + "'>" );
			
			// Pagination
			outputWriter.println( "<input type=submit name='" + FILTER_NAME_STEP_DIRECTION +"' value='" + STEP_DIRECTION_FIRST + "' " + ( filtersWrapper.page <= 1 ? "disabled" : "" ) + ">" );
			outputWriter.println( "<input type=submit name='" + FILTER_NAME_STEP_DIRECTION +"' value='" + STEP_DIRECTION_PREVIOUS + "' " + ( filtersWrapper.page <= 1 ? "disabled" : "" ) + ">" );
			outputWriter.println( "Page <input name='" + FILTER_NAME_PAGE + "' type=text value='" + filtersWrapper.page + "' size=1> out of <b>" + pagesCount + "</b>, page size:" );
			outputWriter.println( "<input name='" + FILTER_NAME_PAGE_SIZE + "' type=text value='" + filtersWrapper.pageSize + "' size=1>" );
			outputWriter.println( "<input type=submit name='" + FILTER_NAME_STEP_DIRECTION +"' value='" + STEP_DIRECTION_NEXT + "' " + ( filtersWrapper.page >= pagesCount ? "disabled" : "" ) + ">" );
			outputWriter.println( "<input type=submit name='" + FILTER_NAME_STEP_DIRECTION +"' value='" + STEP_DIRECTION_LAST + "' " + ( filtersWrapper.page >= pagesCount ? "disabled" : "" ) + ">" );
			outputWriter.println( "</form>" );
			
			outputWriter.flush();
			
			// Hackers table section
			outputWriter.println( "<table border=1>" );
			outputWriter.println( "<tr><th>#<th class='sortCol' onclick=\"" + getJavaScriptForSortingColumn( SORT_BY_VALUE_NAME          , filtersWrapper ) + "\">Name"           + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_NAME           ) ? ( filtersWrapper.ascendantSorting ? " &uarr;" : " &darr;" ) : "" )
										 + "<th class='sortCol' onclick=\"" + getJavaScriptForSortingColumn( SORT_BY_VALUE_GATEWAY       , filtersWrapper ) + "\">Gateway"        + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_GATEWAY        ) ? ( filtersWrapper.ascendantSorting ? " &uarr;" : " &darr;" ) : "" )
										 + "<th class='sortCol' onclick=\"" + getJavaScriptForSortingColumn( SORT_BY_VALUE_REPORT_COUNT  , filtersWrapper ) + "\">Report count"   + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_REPORT_COUNT   ) ? ( filtersWrapper.ascendantSorting ? " &uarr;" : " &darr;" ) : "" )
										 + "<th class='sortCol' onclick=\"" + getJavaScriptForSortingColumn( SORT_BY_VALUE_FIRST_REPORTED, filtersWrapper ) + "\">First reported" + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_FIRST_REPORTED ) ? ( filtersWrapper.ascendantSorting ? " &uarr;" : " &darr;" ) : "" )
										 + "<th class='sortCol' onclick=\"" + getJavaScriptForSortingColumn( SORT_BY_VALUE_LAST_REPORTED , filtersWrapper ) + "\">Last reported"  + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_LAST_REPORTED  ) ? ( filtersWrapper.ascendantSorting ? " &uarr;" : " &darr;" ) : "" ) );
			if ( matchingRecordsCount > 0 ) {
				int recordNumber = ( filtersWrapper.page - 1 )* filtersWrapper.pageSize;
				statement = buildHackersQueryStatement( filtersWrapper, false, connection );
				resultSet = statement.executeQuery();
				while ( resultSet.next() ) {
					final int gateway = resultSet.getInt( 2 );
					outputWriter.println( "<tr class='gat" + ( gateway < GATEWAY_STYLES.length ? gateway : "Un" ) + "'><td align=right>" + (++recordNumber) + "<td>" + resultSet.getString( 1 ) + "<td>" + GATEWAYS[ resultSet.getInt( 2 ) ] + "<td align=center>" + resultSet.getInt( 3 ) + "<td>" + DATE_FORMAT.format( resultSet.getTimestamp( 4 ) ) + "<td>" + DATE_FORMAT.format( resultSet.getTimestamp( 5 ) ) );
				}
			}
			outputWriter.println( "</table>" );
			
			// Footer section
			outputWriter.println( "<p align=right><i>&copy; Andr&aacute;s Belicza, 2008-2009</i></p>" );
			outputWriter.println( "</center></body></html>" );
			
			outputWriter.flush();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		} catch ( final SQLException se ) {
			se.printStackTrace();
			final String errorMessage = "<p>Sorry, the server has encountered an error, we cannot fulfill your request! We apologize.</p>";
			if ( outputWriter != null )
				outputWriter.println( errorMessage );
			else
				sendBackErrorMessage( response, errorMessage );
		}
		finally {
			if ( resultSet != null )
				try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null )
				try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null )
				try { connection.close(); } catch ( final SQLException se ) {}
			
			if ( outputWriter != null )
				outputWriter.close();
		}
	}
	
	/**
	 * Builds a javascript code to be used to reset the filters.
	 * @param filtersWrapper filters wrapper holding the filter parameters
	 * @return the javascript code to be used to reset the filters
	 */
	private static String getJavaScriptForResetFilters( final FiltersWrapper filtersWrapper ) {
		final StringBuilder scriptBuilder = new StringBuilder( "javascript:" );
		scriptBuilder
			.append( "document.getElementsByName('" ).append( FILTER_NAME_NAME ).append( "')[0].value='';" )
			.append( "for(var i=0;i<" ).append( GATEWAYS.length ).append( ";i++) document.getElementsByName('" ).append( FILTER_NAME_GATEWAY ).append( "'+i)[0].checked=true;" )
			.append( "for(var i=0;i<" ).append( GAME_ENGINES.length ).append( ";i++) document.getElementsByName('" ).append( FILTER_NAME_GAME_ENGINE ).append( "'+i)[0].checked=true;" )
			.append( "document.getElementsByName('" ).append( FILTER_NAME_MAP_NAME ).append( "')[0].value='';" )
			.append( "document.getElementsByName('" ).append( FILTER_NAME_MIN_REPORT_COUNT ).append( "')[0].value='" ).append( FILTER_DEFAULT_MIN_REPORT_COUNT ).append( "';" )
			.append( "document.getElementsByName('" ).append( FILTER_NAME_REPORTED_WITH_KEY ).append( "')[0].value='';" );
		return scriptBuilder.toString();
	}
	
	/**
	 * Builds a javascript code to be used for sorting column header onclick event.
	 * @param headerName     name of the header to generate javascript for
	 * @param filtersWrapper filters wrapper holding the filter parameters
	 * @return the javascript code to be used for sorting column header onclick event
	 */
	private static String getJavaScriptForSortingColumn( final String headerName, final FiltersWrapper filtersWrapper ) {
		final StringBuilder scriptBuilder = new StringBuilder( "javascript:" );
		
		if ( !filtersWrapper.sortByValue.equals( headerName ) )
			scriptBuilder.append( "document.getElementsByName('" ).append( FILTER_NAME_SORT_BY ).append( "')[0].value='" ).append( headerName ).append( "';" );
		
		scriptBuilder.append( "document.getElementsByName('" ).append( FILTER_NAME_ASCENDANT_SORTING ).append( "')[0].value=" )
			.append( filtersWrapper.sortByValue.equals( headerName ) ? !filtersWrapper.ascendantSorting : FILTER_DEFAULT_ASCENDANT_SORTING_MAP.get( headerName ) ).append( ';' );
		
		scriptBuilder.append( "document.getElementById('" + FORM_ID + "').submit();" );
		return scriptBuilder.toString();
	}
	
	/**
	 * Builds a query listing the hackers matching the filter parameters.
	 * @param filtersWrapper filters wrapper holding the filter parameters
	 * @param countOnly if true, only a query to count the matching hackers is required
	 * @return a prepared statement with a parameterized query listing the hackers matching the filter parameters
	 * @throws SQLException thrown if <code>connection.prepareStatement()</code> throws an <code>SQLException</code>
	 */
	private PreparedStatement buildHackersQueryStatement( final FiltersWrapper filtersWrapper, final boolean countOnly, final Connection connection ) throws SQLException {
		final StringBuilder queryBuilder = new StringBuilder();
		if ( countOnly )
			queryBuilder.append( "SELECT COUNT(*) FROM (SELECT h.gateway" );
		else
			queryBuilder.append( "SELECT h.name, h.gateway, COUNT(h.gateway) AS reportsCount, MIN(r.version) AS firstReported, MAX(r.version) AS lastReported" );
		
		queryBuilder.append( " FROM hacker h, report r, key k WHERE r.hacker=h.id AND r.key=k.id AND k.revocated=FALSE" );
		
		int sqlParamsCounter  = 0;
		int nameParamIndex    = 0;
		int mapNameParamIndex = 0;
		int keyParamIndex     = 0;
		if ( filtersWrapper.name.length() > 0 ) {
			queryBuilder.append( " AND h.name LIKE ?" );
			nameParamIndex = ++sqlParamsCounter;
		}
		
		if ( filtersWrapper.mapName.length() > 0 ) {
			queryBuilder.append( " AND r.map_name LIKE ?" );
			mapNameParamIndex = ++sqlParamsCounter;
		}
		
		if ( filtersWrapper.reportedWithKey.length() > 0 ) {
			queryBuilder.append( " AND k.value=?" );
			keyParamIndex = ++sqlParamsCounter;
		}
		
		boolean dontNeedAllGateways = false; // Only want to append gateway filter if not all is required
		for ( final boolean gateway : filtersWrapper.gateways )
			if ( !gateway ) {
				dontNeedAllGateways = true;
				break;
			}
		if ( dontNeedAllGateways ) {
			queryBuilder.append( " AND h.gateway IN (" );
			for ( int i = filtersWrapper.gateways.length - 1; i >=0; i-- )
				if ( filtersWrapper.gateways[ i ] )
					queryBuilder.append( i ).append( ',' );
			queryBuilder.append( -1 ).append( ')' );
		}
		
		boolean dontNeedAllGameEngines= false; // Only want to append game engine filter if not all is required
		for ( final boolean gameEngine : filtersWrapper.gameEngines )
			if ( !gameEngine ) {
				dontNeedAllGameEngines = true;
				break;
			}
		if ( dontNeedAllGameEngines ) {
			queryBuilder.append( " AND r.game_engine IN (" );
			for ( int i = filtersWrapper.gameEngines.length - 1; i >=0; i-- )
				if ( filtersWrapper.gameEngines[ i ] )
					queryBuilder.append( i ).append( ',' );
			queryBuilder.append( -1 ).append( ')' );
		}
		
		queryBuilder.append( " GROUP BY h.name, h.gateway HAVING COUNT(h.gateway)>=" ).append( filtersWrapper.minReportCount );
		
		if ( countOnly )
			queryBuilder.append( ')' );
		else {
			queryBuilder.append( " ORDER BY " );
			if ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_LAST_REPORTED ) )
				queryBuilder.append( "lastReported" );
			else if ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_GATEWAY ) )
				queryBuilder.append( "h.gateway" );
			else if ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_REPORT_COUNT ) )
				queryBuilder.append( "reportsCount" );
			else if ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_NAME ) )
				queryBuilder.append( "h.name" );
			else // Default sorting...
				queryBuilder.append( "firstReported" );
			
			queryBuilder.append( filtersWrapper.ascendantSorting ? " ASC" : " DESC" );
			
			queryBuilder.append( " LIMIT " ).append( filtersWrapper.pageSize ).append( " OFFSET " ).append( ( filtersWrapper.page - 1 ) * filtersWrapper.pageSize );
		}
		
		final PreparedStatement statement = connection.prepareStatement( queryBuilder.toString() );
		if ( nameParamIndex > 0 )
			statement.setString( nameParamIndex, "%" + filtersWrapper.name + "%" );
		if ( mapNameParamIndex > 0 )
			statement.setString( mapNameParamIndex, "%" + filtersWrapper.mapName + "%" );
		if ( keyParamIndex > 0 )
			statement.setString( keyParamIndex, filtersWrapper.reportedWithKey );
		
		return statement;
	}
	
	/**
	 * Creates and sends statistics of the hacker database.
	 * @param response the http response
	 */
	private void serveStatistics( final HttpServletResponse response ) {
		setNoCache( response );
		response.setContentType( "text/html" );
		
		Connection connection = null;
		Statement  statement  = null;
		ResultSet  resultSet  = null;
		
		PrintWriter outputWriter = null;
		try {
			
			final int CHARTS_WIDTH  = 750;
			final int CHARTS_HEIGHT = 400;
			
			final Date currentDate = new Date();
			
			connection = dataSource.getConnection();
			
			// Gateway distribution chart data
			statement = connection.createStatement();
			resultSet = statement.executeQuery( "SELECT hacker.gateway, COUNT(DISTINCT hacker.id) FROM report JOIN key on report.key=key.id JOIN hacker on report.hacker=hacker.id WHERE key.revocated=FALSE GROUP BY hacker.gateway ORDER BY COUNT(DISTINCT hacker.id) DESC" );
			final List< int[] > gatewayDistributionList = new ArrayList< int[] >();
			int hackersCount = 0;
			while ( resultSet.next() ) {
				final int hackersInGatway = resultSet.getInt( 2 );
				hackersCount += hackersInGatway;
				gatewayDistributionList.add( new int[] { resultSet.getInt( 1 ), hackersInGatway } );
			}
			resultSet.close();
			statement.close();
			
			// Monthly reports chart data
			statement = connection.createStatement();
			
			final List< Object[] > monthlyReportsList = new ArrayList< Object[] >();
			final PreparedStatement preparedStatement = connection.prepareStatement( "SELECT COUNT(*) FROM report JOIN key on report.key=key.id WHERE key.revocated=FALSE AND version>=? and version<?" );
			statement = preparedStatement; // Store it to the statement variable to close it in case of exception
			final DateFormat monthDateFormat = new SimpleDateFormat( "MMMMM, yyyy" );
			final GregorianCalendar calendar1 = new GregorianCalendar( 2008, Calendar.DECEMBER, 1 );
			final GregorianCalendar calendar2 = new GregorianCalendar( 2008, Calendar.DECEMBER, 1 );
			calendar2.add( Calendar.MONTH, 1 );
			int reportsCount           = 0;
			int maxMonthlyReportsCount = 0;
			while ( calendar1.getTime().before( currentDate  ) ) {
				preparedStatement.setDate( 1, new java.sql.Date( calendar1.getTime().getTime() ) );
				preparedStatement.setDate( 2, new java.sql.Date( calendar2.getTime().getTime() ) );
				
				resultSet = preparedStatement.executeQuery();
				while ( resultSet.next() ) {
					final int reportsInMonth = resultSet.getInt( 1 );
					reportsCount += reportsInMonth;
					if ( reportsInMonth > maxMonthlyReportsCount )
						maxMonthlyReportsCount = reportsInMonth;
					monthlyReportsList.add( new Object[] { monthDateFormat.format( calendar1.getTime() ), reportsInMonth } );
				}
				resultSet.close();
				
				calendar1.setTime( calendar2.getTime() );
				calendar2.add( Calendar.MONTH, 1 );
			}
			if ( maxMonthlyReportsCount == 0 )
				maxMonthlyReportsCount = 1;
			statement.close();
			
			// Gateway distribution chart URL
			final StringBuilder gatewayDistributionChartUrlBuilder = new StringBuilder();
			gatewayDistributionChartUrlBuilder.append( "http://chart.apis.google.com/chart?cht=p3&amp;chs=" ).append( CHARTS_WIDTH ).append( 'x' ).append( CHARTS_HEIGHT )
				.append( "&amp;chtt=BWHF+Hacker+gateway+distribution+at+" ).append( DATE_FORMAT.format( currentDate ).replace( " ", "+" ) );
			gatewayDistributionChartUrlBuilder.append( "&amp;chd=t:" );
			Formatter numberFormatter = new Formatter( gatewayDistributionChartUrlBuilder );
			for ( int i = 0; i < gatewayDistributionList.size(); i++ ) {
				final int[] gatewayDistribution = gatewayDistributionList.get( i ); 
				if ( i > 0 )
					gatewayDistributionChartUrlBuilder.append( ',' );
				numberFormatter.format( "%2.1f", 100.0f * gatewayDistribution[ 1 ] / hackersCount );
			}
			gatewayDistributionChartUrlBuilder.append( "&amp;chl=" );
			for ( int i = 0; i < gatewayDistributionList.size(); i++ ) {
				final int[] gatewayDistribution = gatewayDistributionList.get( i ); 
				if ( i > 0 )
					gatewayDistributionChartUrlBuilder.append( '|' );
				gatewayDistributionChartUrlBuilder.append( GATEWAYS[ gatewayDistribution[ 0 ] ] );
				numberFormatter.format( "+%2.1f", 100.0f * gatewayDistribution[ 1 ] / hackersCount );
				gatewayDistributionChartUrlBuilder.append( "%25" );
			}
			gatewayDistributionChartUrlBuilder.append( "&amp;chco=" );
			for ( int i = 0; i < gatewayDistributionList.size(); i++ ) {
				final int[] gatewayDistribution = gatewayDistributionList.get( i ); 
				if ( i > 0 )
					gatewayDistributionChartUrlBuilder.append( ',' );
				gatewayDistributionChartUrlBuilder.append( GATEWAY_COLORS[ gatewayDistribution[ 0 ] ] );
			}
			
			// Monthly reports chart URL
			final StringBuilder monthlyReportsChartUrlBuilder = new StringBuilder();
			numberFormatter = new Formatter( monthlyReportsChartUrlBuilder );
			monthlyReportsChartUrlBuilder.append( "http://chart.apis.google.com/chart?cht=bvs&amp;chbh=a&amp;chxt=y&amp;chxr=0,0," )
				.append( maxMonthlyReportsCount ).append( ",100&amp;chg=0," );
			numberFormatter.format( "%2.2f", 10000f / maxMonthlyReportsCount );
			monthlyReportsChartUrlBuilder.append( "&amp;chs=" ).append( CHARTS_WIDTH ).append( 'x' ).append( CHARTS_HEIGHT )
				.append( "&amp;chtt=BWHF+Monthly+reports+at+" ).append( DATE_FORMAT.format( currentDate ).replace( " ", "+" ) );
			monthlyReportsChartUrlBuilder.append( "&amp;chd=t:" );
			for ( int i = 0; i < monthlyReportsList.size(); i++ ) {
				final Object[] monthlyReports = monthlyReportsList.get( i ); 
				if ( i > 0 )
					monthlyReportsChartUrlBuilder.append( ',' );
				numberFormatter.format( "%2.1f", 100.0f * (Integer) monthlyReports[ 1 ] / maxMonthlyReportsCount );
			}
			monthlyReportsChartUrlBuilder.append( "&amp;chl=" );
			for ( int i = 0; i < monthlyReportsList.size(); i++ ) {
				final Object[] monthlyReports = monthlyReportsList.get( i ); 
				if ( i > 0 )
					monthlyReportsChartUrlBuilder.append( '|' );
				monthlyReportsChartUrlBuilder.append( ( (String) monthlyReports[ 0 ] ).replace( " ", "+" ) );
			}
			
			// Generate HTML output
			outputWriter = response.getWriter();
			
			outputWriter.println( "<html><head><title>BWHF Hacker database statistics</title>" );
			outputWriter.println( "<link rel='shortcut icon' href='favicon.ico' type='image/x-icon'><style>" );
			for ( int i = 0; i < GATEWAY_STYLES.length; i++ )
				outputWriter.println( ".gat" + i + " {" + GATEWAY_STYLES[ i ] + "}" );
			outputWriter.println( ".gatUn {" + UNKNOWN_GATEWAY_STYLE + "}" );
			outputWriter.println( "</style></head><body style='background:#f7f7f7'><center>" );
			
			// Header section
			outputWriter.println( "<h2>BWHF Hacker database statistics</h2>" );
			outputWriter.println( "<table border=0><tr><td><a href='hackers'>Back to the hacker list</a><td>&nbsp;&nbsp;<a href='http://code.google.com/p/bwhf'>BWHF Agent home page</a></table>" );
			
			outputWriter.println( "<h3>Hacker distribution between gateways</h3>" );
			outputWriter.println( "<table border=0><tr><td>" );
			outputWriter.println( "<tr><td><img src='" + gatewayDistributionChartUrlBuilder.toString() + "' width=" + CHARTS_WIDTH + " height=" + CHARTS_HEIGHT + " title='Hacker distribution between gateways'></img>" );
			outputWriter.println( "<td><table border=1><tr><th>Gatway:<th>Hackers:" );
			// Add gateways with no hackers
			for ( int gateway = 0; gateway < GATEWAYS.length; gateway++ ) {
				boolean gatewayIncluded = false;
				for ( final int[] gatewayDistribution : gatewayDistributionList )
					if ( gatewayDistribution[ 0 ] == gateway ) {
						gatewayIncluded = true;
						break;
					}
				if ( !gatewayIncluded )
					gatewayDistributionList.add( new int[] { gateway, 0 } );
			}
			outputWriter.println( "<tr class='gatUn'><td>Total:<td align=right>" + hackersCount );
			for ( final int[] gatewayDistribution : gatewayDistributionList )
				outputWriter.println( "<tr class='gat" + ( gatewayDistribution[ 0 ] < GATEWAY_STYLES.length ? gatewayDistribution[ 0 ] : "Un" ) + "'><td>" + GATEWAYS[ gatewayDistribution[ 0 ] ] + "<td align=right>" + gatewayDistribution[ 1 ] );
			outputWriter.println( "</table></table>" );
			outputWriter.flush();
			
			outputWriter.println( "<h3>Monthly reports</h3>" );
			outputWriter.println( "<table border=0><tr><td>" );
			outputWriter.println( "<tr><td><img src='" + monthlyReportsChartUrlBuilder.toString() + "' width=" + CHARTS_WIDTH + " height=" + CHARTS_HEIGHT + " title='Monthly reports'></img>" );
			outputWriter.println( "<td><table border=1><tr><th>Month:<th>Reports:" );
			Collections.reverse( monthlyReportsList );
			monthlyReportsList.add( 0, new Object[] { "Total:", reportsCount} );
			for ( final Object[] monthlyReports : monthlyReportsList )
				outputWriter.println( "<tr><td>" + monthlyReports[ 0 ] + "<td align=right>" + monthlyReports[ 1 ] );
			outputWriter.println( "</table></table>" );
			
			// Footer section
			outputWriter.println( "<p align=right><i>&copy; Andr&aacute;s Belicza, 2008-2009</i></p>" );
			outputWriter.println( "</center></body></html>" );
			
			outputWriter.flush();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		} catch ( final SQLException se ) {
			se.printStackTrace();
			final String errorMessage = "<p>Sorry, the server has encountered an error, we cannot fulfill your request! We apologize.</p>";
			if ( outputWriter != null )
				outputWriter.println( errorMessage );
			else
				sendBackErrorMessage( response, errorMessage );
		}
		finally {
			if ( resultSet != null )
				try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null )
				try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null )
				try { connection.close(); } catch ( final SQLException se ) {}
			
			if ( outputWriter != null )
				outputWriter.close();
		}
	}
	
	/**
	 * Serves the download of the hacker list.
	 * @param request  the http request
	 * @param response the http response
	 */
	private void serveDownload( final HttpServletRequest request, final HttpServletResponse response ) {
		setNoCache( response );
		response.setContentType( "text/plain" );
		
		Connection connection = null;
		Statement  statement  = null;
		ResultSet  resultSet  = null;
		
		PrintWriter outputWriter = null;
		boolean     success      = true;
		final long  startTime    = System.nanoTime();
		try {
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			resultSet = statement.executeQuery( "SELECT DISTINCT gateway, name from hacker JOIN report on report.hacker=hacker.id JOIN key on report.key=key.id WHERE key.revocated=FALSE" );
			outputWriter = response.getWriter();
			
			while ( resultSet.next() )
				outputWriter.println( resultSet.getInt( 1 ) + "," + resultSet.getString( 2 ) );
			
			outputWriter.flush();
			
		}
		catch ( final Exception e ) {
			success = false;
		}
		finally {
			if ( connection != null ) {
				PreparedStatement preparedStatement = null;
				try {
					preparedStatement = connection.prepareStatement( "INSERT INTO download_log (ip,success,exec_time_ms) VALUES (?,?,?)" );
					preparedStatement.setString ( 1, request.getRemoteAddr() );
					preparedStatement.setBoolean( 2, success );
					preparedStatement.setInt    ( 3, (int) ( ( System.nanoTime() - startTime ) / 1000000l ) );
					preparedStatement.executeUpdate();
				}
				catch ( final Exception e ) {
				}
				finally {
					if ( preparedStatement != null )
						try { preparedStatement.close(); } catch ( final SQLException se ) {}
				}
			}
			
			if ( resultSet != null )
				try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null )
				try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null )
				try { connection.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Checks a key in the data base if its valid.<br>
	 * A key is valid if it exists in the data base and it has not been revocated.
	 * @param key key to be checked
	 * @return true if the key is valid; false otherwise
	 */
	private boolean checkKey( final String key ) {
		Connection        connection = null;
		PreparedStatement statement  = null;
		ResultSet         resultSet  = null;
		
		try {
			connection = dataSource.getConnection();
			
			statement = connection.prepareStatement( "SELECT id FROM key WHERE revocated=FALSE AND value=?" );
			statement.setString( 1, key );
			
			resultSet = statement.executeQuery();
			if ( resultSet.next() )
				return true;
			
			return false;
		} catch ( final SQLException se ) {
			se.printStackTrace();
			return false;
		}
		finally {
			if ( resultSet != null )
				try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null )
				try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null )
				try { connection.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Handles a report.
	 * @param key         authorization key of the reporter
	 * @param gateway     gateway of the reported players
	 * @param gameEngine  game engine
	 * @param mapName     map name
	 * @param mapName     BWHF agent version
	 * @param playerNames names of players being reported; only non-null values contain information
	 * @param ip          ip of the reporter's computer
	 * @return an error message if report fails; an empty string otherwise
	 */
	private String handleReport( final String key, final int gateway, final int gameEngine, final String mapName, final String agentVersion, final String[] playerNames, final String ip ) {
		Connection        connection = null;
		PreparedStatement statement  = null;
		ResultSet         resultSet  = null;
		PreparedStatement statement2 = null;
		Statement         statement3 = null;
		ResultSet         resultSet3 = null;
		
		try {
			connection = dataSource.getConnection();
			
			// First check the validity of the key
			statement = connection.prepareStatement( "SELECT id FROM key WHERE revocated=FALSE AND value=?" );
			statement.setString( 1, key );
			
			resultSet = statement.executeQuery();
			if ( !resultSet.next() )
				return "Invalid authorization key, report discarded!";
			
			final int keyId = resultSet.getInt( 1 );
			resultSet.close();
			statement.close();
			
			// The rest has to be a transaction
			connection.setAutoCommit( false );
			
			final Integer[] hackerIds = new Integer[ playerNames.length ];
			
			// Search existing hackers with the given names on the same gateways
			statement = connection.prepareStatement( "SELECT id FROM hacker WHERE gateway=? AND name=?" );
			for ( int i = 0; i < playerNames.length && playerNames[ i ] != null; i++ ) {
				statement.setInt   ( 1, gateway          );
				statement.setString( 2, playerNames[ i ] );
				
				resultSet = statement.executeQuery();
				if ( resultSet.next() ) {
					// We found the hacker in the data base
					hackerIds[ i ] = resultSet.getInt( 1 );
				}
				else {
					// New hacker, add it first
					statement2 = connection.prepareStatement( "INSERT INTO hacker (name,gateway) VALUES (?,?)" );
					statement2.setString( 1, playerNames[ i ].toLowerCase() );
					statement2.setInt   ( 2, gateway                        );
					if ( statement2.executeUpdate() > 0 ) {
						statement3 = connection.createStatement();
						resultSet3 = statement3.executeQuery( "CALL IDENTITY()" );
						if ( resultSet3.next() )
							hackerIds[ i ] = resultSet3.getInt( 1 );
						else
							throw new SQLException( "Could not get id of newly inserted hacker." );
						resultSet3.close();
						statement3.close();
					}
					else
						throw new SQLException( "Could not insert new hacker." );
					
					statement2.close();
				}
				
				resultSet.close();
			}
			statement.close();
			
			// Lastly insert the report records
			statement = connection.prepareStatement( "INSERT INTO report (hacker,game_engine,map_name,agent_version,key,ip) VALUES (?,?,?,?,?,?)" );
			statement.setInt   ( 2, gameEngine   );
			statement.setString( 3, mapName      );
			statement.setString( 4, agentVersion );
			statement.setInt   ( 5, keyId        );
			statement.setString( 6, ip           );
			for ( int i = 0; i < hackerIds.length && hackerIds[ i ] != null; i++ ) {
				statement.setInt( 1, hackerIds[ i ] );
				if ( statement.executeUpdate() <= 0 )
					throw new SQLException( "Could not insert report." );
			}
			statement.close();
			
			connection.commit();
			connection.setAutoCommit( true );
			
			return REPORT_ACCEPTED_MESSAGE;
		} catch ( final SQLException se ) {
			se.printStackTrace();
			if ( connection != null )
				try { connection.rollback(); } catch ( final SQLException se2 ) {}
			return "Report processing error!";
		}
		finally {
			if ( resultSet3 != null ) try { resultSet3.close(); } catch ( final SQLException se ) {}
			if ( statement3 != null ) try { statement3.close(); } catch ( final SQLException se ) {}
			if ( statement2 != null ) try { statement2.close(); } catch ( final SQLException se ) {}
			if ( resultSet  != null ) try { resultSet .close(); } catch ( final SQLException se ) {}
			if ( statement  != null ) try { statement .close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Sends back a message as plain text.
	 * @param message  message to be sent
	 * @param response response to be used
	 */
	private void sendBackPlainMessage( final String message, final HttpServletResponse response ) {
		response.setContentType( "text/plain" );
		try {
			final PrintWriter outputWriter = response.getWriter();
			outputWriter.println( message );
			outputWriter.flush();
			outputWriter.close();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Sends back an error message to the client indicating a bad request.
	 * @param response response to be used
	 */
	private void sendBackErrorMessage( final HttpServletResponse response ) {
		sendBackErrorMessage( response, "Bad request!" );
	}
	
	/**
	 * Sends back an error message to the client.
	 * @param response response to be used
	 * @param message  message to be sent back
	 */
	private void sendBackErrorMessage( final HttpServletResponse response, final String message ) {
		response.setContentType( "text/html" );
		try {
			final PrintWriter outputWriter = response.getWriter();
			outputWriter.println( message );
			outputWriter.flush();
			outputWriter.close();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Configures the response for no caching.
	 * @param response response to be configured
	 */
	private void setNoCache( final HttpServletResponse response ) {
		response.setHeader( "Cache-Control", "no-cache" ); // For HTTP 1.1
		response.setHeader( "Pragma"       , "no-cache" ); // For HTTP 1.0
		response.setDateHeader( "Expires", -0 );           // For proxies
	}
	
}
