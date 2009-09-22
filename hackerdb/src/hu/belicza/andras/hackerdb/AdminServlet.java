package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.FILTER_NAME_REPORTED_WITH_KEY;
import static hu.belicza.andras.hackerdb.ServerApiConsts.GATEWAYS;
import static hu.belicza.andras.hackerdb.ServerApiConsts.OPERATION_LIST;
import static hu.belicza.andras.hackerdb.ServerApiConsts.REQUEST_PARAMETER_NAME_OPERATION;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Admin servlet for basic administration tasks and reports.
 * 
 * @author Andras Belicza
 */
public class AdminServlet extends BaseServlet {
	
	/** User name session attribute. */
	private static final String ATTRIBUTE_USER_NAME = "userName";
	
	/** Operation request param.           */
	private static final String REQUEST_PARAM_OPERATION      = "op";
	/** User name request param.           */
	private static final String REQUEST_PARAM_USER_NAME      = "userName";
	/** Password request param.            */
	private static final String REQUEST_PARAM_PASSWORD       = "password";
	/** Hacker name request param.         */
	private static final String REQUEST_PARAM_HACKER_NAME    = "hackername";
	/** Last reports limit request param.  */
	private static final String REQUEST_PARAM_LASTREPS_LIMIT = "lastrepslimit";
	/** Revocate report id request param.  */
	private static final String REQUEST_PARAM_REVOCATE_ID    = "revocate_id";
	/** Reinstate report id request param. */
	private static final String REQUEST_PARAM_REINSTATE_ID   = "reinstate_id";
	
	/** Login operation.        */
	private static final String OPERATION_LOGIN     = "login";
	/** Logout operation.       */
	private static final String OPERATION_LOGOUT    = "logout";
	/** Last reports operation. */
	private static final String OPERATION_LAST_REPS = "lastreps";
	/** New key operation.      */
	private static final String OPERATION_NEW_KEY = "newkey";
	
	/** Default operation for the logged in users. */
	private static final String DEFAULT_OPERATION = OPERATION_LAST_REPS;
	
	/** Time format to format report times. */
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.SSS " );
	
	/** Hacker list menu HTML code to be sent. */
	private static final String ADMIN_PAGE_MENU_HTML = "<p><a href='admin?" + REQUEST_PARAM_OPERATION + "=" + OPERATION_LAST_REPS + "'>Last reports</a>"
			   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='admin?" + REQUEST_PARAM_OPERATION + "=" + OPERATION_NEW_KEY + "'>New key</a>"
			   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='admin?" + REQUEST_PARAM_OPERATION + "=" + OPERATION_LOGOUT + "'>Logout</a></p>";
	
	@Override
	public void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		setNoCache( response );
		try {
			request.setCharacterEncoding( "UTF-8" );
		} catch ( final UnsupportedEncodingException uee ) {
			// This will never happen.
			throw new RuntimeException( "Unsupported UTF-8 encoding?" ); 
		}
		
		final HttpSession session = request.getSession( false );
		
		if ( session == null )
			handleLogin( request, response );
		else
			try {
				String operation = request.getParameter( REQUEST_PARAM_OPERATION );
				
				if ( operation == null )
					operation = OPERATION_LOGIN;
				
				if ( operation.equals( OPERATION_LOGIN ) ) {
					
					if ( false )
						throw new BadRequestException();
					
				} else if ( operation.equals( OPERATION_LAST_REPS ) ) {
					
					handleLastReports( request, response );
					
				} else if ( operation.equals( OPERATION_NEW_KEY ) ) {
					
					handleNewKey( request, response );
					
				} else if ( operation.equals( OPERATION_LOGOUT ) ) {
					
					if ( session != null )
						session.invalidate();
					request.getRequestDispatcher( "admin?" + REQUEST_PARAM_OPERATION + "=" + OPERATION_LOGIN ).forward( request, response );
					
				}
			}
			catch ( final BadRequestException bre ) {
				sendBackErrorMessage( response );
			}
	}
	
	/**
	 * Handles the login sequence.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleLogin( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		ResultSet         resultSet    = null;
		PrintWriter       outputWriter = null;
		
		final String userName = request.getParameter( REQUEST_PARAM_USER_NAME );
		final String password = request.getParameter( REQUEST_PARAM_PASSWORD  );
		
		try {
			String message = null;
			if ( userName != null && password != null ) {
				connection = dataSource.getConnection();
				statement  = connection.prepareStatement( "SELECT id FROM person WHERE name=? AND password=?" );
				statement.setString( 1, userName );
				statement.setString( 2, encodePassword( password ) );
				
				resultSet = statement.executeQuery();
				if ( resultSet.next() ) {
					request.getSession().setAttribute( ATTRIBUTE_USER_NAME, userName );
				}
				else
					message = "Invalid user name or password!";
				resultSet.close();
				statement.close();
			}
			
			if ( request.getSession( false ) == null ) {
				outputWriter = response.getWriter();
				renderHeader( request, outputWriter );
				outputWriter.println( "<h3>Login page</h3>" );
				if ( message != null )
					outputWriter.println( "<p style='color:red'>" + message + "</p>" );
				
				outputWriter.println( "<form action='admin?" + REQUEST_PARAM_OPERATION + "=" + OPERATION_LOGIN + "' method='POST'>" );
				outputWriter.println( "<table border=0>" );
				outputWriter.println( "<tr><td>User name:<td><input type=text name='" + REQUEST_PARAM_USER_NAME + "' id='userNameFieldId'" + ( userName == null ? "" : " value='" + encodeHtmlString( userName ) + "'" ) + ">" );
				outputWriter.println( "<tr><td>Password:<td><input type=password name='" + REQUEST_PARAM_PASSWORD + "'>" );
				outputWriter.println( "<tr><td colspan=2 align=center><input type=submit value='Ok'>" );
				outputWriter.println( "</table>" );
				outputWriter.println( "</form>" );
				outputWriter.println( "<script>document.getElementById('userNameFieldId').focus();</script>" );
				
				renderFooter( outputWriter );
				
				outputWriter.flush();
			}
			else
				request.getRequestDispatcher( "admin?" + REQUEST_PARAM_OPERATION + "=" + DEFAULT_OPERATION ).forward( request, response );
			
		} catch ( final SQLException se ) {
			se.printStackTrace();
			sendBackErrorMessage( response, "SQL error: " + se.getMessage() );
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
	 * Handles the last reports.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleLastReports( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		ResultSet         resultSet    = null;
		PrintWriter       outputWriter = null;
		
		try {
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>Last reports</h3>" );
			
			connection = dataSource.getConnection();
			
			String revocateId  = request.getParameter( REQUEST_PARAM_REVOCATE_ID  );
			if ( revocateId != null && revocateId.length() == 0 )
				revocateId = null;
			String reinstateId = request.getParameter( REQUEST_PARAM_REINSTATE_ID );
			if ( reinstateId != null && reinstateId.length() == 0 )
				reinstateId = null;
			if ( revocateId != null || reinstateId != null ) {
				Integer id = null;
				try {
					id = Integer.valueOf( revocateId != null ? revocateId : reinstateId );
				}
				catch ( final Exception e ) {
				}
				if ( id != null ) {
					statement = connection.prepareStatement( "UPDATE report set revocated=" + ( revocateId == null ? "false" : "true" ) + " WHERE id=?" );
					statement.setInt( 1, id );
					if ( statement.executeUpdate() == 1 )
						outputWriter.println( "<p style='color:green'>Report has been " + ( revocateId == null ? "reinstated" : "revocated" ) + " successfully.</p>" );
					else
						outputWriter.println( "<p style='color:red'>Failed to " + ( revocateId == null ? "reinstate" : "revocate" ) + " the report!</p>" );
					statement.close();
				}
			}
			
			String hackerName = request.getParameter( REQUEST_PARAM_HACKER_NAME );
			if ( hackerName != null && hackerName.length() == 0 )
				hackerName = null;
			
			String lastRepsLimitParam = request.getParameter( REQUEST_PARAM_LASTREPS_LIMIT );
			int lastRepsLimit;
			try {
				lastRepsLimit = Integer.parseInt( lastRepsLimitParam );
				if ( lastRepsLimit < 0 )
					lastRepsLimit = 23;
			}
			catch ( final Exception e ) {
				lastRepsLimit = 23;
			}
			
			outputWriter.println( "<form action='admin?" + REQUEST_PARAM_OPERATION + "=" + OPERATION_LAST_REPS + "' method=POST><p>Hacker name:<input type=text name='" + REQUEST_PARAM_HACKER_NAME + "'"
					+ ( hackerName != null ? " value='" + encodeHtmlString( hackerName ) + "'" : "" ) + ">&nbsp;&nbsp;|&nbsp;&nbsp;Limit:<input type=text name='" + REQUEST_PARAM_LASTREPS_LIMIT + "'" 
					+ " value='" + lastRepsLimit + "' size=1>&nbsp;&nbsp;|&nbsp;&nbsp;<input type=submit value='Refresh'></p>" );
			
			String query = "SELECT report.ip, report.id as report, person.name as reporter, key.id as key, hacker.id as hacker, hacker.name as hacker_name, hacker.gateway as gw, game_engine as BW, report.version as date, substr(report.agent_version,1,4) as agver, game.id as game_id, report.revocated as revoc, key.value FROM report JOIN key on report.key=key.id JOIN person on key.person=person.id JOIN hacker on report.hacker=hacker.id LEFT OUTER JOIN game on report.replay_md5=game.replay_md5 WHERE 1=1";
			if ( hackerName != null )
				query += "and hacker.name=?";
			query += "ORDER BY report.id DESC LIMIT " + lastRepsLimit;
			
			statement = connection.prepareStatement( query ); 
			if ( hackerName != null )
				statement.setString( 1, hackerName );
			
			outputWriter.println( "<input type=hidden name='" + REQUEST_PARAM_REVOCATE_ID  + "'>" );
			outputWriter.println( "<input type=hidden name='" + REQUEST_PARAM_REINSTATE_ID + "'>" );
			
			outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
			outputWriter.println( "<tr class=" + TABLE_HEADER_STYLE_NAME + "><th>#<th>IP<th>Report<th>Reporter<th>Key<th>Hacker<th>Hacker name<th>Gateway<th>Engine<th>Date<th>Agver<th>Game id<th>Revocated" );
			resultSet = statement.executeQuery();
			int rowCounter = 0;
			while ( resultSet.next() ) {
				final int     reportId  = resultSet.getInt( 2 );
				final int     gateway   = resultSet.getInt( 7 );
				final boolean revocated = resultSet.getBoolean( 12 );
				
				outputWriter.println( "<tr class=" + ( gateway < GATEWAY_STYLE_NAMES.length ? GATEWAY_STYLE_NAMES[ gateway ] : UNKNOWN_GATEWAY_STYLE_NAME ) + "><td align=right>" + (++rowCounter)
						+ "<td>" + resultSet.getString( 1 ) + "<td>" + reportId + "<td>" + encodeHtmlString( resultSet.getString( 3 ) ) 
						+ "<td>" + getHackerRecordsByKeyLink( resultSet.getString( 13 ), Integer.toString( resultSet.getInt( 4 ) ), "keyreportsform" ) + "&uarr;<td>" + resultSet.getInt( 5 ) + "<td>" + HackerDbServlet.getHackerRecordsByNameLink( resultSet.getString( 6 ) ) 
						+ "<td>" + GATEWAYS[ gateway ]
						+ "<td>" + ReplayHeader.GAME_ENGINE_SHORT_NAMES[ resultSet.getInt( 8 ) ] + "<td>" + TIME_FORMAT.format( resultSet.getTimestamp( 9 ) )
						+ "<td>" + resultSet.getString( 10 ) + "<td>" + ( resultSet.getObject( 11 ) == null ? "N/A" : PlayersNetworkServlet.getGameDetailsHtmlLink( resultSet.getInt( 11 ), Integer.toString( resultSet.getInt( 11 ) ) ) ) + "<td>" + ( revocated ? "T" : "F" ) );
				outputWriter.println( revocated ? "<input type=submit value='Reinstate' onclick='javascript:this.form." + REQUEST_PARAM_REINSTATE_ID + ".value=\"" + reportId + "\";'>" : "<input type=submit value='Revocate' onclick='javascript:this.form." + REQUEST_PARAM_REVOCATE_ID + ".value=\"" + reportId + "\";'>" );
			}
			resultSet.close();
			statement.close();
			outputWriter.println( "</table></form>" );
			
			outputWriter.println( "<form id='keyreportsform' action='hackers?" + REQUEST_PARAMETER_NAME_OPERATION + '=' + OPERATION_LIST + "' method=POST target='_blank'><input type=hidden name='" + FILTER_NAME_REPORTED_WITH_KEY + "'></form>" );
			
			renderFooter( outputWriter );
			
			outputWriter.flush();
			
		} catch ( final SQLException se ) {
			se.printStackTrace();
			sendBackErrorMessage( response, "SQL error: " + se.getMessage() );
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
	 * Generates and returns an HTML link to the records of a hacker search by key.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code> and an up arrow indicating
	 * that the result will open in a new window.
	 * The link on click will store the key value to a form element and submit the form.
	 * @param key value of the key
	 * @param text text to appear in the link
	 * @param formId id of the form element
	 * @return an HTML link to the records of a hacker search by key
	 */
	private static String getHackerRecordsByKeyLink( final String key, final String text, final String formId ) {
		return "<a href='#' onclick=\"javascript:document.forms['" + formId + "']." + FILTER_NAME_REPORTED_WITH_KEY + ".value='" + key + "';document.forms['" + formId + "'].submit();\">" + text + "</a>";
	}
	
	/**
	 * Handles adding new keys.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleNewKey( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		ResultSet         resultSet    = null;
		PrintWriter       outputWriter = null;
		
		try {
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>New key</h3>" );
			
			connection = dataSource.getConnection();
			
		} catch ( final SQLException se ) {
			se.printStackTrace();
			sendBackErrorMessage( response, "SQL error: " + se.getMessage() );
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
	 * Encodes a password with SHA-256 algorithm and returns the hex string of the result.
	 * 
	 * @param password password to be encoded
	 * @return the hex string of the encoded password 
	 */
	private static final String encodePassword( final String password ) {
		// Obtain a message digester (recommended to store it for further use):
	    MessageDigest messageDigest = null;
	    try {
	        messageDigest = MessageDigest.getInstance( "SHA-256" );
	    }
	    catch ( final NoSuchAlgorithmException nsae ) {
	        throw new RuntimeException( "This should never happen!" );
	    }
	    
	    // Calculate digest bytes:
	    final byte[] messageBytes = messageDigest.digest( password.getBytes() );
	    
	    // Convert it to hex string:
	    final StringBuilder sb = new StringBuilder( messageBytes.length * 2 );
	    for ( int i = 0; i < messageBytes.length; i++ )
	        sb.append( Integer.toHexString( ( messageBytes[ i ] & 0xff ) >> 4 ) ).append( Integer.toHexString( messageBytes[ i ] & 0x0f ) );
	    
	    // Here sb contains the hex string of the digest of the message		
		return sb.toString();
	}
	
	/**
	 * Renders the header for the output pages.
	 * @param outputWriter writer to be used to render
	 */
	private static void renderHeader( final HttpServletRequest request, final PrintWriter outputWriter ) {
		outputWriter.println( "<html><head>" );
		outputWriter.println( COMMON_HTML_HEADER_ELEMENTS );
		outputWriter.println( "<title>BWHF Admin Page</title>" );
		outputWriter.println( "</head><body><center>" );
		outputWriter.println( "<h2>BWHF Admin Page</h2>" );
		if ( request.getSession( false ) != null )
			outputWriter.println( ADMIN_PAGE_MENU_HTML );
	}
	
	/**
	 * Renders the footer for the output pages.
	 * @param outputWriter writer to be used to render
	 */
	private static void renderFooter( final PrintWriter outputWriter ) {
		outputWriter.println( "<p align=right><i>&copy; Andr&aacute;s Belicza, 2008-2009</i></p></center>" );
		outputWriter.println( GOOGLE_ANALYTICS_TRACKING_CODE );
		outputWriter.println( "</body></html>" );
	}
	
}
