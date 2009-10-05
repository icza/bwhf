package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_AKA;
import static hu.belicza.andras.hackerdb.ServerApiConsts.FILTER_NAME_REPORTED_WITH_KEY;
import static hu.belicza.andras.hackerdb.ServerApiConsts.OPERATION_LIST;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_LIST;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_OPERATION;
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
	
	/**
	 * Pages of the admin modul.
	 * @author Andras Belicza
	 */
	private static enum Page {
		LAST_REPORTS   ( 0x01, "Last reports"        ),
		NEW_KEY        ( 0x02, "New key"             ),
		MISC_STAT      ( 0x04, "Miscellaneous stats" ),
		REPORTERS_STAT ( 0x08, "Reporters stats"     ),
		MANAGE_AKAS    ( 0x10, "Manage AKAs"         ),
		CHANGE_PASSWORD( 0x40, "Change password"     ),
		LOGOUT         ( 0x20, "Logout"              );
		
		public final int    pageId;
		public final String displayName;
		private Page( final int pageId, final String displayName ) {
			this.pageId      = pageId;
			this.displayName = displayName;
		}
	}
	
	/** User name session attribute.   */
	private static final String ATTRIBUTE_USER_NAME   = "userName";
	/** User id session attribute.     */
	private static final String ATTRIBUTE_USER_ID     = "userId";
	/** Page access session attribute. */
	private static final String ATTRIBUTE_PAGE_ACCESS = "pageAccess";
	/** Menu HTML session attribute.   */
	private static final String ATTRIBUTE_MENU_HTML   = "menuHtml";
	/** Full admin session attribute.  */
	private static final String ATTRIBUTE_FULL_ADMIN  = "fullAdmin";
	
	/** Page name request param.                   */
	private static final String REQUEST_PARAM_PAGE_NAME         = "pn";
	/** User name request param.                   */
	private static final String REQUEST_PARAM_USER_NAME         = "userName";
	/** Password request param.                    */
	private static final String REQUEST_PARAM_PASSWORD          = "password";
	/** Key id request param.                 */
	private static final String REQUEST_PARAM_KEY_ID            = "keyid";
	/** Hacker name request param.                 */
	private static final String REQUEST_PARAM_HACKER_NAME       = "hackername";
	/** Last reports limit request param.          */
	private static final String REQUEST_PARAM_LASTREPS_LIMIT    = "lastrepslimit";
	/** Revocate report id request param.          */
	private static final String REQUEST_PARAM_REVOCATE_ID       = "revocate_id";
	/** Reinstate report id request param.         */
	private static final String REQUEST_PARAM_REINSTATE_ID      = "reinstate_id";
	/** Change gateway of report id request param. */
	private static final String REQUEST_PARAM_CHANGE_GATEWAY_ID = "changegw_id";
	/** New gateway request param.                 */
	private static final String REQUEST_PARAM_NEW_GATEWAY       = "new_gw";
	/** Number of keys request param.              */
	private static final String REQUEST_PARAM_NUMBER_OF_KEYS    = "numberofkeys";
	/** Name of hte person request param.          */
	private static final String REQUEST_PARAM_PERSON_NAME       = "personname";
	/** Email of the person request param.         */
	private static final String REQUEST_PARAM_PERSON_EMAIL      = "personemail";
	/** Comment to the person request param.       */
	private static final String REQUEST_PARAM_PERSON_COMMENT    = "personcomment";
	/** Aka group name request param.              */
	private static final String REQUEST_PARAM_AKA_GROUP_NAME    = "akagroupname";
	/** Player name request param.                 */
	private static final String REQUEST_PARAM_PLAYER_NAME       = "playername";
	/** AKA managing action request param.         */
	private static final String REQUEST_PARAM_AKA_ACTION        = "akaaction";
	/** Password again request param.              */
	private static final String REQUEST_PARAM_PASSWORD_AGAIN    = "passwordagain";
	
	/** Time format to format report times. */
	private static final DateFormat TIME_FORMAT         = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
	
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
			final Page page = getRequestedPage( request );
			
			if ( page == null ) {
				final PrintWriter outputWriter = response.getWriter();
				renderHeader( request, outputWriter );
				renderMessage( "You do not have access to that page!", true, outputWriter );
				renderFooter( outputWriter );
				return;
			}
			
			switch ( page ) {
			case LAST_REPORTS :
				handleLastReports( request, response );
				break;
			case NEW_KEY :
				handleNewKey( request, response );
				break;
			case MISC_STAT :
				handleMiscStat( request, response );
				break;
			case REPORTERS_STAT :
				handleReportersStat( request, response );
				break;
			case MANAGE_AKAS :
				handleAkas( request, response );
				break;
			case CHANGE_PASSWORD :
				handleChangePassword( request, response );
				break;
			case LOGOUT :
				if ( session != null )
					session.invalidate();
				request.getRequestDispatcher( "admin" ).forward( request, response );
				break;
			}
		}
	}
	
	/**
	 * Returns the requested page.
	 * @param request the http request
	 * @return the requested page parsed from the request; or <null> if the user has no access to the requested page
	 */
	private static Page getRequestedPage( final HttpServletRequest request ) {
		String            pageName   = request.getParameter( REQUEST_PARAM_PAGE_NAME );
		final HttpSession session    = request.getSession( false );
		final int         pageAccess = (Integer) session.getAttribute( ATTRIBUTE_PAGE_ACCESS );
		
		if ( pageName == null ) {
			for ( final Page page : Page.values() )
				if ( ( pageAccess & page.pageId ) != 0 ) {
					pageName = page.name();
					break;
				}
		}
		
		if ( pageName == null )
			return null;
		
		final Page page = Page.valueOf( pageName );
		if ( ( pageAccess & page.pageId ) == 0 )
			return null;
		
		return page;
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
		
		final String userName = getNullStringParamValue( request, REQUEST_PARAM_USER_NAME );
		final String password = getNullStringParamValue( request, REQUEST_PARAM_PASSWORD  );
		
		try {
			String message = null;
			if ( userName != null && password != null ) {
				connection = dataSource.getConnection();
				statement  = connection.prepareStatement( "SELECT id, page_access FROM person WHERE name=? AND password=?" );
				statement.setString( 1, userName );
				statement.setString( 2, encodePassword( password ) );
				
				resultSet = statement.executeQuery();
				final boolean success = resultSet.next();
				if ( success ) {
					final HttpSession session = request.getSession();
					session.setAttribute( ATTRIBUTE_USER_NAME  , userName              );
					session.setAttribute( ATTRIBUTE_USER_ID    , resultSet.getInt( 1 ) );
					session.setAttribute( ATTRIBUTE_PAGE_ACCESS, resultSet.getInt( 2 ) );
					session.setAttribute( ATTRIBUTE_MENU_HTML  , getMenuHtml( resultSet.getInt( 2 ), userName ) );
					session.setAttribute( ATTRIBUTE_FULL_ADMIN , resultSet.getInt( 1 ) == 0 );
				}
				else
					message = "Invalid user name or password!";
				resultSet.close();
				statement.close();
				
				// Login log
				statement  = connection.prepareStatement( "INSERT INTO login_log (ip,name,success) VALUES (?,?,?)" );
				statement.setString ( 1, request.getRemoteAddr() );
				statement.setString ( 2, userName                );
				statement.setBoolean( 3, success                 );
				statement.executeUpdate();
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
			}
			else
				request.getRequestDispatcher( "admin" ).forward( request, response );
			
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
			final boolean fullAdmin = (Boolean) request.getSession( false ).getAttribute( ATTRIBUTE_FULL_ADMIN );
			
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>" + Page.LAST_REPORTS.displayName + ( fullAdmin ? "" : " <i>(limited access)</i>" ) + "</h3>" );
			
			connection = dataSource.getConnection();
			
			final Integer revocateId      = getIntegerParamValue( request, REQUEST_PARAM_REVOCATE_ID       );
			final Integer reinstateId     = getIntegerParamValue( request, REQUEST_PARAM_REINSTATE_ID      );
			final Integer changeGatewayId = getIntegerParamValue( request, REQUEST_PARAM_CHANGE_GATEWAY_ID );
			final Integer newGateway      = getIntegerParamValue( request, REQUEST_PARAM_NEW_GATEWAY       );
			
			if ( revocateId != null || reinstateId != null ) {
				synchronized ( Page.LAST_REPORTS ) {
					statement = connection.prepareStatement( "UPDATE report set revocated=" + ( revocateId == null ? "false" : "true" ) + ", changed_by=" + request.getSession( false ).getAttribute( ATTRIBUTE_USER_ID ) + " WHERE id=" + ( revocateId != null ? revocateId : reinstateId ) );
					if ( statement.executeUpdate() == 1 )
						renderMessage( "Report has been " + ( revocateId == null ? "reinstated" : "revocated" ) + " successfully.", false, outputWriter );
					else
						renderMessage( "Failed to " + ( revocateId == null ? "reinstate" : "revocate" ) + " the report!", true, outputWriter );
					statement.close();
				}
			}
			
			if ( changeGatewayId != null && newGateway != null ) {
				// Change gateway of report:
				// -if hacker has only 1 report, and no hacker with new gateway, then simply change gateway of hacker
				// -else if there is hacker with new gateway then simply change hacker reference in report and if the "old" hacker has no reports, remove it
				// -else just insert a new hacker, and update the report
				synchronized ( HackerDbServlet.class ) {
					
					statement = connection.prepareStatement( "SELECT hacker.id, hacker.name, hacker.gateway FROM report JOIN hacker on report.hacker=hacker.id WHERE report.id=" + changeGatewayId );
					resultSet = statement.executeQuery();
					Integer hackerId      = null;
					String  hackerName    = null;
					Integer hackerGateway = null;
					if ( resultSet.next() ) {
						hackerId      = resultSet.getInt   ( 1 );
						hackerName    = resultSet.getString( 2 );
						hackerGateway = resultSet.getInt   ( 3 );
					}
					resultSet.close();
					statement.close();
					
					if ( hackerGateway != null && !hackerGateway.equals( newGateway ) ) {
						statement = connection.prepareStatement( "SELECT COUNT(*) FROM report WHERE hacker=" + hackerId );
						resultSet = statement.executeQuery();
						Integer hackerReportsCount = null;
						if ( resultSet.next() )
							hackerReportsCount = resultSet.getInt( 1 );
						resultSet.close();
						statement.close();
						
						statement = connection.prepareStatement( "SELECT id FROM hacker WHERE name=? AND gateway=" + newGateway );
						statement.setString( 1, hackerName );
						resultSet = statement.executeQuery();
						Integer hackerIdWithNewGateway = null;
						if ( resultSet.next() )
							hackerIdWithNewGateway = resultSet.getInt( 1 );
						resultSet.close();
						statement.close();
						
						boolean changeSucceeded = false;
						if ( hackerReportsCount == 1 && hackerIdWithNewGateway == null ) {
							statement = connection.prepareStatement( "UPDATE hacker SET gateway=" + newGateway + " WHERE id=" + hackerId );
							changeSucceeded = statement.executeUpdate() == 1;
							statement.close();
							statement = connection.prepareStatement( "UPDATE report SET changed_by=" + request.getSession( false ).getAttribute( ATTRIBUTE_USER_ID ) + " WHERE id=" + changeGatewayId );
							statement.executeUpdate();
						}
						else if ( hackerIdWithNewGateway != null ) {
							statement = connection.prepareStatement( "UPDATE report SET hacker=" + hackerIdWithNewGateway + ", changed_by=" + request.getSession( false ).getAttribute( ATTRIBUTE_USER_ID ) + " WHERE id=" + changeGatewayId );
							changeSucceeded = statement.executeUpdate() == 1;
							statement.close();
							if ( hackerReportsCount == 1 ) {
								// Now it has 0 reports, remove it
								statement = connection.prepareStatement( "DELETE FROM hacker WHERE id=" + hackerId );
								if ( statement.executeUpdate() != 1 )
									getServletContext().log( "Failed to delete hacker with id: " + hackerId + "!" );
								statement.close();
							}
						}
						else {
							statement = connection.prepareStatement( "INSERT INTO hacker (name,gateway) VALUES (?," + newGateway + ")" );
							statement.setString( 1, hackerName );
							final boolean newHackerInserted = statement.executeUpdate() == 1;
							statement.close();
							
							if ( newHackerInserted ) {
								statement = connection.prepareStatement( "UPDATE report SET hacker=(SELECT id FROM hacker WHERE name=? AND gateway=" + newGateway + "), changed_by=" + request.getSession( false ).getAttribute( ATTRIBUTE_USER_ID ) + " WHERE id=" + changeGatewayId );
								statement.setString( 1, hackerName );
								changeSucceeded = statement.executeUpdate() == 1;
								statement.close();
							}
						}
						
						if ( changeSucceeded )
							renderMessage( "Successfully changed the gateway of the report.", false, outputWriter );
						else
							renderMessage( "Failed to changed the gateway of the report!", true, outputWriter );
					}
					else
						renderMessage( "The old and the new gateways are the same!", true, outputWriter );
				}
			}
			
			final Integer keyId = getIntegerParamValue( request, REQUEST_PARAM_KEY_ID );
			String hackerName = getNullStringParamValue( request, REQUEST_PARAM_HACKER_NAME );
			if ( hackerName != null )
				hackerName = hackerName.toLowerCase();
			
			Integer lastRepsLimit = getIntegerParamValue( request, REQUEST_PARAM_LASTREPS_LIMIT );
			if ( lastRepsLimit == null )
				lastRepsLimit = 23;
			
			outputWriter.println( "<form id='keyreportsform' action='hackers?" + REQUEST_PARAMETER_NAME_OPERATION + '=' + OPERATION_LIST + "' method=POST target='_blank'><input type=hidden name='" + FILTER_NAME_REPORTED_WITH_KEY + "'></form>" );
			
			outputWriter.println( "<form id='lastReportsFormId' action='admin?" + REQUEST_PARAM_PAGE_NAME + "=" + Page.LAST_REPORTS.name()
					+ "' method=POST><p>Key id:<input type=text name='" + REQUEST_PARAM_KEY_ID + "' value='" + ( keyId == null ? "" : keyId.toString() )+ "' size=1>&nbsp;&nbsp;|&nbsp;&nbsp;Hacker name:<input type=text name='" + REQUEST_PARAM_HACKER_NAME + "'"
					+ ( hackerName != null ? " value='" + encodeHtmlString( hackerName ) + "'" : "" ) + ">&nbsp;&nbsp;|&nbsp;&nbsp;Limit:<input type=text name='" + REQUEST_PARAM_LASTREPS_LIMIT + "'" 
					+ " value='" + lastRepsLimit + "' size=1>&nbsp;&nbsp;|&nbsp;&nbsp;<input type=submit value='Apply'></p>" );
			
			String query = "SELECT report.id, reporter.name, key.id, hacker.id, hacker.name, hacker.gateway, game_engine, report.version, substr(report.agent_version,1,4), game.id, report.revocated"
					+ ( fullAdmin ? ", key.value, report.ip, changer.name" : "" ) + " FROM report JOIN key on report.key=key.id JOIN person as reporter on key.person=reporter.id JOIN hacker on report.hacker=hacker.id LEFT OUTER JOIN game on report.replay_md5=game.replay_md5"
					+ ( fullAdmin ? " LEFT OUTER JOIN person as changer on report.changed_by=changer.id" : "" );
			if ( keyId != null )
				query += " WHERE key.id=" + keyId;
			if ( hackerName != null )
				query += ( keyId == null ? " WHERE " : " AND " ) + "hacker.name LIKE ?";
			query += " ORDER BY report.id DESC LIMIT " + lastRepsLimit;
			
			statement = connection.prepareStatement( query ); 
			if ( hackerName != null )
				statement.setString( 1, "%" + hackerName + "%" );
			
			outputWriter.println( "<input type=hidden name='" + REQUEST_PARAM_REVOCATE_ID       + "'>" );
			outputWriter.println( "<input type=hidden name='" + REQUEST_PARAM_REINSTATE_ID      + "'>" );
			outputWriter.println( "<input type=hidden name='" + REQUEST_PARAM_CHANGE_GATEWAY_ID + "'>" );
			outputWriter.println( "<input type=hidden name='" + REQUEST_PARAM_NEW_GATEWAY       + "'>" );
			
			outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
			outputWriter.println( "<tr class=" + TABLE_HEADER_STYLE_NAME + "><th>#" + ( fullAdmin ? "<th>IP" : "" ) + "<th>Report<th>Reporter<th>Key<th>Hacker<th>Hacker name<th>Gateway<th>Engine<th>Date<th>Agver<th>Game id<th>Revocated" + ( fullAdmin ? "<th>Changed by" : "" ) );
			resultSet = statement.executeQuery();
			int rowCounter = 0;
			while ( resultSet.next() ) {
				final int     reportId  = resultSet.getInt( 1 );
				final int     gateway   = resultSet.getInt( 6 );
				final boolean revocated = resultSet.getBoolean( 11 );
				
				outputWriter.println( "<tr class=" + ( gateway < GATEWAY_STYLE_NAMES.length ? GATEWAY_STYLE_NAMES[ gateway ] : UNKNOWN_GATEWAY_STYLE_NAME ) + "><td align=right>" + (++rowCounter)
						+ ( fullAdmin ? "<td>" + "<a href='http://www.geoiptool.com/en/?IP=" + resultSet.getString( 13 ) + "' target='_blank'>" + resultSet.getString( 13 ) + "&uarr;</a>" : "" ) + "<td align=right>" + reportId + "<td>" + encodeHtmlString( resultSet.getString( 2 ) ) 
						+ "<td align=right><a href=\"javascript:document.getElementsByName('" + REQUEST_PARAM_KEY_ID + "')[0].value='" + resultSet.getInt( 3 ) + "';document.forms['lastReportsFormId'].submit();\">" + resultSet.getInt( 3 ) + "</a> " + ( fullAdmin ? getHackerRecordsByKeyLink( resultSet.getString( 12 ), "", "keyreportsform" ) : "" ) + "<td align=right>" + resultSet.getInt( 4 )
						+ "<td>" + HackerDbServlet.getHackerRecordsByNameLink( resultSet.getString( 5 ) )
						+ "<td>" + getGatewayComboHtml( gateway, reportId )
						+ "<td>" + ReplayHeader.GAME_ENGINE_SHORT_NAMES[ resultSet.getInt( 7 ) ] + "<td>" + TIME_FORMAT.format( resultSet.getTimestamp( 8 ) )
						+ "<td align=right>" + resultSet.getString( 9 ) + "<td align=right>" + ( resultSet.getObject( 10 ) == null ? "N/A" : PlayersNetworkServlet.getGameDetailsHtmlLink( resultSet.getInt( 10 ), Integer.toString( resultSet.getInt( 10 ) ) ) ) + "<td>" + ( revocated ? "Yes" : "No" ) );
				outputWriter.println( revocated ? "<input type=submit value='Reinstate' onclick='javascript:this.form." + REQUEST_PARAM_REINSTATE_ID + ".value=\"" + reportId + "\";'>" : "<input type=submit value='Revocate' onclick='javascript:this.form." + REQUEST_PARAM_REVOCATE_ID + ".value=\"" + reportId + "\";'>" );
				if ( fullAdmin )
					outputWriter.println( "<td>" + ( resultSet.getString( 14 ) == null ? "&nbsp;" : encodeHtmlString( resultSet.getString( 14 ) ) ) );
			}
			resultSet.close();
			statement.close();
			outputWriter.println( "</table></form>" );
			
			renderFooter( outputWriter );
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
	 * Generates HTML code for a combo to display and change the gateway of a report.
	 * @param gateway  current gateway
	 * @param reportId id of the report
	 * @return HTML code for a combo to display and change the gateway of a report
	 */
	private static String getGatewayComboHtml( final int gateway, final int reportId ) {
		final StringBuilder htmlBuilder = new StringBuilder();
		
		htmlBuilder.append( "<select style='width:100%' id='crg" ).append( reportId )
			.append( "' onchange=\"javascript:if (confirm('Are you sure you want to change the gateway of the report?')){this.form." )
			.append( REQUEST_PARAM_CHANGE_GATEWAY_ID ).append( ".value='" ).append( reportId ).append( "';this.form." )
			.append( REQUEST_PARAM_NEW_GATEWAY ).append( ".value=document.getElementById('crg" ).append( reportId ).append( "').selectedIndex;this.form.submit();}\">" );
		
		for ( int i = 0; i < ServerApiConsts.GATEWAYS.length; i++ )
			htmlBuilder.append( "<option" ).append( i == gateway ? " selected='selected'>" : ">" ).append( ServerApiConsts.GATEWAYS[ i ] );
		htmlBuilder.append( "</select>" );
		
		return htmlBuilder.toString();
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
		return "<a href=\"javascript:document.forms['" + formId + "']." + FILTER_NAME_REPORTED_WITH_KEY + ".value='" + key + "';document.forms['" + formId + "'].submit();\">" + text + "&uarr;</a>";
	}
	
	/**
	 * Handles adding new keys.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleNewKey( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		PrintWriter       outputWriter = null;
		
		try {
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>" + Page.NEW_KEY.displayName + "</h3>" );
			
			final Integer numberOfKeys  = getIntegerParamValue   ( request, REQUEST_PARAM_NUMBER_OF_KEYS );
			final String  personName    = getNullStringParamValue( request, REQUEST_PARAM_PERSON_NAME    );
			final String  personEmail   = getNullStringParamValue( request, REQUEST_PARAM_PERSON_EMAIL   );
			final String  personComment = getNullStringParamValue( request, REQUEST_PARAM_PERSON_COMMENT );
			
			StringBuilder emailMessageBuilder = null;
			if ( numberOfKeys != null || personName != null || personEmail != null || personComment != null ) {
				if ( numberOfKeys == null || personName == null || personEmail == null || personComment == null ) {
					renderMessage( "All fields are required!", true, outputWriter );
				}
				else
					synchronized ( Page.NEW_KEY ) {
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
			
			outputWriter.println( "<form action='admin?" + REQUEST_PARAM_PAGE_NAME + '=' + Page.NEW_KEY.name() + "' method=POST>" );
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
	 * Serves the miscellaneous statistics.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleMiscStat( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection  connection   = null;
		Statement   statement    = null;
		ResultSet   resultSet    = null;
		PrintWriter outputWriter = null;
		
		try {
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>" + Page.MISC_STAT.displayName + "</h3>" );
			
			connection = dataSource.getConnection();
			
			outputWriter.println( "<table border=1>" );
			statement = connection.createStatement();
			
			resultSet = statement.executeQuery( "select ((select count(*) from game)+0.0)/(select count(*) from player)" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Games Per Players quota:<td align=right>" + String.format( "%.5f", resultSet.getFloat( 1 ) ) );
			}
			resultSet.close();
			
			resultSet = statement.executeQuery( "SELECT COUNT(*) FROM (SELECT ip FROM download_log WHERE version>='2009-04-20' GROUP BY ip HAVING COUNT(*)>=10) as foo" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Different IPs with at least 10 downloads:<td align=right>" + DECIMAL_FORMAT.format( resultSet.getInt( 1 ) ) );
			}
			resultSet.close();
			
			resultSet = statement.executeQuery( "SELECT COUNT(DISTINCT key.id) FROM key JOIN report on key.id=report.key WHERE key.revocated=FALSE AND report.version+interval '30 days'>now()" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Active keys (have reports in the last 30 days):<td align=right>" + DECIMAL_FORMAT.format( resultSet.getInt( 1 ) ) );
			}
			resultSet.close();
			
			resultSet = statement.executeQuery( "SELECT COUNT(*) FROM download_log WHERE version + interval '24 hours'>now()" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Hacker list downloads in the last 24 hours:<td align=right>" + DECIMAL_FORMAT.format( resultSet.getInt( 1 ) ) );
			}
			resultSet.close();
			
			resultSet = statement.executeQuery( "SELECT COUNT(*) FROM person" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Persons in database:<td align=right>" + DECIMAL_FORMAT.format( resultSet.getInt( 1 ) ) );
			}
			resultSet.close();
			
			resultSet = statement.executeQuery( "SELECT COUNT(*) FROM key WHERE key.revocated=FALSE" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Keys in database:<td align=right>" + DECIMAL_FORMAT.format( resultSet.getInt( 1 ) ) );
			}
			resultSet.close();
			
			resultSet = statement.executeQuery( "SELECT COUNT(*) FROM person WHERE password IS NOT NULL" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Persons with login:<td align=right>" + DECIMAL_FORMAT.format( resultSet.getInt( 1 ) ) );
			}
			resultSet.close();
			
			resultSet = statement.executeQuery( "SELECT COUNT(*) FROM player WHERE aka_group IS NOT NULL" );
			if ( resultSet.next() ) {
				outputWriter.println( "<tr><th align=left>Players belonging to AKA groups:<td align=right>" + DECIMAL_FORMAT.format( resultSet.getInt( 1 ) ) );
			}
			resultSet.close();
			
			statement.close();
			
			outputWriter.println( "</table>" );
			
			renderFooter( outputWriter );
			
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
			final boolean fullAdmin = (Boolean) request.getSession( false ).getAttribute( ATTRIBUTE_FULL_ADMIN );
			
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>" + Page.REPORTERS_STAT.displayName + ( fullAdmin ? "" : " <i>(limited access)</i>" ) + "</h3>" );
			
			connection = dataSource.getConnection();
			
			outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
			outputWriter.println( "<tr class=" + TABLE_HEADER_STYLE_NAME + "><th>#<th>Key<th>Key owner<th>Reports<br>sent<th>Hackers<br>caught<th>Revocated<br>count<th>Avg daily<br>reports<th>Active period<th>First report<th>Last report" );
			statement = connection.createStatement();
			resultSet = statement.executeQuery( "SELECT key.id, key.value, person.name, COUNT(report.id), COUNT(DISTINCT hacker.id), COUNT(CASE WHEN report.revocated=TRUE OR key.revocated=TRUE THEN 1 END), COUNT(report.id)/(1.000+CAST(CAST(MAX(report.version) AS DATE)-CAST(MIN(report.version) AS DATE) AS bigint)), MIN(report.version), MAX(report.version), 1 + date(MAX(report.version)) - date(MIN(report.version)) FROM report JOIN key on report.key=key.id JOIN person on key.person=person.id JOIN hacker on report.hacker=hacker.id GROUP BY key.id, key.value, person.name ORDER BY COUNT(report.id) DESC;" );
			
			int rowCounter = 0;
			while ( resultSet.next() ) {
				outputWriter.println( "<tr><td align=right>" + (++rowCounter)
						+ "<td align=right>" + ( fullAdmin ? getHackerRecordsByKeyLink( resultSet.getString( 2 ), Integer.toString( resultSet.getInt( 1 ) ), "keyreportsform" ) : resultSet.getInt( 1 ) ) + "<td>" + encodeHtmlString( resultSet.getString( 3 ) )
						+ "<td align=right>" + resultSet.getInt( 4 ) + "<td align=right>" + resultSet.getInt( 5 ) + "<td align=right>" + resultSet.getInt( 6 )
						+ "<td align=right>" + String.format( "%.3f", resultSet.getFloat( 7 ) )
						+ "<td align=center>" + formatDays( resultSet.getInt( 10 ) )
						+ "<td>" + TIME_FORMAT.format( resultSet.getTimestamp( 8 ) ) + "<td>" + TIME_FORMAT.format( resultSet.getTimestamp( 9 ) ) );
			}
			resultSet.close();
			statement.close();
			
			outputWriter.println( "</table></form>" );
			
			outputWriter.println( "<form id='keyreportsform' action='hackers?" + REQUEST_PARAMETER_NAME_OPERATION + '=' + OPERATION_LIST + "' method=POST target='_blank'><input type=hidden name='" + FILTER_NAME_REPORTED_WITH_KEY + "'></form>" );
			
			renderFooter( outputWriter );
			
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
	 * Handles the AKAs.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleAkas( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		ResultSet         resultSet    = null;
		PrintWriter       outputWriter = null;
		
		try {
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>" + Page.MANAGE_AKAS.displayName + "</h3>" );
			connection = dataSource.getConnection();
			
			final String ACTION_NEW_AKA_GROUP = "Create new AKA group";
			final String ACTION_NEW_AKA       = "Add new AKA";
			
			final String akaGroupName = getNullStringParamValue( request, REQUEST_PARAM_AKA_GROUP_NAME );
			String playerName = getNullStringParamValue( request, REQUEST_PARAM_PLAYER_NAME    );
			if ( playerName != null )
				playerName = playerName.toLowerCase();
			final String action = getNullStringParamValue( request, REQUEST_PARAM_AKA_ACTION     );
			
			if ( action != null ) {
				if ( akaGroupName == null )
					renderMessage( "AKA group has to be provided!", true, outputWriter );
				else 
					synchronized ( Page.MANAGE_AKAS ) {
						Integer akaGroupId = null;
						statement = connection.prepareStatement( "SELECT id FROM aka_group WHERE comment=?" );
						statement.setString( 1, akaGroupName );
						resultSet = statement.executeQuery();
						if ( resultSet.next() )
							akaGroupId = resultSet.getInt( 1 );
						resultSet.close();
						statement.close();
						
						if ( action.equals( ACTION_NEW_AKA_GROUP ) ) {
							if ( akaGroupId != null ) {
								renderMessage( "AKA group already exists!", true, outputWriter );
							}
							else {
								statement = connection.prepareStatement( "INSERT INTO aka_group (comment) VALUES (?) " );
								statement.setString( 1, akaGroupName );
								if ( statement.executeUpdate() == 1 )
									renderMessage( "Added 1 new AKA group.", false, outputWriter );
								else
									renderMessage( "Failed to add AKA group!", true, outputWriter );
								statement.close();
							}
						}
						else if ( action.equals( ACTION_NEW_AKA ) ) {
							if ( playerName == null )
								renderMessage( "Player name has to be provided!", true, outputWriter );
							else if ( akaGroupId == null )
								renderMessage( "AKA group does not exist!", true, outputWriter );
							else {
								statement = connection.prepareStatement( "UPDATE player SET aka_group=" + akaGroupId + " WHERE name=?" );
								statement.setString( 1, playerName );
								if ( statement.executeUpdate() == 1 )
									renderMessage( "Added 1 new AKA.", false, outputWriter );
								else
									renderMessage( "Failed to add AKA (player does not exists?)!", true, outputWriter );
								statement.close();
							}
						}
					}
			}
			
			outputWriter.println( "<form action='admin?" + REQUEST_PARAM_PAGE_NAME + '=' + Page.MANAGE_AKAS.name() + "' method=POST><table border=1>" );
			
			outputWriter.println( "<tr><td rowspan=2>AKA group/person name:<input type=text name='" + REQUEST_PARAM_AKA_GROUP_NAME + "'>" );
			outputWriter.println( "<td><input type=submit name='" + REQUEST_PARAM_AKA_ACTION + "' value='" + ACTION_NEW_AKA_GROUP + "'>" );
			
			outputWriter.println( "<tr><td>Player name:<input type=text name='" + REQUEST_PARAM_PLAYER_NAME + "'><input type=submit name='" + REQUEST_PARAM_AKA_ACTION + "' value='" + ACTION_NEW_AKA + "'>" );
			outputWriter.println( "</table></form>" );
			outputWriter.println( "<p>Jump to the Player's Network <a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_AKA + "'>AKA list</a>.</p>" );
			
			renderFooter( outputWriter );
			
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
	 * Handles changing password.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void handleChangePassword( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		PrintWriter       outputWriter = null;
		
		try {
			final boolean fullAdmin = (Boolean) request.getSession( false ).getAttribute( ATTRIBUTE_FULL_ADMIN );
			
			outputWriter = response.getWriter();
			renderHeader( request, outputWriter );
			outputWriter.println( "<h3>" + Page.CHANGE_PASSWORD.displayName + ( fullAdmin ? "" : " <i>(limited access)</i>" ) + "</h3>" );
			connection = dataSource.getConnection();
			
			final String userName      = fullAdmin ? getNullStringParamValue( request, REQUEST_PARAM_USER_NAME ) : null;
			final String password      = getNullStringParamValue( request, REQUEST_PARAM_PASSWORD       );
			final String passwordAgain = getNullStringParamValue( request, REQUEST_PARAM_PASSWORD_AGAIN );
			
			if ( password != null && passwordAgain != null ) {
				if ( !password.equals( passwordAgain ) )
					renderMessage( "Passwords do not match!", true, outputWriter );
				else 
					synchronized ( Page.CHANGE_PASSWORD ) {
						statement = connection.prepareStatement( "UPDATE person set password=? WHERE " + ( fullAdmin ? "name=?" : "id=" + request.getSession( false ).getAttribute( ATTRIBUTE_USER_ID ) ) );
						statement.setString( 1, encodePassword( password ) );
						if ( fullAdmin )
							statement.setString( 2, userName );
						if ( statement.executeUpdate() == 1 )
							renderMessage( "Password is changed.", false, outputWriter );
						else
							renderMessage( "Could not change password" + ( fullAdmin ? " (invalid user name?)!" : "!" ), true, outputWriter );
						statement.close();
					}
			}
			
			outputWriter.println( "<form action='admin?" + REQUEST_PARAM_PAGE_NAME + '=' + Page.CHANGE_PASSWORD.name() + "' method=POST><table border=0>" );
			if ( fullAdmin )
				outputWriter.println( "<tr><td>Change password for user:<td><input type=text name='" + REQUEST_PARAM_USER_NAME + "' value='" + encodeHtmlString( (String) request.getSession( false ).getAttribute( ATTRIBUTE_USER_NAME ) ) + "'>" );
			outputWriter.println( "<tr><td>New password:<td><input id='passwordFieldId' type=password name='" + REQUEST_PARAM_PASSWORD + "'>" );
			outputWriter.println( "<tr><td>New password again:<td><input type=password name='" + REQUEST_PARAM_PASSWORD_AGAIN + "'>" );
			outputWriter.println( "<tr><td colspan=2 align=center><input type=submit value='Change'></table></form>" );
			outputWriter.println( "<script>document.getElementById('passwordFieldId').focus();</script>" );
			
			renderFooter( outputWriter );
			
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
		outputWriter.println( getCurrentTimeCode() );
		outputWriter.println( "<h2>BWHF Admin Page</h2>" );
		final HttpSession session = request.getSession( false );
		if ( session != null )
			outputWriter.println( session.getAttribute( ATTRIBUTE_MENU_HTML ) );
		outputWriter.flush();
	}
	
	/**
	 * Returns the menu HTML for the current user.<br>
	 * The menu will only contain pages to which the user has access.
	 * @param pageAccess page access bitmask
	 * @param userName name of the user
	 * @return the menu HTML for the current user.
	 */
	private static String getMenuHtml( final int pageAccess, final String userName ) {
		final StringBuilder menuBuilder = new StringBuilder( "<p>" );
		
		boolean firstPage = true;
		for ( final Page page : Page.values() )
			if ( ( pageAccess & page.pageId ) != 0 ) {
				if ( firstPage )
					firstPage = false;
				else
					menuBuilder.append( "&nbsp;&nbsp;|&nbsp;&nbsp;" );
				menuBuilder.append( "<a href='admin?" ).append( REQUEST_PARAM_PAGE_NAME ).append( '=' ).append( page.name() )
					.append( "'>" ).append( page.displayName ).append( page == Page.LOGOUT ? " " + encodeHtmlString( userName ) : "" ).append( "</a>" );
			}
		
		return menuBuilder.toString();
	}
	
	/**
	 * Renders the footer for the output pages.
	 * @param outputWriter writer to be used to render
	 */
	private static void renderFooter( final PrintWriter outputWriter ) {
		outputWriter.println( "<p align=right><i>&copy; Andr&aacute;s Belicza, 2008-2009</i></td></center>" );
		outputWriter.println( GOOGLE_ANALYTICS_TRACKING_CODE );
		outputWriter.println( "</body></html>" );
		outputWriter.flush();
	}
	
}
