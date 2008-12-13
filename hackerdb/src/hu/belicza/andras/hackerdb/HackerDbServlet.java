package hu.belicza.andras.hackerdb;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve hacker list related requests.<br>
 * These include:
 * <ul>
 * 	<li>Sending a part of the hacker list.
 * 	<li>Perform authorization key validation.
 * 	<li>Process a report of hackers.
 * </ul>
 * 
 * @author Andras Belicza
 */
public class HackerDbServlet extends HttpServlet {
	
	/** Name of the request parameter defining the operation. */
	private static final String REQUEST_OPERATION_NAME   = "op";
	
	/** List hackers operation value.                         */
	private static final String REQUEST_OPERATION_LIST   = "lst";
	/** Check authorization key operation value.              */
	private static final String REQUEST_OPERATION_CHECK  = "chk";
	/** Report hackers operation value.                       */
	private static final String REQUEST_OPERATION_REPORT = "rep";
	
	@Override
	public void doGet( final HttpServletRequest request, final HttpServletResponse response ) {
		final String operation = request.getParameter( REQUEST_OPERATION_NAME );
		if ( operation == null )
			sendBackErrorMessage( response );
		
		if ( operation.equals( REQUEST_OPERATION_LIST ) ) {
			
		} else if ( operation.equals( REQUEST_OPERATION_CHECK ) ) {
			
		} if ( operation.equals( REQUEST_OPERATION_REPORT ) ) {
			
		}
	}
	
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
	
}
