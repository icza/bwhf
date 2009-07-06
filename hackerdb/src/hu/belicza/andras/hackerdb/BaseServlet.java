package hu.belicza.andras.hackerdb;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * Base functionalities for the servlets.
 * 
 * @author Andras Belicza
 */
public class BaseServlet extends HttpServlet {
	
	/** Gateway colors.                                              */
	protected static final String[] GATEWAY_COLORS                = new String[] { "ff5050", "00ffff", "00ff00", "ffff00", "000000", "f080f0" };
	/** Gateway foreground colors.                                   */
	protected static final String[] GATEWAY_FOREGROUND_COLORS     = new String[] { "000000", "000000", "000000", "000000", "ffffff", "000000" };
	/** Style for unknown gateway.                                   */
	protected static final String   UNKNOWN_GATEWAY_STYLE         = "background:#ffffff;color:#000000";
	/** Names of the gateway styles.                                 */
	protected static final String[] GATEWAY_STYLE_NAMES;
	/** Style for unknown gateway.                                   */
	protected static final String   UNKNOWN_GATEWAY_STYLE_NAME    = "gatUn";
	/** Style for table headers.                                     */
	protected static final String   TABLE_HEADER_STYLE_NAME       = "ths";
	/** Style for sorting columns.                                   */
	protected static final String   SORTING_COLUMN_STYLE_NAME     = "sortCol";
	/** Style for sorting columns.                                   */
	protected static final String   NON_SORTING_COLUMN_STYLE_NAME = "nonSortCol";
	/** Common header HTML elements.                                 */
	protected static final String   COMMON_HTML_HEADER_ELEMENTS;
	static {
		final StringBuilder commonHtmlHeaderElementsBuilder = new StringBuilder( "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n" );
		commonHtmlHeaderElementsBuilder.append( "<link rel='shortcut icon' href='favicon.ico' type='image/x-icon'>\n" )
			.append( "<style>p,h2,h3 {margin:6;padding:0}\n." )
			.append( SORTING_COLUMN_STYLE_NAME ).append( " {cursor:pointer}\n." )
			.append( NON_SORTING_COLUMN_STYLE_NAME ).append( " {cursor:default}\n." )
			.append( TABLE_HEADER_STYLE_NAME ).append( " {background:#cccccc}\n" );
		
		GATEWAY_STYLE_NAMES = new String[ GATEWAY_COLORS.length ];
		for ( int i = 0; i < GATEWAY_COLORS.length; i++ ) {
			GATEWAY_STYLE_NAMES[ i ] = "gat" + i;
			commonHtmlHeaderElementsBuilder.append( '.' ).append( GATEWAY_STYLE_NAMES[ i ] )
				.append( " {background:#" ).append( GATEWAY_COLORS[ i ] )
				.append( ";color:#" ).append( GATEWAY_FOREGROUND_COLORS[ i ] ).append( "}\n" );
		}
		commonHtmlHeaderElementsBuilder.append( '.' ).append( UNKNOWN_GATEWAY_STYLE_NAME ).append( " {" ).append( UNKNOWN_GATEWAY_STYLE ).append( "}</style>" );
		
		COMMON_HTML_HEADER_ELEMENTS = commonHtmlHeaderElementsBuilder.toString();
	}
	
	/** URL of the database. */
	private static final String DATABASE_URL = "jdbc:hsqldb:hsql://localhost/hackers";
	
	/** Data source to provide pooled connections to the hacker database. */
	protected final BasicDataSource dataSource = new BasicDataSource();
	
	@Override
	public void init( final ServletConfig config ) throws ServletException {
		dataSource.setDriverClassName( "org.hsqldb.jdbcDriver" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );
		dataSource.setUrl( DATABASE_URL );
		
		super.init( config );
	}
	
	/**
	 * Sends back a message as plain text.
	 * @param message  message to be sent
	 * @param response response to be used
	 */
	protected static void sendBackPlainMessage( final String message, final HttpServletResponse response ) {
		response.setContentType( "text/plain" );
		
		PrintWriter output = null;
		try {
			output = response.getWriter();
			output.println( message );
			output.flush();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		finally {
			if ( output != null )
				 output.close();
		}
	}
	
	/**
	 * Sends back an error message to the client indicating a bad request.
	 * @param response response to be used
	 */
	protected static void sendBackErrorMessage( final HttpServletResponse response ) {
		sendBackErrorMessage( response, "Bad request!" );
	}
	
	/**
	 * Sends back an error message to the client.
	 * @param response response to be used
	 * @param message  message to be sent back
	 */
	protected static void sendBackErrorMessage( final HttpServletResponse response, final String message ) {
		response.setContentType( "text/html" );
		
		PrintWriter output = null;
		try {
			output = response.getWriter();
			output.println( message );
			output.flush();
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		finally {
			if ( output != null )
				 output.close();
		}
	}
	
	/**
	 * Configures the response for no caching.
	 * @param response response to be configured
	 */
	protected static void setNoCache( final HttpServletResponse response ) {
		response.setHeader( "Cache-Control", "no-cache" ); // For HTTP 1.1
		response.setHeader( "Pragma"       , "no-cache" ); // For HTTP 1.0
		response.setDateHeader( "Expires", -0 );           // For proxies
	}
	
	/**
	 * Encodes an input string for HTML rendering.
	 * @param input input string to be encoded
	 * @return an encoded string for HTML rendering
	 */
	protected static String encodeHtmlString( final String input ) {
		final StringBuilder encodedHtml = new StringBuilder();
		final int length = input.length();
		
		for ( int i = 0; i < length; i++ ) {
			final char ch = input.charAt( i );
			
			if ( ch >= 'a' && ch <='z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' )
				encodedHtml.append( ch ); // safe
			else if ( Character.isISOControl( ch ) )
				; // Not safe, do not include it in the output
			else
				encodedHtml.append( "&#" ).append( (int) ch ).append( ';' );
		}
		
		return encodedHtml.toString();
	}
	
	/**
	 * Returns the value of a request parameter.
	 * @param request   http request object
	 * @param paramName name of the parameter whose value is to be returned
	 * @return the value of a request parameter or an empty string if the parameter does not have a value
	 */
	protected static String getStringParamValue( final HttpServletRequest request, final String paramName ) {
		final String paramValue = request.getParameter( paramName );
		return paramValue == null ? "" : paramValue;
	}
	
	/**
	 * Returns the integer value parsed from a request parameter.
	 * @param request      http request object
	 * @param paramName    name of the parameter whose value is to be returned
	 * @param defaultValue default value to be returned if the param is missing or invalid (not an int)
	 * @return the value of a request parameter or <code>defaultValue</code> if the parameter does not have a value or it does not contain a valid integer
	 */
	protected static int getIntParamValue( final HttpServletRequest request, final String paramName, final int defaultValue ) {
		try {
			return Integer.parseInt( request.getParameter( paramName ) );
		}
		catch ( final Exception e ) {
			return defaultValue;
		}
	}
	
	/**
	 * Returns the integer value parsed from a request parameter.
	 * @param request   http request object
	 * @param paramName name of the parameter whose value is to be returned
	 * @return the value of a request parameter 
	 */
	protected static Integer getIntegerParamValue( final HttpServletRequest request, final String paramName ) {
		try {
			return Integer.valueOf( request.getParameter( paramName ) );
		}
		catch ( final Exception e ) {
			return null;
		}
	}
	
}
