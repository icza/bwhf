package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_AKA;
import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_GAME;
import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_PLAYER;
import static hu.belicza.andras.hackerdb.ServerApiConsts.GATEWAYS;
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
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_SORTING_INDEX;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle adding new games to the Players' Network database.
 * 
 * @author Andras Belicza
 */
public class PlayersNetworkServlet extends BaseServlet {
	
	/** Simple date format to format and parse replay save time. */
	private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
	/** Size of the list in one page. */
	private static final int PAGE_SIZE = 25;
	
	/**
	 * Defines the header of a table.<br>
	 * @author Andras Belicza
	 */
	private static class TableHeader {
		/** Defines which columns are sortable; <code>null</code> values specifies columns which cannot be bases of sorting. */
		public final String[]  sortingColumns;
		/** Text values of the header cells. */
		public final String[]  headers;
		/** Tells if a column has a default descendant sorting. */
		public final boolean[] sortingDefaultDescs;
		/** Default sorting index. */
		public final int       defaultSortingIndex;
		
		public TableHeader( final String[] sortingColumns, final String[] headers, final boolean[] sortingDefaultDescs, final int defaultSortingIndex ) {
			this.sortingColumns      = sortingColumns;
			this.headers             = headers;
			this.sortingDefaultDescs = sortingDefaultDescs;
			this.defaultSortingIndex = defaultSortingIndex;
		}
	}
	
	/** Header of the game table. */
	private static final TableHeader GAME_TABLE_HEADER   = new TableHeader(
			new String[]  { null     , "save_time", "map_name", "frames"  , "type"     , "save_time", null      },
			new String[]  { "Details", "Engine"   , "Map"     , "Duration", "Game type", "Played on", "Players" },
			new boolean[] { false    , true       , false     , true      , false      , true       , false     },
			5
			
	);
	/** Header of the game table. */
	private static final TableHeader PLAYER_TABLE_HEADER = new TableHeader(
			new String[]  { null , "player.name", "COUNT(player.id)", "MIN(game.save_time)", "MAX(game.save_time)" },
			new String[]  { "#"  , "Player"     , "Games count"     , "First game"         , "Last game"           },
			new boolean[] { false, false        , true              , true                 , true                  },
			2
	);
	
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
		PrintWriter       outputWriter = null;
		Connection        connection   = null;
		Statement         statement    = null;
		ResultSet         resultSet    = null;
		PreparedStatement statement2   = null;
		ResultSet         resultSet2   = null;
		
		try {
			outputWriter = response.getWriter();
			
			connection = dataSource.getConnection();
			
			renderHeader( outputWriter );
			
			final StringBuilder pagerUrlBuilder = new StringBuilder( "players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + entity );
			final Integer player1 = getIntegerParamValue( request, PN_REQUEST_PARAM_NAME_PLAYER1 );
			final Integer player2 = player1 == null ? null : getIntegerParamValue( request, PN_REQUEST_PARAM_NAME_PLAYER2 );
			if ( player1 != null ) {
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PLAYER1 ).append( '=' ).append( player1 );
				if ( player2 != null )
					pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PLAYER2 ).append( '=' ).append( player2 );
			}
			final String pagerUrlWithoutSorting = pagerUrlBuilder.toString(); // For table headers to add proper sortings
			int sortingIndex = getIntParamValue( request, PN_REQUEST_PARAM_NAME_SORTING_INDEX, -1 );
			boolean sortingDesc = request.getParameter( PN_REQUEST_PARAM_NAME_SORTING_DESC ) != null;
			final TableHeader tableHeader = entity.equals( ENTITY_GAME ) ? GAME_TABLE_HEADER : entity.equals( ENTITY_PLAYER ) ? PLAYER_TABLE_HEADER : null;
			if ( tableHeader != null && ( sortingIndex <= 0 || sortingIndex >= tableHeader.sortingColumns.length ) ) {
				sortingIndex = tableHeader.defaultSortingIndex;
				sortingDesc  = tableHeader.sortingDefaultDescs[ sortingIndex ];
			}
			pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_SORTING_INDEX ).append( '=' ).append( sortingIndex );
			if ( sortingDesc )
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_SORTING_DESC );
			
			if ( entity.equals( ENTITY_GAME ) ) {
				
				// Title section
				outputWriter.print( "<h3>Game list" );
				String countQuery;
				if ( player1 != null ) {
					outputWriter.print( " of " );
					outputWriter.print( getPlayerDetailsHtmlLink( player1, getPlayerName( player1, connection ), null ) );
					if ( player2 != null ) {
						countQuery = "SELECT COUNT(*) FROM (SELECT game FROM game_player WHERE player=" + player1 + " OR player=" + player2 + " GROUP BY game HAVING COUNT(*)=2)";
						outputWriter.print( " and " );
						outputWriter.print( getPlayerDetailsHtmlLink( player2, getPlayerName( player2, connection ), null ) );
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
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query;
				if ( player1 == null )
					query = "SELECT id, engine, save_time, map_name, frames, type FROM game";
				else if ( player2 == null )
					query = "SELECT game.id, engine, save_time, map_name, frames, type FROM game JOIN game_player on game.id=game_player.game WHERE game_player.player=" + player1;
				else
					query = "SELECT DISTINCT game.id, engine, save_time, map_name, frames, type FROM game JOIN game_player on game.id=game_player.game WHERE game_player.player=" + player1 + " OR game_player.player=" + player2 + " GROUP BY game.id, engine, save_time, map_name, frames, type HAVING COUNT(*)=2";
				
				query += " ORDER BY " + GAME_TABLE_HEADER.sortingColumns[ sortingIndex ] + ( sortingDesc ? " DESC" : "" )
				       + " LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
				renderSortingTableHeaderRow( outputWriter, GAME_TABLE_HEADER, pagerUrlWithoutSorting, sortingIndex, sortingDesc, page );
				statement = connection.createStatement();
				resultSet = statement.executeQuery( query );
				final ReplayHeader replayHeader = new ReplayHeader();
				int colCounter;
				statement2 = connection.prepareStatement( "SELECT player.id, player.name FROM game_player JOIN player on game_player.player=player.id WHERE game_player.game=?" );
				while ( resultSet.next() ) {
					colCounter = 1;
					final int gameId = resultSet.getInt( colCounter++ );
					replayHeader.gameEngine = (byte) resultSet.getInt( colCounter++ );
					replayHeader.saveTime   = resultSet.getDate( colCounter++ );
					replayHeader.mapName    = resultSet.getString( colCounter++ );
					replayHeader.gameFrames = resultSet.getInt( colCounter++ );
					replayHeader.gameType   = (short) resultSet.getInt( colCounter++ );
					
					outputWriter.print( "<tr><td align=right>" + getGameDetailsHtmlLink( gameId, Integer.toString( ++recordCounter ) ) );
					outputWriter.print( "<td>" + ReplayHeader.GAME_ENGINE_SHORT_NAMES[ replayHeader.gameEngine ] + " " + replayHeader.guessVersionFromDate() );
					outputWriter.print( "<td>" + replayHeader.mapName );
					outputWriter.print( "<td>" + replayHeader.getDurationString( true ) );
					outputWriter.print( "<td>" + ReplayHeader.GAME_TYPE_NAMES[ replayHeader.gameType ] );
					outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( replayHeader.saveTime ) );
					
					statement2.setInt( 1, gameId );
					resultSet2 = statement2.executeQuery();
					final StringBuilder playersBuilder = new StringBuilder();
					while ( resultSet2.next() ) {
						if ( playersBuilder.length() > 0 )
							playersBuilder.append( ", " );
						getPlayerDetailsHtmlLink( resultSet2.getInt( 1 ), resultSet2.getString( 2 ), playersBuilder );
					}
					resultSet2.close();
					outputWriter.print( "<td>" + playersBuilder.toString() );
				}
				statement2.close();
				outputWriter.println( "</table>" );
				
			}
			else if ( entity.equals( ENTITY_PLAYER ) ) {
				
				outputWriter.print( "<h3>Player list" );
				String countQuery;
				if ( player1 != null ) {
					outputWriter.print( " who played with " );
					outputWriter.print( getPlayerDetailsHtmlLink( player1, getPlayerName( player1, connection ), null ) );
					countQuery = "SELECT COUNT(DISTINCT player) FROM game_player WHERE game IN (SELECT game FROM game_player WHERE player=" + player1 + ")";
				}
				else
					countQuery = "SELECT COUNT(*) FROM player";
				outputWriter.println( "</h3>" );
				
				// Pages count section
				final int playersCount = executeCountStatement( countQuery, connection );
				outputWriter.println( "<p>Players count: <b>" + playersCount + "</b><br>" );
				
				// Pager links section
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PAGE ).append( '=' );
				final int maxPage = ( playersCount - 1 ) / PAGE_SIZE + 1;
				if ( page < 1 )
					page = 1;
				if ( page > maxPage )
					page = maxPage;
				renderPagerLinks( outputWriter, page, maxPage, pagerUrlBuilder.toString() );
				outputWriter.println( "</p>" );
				
				outputWriter.flush();
				
				// Player data section
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query;
				if ( player1 == null )
					query = "SELECT player.id, player.name, COUNT(player.id), MIN(game.save_time), MAX(game.save_time) FROM game_player JOIN player on player.id=game_player.player JOIN game on game.id=game_player.game GROUP BY player.id, player.name";
				else
					query = "SELECT player.id, player.name, COUNT(player.id), MIN(game.save_time), MAX(game.save_time) FROM game_player JOIN player on player.id=game_player.player JOIN game on game.id=game_player.game WHERE game_player.game IN (SELECT game FROM game_player WHERE player=" + player1 + ") GROUP BY player.id, player.name";
				
				query += " ORDER BY " + PLAYER_TABLE_HEADER.sortingColumns[ sortingIndex ] + ( sortingDesc ? " DESC" : "" )
				       + " LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
				renderSortingTableHeaderRow( outputWriter, PLAYER_TABLE_HEADER, pagerUrlWithoutSorting, sortingIndex, sortingDesc, page );
				statement = connection.createStatement();
				resultSet = statement.executeQuery( query );
				while ( resultSet.next() ) {
					outputWriter.print( "<tr><td align=right>" + (++recordCounter) );
					final int playerId = resultSet.getInt( 1 );
					outputWriter.print( "<td>" + getPlayerDetailsHtmlLink( playerId, resultSet.getString( 2 ), null ) );
					outputWriter.print( "<td align=center>" + ( player1 == null ? getGameListOfPlayerHtmlLink ( playerId, Integer.toString( resultSet.getInt( 3 ) ) )
							                                                    : getGameListOfPlayersHtmlLink( player1, playerId, Integer.toString( resultSet.getInt( 3 ) ) ) ) );
					outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 4 ) ) );
					outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 5 ) ) );
				}
				outputWriter.println( "</table>" );
			}
			else if ( entity.equals( ENTITY_AKA ) ) {
				
			}
			
			renderFooter( outputWriter );
		}
		catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		catch ( final SQLException se ) {
		}
		finally {
			if ( resultSet2 != null ) try { resultSet2.close(); } catch ( final SQLException se ) {}
			if ( statement2 != null ) try { statement2.close(); } catch ( final SQLException se ) {}
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
			if ( outputWriter != null ) outputWriter.close();
		}
	}
	
	/**
	 * Renders a table header row which is capable of defining sorting columns and sorting order.
	 * 
	 * @param outputWriter           output writer to be used
	 * @param tableHeader            table header defining the headers
	 * @param pagerUrlWithoutSorting url to be used when action was performed to change sorting; sorting params needs to be added
	 * @param sortingIndex           the current sorting index
	 * @param sortingDesc            tells if currently sorting is descendant
	 * @param currentPage            the current page 
	 */
	private static void renderSortingTableHeaderRow( final PrintWriter outputWriter, final TableHeader tableHeader, String pagerUrlWithoutSorting, final int sortingIndex, final boolean sortingDesc, final int currentPage ) {
		pagerUrlWithoutSorting += '&' + PN_REQUEST_PARAM_NAME_PAGE + "=" + currentPage;
		outputWriter.print( "<tr>" );
		
		for ( int i = 0; i < tableHeader.headers.length; i++ )
			if ( tableHeader.sortingColumns[ i ] == null )
				outputWriter.print( "<th style='cursor:default;'>" + tableHeader.headers[ i ] );
			else {
				outputWriter.print( "<th style='cursor:pointer;' onclick=\"javascript:window.location='" 
						+ pagerUrlWithoutSorting + '&' + PN_REQUEST_PARAM_NAME_SORTING_INDEX + "=" + i + ( sortingIndex == i && !sortingDesc || sortingIndex != i && tableHeader.sortingDefaultDescs[ i ] ? '&' + PN_REQUEST_PARAM_NAME_SORTING_DESC : "" )
						+ "'\">" + tableHeader.headers[ i ] );
				if ( sortingIndex == i )
					outputWriter.print( sortingDesc ? " &darr;" : " &uarr;" );
			}
		
		outputWriter.println();
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
		Connection  connection   = null;
		Statement   statement    = null;
		ResultSet   resultSet    = null;
		Statement   statement2   = null;
		ResultSet   resultSet2   = null;
		
		try {
			connection = dataSource.getConnection();
			
			outputWriter = response.getWriter();
			
			renderHeader( outputWriter );
			
			if ( entity.equals( ENTITY_GAME ) ) {
				
				outputWriter.println( "<h3>Details of game id=" + entityId + " </h3>" );
				
				statement = connection.createStatement();
				resultSet = statement.executeQuery( "SELECT engine, frames, save_time, name, map_width, map_height, type, creator_name, map_name, gateway FROM game WHERE id=" + entityId );
				
				if ( resultSet.next() ) {
					final ReplayHeader replayHeader = new ReplayHeader();
					int colCounter = 1;
					replayHeader.gameEngine  = (byte) resultSet.getInt( colCounter++ );
					replayHeader.gameFrames  = resultSet.getInt( colCounter++ );
					replayHeader.saveTime    = resultSet.getTimestamp( colCounter++ );
					replayHeader.gameName    = resultSet.getString( colCounter++ );
					replayHeader.mapWidth    = (short) resultSet.getInt( colCounter++ );
					replayHeader.mapHeight   = (short) resultSet.getInt( colCounter++ );
					replayHeader.gameType    = (short) resultSet.getInt( colCounter++ );
					replayHeader.creatorName = resultSet.getString( colCounter++ );
					replayHeader.mapName     = resultSet.getString( colCounter++ );
					final Integer gateway    = resultSet.getInt( colCounter++ );
					
					outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
					outputWriter.println( "<tr><th align=left>Game engine:<td>" + replayHeader.getGameEngineString() );
					outputWriter.println( "<tr><th align=left>Version:<td>" + replayHeader.guessVersionFromDate() );
					outputWriter.println( "<tr><th align=left>Duration:<td>" + replayHeader.getDurationString( false ) );
					outputWriter.println( "<tr><th align=left>Saved on:<td>" + replayHeader.saveTime );
					outputWriter.println( "<tr><th align=left>Game name:<td>" + replayHeader.gameName );
					outputWriter.println( "<tr><th align=left>Map name:<td>" + replayHeader.mapName );
					outputWriter.println( "<tr><th align=left>Map size:<td>" + replayHeader.getMapSize() );
					outputWriter.println( "<tr><th align=left>Creator name:<td>" + replayHeader.creatorName );
					outputWriter.println( "<tr><th align=left>Game type:<td>" + ReplayHeader.GAME_TYPE_NAMES[ replayHeader.gameType ] );
					if ( gateway != null && gateway >= 0 && gateway < GATEWAYS.length )
						outputWriter.print( "<tr><th align=left>Reported gateway:<td>" + GATEWAYS[ gateway ] );
					
					final int seconds = replayHeader.getDurationSeconds();
					statement2 = connection.createStatement();
					resultSet2 = statement2.executeQuery( "SELECT player.id, player.name, game_player.race, game_player.actions_count, game_player.color FROM game_player JOIN player on game_player.player=player.id WHERE game_player.game=" + entityId + " ORDER BY game_player.actions_count DESC" );
					outputWriter.print( "<tr><td colspan=2 align=center><table border=0 cellspacing=6><tr><th>Player<th>Race<th>Actions<th>APM<th>Color" );
					while ( resultSet2.next() ) {
						outputWriter.print( "<tr><td>" );
						outputWriter.print( getPlayerDetailsHtmlLink( resultSet2.getInt( 1 ), resultSet2.getString( 2 ), null ) );
						String colorName;
						try {
							colorName = ReplayHeader.IN_GAME_COLOR_NAMES[ resultSet2.getInt( 5 ) ];
						}
						catch ( final Exception e ) {
							colorName = "<unknown>";
						}
						outputWriter.print( "<td>" + ReplayHeader.RACE_NAMES[ resultSet2.getInt( 3 ) ] );
						outputWriter.print( "<td align=right>" + resultSet2.getInt( 4 ) );
						outputWriter.println( "<td align=right>" + resultSet2.getInt( 4 ) * 60 / seconds + "<td>" + colorName );
					}
					outputWriter.println( "</table>" );
					resultSet2.close();
					statement2.close();
					
					outputWriter.print( "</table>" );
				}
				else
					outputWriter.println( "<p><b><i><font color='red'>The referred game could not be found!</font></i></b></p>" );
				
			} else if ( entity.equals( ENTITY_PLAYER ) ) {
				
				final String playerNameHtml = encodeHtmlString( getPlayerName( entityId, connection ) );
				outputWriter.println( "<h3>Details of player " + playerNameHtml + " </h3>" );
				
				// TODO: aka's should be included!!
				statement = connection.createStatement();
				resultSet = statement.executeQuery( "SELECT COUNT(*), MIN(save_time), MAX(save_time) FROM game_player JOIN game on game.id=game_player.game WHERE player=" + entityId );
				
				if ( resultSet.next() ) {
					outputWriter.print( "<table border=1 cellspacing=0 cellpadding=2>" );
					outputWriter.print( "<tr><th align=left>Games count:<td align=right>" + resultSet.getInt( 1 ) );
					outputWriter.print( "<tr><th align=left>First game:<td align=right>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 2 ) ) );
					outputWriter.print( "<tr><th align=left>Last game:<td align=right>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 3 ) ) );
					final int days = 1 + (int) ( ( resultSet.getDate( 3 ).getTime() - resultSet.getDate( 2 ).getTime() ) / (1000l*60l*60l*24l) );
					outputWriter.print( "<tr><th align=left>Presence:<td align=right>" + formatDays( days ) );
					outputWriter.print( "<tr><th align=left>Average games per day:<td align=right>" + new Formatter().format( "%.2f", ( resultSet.getInt( 1 ) / (float) days ) ) );
					outputWriter.print( "<tr><th align=left>Game list:<td align=right>" + getGameListOfPlayerHtmlLink( entityId, "Games of " + playerNameHtml ) );
					outputWriter.print( "<tr><th align=left>Player list:<td align=right>" + getPlayerListWhoPlayedWithAPlayerHtmlLink( entityId, "Who did " + playerNameHtml + " play with?" ) );
					outputWriter.print( "</table>" );
				}
				else
					outputWriter.println( "<p><b><i><font color='red'>The referred player could not be found!</font></i></b></p>" );
				
				resultSet.close();
				statement.close();
				
			}
			
			renderFooter( outputWriter );
		}
		catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		catch ( final SQLException se ) {
		}
		finally {
			if ( resultSet2 != null ) try { resultSet2.close(); } catch ( final SQLException se ) {}
			if ( statement2 != null ) try { statement2.close(); } catch ( final SQLException se ) {}
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
			if ( outputWriter != null ) outputWriter.close();
		}
	}
	
	/**
	 * Formats the days to human readable format (breaks it down to years, months, days).
	 * @param days number of days to be formatted
	 * @return the formatted days in human readable format (years, months, days)
	 */
	private static String formatDays( int days ) {
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
	 * Returns the name of the player specified by its id.
	 * @param id         id of the player
	 * @param connection connection to be used
	 * @return the name of the player specified by its id
	 * @throws SQLException if SQLException occurs
	 */
	private static String getPlayerName( final int id, final Connection connection ) throws SQLException {
		PreparedStatement statement = null;
		ResultSet         resultSet = null;
		
		try {
			statement = connection.prepareStatement( "SELECT name FROM player WHERE id=?" );
			statement.setInt( 1, id );
			resultSet = statement.executeQuery();
			if ( resultSet.next() )
				return resultSet.getString( 1 );
			else
				return "";
		}
		finally {
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Generates and returns an HTML link to the details page of a player.<br>
	 * An HTML anchor tag will be returned whose text is the name of the player.<br>
	 * If <code>builder</code> is null, a new <code>StringBuilder</code> will be created and returned.
	 * @param id         id of the player
	 * @param playerName name of the player
	 * @param builder    builder to use to build the link
	 * @return the builder used to build the an HTML link to the details page of the player
	 */
	private static StringBuilder getPlayerDetailsHtmlLink( final int id, final String playerName, StringBuilder builder ) {
		if ( builder == null )
			builder = new StringBuilder();
		
		builder.append( "<a href='players?" ).append( PN_REQUEST_PARAM_NAME_OPERATION ).append( '=' ).append( PN_OPERATION_DETAILS )
							   .append( '&' ).append( PN_REQUEST_PARAM_NAME_ENTITY    ).append( '=' ).append( ENTITY_PLAYER )
							   .append( '&' ).append( PN_REQUEST_PARAM_NAME_ENTITY_ID ).append( '=' ).append( id ).append( "'>" ).append( encodeHtmlString( playerName ) ).append( "</a>" );
		
		return builder;
	}
	
	/**
	 * Generates and returns an HTML link to the details page of a game.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code>.
	 * @param id   id of the game
	 * @param text text to appear in the link
	 * @return an HTML link to the details page of the player
	 */
	private static String getGameDetailsHtmlLink( final int id, final String text ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_DETAILS
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_GAME
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY_ID + '=' + id + "'>" + text + "</a>"; 
	}
	
	/**
	 * Generates and returns an HTML link to the game list of a player.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code>.
	 * @param playerId id of the player
	 * @param text     text to appear in the link
	 * @return an HTML link to the game list of a player
	 */
	private static String getGameListOfPlayerHtmlLink( final int playerId, final String text ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_GAME
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER1   + '=' + playerId + "'>" + text + "</a>";
	}
	
	/**
	 * Generates and returns an HTML link to the game list of 2 players.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code>.
	 * @param player1Id id of player #1
	 * @param player2Id id of player #2
	 * @param text text to appear in the link
	 * @return an HTML link to the game list of a player
	 */
	private static String getGameListOfPlayersHtmlLink( final int player1Id, final int player2Id, final String text ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_GAME
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER1   + '=' + player1Id
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER2   + '=' + player2Id + "'>" + text + "</a>";
	}
	
	/**
	 * Generates and returns an HTML link to the player list who played with a player.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code>.
	 * @param playerId id of the player who's mates are searched for
	 * @param text     text to appear in the link
	 * @return an HTML link to the player list who played with a player
	 */
	private static String getPlayerListWhoPlayedWithAPlayerHtmlLink( final int playerId, final String text ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_PLAYER
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER1   + '=' + playerId + "'>" + text + "</a>";
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
				   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_PLAYER + "'>Player list</a>"
				   + "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_AKA    + "'>AKA list</a></p>" );
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
