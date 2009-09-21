package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.OPERATION_CHECK;
import static hu.belicza.andras.hackerdb.ServerApiConsts.OPERATION_LIST;
import static hu.belicza.andras.hackerdb.ServerApiConsts.REQUEST_PARAMETER_NAME_OPERATION;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Admin servlet for basic administration tasks and reports.
 * 
 * @author Andras Belicza
 */
public class AdminServlet extends BaseServlet {
	
	/** The user name session attribute. */
	private static final String ATTRIBUTE_USER_NAME = "userName";
	
	/** Hacker list menu HTML code to be sent. */
	private static final String ADMIN_PAGE_MENU_HTML = "<p><a href='admin?op=lastrep'>Last reports</a>"
			   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='admin?op=newKey'>New key</a>"
			   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='admin?op=logout'>Logout</a></p>";
	
	@Override
	public void doGet( final HttpServletRequest request, final HttpServletResponse response ) {
		setNoCache( response );
		try {
			request.setCharacterEncoding( "UTF-8" );
		} catch ( final UnsupportedEncodingException uee ) {
			// This will never happen.
			throw new RuntimeException( "Unsupported UTF-8 encoding?" ); 
		}
		
		final HttpSession session = request.getSession( false );
		
		if ( session == null )
			renderLoginPage( request, response );
		else
			try {
				String operation = request.getParameter( REQUEST_PARAMETER_NAME_OPERATION );
				
				if ( operation == null )
					operation = OPERATION_LIST;
				
				if ( operation.equals( OPERATION_LIST ) ) {
					if ( false )
						throw new BadRequestException();
				} else if ( operation.equals( OPERATION_CHECK ) ) {
				}
			}
			catch ( final BadRequestException bre ) {
				sendBackErrorMessage( response );
			}
	}
	
	/**
	 * Renders the login page.
	 * @param request the http servlet request
	 * @param response the http servlet repsonse
	 */
	private void renderLoginPage( final HttpServletRequest request, final HttpServletResponse response ) {
		Connection        connection   = null;
		PreparedStatement statement    = null;
		ResultSet         resultSet    = null;
		PrintWriter       outputWriter = null;
		
		final String userName = request.getParameter( "userName" );
		final String password = request.getParameter( "password" );
		
		try {
			outputWriter = response.getWriter();
			String message = null;
			if ( userName != null && password != null ) {
				connection = dataSource.getConnection();
				statement  = connection.prepareStatement( "SELECT id FROM person WHERE name=? AND " );
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
				renderHeader( outputWriter );
				outputWriter.println( "<h2>Login page</h2>" );
				if ( message != null )
					outputWriter.println( "<p style='color:red'>" + message + "</p>" );
				
				renderFooter( outputWriter );
			}
			
			outputWriter.flush();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
			sendBackErrorMessage( response, "I/O error." + ie.getMessage() );
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
	private static void renderHeader( final PrintWriter outputWriter ) {
		outputWriter.println( "<html><head>" );
		outputWriter.println( COMMON_HTML_HEADER_ELEMENTS );
		outputWriter.println( "<title>BWHF Admin Page</title>" );
		outputWriter.println( "</head><body><center>" );
		outputWriter.println( "<h2>BWHF Admin Page</h2>" );
		outputWriter.println( ADMIN_PAGE_MENU_HTML );
	}
	
	/**
	 * Renders the footer for the output pages.
	 * @param outputWriter writer to be used to render
	 */
	private static void renderFooter( final PrintWriter outputWriter ) {
		outputWriter.println( "<p><i>&copy; Andr&aacute;s Belicza, 2008-2009</i></p></center>" );
		outputWriter.println( GOOGLE_ANALYTICS_TRACKING_CODE );
		outputWriter.println( "</body></html>" );
	}
	
}
