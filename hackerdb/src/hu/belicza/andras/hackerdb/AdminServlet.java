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
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

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
	
	/** Operation request param.             */
	private static final String REQUEST_PARAM_OPERATION      = "op";
	/** User name request param.             */
	private static final String REQUEST_PARAM_USER_NAME      = "userName";
	/** Password request param.              */
	private static final String REQUEST_PARAM_PASSWORD       = "password";
	/** Hacker name request param.           */
	private static final String REQUEST_PARAM_HACKER_NAME    = "hackername";
	/** Last reports limit request param.    */
	private static final String REQUEST_PARAM_LASTREPS_LIMIT = "lastrepslimit";
	/** Revocate report id request param.    */
	private static final String REQUEST_PARAM_REVOCATE_ID    = "revocate_id";
	/** Reinstate report id request param.   */
	private static final String REQUEST_PARAM_REINSTATE_ID   = "reinstate_id";
	/** Number of keys request param.        */
	private static final String REQUEST_PARAM_NUMBER_OF_KEYS = "numberofkeys";
	/** Name of hte person request param.    */
	private static final String REQUEST_PARAM_PERSON_NAME    = "personname";
	/** Email of the person request param.   */
	private static final String REQUEST_PARAM_PERSON_EMAIL   = "personemail";
	/** Comment to the person request param. */
	private static final String REQUEST_PARAM_PERSON_COMMENT = "personcomment";
	
	/** Last reports operation.   */
	private static final String OPERATION_LAST_REPS      = "lastreps";
	/** New key operation.        */
	private static final String OPERATION_NEW_KEY        = "newkey";
	/** Reporters stat operation. */
	private static final String OPERATION_REPORTERS_STAT = "reportersstat";
	/** Logout operation.         */
	private static final String OPERATION_LOGOUT         = "logout";
	
	/** Default operation for the logged in users. */
	private static final String DEFAULT_OPERATION = OPERATION_LAST_REPS;
	
	/** Time format to format report times. */
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.SSS " );
	
	/** Hacker list menu HTML code to be sent. */
	private static final String ADMIN_PAGE_MENU_HTML = "<p><a href='admin?" + REQUEST_PARAM_OPERATION + "=" + OPERATION_LAST_REPS + "'>Last reports</a>"
			   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='admin?" + REQUEST_PARAM_OPERATION + '=' + OPERATION_NEW_KEY + "'>New key</a>"
			   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='admin?" + REQUEST_PARAM_OPERATION + '=' + OPERATION_REPORTERS_STAT + "'>Reporters stat</a>"
			   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='admin?" + REQUEST_PARAM_OPERATION + '=' + OPERATION_LOGOUT + "'>Logout</a></p>";
	
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
		else {
			String operation = request.getParameter( REQUEST_PARAM_OPERATION );
			
			if ( operation == null )
				operation = OPERATION_LAST_REPS;
			
			if ( operation.equals( OPERATION_LAST_REPS ) ) {
				
				handleLastReports( request, response );
				
			} else if ( operation.equals( OPERATION_NEW_KEY ) ) {
				
				handleNewKey( request, response );
				
			} else if ( operation.equals( OPERATION_REPORTERS_STAT ) ) {
				
				handleReportersStat( request, response );
				
			} else if ( operation.equals( OPERATION_LOGOUT ) ) {
				
				if ( session != null )
					session.invalidate();
				request.getRequestDispatcher( "admin" ).forward( request, response );
				
			}
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
				outputWriter.println( "<h3>Login</h3>" );
				if ( message != null )
					renderMessage( message, true, outputWriter );
				
				outputWriter.println( "<form action='admin' method='POST'>" );
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
			
			final String revocateId  = getNullStringParamValue( request, REQUEST_PARAM_REVOCATE_ID  );
			final String reinstateId = getNullStringParamValue( request, REQUEST_PARAM_REINSTATE_ID );
			
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
						renderMessage( "Report has been " + ( revocateId == null ? "reinstated" : "revocated" ) + " successfully.", false, outputWriter );
					else
						renderMessage( "Failed to " + ( revocateId == null ? "reinstate" : "revocate" ) + " the report!", true, outputWriter );
					statement.close();
				}
			}
			
			final String hackerName = getNullStringParamValue( request, REQUEST_PARAM_HACKER_NAME );
			
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
						+ "<td>" + resultSet.getString( 1 ) + "<td align=right>" + reportId + "<td>" + encodeHtmlString( resultSet.getString( 3 ) ) 
						+ "<td align=right>" + getHackerRecordsByKeyLink( resultSet.getString( 13 ), Integer.toString( resultSet.getInt( 4 ) ), "keyreportsform" ) + "&uarr;<td align=right>" + resultSet.getInt( 5 )
						+ "<td>" + HackerDbServlet.getHackerRecordsByNameLink( resultSet.getString( 6 ) ) + "<td>" + GATEWAYS[ gateway ]
						+ "<td>" + ReplayHeader.GAME_ENGINE_SHORT_NAMES[ resultSet.getInt( 8 ) ] + "<td>" + TIME_FORMAT.format( resultSet.getTimestamp( 9 ) )
						+ "<td align=right>" + resultSet.getString( 10 ) + "<td align=right>" + ( resultSet.getObject( 11 ) == null ? "N/A" : PlayersNetworkServlet.getGameDetailsHtmlLink( resultSet.getInt( 11 ), Integer.toString( resultSet.getInt( 11 ) ) ) ) + "<td>" + ( revocated ? "Yes" : "No" ) );
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
	private synchronized void handleNewKey( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		PrintWriter       outputWriter = null;
		
		try {
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>New key</h3>" );
			
			final Integer numberOfKeys  = getIntegerParamValue   ( request, REQUEST_PARAM_NUMBER_OF_KEYS );
			final String  personName    = getNullStringParamValue( request, REQUEST_PARAM_PERSON_NAME    );
			final String  personEmail   = getNullStringParamValue( request, REQUEST_PARAM_PERSON_EMAIL   );
			final String  personComment = getNullStringParamValue( request, REQUEST_PARAM_PERSON_COMMENT );
			
			StringBuilder emailMessageBuilder = null;
			if ( numberOfKeys != null || personName != null || personEmail != null || personComment != null ) {
				if ( numberOfKeys == null || personName == null || personEmail == null || personComment == null ) {
					renderMessage( "All fields are required!", true, outputWriter );
				}
				else {
					final int    PASSWORD_LENGTH = 20;
					final char[] PASSWORD_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
					// Generate keys
					final Random          random      = new Random( System.nanoTime() + Runtime.getRuntime().freeMemory() );
					final StringBuilder[] keyBuilders = new StringBuilder[ numberOfKeys ];
					
					for ( int i = 0; i < keyBuilders.length; i++ ) {
						final StringBuilder keyBuilder = keyBuilders[ i ] = new StringBuilder();
						for ( int j = 0; j < PASSWORD_LENGTH; j++ )
							keyBuilder.append( PASSWORD_CHARSET[ random.nextInt( PASSWORD_CHARSET.length ) ] );
					}
					
					connection = dataSource.getConnection();
					statement  = connection.prepareStatement( "INSERT INTO person (name,email,comment) VALUES (?,?,?)" );
					statement.setString( 1, personName    );
					statement.setString( 2, personEmail   );
					statement.setString( 3, personComment );
					final boolean personAdded = statement.executeUpdate() == 1;
					statement.close();
					if ( personAdded ) {
						int keysAdded = 0;
						statement = connection.prepareStatement( "INSERT INTO key (value,person) VALUES (?,(SELECT MAX(id) FROM person))" );
						for ( final StringBuilder keyBuilder : keyBuilders ) {
							statement.setString( 1, keyBuilder.toString() );
							if ( statement.executeUpdate() == 1 )
								keysAdded++;
						}
						statement.close();
						if ( keysAdded == keyBuilders.length )
							renderMessage( "Added person and " + keysAdded + ( keysAdded == 1 ? " key." : " keys." ), false, outputWriter );
						else
							renderMessage( "Added person and " + keysAdded + ( keysAdded == 1 ? " key" : " keys" ) + ", failed to add " + ( keyBuilders.length - keysAdded ) + ( keyBuilders.length - keysAdded == 1 ? " key!" : " keys!" ), true, outputWriter );
						
						// Build the email text
						emailMessageBuilder = new StringBuilder();
						emailMessageBuilder.append( personEmail ).append( "\nBWHF authorization key" ).append( keyBuilders.length == 1 ? "" : "s" )
							.append( "\n\n\nHi " ).append( personName ).append( "!\n\n" )
							.append( keyBuilders.length == 1 ? "This is your BWHF authorization key:" : "Here are your BWHF authorization keys:" );
						for ( final StringBuilder keyBuilder : keyBuilders )
							emailMessageBuilder.append( '\n' ).append( keyBuilder );
						emailMessageBuilder.append( "\n\n" );
						if ( keyBuilders.length == 1 ) {
							emailMessageBuilder.append( "It's your own, don't give it to anyone. After you enter your key, check \"report hackers\" and you're good to go." );
						}
						else {
							emailMessageBuilder.append( "Plz make sure that one key is only used by one person (a unique key should be given to each person)." );
						}
						emailMessageBuilder.append( "\n\nIf you have old hacker replays (only from 2009), send them and I add them in your name (with your key). Just reply to this email. Don't forget to indicate the gateway for the replays!" );
						emailMessageBuilder.append( "\n\nCheers,\n    Dakota_Fanning" );
					}
					else
						renderMessage( "Could not insert person!", true, outputWriter );
				}
			}
			
			outputWriter.println( "<form action='admin?" + REQUEST_PARAM_OPERATION + '=' + OPERATION_NEW_KEY + "' method=POST>" );
			outputWriter.println( "<table border=0>" );
			outputWriter.println( "<tr><td align=right>Number of keys*:<td><input type=text name='" + REQUEST_PARAM_NUMBER_OF_KEYS + "' value='1'>" );
			outputWriter.println( "<tr><td align=right>Name of the person*:<td><input type=text name='" + REQUEST_PARAM_PERSON_NAME + "'>" );
			outputWriter.println( "<tr><td align=right>E-mail of the person*:<td><input type=text name='" + REQUEST_PARAM_PERSON_EMAIL + "'>" );
			outputWriter.println( "<tr><td align=right>Comment to the person*:<td><input type=text name='" + REQUEST_PARAM_PERSON_COMMENT + "'>" );
			outputWriter.println( "<tr><td colspan=2 align=center><input type=submit value='Generate and Add'>" );
			outputWriter.println( "</table>" );
			outputWriter.println( "</form>" );
			
			if ( emailMessageBuilder != null ) {
				outputWriter.println( "<p>E-mail to send:</p>" );
				outputWriter.println( "<textarea rows=15 cols=80 readonly>" );
				outputWriter.println( emailMessageBuilder );
				outputWriter.println( "</textarea>" );
			}
			
			renderFooter( outputWriter );
			
			outputWriter.flush();
			
		} catch ( final SQLException se ) {
			se.printStackTrace();
			sendBackErrorMessage( response, "SQL error: " + se.getMessage() );
		}
		finally {
			if ( statement != null )
				try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null )
				try { connection.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Serves the reporters statistics.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleReportersStat( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection  connection   = null;
		Statement   statement    = null;
		ResultSet   resultSet    = null;
		PrintWriter outputWriter = null;
		
		try {
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>Reporters statistics</h3>" );
			
			connection = dataSource.getConnection();
			
			outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
			outputWriter.println( "<tr class=" + TABLE_HEADER_STYLE_NAME + "><th>#<th>Key<th>Key owner<th>Reports<br>sent<th>Hackers<br>caught<th>Revocated<br>count<th>Avg daily<br>reports<th>First report<th>Last report" );
			statement = connection.createStatement();
			resultSet = statement.executeQuery( "SELECT key.id, key.value, person.name, COUNT(report.id), COUNT(DISTINCT hacker.id), COUNT(CASE WHEN report.revocated=TRUE OR key.revocated=TRUE THEN 1 END), COUNT(report.id)/(1.000+CAST(CAST(MAX(report.version) AS DATE)-CAST(MIN(report.version) AS DATE) AS bigint)), MIN(report.version), MAX(report.version) FROM report JOIN key on report.key=key.id JOIN person on key.person=person.id JOIN hacker on report.hacker=hacker.id GROUP BY key.id, key.value, person.name ORDER BY COUNT(report.id) DESC;" );
			
			int rowCounter = 0;
			while ( resultSet.next() ) {
				outputWriter.println( "<tr><td align=right>" + (++rowCounter)
						+ "<td align=right>" + getHackerRecordsByKeyLink( resultSet.getString( 2 ), Integer.toString( resultSet.getInt( 1 ) ), "keyreportsform" ) + "&uarr;<td>" + encodeHtmlString( resultSet.getString( 3 ) )
						+ "<td align=right>" + resultSet.getInt( 4 ) + "<td align=right>" + resultSet.getInt( 5 ) + "<td align=right>" + resultSet.getInt( 6 )
						+ "<td align=right>" + String.format( "%.3f", resultSet.getFloat( 7 ) ) 
						+ "<td>" + TIME_FORMAT.format( resultSet.getTimestamp( 8 ) ) + "<td>" + TIME_FORMAT.format( resultSet.getTimestamp( 9 ) ) );
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
	 * Renders a message.
	 * @param message message to be rendered
	 * @param error tells if the message is an error message
	 * @param outputWriter output writer to be used to render
	 */
	private static void renderMessage( final String message, final boolean error, final PrintWriter outputWriter ) {
		outputWriter.println( "<p style='color:" + ( error ? "red" : "green" ) + "'>" + message + "</p>" );
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
