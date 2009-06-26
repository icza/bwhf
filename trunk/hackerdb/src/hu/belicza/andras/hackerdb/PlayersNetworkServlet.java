package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.*;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
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
			new String[]  { null , "player.name", "COUNT(player.id)", "MIN(game.save_time)", "MAX(game.save_time)", "SUM(game.frames)"    },
			new String[]  { "#"  , "Player"     , "Games count"     , "First game"         , "Last game"          , "Total time in games" },
			new boolean[] { false, false        , true              , true                 , true                 , true                  },
			2
	);
	/** Header of the AKA groups table. */
	private static final TableHeader AKA_GROUPS_TABLE_HEADER = new TableHeader(
			new String[]  { null , null      },
			new String[]  { "#"  , "AKA group" },
			new boolean[] { false, false     },
			0
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
		PreparedStatement statement3   = null;
		
		try {
			response.setCharacterEncoding( "UTF-8" );
			outputWriter = response.getWriter();
			
			connection = dataSource.getConnection();
			
			renderHeader( request, outputWriter );
			
			final StringBuilder pagerUrlBuilder = new StringBuilder( "players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + entity );
			final Integer player1 = getIntegerParamValue( request, PN_REQUEST_PARAM_NAME_PLAYER1 );
			final Integer player2 = player1 == null ? null : getIntegerParamValue( request, PN_REQUEST_PARAM_NAME_PLAYER2 );
			if ( player1 != null ) {
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PLAYER1 ).append( '=' ).append( player1 );
				if ( player2 != null )
					pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PLAYER2 ).append( '=' ).append( player2 );
			}
			final boolean includeAkas = request.getParameter( PN_REQUEST_PARAM_NAME_INCLUDE_AKAS ) != null;
			if ( includeAkas)
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_INCLUDE_AKAS );
			String pagerUrlWithoutSorting = pagerUrlBuilder.toString(); // For table headers to add proper sortings
			int sortingIndex = getIntParamValue( request, PN_REQUEST_PARAM_NAME_SORTING_INDEX, -1 );
			boolean sortingDesc = request.getParameter( PN_REQUEST_PARAM_NAME_SORTING_DESC ) != null;
			final TableHeader tableHeader = entity.equals( ENTITY_GAME ) ? GAME_TABLE_HEADER : entity.equals( ENTITY_PLAYER ) ? PLAYER_TABLE_HEADER : null;
			if ( tableHeader != null && ( sortingIndex <= 0 || sortingIndex >= tableHeader.sortingColumns.length || tableHeader.sortingColumns[ sortingIndex ] == null ) ) {
				sortingIndex = tableHeader.defaultSortingIndex;
				sortingDesc  = tableHeader.sortingDefaultDescs[ sortingIndex ];
			}
			pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_SORTING_INDEX ).append( '=' ).append( sortingIndex );
			if ( sortingDesc )
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_SORTING_DESC );
			String nameFilter = request.getParameter( PN_REQUEST_PARAM_NAME_NAME_FILTER );
			if ( nameFilter != null && nameFilter.length() == 0 )
				nameFilter = null;
			final String queryParam = nameFilter == null ? null : '%' + nameFilter + '%';
			final String pagerUrlWithoutNameFilter = pagerUrlBuilder.toString(); // For adding new name filter
			if ( nameFilter != null ) {
				final String nameFilterParam = '&' + PN_REQUEST_PARAM_NAME_NAME_FILTER + '=' + URLEncoder.encode( nameFilter, "UTF-8" );
				pagerUrlBuilder.append( nameFilterParam );
				pagerUrlWithoutSorting += nameFilterParam;
			}
			
			String akaIdList1 = null;
			String akaIdList2 = null;
			String akaIdList  = null;
			boolean hasAka1   = false;
			boolean hasAka2   = false;
			boolean hasAka    = false;
			if ( includeAkas && player1 != null ) {
				akaIdList1 = getPlayerAkaIdList( player1, connection );
				akaIdList2 = player2 == null ? null : getPlayerAkaIdList( player2, connection );
				hasAka1 = akaIdList1 != null;
				hasAka2 = akaIdList2 != null;
				hasAka = hasAka1 || hasAka2;
				if ( akaIdList1 == null )
					akaIdList1 = Integer.toString( player1 );
				if ( player2 != null && akaIdList2 == null )
					akaIdList2 = Integer.toString( player2 );
				akaIdList = player2 == null ? akaIdList1 : akaIdList1 + ',' + akaIdList2;
			}
			
			if ( entity.equals( ENTITY_GAME ) ) {
				
				// Title section
				outputWriter.print( "<h3>Game list" );
				String countQuery;
				String player1Name = null, player2Name = null;
				if ( player1 != null ) {
					outputWriter.print( " of " );
					outputWriter.print( getPlayerDetailsHtmlLink( player1, player1Name = getPlayerName( player1, connection ), null ) );
					if ( player2 != null ) {
						countQuery = "SELECT COUNT(*) FROM (SELECT game FROM game_player WHERE player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 + " OR player=" + player2 ) + " GROUP BY game HAVING COUNT(*)=2)";
						outputWriter.print( " and " );
						outputWriter.print( getPlayerDetailsHtmlLink( player2, player2Name = getPlayerName( player2, connection ), null ) );
					}
					else
						countQuery = "SELECT COUNT(*) FROM game_player WHERE game_player.player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 ); // No need distinct, 1 player is only once at the most in a game
				}
				else
					countQuery = "SELECT COUNT(*) FROM game";
				outputWriter.println( "</h3>" );
				if ( hasAka ) {
					statement = connection.createStatement();
					outputWriter.print( "<p>AKAs included " );
					if ( hasAka1 ) {
						outputWriter.print( "from <b>" + encodeHtmlString( player1Name ) + "</b>: " );
						resultSet = statement.executeQuery( "SELECT id, name FROM player WHERE id IN (" + akaIdList1 + ") AND id!=" + player1 );
						outputWriter.print( generatePlayerHtmlLinkListFromResultSet( resultSet ) );
						resultSet.close();
					}
					if ( hasAka2 ) {
						outputWriter.print( ( player1Name == null ? "" : "; " ) + "from <b>" + encodeHtmlString( player2Name ) + "</b>: " );
						resultSet = statement.executeQuery( "SELECT id, name FROM player WHERE id IN (" + akaIdList2 + ") AND id!=" + player2 );
						outputWriter.print( generatePlayerHtmlLinkListFromResultSet( resultSet ) );
						resultSet.close();
					}
					statement.close();
					outputWriter.print( "</p>" );
				}
				
				// Pages count section
				final int gamesCount = executeCountStatement( countQuery, connection );
				outputWriter.println( "<p>Games count: <b>" + gamesCount + "</b><br>" );
				outputWriter.flush();
				
				// Pager links section
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PAGE ).append( '=' );
				final int maxPage = ( gamesCount - 1 ) / PAGE_SIZE + 1;
				if ( page < 1 )
					page = 1;
				if ( page > maxPage )
					page = maxPage;
				renderPagerLinks( outputWriter, page, maxPage, pagerUrlBuilder.toString() );
				outputWriter.println( "</p>" );
				
				// Game data section
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query;
				if ( player1 == null )
					query = "SELECT id, engine, save_time, map_name, frames, type FROM game";
				else if ( player2 == null )
					query = "SELECT game.id, engine, save_time, map_name, frames, type FROM game JOIN game_player on game.id=game_player.game WHERE game_player.player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 );
				else
					query = "SELECT DISTINCT game.id, engine, save_time, map_name, frames, type FROM game JOIN game_player on game.id=game_player.game WHERE game_player.player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 + " OR game_player.player=" + player2 ) + " GROUP BY game.id, engine, save_time, map_name, frames, type HAVING COUNT(*)=2";
				
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
					replayHeader.guessedVersion = null; // This is cached so I have to clear it...
					replayHeader.gameEngine = (byte) resultSet.getInt( colCounter++ );
					replayHeader.saveTime   = resultSet.getDate( colCounter++ );
					replayHeader.mapName    = resultSet.getString( colCounter++ );
					replayHeader.gameFrames = resultSet.getInt( colCounter++ );
					replayHeader.gameType   = (short) resultSet.getInt( colCounter++ );
					
					outputWriter.print( "<tr><td align=right>" + getGameDetailsHtmlLink( gameId, Integer.toString( ++recordCounter ) ) + "&nbsp;" );
					outputWriter.print( "<td>" + ReplayHeader.GAME_ENGINE_SHORT_NAMES[ replayHeader.gameEngine ] + " " + replayHeader.guessVersionFromDate() );
					outputWriter.print( "<td>" + replayHeader.mapName );
					outputWriter.print( "<td>" + replayHeader.getDurationString( true ) );
					outputWriter.print( "<td>" + ReplayHeader.GAME_TYPE_NAMES[ replayHeader.gameType ] );
					outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( replayHeader.saveTime ) );
					
					statement2.setInt( 1, gameId );
					resultSet2 = statement2.executeQuery();
					outputWriter.print( "<td>" + generatePlayerHtmlLinkListFromResultSet( resultSet2 ) );
					resultSet2.close();
				}
				statement2.close();
				outputWriter.println( "</table>" );
				
			}
			else if ( entity.equals( ENTITY_PLAYER ) ) {
				
				outputWriter.print( "<h3>Player list" );
				String countQuery;
				String player1Name = null;
				if ( player1 != null ) {
					outputWriter.print( " who played with " );
					outputWriter.print( getPlayerDetailsHtmlLink( player1, player1Name = getPlayerName( player1, connection ), null ) );
					if ( nameFilter == null )
						countQuery = "SELECT COUNT(DISTINCT player) FROM game_player WHERE player" + ( hasAka1 ? " NOT IN (" + akaIdList1 + ")" : "!=" + player1 ) + " AND game IN (SELECT game FROM game_player WHERE player" + ( hasAka1 ? " IN (" + akaIdList1 + ")" : "=" + player1 ) + ")";
					else
						countQuery = "SELECT COUNT(DISTINCT player) FROM game_player JOIN player on game_player.player=player.id WHERE player.name LIKE ? AND player" + ( hasAka1 ? " NOT IN (" + akaIdList1 + ")" : "!=" + player1 ) + " AND game IN (SELECT game FROM game_player WHERE player" + ( hasAka1 ? " IN (" + akaIdList1 + ")" : "=" + player1 ) + ")";
				}
				else
					countQuery = "SELECT COUNT(*) FROM player" + ( nameFilter == null ? "" : " WHERE name LIKE ?" );
				
				outputWriter.println( "</h3>" );
				if ( hasAka1 ) {
					statement = connection.createStatement();
					outputWriter.print( "<p>AKAs included: " );
					resultSet = statement.executeQuery( "SELECT id, name FROM player WHERE id IN (" + akaIdList1 + ") AND id!=" + player1 );
					outputWriter.print( generatePlayerHtmlLinkListFromResultSet( resultSet ) );
					resultSet.close();
					statement.close();
					outputWriter.print( "</p>" );
				}
				
				outputWriter.println( "<p>" );
				
				// Pages count section
				final int playersCount = queryParam == null ? executeCountStatement( countQuery, connection ) : executeCountStatement( countQuery, queryParam, connection );
				final int maxPage = ( playersCount - 1 ) / PAGE_SIZE + 1;
				if ( page < 1 )
					page = 1;
				if ( page > maxPage )
					page = maxPage;
				outputWriter.println( "Players count: <b>" + playersCount + "</b><br>" );
				outputWriter.flush();
				
				// Filter section
				renderFiltersSection( outputWriter, nameFilter, page, pagerUrlWithoutNameFilter );
				
				// Pager links section
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PAGE ).append( '=' );
				renderPagerLinks( outputWriter, page, maxPage, pagerUrlBuilder.toString() );
				
				outputWriter.println( "</p>" );
				
				// Player data section
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query;
				if ( player1 == null )
					query = "SELECT player.id, player.name, COUNT(player.id), MIN(game.save_time), MAX(game.save_time), SUM(game.frames) FROM game_player JOIN player on player.id=game_player.player JOIN game on game.id=game_player.game" + ( nameFilter == null ? "" : " WHERE player.name LIKE ?" ) + " GROUP BY player.id, player.name";
				else
					query = "SELECT player.id, player.name, COUNT(player.id), MIN(game.save_time), MAX(game.save_time), SUM(game.frames) FROM game_player JOIN player on player.id=game_player.player JOIN game on game.id=game_player.game WHERE game_player.player" + ( hasAka1 ? " NOT IN (" + akaIdList1 + ")" : "!=" + player1 ) + " AND game_player.game IN (SELECT game FROM game_player WHERE player" + ( hasAka1 ? " IN (" + akaIdList1 + ")" : "=" + player1 ) + ")" + ( nameFilter == null ? "" : " AND player.name LIKE ?" ) + " GROUP BY player.id, player.name";
				
				query += " ORDER BY " + PLAYER_TABLE_HEADER.sortingColumns[ sortingIndex ] + ( sortingDesc ? " DESC" : "" )
				       + " LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
				renderSortingTableHeaderRow( outputWriter, PLAYER_TABLE_HEADER, pagerUrlWithoutSorting, sortingIndex, sortingDesc, page );
				if ( queryParam == null ) {
					statement = connection.createStatement();
					resultSet = statement.executeQuery( query );
				}
				else {
					statement2 = connection.prepareStatement( query );
					statement2.setString( 1, queryParam );
					resultSet = statement2.executeQuery();
				}
				while ( resultSet.next() ) {
					outputWriter.print( "<tr><td align=right>" + (++recordCounter) );
					final int playerId = resultSet.getInt( 1 );
					outputWriter.print( "<td>" + getPlayerDetailsHtmlLink( playerId, resultSet.getString( 2 ), null ) );
					outputWriter.print( "<td align=center>" + ( player1 == null ? getGameListOfPlayerHtmlLink ( playerId, Integer.toString( resultSet.getInt( 3 ) ), false )
							                                                    : getGameListOfPlayersHtmlLink( player1, playerId, Integer.toString( resultSet.getInt( 3 ) ), includeAkas ) + ( hasAka1 ? " *" : "" ) ) );
					outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 4 ) ) );
					outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 5 ) ) );
					outputWriter.print( "<td align=center>" + ReplayHeader.formatFrames( resultSet.getInt( 6 ), new StringBuilder(), true ) );
				}
				if ( queryParam == null )
					statement.close();
				else
					statement2.close();
				outputWriter.println( "</table>" );
				if ( hasAka1 )
					outputWriter.println( "<i>(* AKAs from the listed player are not included, only from " + encodeHtmlString( player1Name ) + ", the actual games count might be higher)</i>" );
			}
			else if ( entity.equals( ENTITY_AKA ) ) {
				
				outputWriter.print( "<h3>Registered AKA list" );
				outputWriter.println( "</h3>" );
				
				outputWriter.println( "<p>" );
				
				// Pages count section
				final int akaGroupsCount = nameFilter == null ? executeCountStatement( "SELECT COUNT(*) FROM aka_group", connection ) : executeCountStatement( "SELECT COUNT(DISTINCT aka_group.id) FROM aka_group JOIN player on aka_group.id=player.aka_group WHERE player.name LIKE ?", queryParam, connection );
				final int maxPage = ( akaGroupsCount - 1 ) / PAGE_SIZE + 1;
				if ( page < 1 )
					page = 1;
				if ( page > maxPage )
					page = maxPage;
				outputWriter.println( "AKA groups count: <b>" + akaGroupsCount + "</b><br>" );
				outputWriter.flush();
				
				// Filter section
				renderFiltersSection( outputWriter, nameFilter, page, pagerUrlWithoutNameFilter );
				
				// Pager links section
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PAGE ).append( '=' );
				renderPagerLinks( outputWriter, page, maxPage, pagerUrlBuilder.toString() );
				
				outputWriter.println( "</p>" );
				
				// Aka group data section
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query;
				if ( nameFilter == null )
					query = "SELECT id FROM aka_group WHERE 1=1 LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				else
					query = "SELECT DISTINCT id FROM aka_group JOIN player on aka_group.id=player.aka_group WHERE player.name LIKE ? LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
				renderSortingTableHeaderRow( outputWriter, AKA_GROUPS_TABLE_HEADER, pagerUrlWithoutSorting, sortingIndex, sortingDesc, page );
				if ( nameFilter == null ) {
					statement = connection.createStatement();
					resultSet = statement.executeQuery( query );
				}
				else {
					statement3 = connection.prepareStatement( query );
					statement3.setString( 1, queryParam );
					resultSet = statement3.executeQuery();
				}
				statement2 = connection.prepareStatement( "SELECT id, name FROM player WHERE aka_group=?" );
				while ( resultSet.next() ) {
					outputWriter.print( "<tr><td align=right>" + (++recordCounter) );
					final int akaGroupId = resultSet.getInt( 1 );
					
					statement2.setInt( 1, akaGroupId );
					resultSet2 = statement2.executeQuery();
					outputWriter.print( "<td>" + generatePlayerHtmlLinkListFromResultSet( resultSet2 ) );
					resultSet2.close();
				}
				statement2.close();
				resultSet.close();
				if ( nameFilter == null )
					statement.close();
				else
					statement3.close();
				outputWriter.println( "</table>" );
				
			}
			
			renderFooter( request, outputWriter );
		}
		catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		catch ( final SQLException se ) {
		}
		finally {
			if ( statement3 != null ) try { statement3.close(); } catch ( final SQLException se ) {}
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
		outputWriter.print( "<tr style='background:#cccccc'>" );
		
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
	 * Generates and returns a comma separated HTML link list of players read from the supplied result set.<br>
	 * The result set must contain player id's and player names. 
	 * @param resultSet result set to be read from
	 * @return a comma separated HTML link list of players read from the supplied result set
	 * @throws SQLException if sql exception occurs when reading from the result set 
	 */
	private static String generatePlayerHtmlLinkListFromResultSet( final ResultSet resultSet ) throws SQLException {
		final StringBuilder playersBuilder = new StringBuilder();
		
		while ( resultSet.next() ) {
			if ( playersBuilder.length() > 0 )
				playersBuilder.append( ", " );
			getPlayerDetailsHtmlLink( resultSet.getInt( 1 ), resultSet.getString( 2 ), playersBuilder );
		}
		
		return playersBuilder.toString();
	}
	
	/**
	 * Returns the comma separated id list of the akas of a player.<br>
	 * Returns <code>null</code> if the player does not have akas.
	 * @param playerId   id of the player
	 * @param connection connection to be used
	 * @return the comma separated id list of the akas of a player; or <code>null</code> if the player does not have akas
	 * @throws SQLException if SQLException occurs
	 */
	private static String getPlayerAkaIdList( final int playerId, final Connection connection ) throws SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery( "SELECT id FROM player WHERE aka_group=(SELECT aka_group FROM player WHERE id=" + playerId + ")" );
			final StringBuilder playerAkaIdListBuilder = new StringBuilder();
			while ( resultSet.next() ) {
				if ( playerAkaIdListBuilder.length() > 0 )
					playerAkaIdListBuilder.append( ',' );
				playerAkaIdListBuilder.append( resultSet.getInt( 1 ) );
			}
			return playerAkaIdListBuilder.length() == 0 ? null : playerAkaIdListBuilder.toString();
		}
		finally {
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
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
		Connection  connection   = null;
		Statement   statement    = null;
		ResultSet   resultSet    = null;
		Statement   statement2   = null;
		ResultSet   resultSet2   = null;
		
		try {
			connection = dataSource.getConnection();
			
			response.setCharacterEncoding( "UTF-8" );
			outputWriter = response.getWriter();
			
			renderHeader( request, outputWriter );
			
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
				outputWriter.println( "<h3>Details of player " + playerNameHtml + "</h3>" );
				
				statement  = connection.createStatement();
				resultSet  = statement.executeQuery( "SELECT COUNT(*), MIN(save_time), MAX(save_time), SUM(frames), SUM(CASE WHEN race=0 THEN 1 END), SUM(CASE WHEN race=1 THEN 1 END), SUM(CASE WHEN race=2 THEN 1 END) FROM game_player JOIN game on game.id=game_player.game WHERE player=" + entityId );
				final String akaIdList = getPlayerAkaIdList( entityId, connection );
				final boolean hasAka = akaIdList != null;
				if ( hasAka ) {
					statement2 = connection.createStatement();
					resultSet2 = statement.executeQuery( "SELECT id, name FROM player WHERE id IN (" + akaIdList + ") AND id!=" + entityId );
					outputWriter.println( "<p>AKAs: " + generatePlayerHtmlLinkListFromResultSet( resultSet2 ) + "</p>" );
					resultSet2.close();
					
					resultSet2 = statement.executeQuery( "SELECT COUNT(*), MIN(save_time), MAX(save_time), SUM(frames), SUM(CASE WHEN race=0 THEN 1 END), SUM(CASE WHEN race=1 THEN 1 END), SUM(CASE WHEN race=2 THEN 1 END) FROM game_player JOIN game on game.id=game_player.game WHERE player IN (" + akaIdList + ")" );
				}
				
				if ( resultSet.next() ) {
					if ( hasAka )
						resultSet2.next();
					outputWriter.print( "<table border=1 cellspacing=0 cellpadding=2>" );
					if ( hasAka ) outputWriter.print( "<tr><td><th>Details of " + playerNameHtml + "<th>Details with AKAs included" );
					
					final int gamesCount  = resultSet.getInt( 1 );
					final int gamesCount2 = hasAka ? resultSet2.getInt( 1 ) : 0;
					outputWriter.print( "<tr><th align=left>Games count:<td>" + gamesCount );
					if ( hasAka ) outputWriter.print( "<td>" + gamesCount2 );
					
					outputWriter.print( "<tr><th align=left>First game:<td>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 2 ) ) );
					if ( hasAka ) outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( resultSet2.getDate( 2 ) ) );
					outputWriter.print( "<tr><th align=left>Last game:<td>" + SIMPLE_DATE_FORMAT.format( resultSet.getDate( 3 ) ) );
					if ( hasAka ) outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( resultSet2.getDate( 3 ) ) );
					final int days  = 1 + (int) ( ( resultSet.getDate( 3 ).getTime() - resultSet.getDate( 2 ).getTime() ) / (1000l*60l*60l*24l) );
					final int days2 = hasAka ? 1 + (int) ( ( resultSet2.getDate( 3 ).getTime() - resultSet2.getDate( 2 ).getTime() ) / (1000l*60l*60l*24l) ) : 0;
					outputWriter.print( "<tr><th align=left>Presence:<td>" + formatDays( days ) );
					if ( hasAka ) outputWriter.print( "<td>" + formatDays( days2 ) );
					
					outputWriter.print( "<tr><th align=left>Total time in games:<td>" + ReplayHeader.formatFrames( resultSet.getInt( 4 ), new StringBuilder(), true ) );
					if ( hasAka ) outputWriter.print( "<td>" + ReplayHeader.formatFrames( resultSet2.getInt( 4 ), new StringBuilder(), true ) );
					outputWriter.print( "<tr><th align=left>Average games per day:<td>" + new Formatter().format( "%.2f", ( resultSet.getInt( 1 ) / (float) days ) ) );
					if ( hasAka ) outputWriter.print( "<td>" + new Formatter().format( "%.2f", ( resultSet2.getInt( 1 ) / (float) days2 ) ) );
					outputWriter.print( "<tr><th align=left>Race distribution:<td>Zerg: " + (int) ( resultSet.getInt( 5 ) * 100.0f / gamesCount + 0.5f ) + "%, Terran: " + (int) ( resultSet.getInt( 6 ) * 100.0f / gamesCount + 0.5f ) + "%, Protoss: " + (int) ( resultSet.getInt( 7 ) * 100.0f / gamesCount + 0.5f ) + "%" );
					if ( hasAka ) outputWriter.print( "<td>Zerg: " + (int) ( resultSet2.getInt( 5 ) * 100.0f / gamesCount2 + 0.5f ) + "%, Terran: " + (int) ( resultSet2.getInt( 6 ) * 100.0f / gamesCount2 + 0.5f ) + "%, Protoss: " + (int) ( resultSet2.getInt( 7 ) * 100.0f / gamesCount2 + 0.5f ) + "%" );
					outputWriter.print( "<tr><th align=left>Game list:<td>" + getGameListOfPlayerHtmlLink( entityId, "Games of " + playerNameHtml, false ) );
					if ( hasAka ) outputWriter.print( "<td>" + getGameListOfPlayerHtmlLink( entityId, "Games with AKAs included", true ) );
					outputWriter.print( "<tr><th align=left>Player list:<td>" + getPlayerListWhoPlayedWithAPlayerHtmlLink( entityId, "Who played with " + playerNameHtml + "?", false ) );
					if ( hasAka ) outputWriter.print( "<td>" + getPlayerListWhoPlayedWithAPlayerHtmlLink( entityId, "Players with AKAs included", true ) );
					outputWriter.print( "</table>" );
				}
				else
					outputWriter.println( "<p><b><i><font color='red'>The referred player could not be found!</font></i></b></p>" );
				
				if ( akaIdList != null ) {
					resultSet2.close();
					statement2.close();
				}
				resultSet.close();
				statement.close();
				
			}
			
			renderFooter( request, outputWriter );
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
	 * Renders the filter section.
	 * @param outputWriter output writer to use
	 * @param nameFilter   value of the name filter
	 * @param page         current page
	 * @param maxPage      max page
	 * @param pagerUrl     url to be used for the pager links; ends with '=' and only the target page number have to be appended
	 */
	private static void renderFiltersSection( final PrintWriter outputWriter, final String nameFilter, final int page, final String pagerUrlWithoutNameFilter ) {
		outputWriter.println( "Filter by name: <input type=text id='8347' onkeydown=\"javascritp:if(event.keyCode==13) document.getElementById('4358').onclick()\""
				+ ( nameFilter == null ? "" : " value='" + encodeHtmlString( nameFilter ) + "'" )
				+ "> <a id='4358' href='#' onclick=\"javascript:window.location='" + pagerUrlWithoutNameFilter
				+ '&' + PN_REQUEST_PARAM_NAME_PAGE + "=" + page
				+ "&" + PN_REQUEST_PARAM_NAME_NAME_FILTER + "='+escape(document.getElementById('8347').value);\">Apply</a>&nbsp;&nbsp;<a href='#' onclick=\"javascript:window.location='" + pagerUrlWithoutNameFilter
				+ '&' + PN_REQUEST_PARAM_NAME_PAGE + "=" + page + "'\">Clear</a><br>" );
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
			outputWriter.print( "<a href='" + pagerUrl + ( page - 1 ) + "'>Prev</a>" );
		}
		else {
			outputWriter.print( "First&nbsp;&nbsp;Prev" );
		}
		outputWriter.print( "&nbsp;&nbsp;|&nbsp;&nbsp;Page <b>" + page + "</b> out of <b>" + maxPage + "</b>.&nbsp;&nbsp;|&nbsp;&nbsp;" );
		if ( page < maxPage ) {
			outputWriter.print( "<a href='" + pagerUrl + ( page + 1 ) + "'>Next</a>&nbsp;&nbsp;" );
			outputWriter.print( "<a href='" + pagerUrl + maxPage + "'>Last</a>" );
		}
		else {
			outputWriter.print( "Next&nbsp;&nbsp;Last" );
		}
		outputWriter.print( "&nbsp;&nbsp;|&nbsp;&nbsp;To page: <input id='2130' type=text size=1 onkeydown=\"javascritp:if(event.keyCode==13) document.getElementById('3498').onclick()\"> <a id='3498' href='#' onclick=\"javascript:window.location='"
				+ pagerUrl + "'+escape(document.getElementById('2130').value);\">Jump</a>" );
	}
	
	/**
	 * Executes a count statement and returns its result.<br>
	 * If the query does not return anything, <code>0</code> is returned.
	 * @param countQuery the count SQL query
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
	 * Executes a count statement and returns its result.<br>
	 * If the query does not return anything, <code>0</code> is returned.
	 * @param countQuery the count SQL query
	 * @param queryParam query parameter
	 * @param connection connection to be used
	 * @return the result of the count query, or <code>0</code> if the query does not return anything
	 * @throws SQLException if SQLException occurs
	 */
	private static int executeCountStatement( final String countQuery, final String queryParam, final Connection connection ) throws SQLException {
		PreparedStatement statement = null;
		ResultSet         resultSet = null;
		
		try {
			statement = connection.prepareStatement( countQuery );
			statement.setString( 1, queryParam );
			resultSet = statement.executeQuery();
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
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery( "SELECT name FROM player WHERE id=" + id );
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
	 * @param playerId    id of the player
	 * @param text        text to appear in the link
	 * @param includeAkas tells if akas should be included (adds an extra parameter)
	 * @return an HTML link to the game list of a player
	 */
	private static String getGameListOfPlayerHtmlLink( final int playerId, final String text, final boolean includeAkas ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_GAME
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER1   + '=' + playerId
							 + ( includeAkas ? '&' + PN_REQUEST_PARAM_NAME_INCLUDE_AKAS : "" ) + "'>" + text + "</a>";
	}
	
	/**
	 * Generates and returns an HTML link to the game list of 2 players.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code>.
	 * @param player1Id   id of player #1
	 * @param player2Id   id of player #2
	 * @param text text   to appear in the link
	 * @param includeAkas tells if akas should be included (adds an extra parameter)
	 * @return an HTML link to the game list of a player
	 */
	private static String getGameListOfPlayersHtmlLink( final int player1Id, final int player2Id, final String text, final boolean includeAkas ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_GAME
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER1   + '=' + player1Id
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER2   + '=' + player2Id
							 + ( includeAkas ? '&' + PN_REQUEST_PARAM_NAME_INCLUDE_AKAS : "" ) + "'>" + text + "</a>";
	}
	
	/**
	 * Generates and returns an HTML link to the player list who played with a player.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code>.
	 * @param playerId    id of the player who's mates are searched for
	 * @param text        text to appear in the link
	 * @param includeAkas tells if akas should be included (adds an extra parameter)
	 * @return an HTML link to the player list who played with a player
	 */
	private static String getPlayerListWhoPlayedWithAPlayerHtmlLink( final int playerId, final String text, final boolean includeAkas ) {
		return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST
							 + '&' + PN_REQUEST_PARAM_NAME_ENTITY    + '=' + ENTITY_PLAYER
							 + '&' + PN_REQUEST_PARAM_NAME_PLAYER1   + '=' + playerId
							 + ( includeAkas ? '&' + PN_REQUEST_PARAM_NAME_INCLUDE_AKAS : "" ) + "'>" + text + "</a>";
	}
	
	/**
	 * Renders the header for the output pages.
	 * @param request      http request
	 * @param outputWriter writer to be used to render
	 */
	private static void renderHeader( final HttpServletRequest request, final PrintWriter outputWriter ) {
		request.setAttribute( "startTimeNanos", System.nanoTime() ); 
		outputWriter.println( "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>BWHF Players' Network</title>" );
		outputWriter.println( "<link rel='shortcut icon' href='favicon.ico' type='image/x-icon'><style>p,h2,h3 {margin:6;padding:0;}</style>" );
		outputWriter.println( "</head><body><center>" );
		outputWriter.println( "<h2>BWHF Players' Network</h2>" );
		outputWriter.println(           "<p><a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_GAME   + "'>Game list</a>"
				+ "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_PLAYER + "'>Player list</a>"
				+ "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_AKA    + "'>AKA list</a>"
				+ "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='http://code.google.com/p/bwhf/wiki/PlayersNetwork'>Help</a></p>" );
	}
	
	/**
	 * Renders the footer for the output pages.
	 * @param request      http request
	 * @param outputWriter writer to be used to render
	 */
	private static void renderFooter( final HttpServletRequest request, final PrintWriter outputWriter ) {
		final int  executionMs = (int) ( ( System.nanoTime() - (Long) request.getAttribute( "startTimeNanos" ) ) / 1000000l );
		
		outputWriter.println( "<hr><table border=0 width='100%'><tr><td width='40%' align='left'><a href='http://code.google.com/p/bwhf'>BWHF Agent home page</a>&nbsp;&nbsp;<a href='hackers'>BWHF Hacker Database</a>"
							+ "<td align=center width='20%'><i>served in " + (executionMs / 1000) + " sec, " + (executionMs % 1000) + " ms</i>"
							+ "<td align=right widht='40%'><i>&copy; Andr&aacute;s Belicza, 2008-2009</i></table>" );
		outputWriter.println( "</center></body></html>" );
	}
	
}
