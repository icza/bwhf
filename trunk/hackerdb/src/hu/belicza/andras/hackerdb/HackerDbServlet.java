package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_DESCENDANT_SORTING;
import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_GATEWAY;
import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_MIN_REPORT_COUNT;
import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_NAME;
import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_NO_ALL_GATEWAYS;
import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_PAGE;
import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_PAGE_SIZE;
import static hu.belicza.andras.hackerdb.ApiConsts.FILTER_NAME_SORT_BY;
import static hu.belicza.andras.hackerdb.ApiConsts.GATEWAYS;
import static hu.belicza.andras.hackerdb.ApiConsts.OPERATION_CHECK;
import static hu.belicza.andras.hackerdb.ApiConsts.OPERATION_LIST;
import static hu.belicza.andras.hackerdb.ApiConsts.OPERATION_REPORT;
import static hu.belicza.andras.hackerdb.ApiConsts.REQUEST_PARAMETER_NAME_GATEWAY;
import static hu.belicza.andras.hackerdb.ApiConsts.REQUEST_PARAMETER_NAME_KEY;
import static hu.belicza.andras.hackerdb.ApiConsts.REQUEST_PARAMETER_NAME_OPERATION;
import static hu.belicza.andras.hackerdb.ApiConsts.REQUEST_PARAMETER_NAME_PLAYER;
import static hu.belicza.andras.hackerdb.ApiConsts.SORT_BY_VALUE_GATEWAY;
import static hu.belicza.andras.hackerdb.ApiConsts.SORT_BY_VALUE_NAME;
import static hu.belicza.andras.hackerdb.ApiConsts.SORT_BY_VALUE_REPORT_COUNT;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	
	/** URL of the hacker data base. */
	private static final String DATABASE_URL = "jdbc:hsqldb:hsql://localhost/hackers";
	
	/** Styles used for different gateways in the hacker list table. */
	private final String[] GATEWAY_STYLES        = new String[] { "background:#ff2020;color:#000000;", "background:#00ffff;color:#000000;", "background:#00ff00;color:#000000;", "background:#ffff00;color:#000000;", "background:#000000;color:#ffffff;", "background:#ffffff;color:#000000;" };
	/** Style for unknown gatway.                                    */
	private final String   UNKNOWN_GATEWAY_STYLE = "background:#f080f0;color:#000000;";
	
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
				filtersWrapper.gateways           = new boolean[ GATEWAYS.length ];
				for ( int i = 0; i < filtersWrapper.gateways.length; i++ )
					filtersWrapper.gateways[ i ]  = request.getParameter( FILTER_NAME_NO_ALL_GATEWAYS ) == null || request.getParameter( FILTER_NAME_GATEWAY + i ) != null;
				filtersWrapper.minReportCount    = getIntParamValue( request, FILTER_NAME_MIN_REPORT_COUNT, 1 );
				filtersWrapper.sortByValue       = getStringParamValue( request, FILTER_NAME_SORT_BY );
				if ( filtersWrapper.sortByValue.length() == 0 )
					filtersWrapper.sortByValue   = SORT_BY_VALUE_NAME;
				filtersWrapper.descendantSorting = request.getParameter( FILTER_NAME_DESCENDANT_SORTING ) != null ;
				filtersWrapper.page              = getIntParamValue( request, FILTER_NAME_PAGE, 1 );
				filtersWrapper.pageSize          = getIntParamValue( request, FILTER_NAME_PAGE_SIZE, 20 );
				
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
				
				final String[] playerNames = new String[ 8 ];
				for ( int i = 0; i <= playerNames.length && ( playerNames[ i ] = request.getParameter( REQUEST_PARAMETER_NAME_PLAYER + i ) ) != null; i++ )
					;
				if ( playerNames[ 0 ] == null )
					throw new BadRequestException();
				
				sendBackPlainMessage( handleReport( key, gatewayIndex, playerNames, request.getRemoteAddr() ), response );
			}
		}
		catch ( final BadRequestException bre ) {
			sendBackErrorMessage( response );
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
			
			if ( filtersWrapper.page < 1 )
				filtersWrapper.page = 1;
			if ( filtersWrapper.pageSize < 1 )
				filtersWrapper.pageSize = 1;
			
			statement = buildHackersQueryStatement( filtersWrapper, true, connection );
			resultSet = statement.executeQuery();
			int matchingRecordsCount = 0;
			if ( resultSet.next() )
				matchingRecordsCount = resultSet.getInt( 1 );
			else
				throw new SQLException( "Could not count matching hackers." );
			resultSet.close();
			statement.close();
			
			final int pagesCount = ( (matchingRecordsCount-1) / filtersWrapper.pageSize ) + 1;
			if ( filtersWrapper.page > pagesCount )
				filtersWrapper.page = pagesCount;
			
			outputWriter = response.getWriter();
			
			outputWriter.println( "<html><head><title>BWHF Hacker data base</title><style>" );
			for ( int i = 0; i < GATEWAY_STYLES.length; i++ )
				outputWriter.println( ".gateway" + i + " {" + GATEWAY_STYLES[ i ] + "}" );
			outputWriter.println( ".gatewayUn {" + UNKNOWN_GATEWAY_STYLE + "}" );
			outputWriter.println( "</style></head><body><center>" );
			
			// Header section
			outputWriter.println( "<h2>BWHF Hacker data base</h2>" );
			outputWriter.println( "<p align=right>Go to <a href='http://code.google.com/p/bwhf'>BWHF Agent home page</a></p>" );
			
			// Controls section
			outputWriter.println( "<form action='/hackers' method='GET'>" );
			
			// Filters
			outputWriter.println( "<b>Filters:</b><table border=1>" );
			outputWriter.println( "<tr><th>Name<th>Gateway<th>Min. report count" );
			outputWriter.println( "<tr><td><input name='" + FILTER_NAME_NAME + "' type=text value='" + filtersWrapper.name + "'><td>" );
			
			// Render gateways here
			outputWriter.println( "<input type=hidden name='" + FILTER_NAME_NO_ALL_GATEWAYS + "' value='no'>" ); // First we select all gatways, on subsequent calls we only want to select selected gateways
			for ( int i = 0; i < GATEWAYS.length; i++ )
				outputWriter.println( "<input name='" + FILTER_NAME_GATEWAY + i + "' type=checkbox " + ( filtersWrapper.gateways[ i ] ? "checked" : "" ) + ">" + GATEWAYS[ i ] );
			outputWriter.println( "<td><input name='" + FILTER_NAME_MIN_REPORT_COUNT +"' type=text value='" + filtersWrapper.minReportCount + "'>" );
			outputWriter.println( "</table>" );
			outputWriter.println( "<p><input type=submit value='Go / Refresh'></p>" );
			outputWriter.println( "<p><b>Hackers matching the filters: " + matchingRecordsCount + "</b></p>" );
			
			// Sorting
			outputWriter.println( "<p>Sort hackers by: <select name='" + FILTER_NAME_SORT_BY + "'>" );
			outputWriter.println( "<option value='" + SORT_BY_VALUE_NAME + "' " + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_NAME ) ? "selected" : "" ) + ">Name</value>" );
			outputWriter.println( "<option value='" + SORT_BY_VALUE_GATEWAY + "' " + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_GATEWAY ) ? "selected" : "" ) + ">Gateway</value>" );
			outputWriter.println( "<option value='" + SORT_BY_VALUE_REPORT_COUNT + "' " + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_REPORT_COUNT ) ? "selected" : "" ) + ">Report count</value>" );
			outputWriter.println( "</select>" );
			outputWriter.println( "<input name='" + FILTER_NAME_DESCENDANT_SORTING + "' type=checkbox " + ( filtersWrapper.descendantSorting ? "checked" : "" ) + ">Descendant sorting</p>" );
			
			// Pagination
			outputWriter.println( "<input type=submit value='First' " + ( filtersWrapper.page <= 1 ? "disabled" : "" ) + ">" );
			outputWriter.println( "<input type=submit value='Previous' " + ( filtersWrapper.page <= 1 ? "disabled" : "" ) + ">" );
			outputWriter.println( "Page <input name='" + FILTER_NAME_PAGE + "' type=text value='" + filtersWrapper.page + "' size=1> out of <b>" + pagesCount + "</b>, page size:" );
			outputWriter.println( "<input name='" + FILTER_NAME_PAGE_SIZE + "' type=text value='" + filtersWrapper.pageSize + "' size=1>" );
			outputWriter.println( "<input type=submit value='Next' " + ( filtersWrapper.page >= pagesCount ? "disabled" : "" ) + ">" );
			outputWriter.println( "<input type=submit value='Last' " + ( filtersWrapper.page >= pagesCount ? "disabled" : "" ) + ">" );
			outputWriter.println( "</form>" );
			
			// Hackers table section
			outputWriter.println( "<table border=1>" );
			outputWriter.println( "<tr><th>#<th>Name" + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_NAME ) ? ( filtersWrapper.descendantSorting ? " &darr;" : " &uarr;" ) : "" )
					+ "<th>Gateway" + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_GATEWAY ) ? ( filtersWrapper.descendantSorting ? " &darr;" : " &uarr;" ) : "" )
					+ "<th>Report count" + ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_REPORT_COUNT ) ? ( filtersWrapper.descendantSorting ? " &darr;" : " &uarr;" ) : "" ) + "" );
			if ( matchingRecordsCount > 0 ) {
				int recordNumber = ( filtersWrapper.page - 1 )* filtersWrapper.pageSize;
				statement = buildHackersQueryStatement( filtersWrapper, false, connection );
				resultSet = statement.executeQuery();
				while ( resultSet.next() ) {
					final int gateway = resultSet.getInt( 2 );
					outputWriter.println( "<tr class='gateway" + ( gateway < GATEWAY_STYLES.length ? gateway : "Un" ) + "'><td>" + (++recordNumber) + "<td>" + resultSet.getString( 1 ) + "<td>" + GATEWAYS[ resultSet.getInt( 2 ) ] + "<td>" + resultSet.getInt( 3 ) );
				}
			}
			outputWriter.println( "</table>" );
			
			// Footer section
			outputWriter.println( "<p align=right><i>&copy; Andr&aacute;s Belicza, 2008</i></p>" );
			outputWriter.println( "</center></body></html>" );
			
			outputWriter.flush();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		} catch ( final SQLException se ) {
			se.printStackTrace();
			if ( outputWriter != null )
				outputWriter.println( "<p>Sorry, the server has encountered an error, we cannot fulfill your request!</p>" );
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
			queryBuilder.append( "SELECT h.name, h.gateway, COUNT(h.gateway) AS reportsCount" );
		
		queryBuilder.append( " FROM hacker h, report r, key k WHERE r.hacker=h.id AND r.key=k.id AND k.revocated=FALSE" );
		
		if ( filtersWrapper.name.length() > 0 )
			queryBuilder.append( " AND h.name LIKE ?" );
		
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
		
		queryBuilder.append( " GROUP BY h.name, h.gateway HAVING COUNT(h.gateway)>=" ).append( filtersWrapper.minReportCount );
		
		if ( countOnly )
			queryBuilder.append( ')' );
		else {
			queryBuilder.append( " ORDER BY " );
			if ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_GATEWAY ) )
				queryBuilder.append( "h.gateway" );
			else if ( filtersWrapper.sortByValue.equals( SORT_BY_VALUE_REPORT_COUNT ) )
				queryBuilder.append( "reportsCount" );
			else
				queryBuilder.append( "h.name" );
			
			queryBuilder.append( filtersWrapper.descendantSorting ? " DESC" : " ASC" );
			
			queryBuilder.append( " LIMIT " ).append( filtersWrapper.pageSize ).append( " OFFSET " ).append( ( filtersWrapper.page - 1 ) * filtersWrapper.pageSize );
		}
		
		final PreparedStatement statement = connection.prepareStatement( queryBuilder.toString() );
		if ( filtersWrapper.name.length() > 0 )
			statement.setString( 1, "%" + filtersWrapper.name + "%" );
		
		return statement;
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
	 * @param playerNames names of players being reported; only non-null values contain information
	 * @param ip          ip of the reporter's computer
	 * @return an error message if report fails; an empty string otherwise
	 */
	private String handleReport( final String key, final int gateway, final String[] playerNames, final String ip ) {
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
					statement2.setString( 1, playerNames[ i ] );
					statement2.setInt   ( 2, gateway          );
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
			statement = connection.prepareStatement( "INSERT INTO report (hacker,key,ip) VALUES (?,?,?)" );
			statement.setInt   ( 2, keyId );
			statement.setString( 3, ip    );
			for ( int i = 0; i < hackerIds.length && hackerIds[ i ] != null; i++ ) {
				statement.setInt( 1, hackerIds[ i ] );
				if ( statement.executeUpdate() <= 0 )
					throw new SQLException( "Could not insert report." );
			}
			statement.close();
			
			connection.commit();
			connection.setAutoCommit( true );
			
			return "";
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
	 * Sends back an error message indicating a bad request.
	 * @param response response to be used
	 */
	private void sendBackErrorMessage( final HttpServletResponse response ) {
		response.setContentType( "text/html" );
		try {
			final PrintWriter outputWriter = response.getWriter();
			outputWriter.println( "Bad request!" );
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
		response.setDateHeader ( "Expires", -0 );          // For proxies
	}
	
}
