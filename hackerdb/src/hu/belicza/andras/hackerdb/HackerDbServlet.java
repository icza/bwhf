package hu.belicza.andras.hackerdb;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	/** Data source to provide pooled connections to the hacker database. */
	private static final BasicDataSource dataSource = new BasicDataSource();
	static {
		dataSource.setDriverClassName( "org.hsqldb.jdbcDriver" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );
		dataSource.setUrl( DATABASE_URL );
	}
	
	/** Name of the operation request parameter. */
	private static final String REQUEST_PARAMETER_NAME_OPERATION = "op";
	/** Name of the key request parameter.       */
	private static final String REQUEST_PARAMETER_NAME_KEY       = "key";
	
	/** List hackers operation value.            */
	private static final String OPERATION_LIST   = "lst";
	/** Check authorization key operation value. */
	private static final String OPERATION_CHECK  = "chk";
	/** Report hackers operation value.          */
	private static final String OPERATION_REPORT = "rep";
	
	
	
	@Override
	public void doGet( final HttpServletRequest request, final HttpServletResponse response ) {
		setNoCache( response );
		
		try {
			final String operation = request.getParameter( REQUEST_PARAMETER_NAME_OPERATION );
			
			if ( operation == null )
				throw new BadRequestException();
			
			if ( operation.equals( OPERATION_LIST ) ) {
				
			} else if ( operation.equals( OPERATION_CHECK ) ) {
				
				// Check the validity of an authorization key
				final String key = request.getParameter( REQUEST_PARAMETER_NAME_KEY );
				if ( key == null )
					throw new BadRequestException();
				
				sendBackPlainMessage( Boolean.toString( checkKey( key ) ), response );
				
			} else if ( operation.equals( OPERATION_REPORT ) ) {
				// Check the validity of an authorization key
				
				final String key = request.getParameter( REQUEST_PARAMETER_NAME_KEY );
				if ( key == null )
					throw new BadRequestException();
				
			}
		}
		catch ( final BadRequestException bre ) {
			sendBackErrorMessage( response );
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
			
			statement = connection.prepareStatement( "SELECT COUNT(*) FROM key WHERE revocated=FALSE AND value=?" );
			statement.setString( 1, key );
			
			resultSet = statement.executeQuery();
			if ( resultSet.next() )
				if ( resultSet.getInt( 1 ) > 0 )
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
