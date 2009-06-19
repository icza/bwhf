package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_AKA;
import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_GAME;
import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_PLAYER;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_DETAILS;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_LIST;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_SEND;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY_ID;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_OPERATION;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_PAGE;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_PLAYER1;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_PLAYER2;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_SORTING_DESC;

import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle adding new games to the Players' Network database.
 * 
 * @author Andras Belicza
 */
public class PlayersNetworkServlet extends BaseServlet {
	
	/** Size of the list in one page. */
	private static final int PAGE_SIZE = 25;
	
	@Override
	public void doPost( final HttpServletRequest request, final HttpServletResponse response ) {
		doGet( request, response );
	}
	
	@Override
	public void doGet( final HttpServletRequest request, final HttpServletResponse response ) {
		setNoCache( response );
		
		try {
			String operation = request.getParameter( PN_REQUEST_PARAM_NAME_OPERATION );
			if ( operation == null )
				operation = PN_OPERATION_LIST;
			
			if ( operation.equals( PN_OPERATION_SEND ) ) {
				handleSend( request, response );
			} else if ( operation.equals( PN_OPERATION_LIST ) ) {
				String entity = request.getParameter( PN_REQUEST_PARAM_NAME_ENTITY );
				if ( entity == null )
					entity = ENTITY_GAME;
				
				int page;
				try {
					page = Integer.parseInt( request.getParameter( PN_REQUEST_PARAM_NAME_PAGE ) );
				}
				catch ( final Exception e ) {
					page = 1;
				}
				
				handleList( request, response, entity, page );
			} else if ( operation.equals( PN_OPERATION_DETAILS ) ) {
				final String entity = request.getParameter( PN_REQUEST_PARAM_NAME_ENTITY );
				if ( entity == null )
					throw new BadRequestException();
				
				int entityId;
				try {
					entityId = Integer.parseInt( request.getParameter( PN_REQUEST_PARAM_NAME_ENTITY_ID ) );
				}
				catch ( final Exception e ) {
					throw new BadRequestException();
				}
				
				handleDetails( request, response, entity, entityId );
			}
		}
		catch ( final BadRequestException bre ) {
			sendBackErrorMessage( response );
		}
	}
	
	/**
	 * Handles a send request.<br>
	 * A send request sends info about a game.
	 * @param request  http request
	 * @param response http response
	 */
	private void handleSend( final HttpServletRequest request, final HttpServletResponse response ) {
		int engine = 0, frames = 0, mapWidth = 0, mapHeight = 0, speed = 0, type = 0, subType = 0;
		long saveTime = 0l;
		String name = null, creatorName = null, mapName = null, replayMd5 = null, agentVersion = null;
		final List< String  > playerNameList    = new ArrayList< String  >( 8 );
		final List< Integer > playerRaceList    = new ArrayList< Integer >( 8 );
		final List< Integer > playerActionsList = new ArrayList< Integer >( 8 );
		final List< Integer > playerColorList   = new ArrayList< Integer >( 8 );
		Integer gateway = null;
		
		try {
			engine       = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_ENGINE ) );
			frames       = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_FRAMES ) );
			saveTime     = Long.parseLong( request.getParameter( ServerApiConsts.GAME_PARAM_SAVE_TIME ) );
			name         = request.getParameter( ServerApiConsts.GAME_PARAM_NAME );
			mapWidth     = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_MAP_WIDTH  ) );
			mapHeight    = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_MAP_HEIGHT ) );
			speed        = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_SPEED      ) );
			type         = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_TYPE       ) );
			subType      = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_SUB_TYPE   ) );
			creatorName  = request.getParameter( ServerApiConsts.GAME_PARAM_CREATOR_NAME  );
			mapName      = request.getParameter( ServerApiConsts.GAME_PARAM_MAP_NAME      ).toLowerCase();
			replayMd5    = request.getParameter( ServerApiConsts.GAME_PARAM_REPLAY_MD5    );
			agentVersion = request.getParameter( ServerApiConsts.GAME_PARAM_AGENT_VERSION );
			try { gateway = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_GATEWAY ) ); } catch ( final Exception e ) {} // Gateway is optional...
			
			int playerCounter = 0;
			while ( request.getParameter( ServerApiConsts.GAME_PARAM_PLAYER_NAME + playerCounter ) != null ) {
				playerNameList   .add( request.getParameter( ServerApiConsts.GAME_PARAM_PLAYER_NAME + playerCounter ).toLowerCase()          );
				playerRaceList   .add( Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_PLAYER_RACE    + playerCounter ) ) );
				playerActionsList.add( Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_PLAYER_ACTIONS + playerCounter ) ) );
				playerColorList  .add( Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_PLAYER_COLOR   + playerCounter ) ) );
				playerCounter++;
			}
			
			if ( name == null || creatorName == null || mapName == null || replayMd5 == null || replayMd5.length() == 0 || agentVersion == null || playerNameList.isEmpty() || playerNameList.size() > 8 )
				throw new Exception();
		}
		catch ( final Exception e ) {
			sendBackPlainMessage( "Bad request, missing or invalid parameters!", response );
			return;
		}
		
		Connection        connection = null;
		PreparedStatement statement  = null;
		ResultSet         resultSet  = null;
		PreparedStatement statement2 = null;
		
		try {
			connection = dataSource.getConnection();
			
			statement = connection.prepareStatement( "SELECT id FROM game WHERE replay_md5=?" );
			statement.setString( 1, replayMd5 );
			resultSet = statement.executeQuery();
			if ( resultSet.next() ) {
				// This game has already been reported
				sendBackPlainMessage( ServerApiConsts.REPORT_ACCEPTED_MESSAGE, response );
				return;
			}
			resultSet.close();
			statement.close();
			
			synchronized ( PlayersNetworkServlet.class ) {
				connection.setAutoCommit( false );
				
				// First create the players
				for ( int i = 0; i < playerNameList.size(); i++ ) {
					// Check if player already exists
					statement = connection.prepareStatement( "SELECT id FROM player WHERE name=?" );
					statement.setString( 1, playerNameList.get( i ) );
					resultSet = statement.executeQuery();
					if ( !resultSet.next() ) {
						// Player doesn't exist, let's create it
						statement2 = connection.prepareStatement( "INSERT INTO player (name) VALUES (?)" );
						statement2.setString( 1, playerNameList.get( i ) );
						if ( statement2.executeUpdate() == 0 )
							throw new Exception( "Could not insert player!" );
						statement2.close();
					}
					resultSet.close();
					statement.close();
				}
				
				// Players exist now. Let's insert the game.
				statement = connection.prepareStatement( "INSERT INTO game (engine,frames,save_time,name,map_width,map_height,speed,type,sub_type,creator_name,map_name,replay_md5,agent_version,gateway,ip) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" );
				int colCounter = 1;
				statement.setInt      ( colCounter++, engine                    );
				statement.setInt      ( colCounter++, frames                    );
				statement.setTimestamp( colCounter++, new Timestamp( saveTime ) );
				statement.setString   ( colCounter++, name                      );
				statement.setInt      ( colCounter++, mapWidth                  );
				statement.setInt      ( colCounter++, mapHeight                 );
				statement.setInt      ( colCounter++, speed                     );
				statement.setInt      ( colCounter++, type                      );
				statement.setInt      ( colCounter++, subType                   );
				statement.setString   ( colCounter++, creatorName               );
				statement.setString   ( colCounter++, mapName                   );
				statement.setString   ( colCounter++, replayMd5                 );
				statement.setString   ( colCounter++, agentVersion              );
				if ( gateway == null )
					statement.setNull ( colCounter++, Types.INTEGER             );
				else
					statement.setInt  ( colCounter++, gateway                   );
				statement.setString   ( colCounter++, request.getRemoteAddr()   );
				if ( statement.executeUpdate() == 0 )
					throw new Exception( "Could not insert game!" );
				statement.close();
				
				// Lastly insert the connections between the game and players
				statement = connection.prepareStatement( "INSERT INTO game_player (game,player,race,actions_count,color) VALUES ((select max(id) from game),(select id from player where name=?),?,?,?)" );
				for ( int i = 0; i < playerNameList.size(); i++ ) {
					colCounter = 1;
					statement.setString( colCounter++, playerNameList   .get( i ) );
					statement.setInt   ( colCounter++, playerRaceList   .get( i ) );
					statement.setInt   ( colCounter++, playerActionsList.get( i ) );
					statement.setInt   ( colCounter++, playerColorList  .get( i ) );
					
					if ( statement.executeUpdate() == 0 )
						throw new Exception( "Could not insert game_player!" );
				}
				statement.close();
				
				connection.commit();
			}
			
			sendBackPlainMessage( ServerApiConsts.REPORT_ACCEPTED_MESSAGE, response );
		}
		catch ( final Exception e ) {
			e.printStackTrace();
			if ( connection != null )
				try { connection.rollback(); } catch ( final SQLException se2 ) {}
			sendBackPlainMessage( "Internal server error!", response );
		}
		finally {
			try {
				if ( !connection.getAutoCommit() )
					connection.setAutoCommit( true );
			} catch ( final SQLException se ) {
			}
			if ( statement2 != null ) try { statement2.close(); } catch ( final SQLException se ) {}
			if ( resultSet  != null ) try { resultSet .close(); } catch ( final SQLException se ) {}
			if ( statement  != null ) try { statement .close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Handles a list request.<br>
	 * Lists some kind of entity.
	 * @param request  http request
	 * @param response http response
	 * @param entity   entity to be listed
	 * @param page     page to be listed
	 */
	private void handleList( final HttpServletRequest request, final HttpServletResponse response, final String entity, int page ) {
		Connection  connection   = null;
		PrintWriter outputWriter = null;
		Statement   statement    = null;
		ResultSet   resultSet    = null;
		
		try {
			outputWriter = response.getWriter();
			
			connection = dataSource.getConnection();
			
			renderHeader( outputWriter );
			
			final StringBuilder pagerUrlBuilder = new StringBuilder( "players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + entity );
			
			if ( entity.equals( ENTITY_GAME ) ) {
				final Integer player1 = getIntegerParamValue( request, PN_REQUEST_PARAM_NAME_PLAYER1 );
				final Integer player2 = getIntegerParamValue( request, PN_REQUEST_PARAM_NAME_PLAYER2 );
				
				// Title section
				outputWriter.print( "<h3>Game list" );
				String countQuery;
				if ( player1 != null ) {
					pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PLAYER1 ).append( '=' ).append( player1 );
					outputWriter.print( " of " );
					outputWriter.print( getPlayerDetailsHtmlLink( player1, connection ) );
					if ( player2 != null ) {
						pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PLAYER2 ).append( '=' ).append( player2 );
						countQuery = "SELECT COUNT(DISTINCT game) FROM game_player WHERE player=" + player1 + " OR player=" + player2 + " GORUP BY game HAVING COUNT(*)=2";
						outputWriter.print( " and " );
						outputWriter.print( getPlayerDetailsHtmlLink( player2, connection ) );
					}
					else
						countQuery = "SELECT COUNT(*) FROM game_player WHERE game_player.player=" + player1; // No need distinct, 1 player is only once at the most in a game
				}
				else
					countQuery = "SELECT COUNT(*) FROM game";
				outputWriter.println( "</h3>" );
				
				// Pages count section
				final int gamesCount = executeCountStatement( countQuery, connection );
				outputWriter.println( "<p>Games count: <b>" + gamesCount + "</b><br>" );
				
				// Pager links section
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PAGE ).append( '=' );
				final int maxPage = ( gamesCount - 1 ) / PAGE_SIZE + 1;
				if ( page < 1 )
					page = 1;
				if ( page > maxPage )
					page = maxPage;
				renderPagerLinks( outputWriter, page, maxPage, pagerUrlBuilder.toString() );
				outputWriter.println( "</p>" );
				
				outputWriter.flush();
				
				// Game data section
				final String[] sortingColumns = new String[] { "", "save_time", "map_name", "frames", "type" };
				int sortingIndex = getIntParamValue( request, PN_REQUEST_PARAM_NAME_PLAYER2, -1 );
				if ( sortingIndex <= 0 || sortingIndex >= sortingColumns.length )
					sortingIndex = sortingColumns.length - 1;
				
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query = "SELECT id, engine, save_time, map_name, frames, type FROM game JOIN game_player on game.id=game_player.game ORDER BY " + sortingColumns[ sortingIndex ] 
				             + ( getStringParamValue( request, PN_REQUEST_PARAM_NAME_SORTING_DESC ) != null ? " DESC" : "" )
				             + " LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				outputWriter.print( "<table border=1><tr><th>#<th>Engine<th>Map:<th>Duration:<th>Game type<th>Played on:<th>Players:" );
				statement = connection.createStatement();
				resultSet = statement.executeQuery( query );
				final ReplayHeader replayHeader = new ReplayHeader();
				int colCounter;
				while ( resultSet.next() ) {
					colCounter = 2;
					replayHeader.gameEngine = (byte) resultSet.getInt( colCounter++ );
					replayHeader.saveTime   = resultSet.getDate( colCounter++ );
					replayHeader.mapName    = resultSet.getString( colCounter++ );
					replayHeader.gameFrames = resultSet.getInt( colCounter++ );
					replayHeader.gameType   = (short) resultSet.getInt( colCounter++ );
					
					//outputWriter.print( "<tr><td>" + (++recordCounter) );
					//outputWriter.print( "<td>" + ReplayHeader.GAME_ENGINE_SHORT_NAMES[ replayHeader.gameEngine ] + " " + replayHeader.guessVersionFromDate() );
					
				}
				outputWriter.println( "</table>" );
				
			}
			
			renderFooter( outputWriter );
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		} catch ( final SQLException se ) {
		}
		finally {
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
			if ( outputWriter != null ) outputWriter.close();
		}
	}
	
	/**
	 * Handles a details request.<br>
	 * Sends details about an entity.
	 * @param request  http request
	 * @param response http response
	 * @param entity   entity to be detailed
	 * @param entityId id of entity to be detailed
	 */
	private void handleDetails( final HttpServletRequest request, final HttpServletResponse response, final String entity, final int entityId ) {
		PrintWriter outputWriter = null;
		try {
			outputWriter = response.getWriter();
			
			renderHeader( outputWriter );
			
			renderFooter( outputWriter );
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		finally {
			if ( outputWriter != null )
				outputWriter.close();
		}
	}
	
	/**
	 * Renders the pager link to the output.
	 * @param outputWriter output writer to use
	 * @param page         current page
	 * @param maxPage      max page
	 * @param pagerUrl     url to be used for the pager links; ends with '=' and only the target page number have to be appended
	 */
	private static void renderPagerLinks( final PrintWriter outputWriter, final int page, final int maxPage, final String pagerUrl ) {
		if ( page > 1 ) {
			outputWriter.print( "<a href='" + pagerUrl + "1'>First</a>&nbsp;&nbsp;" );
			outputWriter.print( "<a href='" + pagerUrl + ( page - 1 ) + "'>Previous</a>" );
		}
		else {
			outputWriter.print( "First&nbsp;&nbsp;Previous" );
		}
		outputWriter.print( "&nbsp;&nbsp;|&nbsp;&nbsp;Page <b>" + page + "</b> out of <b>" + maxPage + "</b>.&nbsp;&nbsp;|&nbsp;&nbsp;" );
		if ( page < maxPage ) {
			outputWriter.print( "<a href='" + pagerUrl + ( page + 1 ) + "'>Next</a>&nbsp;&nbsp;" );
			outputWriter.print( "<a href='" + pagerUrl + maxPage + "'>Last</a>" );
		}
		else {
			outputWriter.print( "Next&nbsp;&nbsp;Last" );
		}
	}
	
	/**
	 * Executes a count statement and returns its result.<br>
	 * If the query does not return anything, <code>0</code> is returned.
	 * @param countQueyr the count SQL query
	 * @param connection connection to be used
	 * @return the result of the count query, or <code>0</code> if the query does not return anything
	 * @throws SQLException if SQLException occurs
	 */
	private static int executeCountStatement( final String countQuery, final Connection connection ) throws SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery( countQuery );
			if ( resultSet.next() )
				return resultSet.getInt( 1 );
			else
				return 0;
		}
		finally {
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Generates and returns an HTML link to the details page of a player.<br>
	 * An HTML anchor tag will be returned whose text is the name of the player.
	 * @param id         id of the player
	 * @param connection connection to be used
	 * @return an HTML link to the details page of the player
	 * @throws SQLException if SQLException occurs
	 */
	private static String getPlayerDetailsHtmlLink( final int id, final Connection connection ) throws SQLException {
		PreparedStatement statement = null;
		ResultSet         resultSet = null;
		
		try {
			statement = connection.prepareStatement( "SELECT name FROM player WHERE id=?" );
			statement.setInt( 1, id );
			resultSet = statement.executeQuery();
			if ( resultSet.next() )
				return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_DETAILS
									 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_PLAYER
									 + '&' + PN_REQUEST_PARAM_NAME_ENTITY_ID + '=' + id + "'>" + encodeHtmlString( resultSet.getString( 1 ) ) + "</a>"; 
			else
				return "";
		}
		finally {
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Generates and returns an HTML link to the details page of a game.<br>
	 * An HTML anchor tag will be returned whose text is 'Game details'.
	 * @param id id of the game
	 * @return an HTML link to the details page of the player
	 */
	private static String getGameDetailsHtmlLink( final int id ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_DETAILS
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_GAME
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY_ID + '=' + id + "'>Game details</a>"; 
	}
	
	/**
	 * Renders the header for the output pages.
	 * @param outputWriter writer to be used to render
	 */
	private void renderHeader( final PrintWriter outputWriter ) {
		outputWriter.println( "<html><head><title>BWHF Players' Network</title>" );
		outputWriter.println( "<link rel='shortcut icon' href='favicon.ico' type='image/x-icon'>" );
		outputWriter.println( "</head><body><center>" );
		outputWriter.println( "<h2>BWHF Players' Network</h2>" );
		outputWriter.println( "<p><a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_GAME   + "'>Game list</a>"
				   + "&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_PLAYER + "'>Player list</a>"
				   + "&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_AKA    + "'>AKA list</a></p>" );
	}
	
	/**
	 * Renders the footer for the output pages.
	 * @param outputWriter writer to be used to render
	 */
	private void renderFooter( final PrintWriter outputWriter ) {
		outputWriter.println( "<hr><table border=0 width='100%'><tr><td width='50%' align='left'><a href='http://code.google.com/p/bwhf'>BWHF Agent home page</a>&nbsp;&nbsp;<a href='hackers'>BWHF Hacker Database</a><td align=right><i>&copy; Andr&aacute;s Belicza, 2008-2009</i></table>" );
		outputWriter.println( "</center></body></html>" );
	}
	
}
