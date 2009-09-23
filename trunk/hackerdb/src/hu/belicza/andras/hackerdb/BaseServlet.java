package hu.belicza.andras.hackerdb;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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
	
	/** Decimal format used to format numbers (example: 12,345,678). */
	protected static DecimalFormat DECIMAL_FORMAT;
	static {
		DECIMAL_FORMAT = new DecimalFormat();
		DECIMAL_FORMAT.setGroupingSize( 3 );
		
		final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setGroupingSeparator( ',' );
		DECIMAL_FORMAT.setDecimalFormatSymbols( dfs );
	}
	
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
			.append( "<style>body {background-image:url(\"BWHF-logo.png\");background-position:0 0;background-repeat:no-repeat;}\n" )
			.append( "p,h2,h3 {margin:6;padding:0}\n." )
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
	
	/** Header Google Ad HTML */
	protected static final String GOOGLE_AD_HTML_HEADER = "<script type=\"text/javascript\"><!--\n"
		+ "google_ad_client = \"pub-4479321142068297\";\n"
		+ "/* Header ad 728x90 */\n"
		+ "google_ad_slot = \"4124263936\";\n"
		+ "google_ad_width = 728;\n"
		+ "google_ad_height = 90;\n"
		+ "//-->\n"
		+ "</script>\n"
		+ "<script type=\"text/javascript\"\n"
		+ "src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n"
		+ "</script>\n";
	
	/** The tracking HTML code for Google Analytics. */
	protected static final String GOOGLE_ANALYTICS_TRACKING_CODE = "<script type=\"text/javascript\">"
		+ "var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");"
		+ "document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));"
		+ "</script>"
		+ "<script type=\"text/javascript\">"
		+ "try {"
		+ "var pageTracker = _gat._getTracker(\"UA-4884955-10\");"
		+ "pageTracker._trackPageview();"
		+ "} catch(err) {}</script>";
	
	
	/** URL of the database. */
	private static final String DATABASE_URL = "jdbc:postgresql://localhost/hackers";
	
	/** Data source to provide pooled connections to the hacker database. */
	protected final BasicDataSource dataSource = new BasicDataSource();
	
	@Override
	public void init( final ServletConfig config ) throws ServletException {
		dataSource.setDriverClassName( "org.postgresql.Driver" );
		dataSource.setUsername( "postgres" );
		dataSource.setPassword( "a" );
		dataSource.setUrl( DATABASE_URL );
		
		super.init( config );
	}
	
	@Override
	public void doPost( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException {
		doGet( request, response );
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
	 * Returns the value of a request parameter.
	 * @param request   http request object
	 * @param paramName name of the parameter whose value is to be returned
	 * @return the value of a request parameter if its length is greater than 0; <code>null</code> otherwise
	 */
	protected static String getNullStringParamValue( final HttpServletRequest request, final String paramName ) {
		final String paramValue = request.getParameter( paramName );
		return paramValue == null || paramValue.length() == 0 ? null : paramValue;
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
	 * @return the value of a request parameter; or <code>null</code> if the parameter is null or an integer cannot be parsed from its value 
	 */
	protected static Integer getIntegerParamValue( final HttpServletRequest request, final String paramName ) {
		try {
			return Integer.valueOf( request.getParameter( paramName ) );
		}
		catch ( final Exception e ) {
			return null;
		}
	}
	
	/**
	 * Formats the days to human readable format (breaks it down to years, months, days).
	 * @param days number of days to be formatted
	 * @return the formatted days in human readable format (years, months, days)
	 */
	public static String formatDays( int days ) {
		final StringBuilder formatBuilder = new StringBuilder();
		
		final int years = days / 365;
		if ( years > 0 )
			formatBuilder.append( years ).append( years == 1 ? " year, " : " years, " );
		days = days % 365;
		
		final int months = days / 30;
		if ( months > 0 )
			formatBuilder.append( months ).append( months == 1 ? " month, " : " months, " );
		days = days % 30;
		
		formatBuilder.append( days ).append( days == 1 ? " day" : " days" );
		
		return formatBuilder.toString();
	}
	
}
