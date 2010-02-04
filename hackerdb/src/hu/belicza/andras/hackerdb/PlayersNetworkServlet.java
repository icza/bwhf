package hu.belicza.andras.hackerdb;

import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_AKA;
import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_GAME;
import static hu.belicza.andras.hackerdb.ServerApiConsts.ENTITY_PLAYER;
import static hu.belicza.andras.hackerdb.ServerApiConsts.GATEWAYS;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_CHECK_RECORDS;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_DETAILS;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_LIST;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_OPERATION_SEND;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY_ID;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY_NAME;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_INCLUDE_AKAS;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_NAME_FILTER;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_OPERATION;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_PAGE;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_PLAYER1;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_PLAYER2;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_SORTING_DESC;
import static hu.belicza.andras.hackerdb.ServerApiConsts.PN_REQUEST_PARAM_NAME_SORTING_INDEX;
import static hu.belicza.andras.hackerdb.ServerApiConsts.REQUEST_PARAMETER_NAME_PLAYER;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle adding new games to the Players' Network database.
 * @author Andras Belicza
 */
public class PlayersNetworkServlet extends BaseServlet {
	
	/** Simple date format to format and parse replay save time.           */
	private static final DateFormat SIMPLE_DATE_FORMAT   = new SimpleDateFormat( "yyyy-MM-dd" );
	/** Full date format to format and parse replay save and report times. */
	private static final DateFormat FULL_DATE_FORMAT     = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	/** Simple date format to format dates for the activity chart.         */
	private static final DateFormat ACTIVITY_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM" );
	/** Earliest date of the replays. */
	private static final long       EARLIEST_REPLAY_DATE; 
	static {
		try {
			// Version 1.08 introduced replay which was released on 2001-05-18. -2 day for time zone issues.
			EARLIEST_REPLAY_DATE = SIMPLE_DATE_FORMAT.parse( "2001-05-16" ).getTime();
		} catch ( final ParseException pe ) {
			throw new RuntimeException( pe );
		}
	}
	/** Cached string for the "N/A" html string. */
	private static final String UNKOWN_HTML_STRING = encodeHtmlString( "N/A" );
	/** Size of the list in one page. */
	private static final int PAGE_SIZE = 25;
	/** Limit for short games to be excluded from some statistics (2 min). */
	private static final int SHORT_GAME_FRAME_LIMIT = (int) ( 1000.0f / 42 * 120 );
	/** Limit for obs mode frames per action (=30 APM).                    */
	private static final int OBS_MODE_FPA_LIMIT     = 1000 * 60 / 30 / 42;   // 1000/42   * 60  / 30    => 1000/42=frames/sec, 60=seconds/min, 30=APM limit
	
	/** Query part to match a player name to a hacker. */
	private static final String IS_HACKER_QUERY_PART = "(SELECT hacker.id FROM hacker JOIN report on report.hacker=hacker.id JOIN key on key.id=report.key WHERE name=player.name AND guarded=FALSE AND key.revocated=FALSE and report.revocated=FALSE LIMIT 1)";
	
	/**
	 * Creates a hacker tag that points to the reports of a named hacker.
	 * @param playerName name of player who to create the tag for
	 * @return the hacker tag HTML code
	 */
	private static String createHackerTagHtml( final String playerName ) {
		try {
			final StringBuilder tagBuilder = new StringBuilder( "<a class='hacker' href='hackers?" );
			tagBuilder.append( ServerApiConsts.FILTER_NAME_NAME ).append( '=' ).append( URLEncoder.encode( '"' + playerName + '"', "UTF-8" ) );
			tagBuilder.append( "'>R</a>" );
			return tagBuilder.toString();
		} catch ( final UnsupportedEncodingException uee ) {
			uee.printStackTrace();
			return "<span class='hacker'>R</span>";
		}
	}
	
	/**
	 * Defines the header of a table.<br>
	 * @author Andras Belicza
	 */
	private static class TableHeader {
		/** Defines which columns are sortable; <code>null</code> values specify columns which cannot be bases of sorting. */
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
	private static final TableHeader GAME_TABLE_HEADER = new TableHeader(
			new String[]  { "COALESCE(gateway,99)", "COALESCE(save_time,'1998-01-01')", "map_name", "frames"  , "type", "COALESCE(save_time,'1998-01-01')", "game.version", null      },
			new String[]  { "Details"             , "Engine"                          , "Map"     , "Duration", "Type", "Played on"                       , "Reported"    , "Players" },
			new boolean[] { false                 , true                              , false     , true      , false , true                              , true          , false     },
			6
	);
	/** Header of the player table. */
	private static final TableHeader PLAYER_TABLE_HEADER = new TableHeader(
			new String[]  { null , "name"       , "games_count"     , "COALESCE(first_game,'1998-01-01')"         , "COALESCE(last_game,'1998-01-01')"          , "total_frames"        },
			new String[]  { "#"  , "Player"     , "Games count"     , "First game"                                , "Last game"                                 , "Total time in games" },
			new boolean[] { false, false        , true              , true                                        , true                                        , true                  },
			2
	);
	/** Header of the player table with player. */
	private static final TableHeader PLAYER_TABLE_PLAYER_HEADER = new TableHeader(
			new String[]  { null , "player.name", "COUNT(player.id)", "COALESCE(MIN(game.save_time),'1998-01-01')", "COALESCE(MAX(game.save_time),'1998-01-01')", "SUM(game.frames)"    },
			new String[]  { "#"  , "Player"     , "Games count"     , "First game"                                , "Last game"                                 , "Total time in games" },
			new boolean[] { false, false        , true              , true                                        , true                                        , true                  },
			2
	);
	/** Header of the AKA groups table. */
	private static final TableHeader AKA_GROUPS_TABLE_HEADER = new TableHeader(
			new String[]  { null , "comment", null   },
			new String[]  { "#"  , "Person" , "AKAs" },
			new boolean[] { false, false    , false  },
			1
	);
	
	@Override
	public void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		setNoCache( response );
		response.setCharacterEncoding( "UTF-8" );
		try {
			request.setCharacterEncoding( "UTF-8" );
		} catch ( final UnsupportedEncodingException uee ) {
			// This will never happen.
			throw new RuntimeException( "Unsupported UTF-8 encoding?" ); 
		}
		
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
				
				int entityId = -1;
				String entityName = null;
				try {
					entityId = Integer.parseInt( request.getParameter( PN_REQUEST_PARAM_NAME_ENTITY_ID ) );
				}
				catch ( final Exception e ) {
					if ( entity.equals( ENTITY_PLAYER ) )
						entityName = request.getParameter( PN_REQUEST_PARAM_NAME_ENTITY_NAME );
					if ( entityName == null )
						throw new BadRequestException();
				}
				
				handleDetails( request, response, entity, entityId, entityName );
			} else if ( operation.equals( PN_OPERATION_CHECK_RECORDS ) ) {
				final String agentVersion = request.getParameter( ServerApiConsts.REQUEST_PARAMETER_NAME_AGENT_VERSION  );
				
				final String[] playerNames = new String[ 8 ];
				String playerName;
				for ( int i = 0; i < 8; i++ )
					if ( ( playerName = request.getParameter( REQUEST_PARAMETER_NAME_PLAYER + i ) ) != null )
						playerNames[ i ] = playerName;
				
				handleRecordCheck( response, agentVersion, playerNames );
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
	private void handleSend( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
		int engine = 0, frames = 0, mapWidth = 0, mapHeight = 0, speed = 0, type = 0, subType = 0;
		Long saveTime = null;
		String name = null, creatorName = null, mapName = null, replayMd5 = null, agentVersion = null;
		final List< String  > playerNameList    = new ArrayList< String  >( 8 );
		final List< Integer > playerRaceList    = new ArrayList< Integer >( 8 );
		final List< Integer > playerActionsList = new ArrayList< Integer >( 8 );
		final List< Integer > playerColorList   = new ArrayList< Integer >( 8 );
		Integer gateway = null;
		
		try {
			engine       = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_ENGINE ) );
			frames       = Integer.parseInt( request.getParameter( ServerApiConsts.GAME_PARAM_FRAMES ) );
			saveTime     = Long.valueOf( request.getParameter( ServerApiConsts.GAME_PARAM_SAVE_TIME ) );
			if ( saveTime < EARLIEST_REPLAY_DATE || saveTime > new Date().getTime() + 48*60*60*1000l ) // Allowing 2 days for time zone issues...
				saveTime = null;
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
			
			synchronized ( PlayersNetworkServlet.class ) {
				// No parallel processing is allowed, because currently we assume the max game id is what we inserted when connecting players to the game
				
				// This query has to be in the sync block too, else there might be duplicates (there were in the past)
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
			
				connection.setAutoCommit( false );
				
				// First create the players
				statement = connection.prepareStatement( "SELECT id FROM player WHERE name=?" );
				for ( final String playerName : playerNameList ) {
					// Check if player already exists
					statement.setString( 1, playerName );
					resultSet = statement.executeQuery();
					if ( !resultSet.next() ) {
						// Player doesn't exist, let's create it
						statement2 = connection.prepareStatement( "INSERT INTO player (name) VALUES (?)" );
						statement2.setString( 1, playerName );
						if ( statement2.executeUpdate() == 0 )
							throw new Exception( "Could not insert player!" );
						statement2.close();
					}
					resultSet.close();
				}
				statement.close();
				
				// Players exist now. Let's insert the game.
				statement = connection.prepareStatement( "INSERT INTO game (engine,frames,save_time,name,map_width,map_height,speed,type,sub_type,creator_name,map_name,replay_md5,agent_version,gateway,ip) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" );
				int colCounter = 1;
				statement.setInt      ( colCounter++, engine                    );
				statement.setInt      ( colCounter++, frames                    );
				if ( saveTime == null )
					statement.setNull( colCounter++, Types.TIMESTAMP );
				else
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
				
				// Insert the connections between the game and players
				statement = connection.prepareStatement( "INSERT INTO game_player (game,player,race,actions_count,color) VALUES ((select max(id) from game),(select id from player where name=?),?,?,?)" ); // We assume the last game is the one we inserted so no parallel processing allowed
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
				
				// Lastly update redundant data
				statement = connection.prepareStatement( 
						saveTime == null ? "UPDATE player SET games_count=games_count+1, total_frames=total_frames+? WHERE name=?"
										 : "UPDATE player SET games_count=games_count+1, first_game=CASE WHEN ?>first_game THEN first_game ELSE ? END, last_game=CASE WHEN ?<last_game THEN last_game ELSE ? END, total_frames=total_frames+? WHERE name=?" );
				for ( final String playerName : playerNameList ) {
					colCounter = 1;
					if ( saveTime != null ) {
						final Timestamp timestamp = new Timestamp( saveTime );
						statement.setTimestamp( colCounter++, timestamp );
						statement.setTimestamp( colCounter++, timestamp );
						statement.setTimestamp( colCounter++, timestamp );
						statement.setTimestamp( colCounter++, timestamp );
					}
					statement.setInt   ( colCounter++, frames     );
					statement.setString( colCounter++, playerName );
					
					if ( statement.executeUpdate() == 0 )
						throw new Exception( "Could not update player: " + playerName + "!" );
				}
				statement.close();
				connection.commit();
			}
			
			sendBackPlainMessage( ServerApiConsts.REPORT_ACCEPTED_MESSAGE, response );
		}
		catch ( final Exception e ) {
			if ( e instanceof IOException )
				throw (IOException) e;
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
	private void handleList( final HttpServletRequest request, final HttpServletResponse response, final String entity, int page ) throws IOException {
		PrintWriter       outputWriter = null;
		Connection        connection   = null;
		Statement         statement    = null;
		ResultSet         resultSet    = null;
		PreparedStatement statement2   = null;
		ResultSet         resultSet2   = null;
		PreparedStatement statement3   = null;
		
		try {
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
			final TableHeader tableHeader = entity.equals( ENTITY_GAME ) ? GAME_TABLE_HEADER : entity.equals( ENTITY_PLAYER ) ? ( player1 == null ? PLAYER_TABLE_HEADER : PLAYER_TABLE_PLAYER_HEADER ) : entity.equals( ENTITY_AKA ) ? AKA_GROUPS_TABLE_HEADER : null;
			if ( tableHeader != null && ( sortingIndex < 0 || sortingIndex >= tableHeader.sortingColumns.length || tableHeader.sortingColumns[ sortingIndex ] == null ) ) {
				sortingIndex = tableHeader.defaultSortingIndex;
				sortingDesc  = tableHeader.sortingDefaultDescs[ sortingIndex ];
			}
			pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_SORTING_INDEX ).append( '=' ).append( sortingIndex );
			if ( sortingDesc )
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_SORTING_DESC );
			String nameFilter = request.getParameter( PN_REQUEST_PARAM_NAME_NAME_FILTER );
			if ( nameFilter != null )
				nameFilter = nameFilter.length() == 0 ? null : nameFilter.toLowerCase();
			final String queryParam = nameFilter == null ? null : nameFilter.length() > 1 && nameFilter.charAt( 0 ) == '"' && nameFilter.charAt( nameFilter.length() - 1 ) == '"' ? nameFilter.substring( 1, nameFilter.length() - 1 ) : '%' + nameFilter + '%';
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
				String[] playerNames1 = null, playerNames2 = null;
				if ( player1 != null ) {
					outputWriter.print( " of " );
					playerNames1 = getPlayerName( player1, connection );
					outputWriter.print( getPlayerDetailsHtmlLink( player1, player1Name = playerNames1[ 0 ], playerNames1[ 1 ] != null, null ) );
					if ( player2 != null ) {
						if ( nameFilter == null )
							countQuery = "SELECT COUNT(*) FROM (SELECT game FROM game_player WHERE player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 + " OR player=" + player2 ) + " GROUP BY game HAVING COUNT(*)=2) as foo";
						else
							countQuery = "SELECT COUNT(*) FROM (SELECT game FROM game_player JOIN game on game.id=game_player.game WHERE player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 + " OR player=" + player2 ) + " AND map_name LIKE ? GROUP BY game HAVING COUNT(*)=2) as foo";
						outputWriter.print( " and " );
						playerNames2 = getPlayerName( player2, connection );
						outputWriter.print( getPlayerDetailsHtmlLink( player2, player2Name = playerNames2[ 0 ], playerNames2[ 1 ] != null, null ) );
					}
					else {
						if ( nameFilter == null )
							countQuery = "SELECT COUNT(*) FROM game_player WHERE player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 ); // No need distinct, 1 player is only once at the most in a game
						else
							countQuery = "SELECT COUNT(*) FROM game_player JOIN game on game.id=game_player.game WHERE player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 ) + " AND map_name LIKE ?"; // No need distinct, 1 player is only once at the most in a game
					}
				}
				else
					countQuery = "SELECT COUNT(*) FROM game" + ( nameFilter == null ? "" : " WHERE map_name LIKE ?" );
				outputWriter.println( "</h3>" );
				if ( hasAka ) {
					statement = connection.createStatement();
					outputWriter.print( "<p>AKAs included " );
					if ( hasAka1 ) {
						outputWriter.print( "from <b>" + encodeHtmlString( player1Name ) + ( playerNames1[ 1 ] == null ? "" : createHackerTagHtml( player1Name ) ) + "</b>: " );
						resultSet = statement.executeQuery( "SELECT id, name, " + IS_HACKER_QUERY_PART + " FROM player WHERE id IN (" + akaIdList1 + ") AND id!=" + player1 );
						outputWriter.print( generatePlayerHtmlLinkListFromResultSet( resultSet ) );
						resultSet.close();
					}
					if ( hasAka2 ) {
						outputWriter.print( ( player1Name == null ? "" : "; " ) + "from <b>" + encodeHtmlString( player2Name ) + ( playerNames2[ 1 ] == null ? "" : createHackerTagHtml( player2Name ) ) + "</b>: " );
						resultSet = statement.executeQuery( "SELECT id, name, " + IS_HACKER_QUERY_PART + " FROM player WHERE id IN (" + akaIdList2 + ") AND id!=" + player2 );
						outputWriter.print( generatePlayerHtmlLinkListFromResultSet( resultSet ) );
						resultSet.close();
					}
					statement.close();
					outputWriter.print( "</p>" );
				}
				
				// Pages count section
				final int gamesCount = queryParam == null ? executeCountStatement( countQuery, connection ) : executeCountStatement( countQuery, queryParam, connection );
				outputWriter.println( "<p>Games count: <b>" + DECIMAL_FORMAT.format( gamesCount ) + "</b><br>" );
				outputWriter.flush();
				
				// Filter section
				renderFiltersSection( outputWriter, "Filter by map name", nameFilter, page, pagerUrlWithoutNameFilter );
				
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
					query = "SELECT id, engine, COALESCE(save_time,'1998-01-01'), map_name, frames, type, COALESCE(gateway,99), game.version FROM game" + ( nameFilter == null ? "" : " WHERE map_name LIKE ?" );
				else if ( player2 == null )
					query = "SELECT game.id, engine, COALESCE(save_time,'1998-01-01'), map_name, frames, type, COALESCE(gateway,99), game.version FROM game JOIN game_player on game.id=game_player.game WHERE " + ( nameFilter == null ? "" : "map_name LIKE ? AND " ) + "game_player.player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 );
				else
					query = "SELECT DISTINCT game.id, engine, COALESCE(save_time,'1998-01-01'), map_name, frames, type, COALESCE(gateway,99), game.version FROM game JOIN game_player on game_player.game=game.id WHERE " + ( nameFilter == null ? "" : "map_name LIKE ? AND " ) + "(game_player.player" + ( hasAka ? " IN (" + akaIdList + ")" : "=" + player1 + " OR game_player.player=" + player2 ) + ") GROUP BY game.id, engine, save_time, map_name, frames, type, gateway, game.version HAVING COUNT(*)=2";
				
				query += " ORDER BY " + tableHeader.sortingColumns[ sortingIndex ] + ( sortingDesc ? " DESC" : "" )
				       + " LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				if ( queryParam == null ) {
					statement = connection.createStatement();
					resultSet = statement.executeQuery( query );
				}
				else {
					statement3 = connection.prepareStatement( query );
					statement3.setString( 1, queryParam );
					resultSet = statement3.executeQuery();
				}
				
				outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
				renderSortingTableHeaderRow( outputWriter, tableHeader, pagerUrlWithoutSorting, sortingIndex, sortingDesc, page );
				final ReplayHeader replayHeader = new ReplayHeader();
				int colCounter;
				statement2 = connection.prepareStatement( "SELECT player.id, player.name, " + IS_HACKER_QUERY_PART + " FROM game_player JOIN player on game_player.player=player.id WHERE game_player.game=?" );
				while ( resultSet.next() ) {
					colCounter = 1;
					final int gameId = resultSet.getInt( colCounter++ );
					replayHeader.guessedVersion = null; // This is cached so I have to clear it...
					replayHeader.gameEngine = (byte) resultSet.getInt( colCounter++ );
					replayHeader.saveTime   = resultSet.getDate( colCounter++ );
					if ( replayHeader.saveTime.getTime() < EARLIEST_REPLAY_DATE )
						replayHeader.saveTime = null;
					replayHeader.mapName    = resultSet.getString( colCounter++ );
					replayHeader.gameFrames = resultSet.getInt( colCounter++ );
					replayHeader.gameType   = (short) resultSet.getInt( colCounter++ );
					
					final Integer gateway = resultSet.getInt( 7 );
					outputWriter.print( "<tr><td align=right class=" + ( gateway < GATEWAY_STYLE_NAMES.length ? GATEWAY_STYLE_NAMES[ gateway ] : UNKNOWN_GATEWAY_STYLE_NAME ) + ">" + getGameDetailsHtmlLink( gameId, DECIMAL_FORMAT.format( ++recordCounter ) ) + "&nbsp;" );
					outputWriter.print( "<td>" + ReplayHeader.GAME_ENGINE_SHORT_NAMES[ replayHeader.gameEngine ] + " " + ( replayHeader.saveTime == null ? "" : replayHeader.guessVersionFromDate() ) );
					outputWriter.print( "<td>" + getGameListWithMapHtmlLink( replayHeader.mapName, replayHeader.mapName, player1, player2, includeAkas) );
					outputWriter.print( "<td align=right>" + replayHeader.getDurationString( false ) );
					outputWriter.print( "<td align=center>" + getGameTypeName( replayHeader.gameType, false ) );
					outputWriter.print( "<td>" + ( replayHeader.saveTime == null ? UNKOWN_HTML_STRING : SIMPLE_DATE_FORMAT.format( replayHeader.saveTime ) ) );
					outputWriter.print( "<td>" + SIMPLE_DATE_FORMAT.format( resultSet.getTimestamp( 8 ) ) );
					
					statement2.setInt( 1, gameId );
					resultSet2 = statement2.executeQuery();
					outputWriter.print( "<td>" + generatePlayerHtmlLinkListFromResultSet( resultSet2 ) );
					resultSet2.close();
				}
				statement2.close();
				outputWriter.println( "</table>" );
				
				if ( queryParam == null )
					statement.close();
				else
					statement3.close();
				
			}
			else if ( entity.equals( ENTITY_PLAYER ) ) {
				
				outputWriter.print( "<h3>Player list" );
				String countQuery;
				String player1Name = null;
				if ( player1 != null ) {
					outputWriter.print( " who played with " );
					final String[] playerNames = getPlayerName( player1, connection );
					outputWriter.print( getPlayerDetailsHtmlLink( player1, player1Name = playerNames[ 0 ], playerNames[ 1 ] != null, null ) );
					if ( nameFilter == null )
						countQuery = "SELECT COUNT(DISTINCT player) FROM game_player WHERE player" + ( hasAka1 ? " NOT IN (" + akaIdList1 + ")" : "!=" + player1 ) + " AND game IN (SELECT game FROM game_player WHERE player" + ( hasAka1 ? " IN (" + akaIdList1 + ")" : "=" + player1 ) + ")";
					else
						countQuery = "SELECT COUNT(DISTINCT player) FROM game_player JOIN player on game_player.player=player.id WHERE player.name LIKE ? AND player" + ( hasAka1 ? " NOT IN (" + akaIdList1 + ")" : "!=" + player1 ) + " AND game IN (SELECT game FROM game_player WHERE player" + ( hasAka1 ? " IN (" + akaIdList1 + ")" : "=" + player1 ) + ")";
				}
				else
					countQuery = "SELECT COUNT(*) FROM player WHERE " + ( nameFilter == null ? "is_computer=false" : "name LIKE ?" );
				
				outputWriter.println( "</h3>" );
				if ( hasAka1 ) {
					statement = connection.createStatement();
					outputWriter.print( "<p>AKAs included: " );
					resultSet = statement.executeQuery( "SELECT id, name, " + IS_HACKER_QUERY_PART + " FROM player WHERE id IN (" + akaIdList1 + ") AND id!=" + player1 );
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
				outputWriter.println( "Players count: <b>" + DECIMAL_FORMAT.format( playersCount ) + "</b><br>" );
				outputWriter.flush();
				
				// Filter section
				renderFiltersSection( outputWriter, "Filter by name", nameFilter, page, pagerUrlWithoutNameFilter );
				
				// Pager links section
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PAGE ).append( '=' );
				renderPagerLinks( outputWriter, page, maxPage, pagerUrlBuilder.toString() );
				
				outputWriter.println( "</p>" );
				
				// Player data section
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query;
				if ( player1 == null )
					query = "SELECT id, name, games_count, first_game, last_game, total_frames FROM player WHERE " + ( nameFilter == null ? "is_computer=false" : "player.name LIKE ?" );
				else
					query = "SELECT player.id, player.name, COUNT(player.id), MIN(game.save_time), MAX(game.save_time), SUM(game.frames) FROM game_player JOIN player on player.id=game_player.player JOIN game on game.id=game_player.game WHERE player.id" + ( hasAka1 ? " NOT IN (" + akaIdList1 + ")" : "!=" + player1 ) + " AND game.id IN (SELECT game FROM game_player WHERE player" + ( hasAka1 ? " IN (" + akaIdList1 + ")" : "=" + player1 ) + ")" + ( nameFilter == null ? "" : " AND player.name LIKE ?" ) + " GROUP BY player.id, player.name";
				
				query += " ORDER BY " + tableHeader.sortingColumns[ sortingIndex ] + ( sortingDesc ? " DESC" : "" )
				       + " LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
				renderSortingTableHeaderRow( outputWriter, tableHeader, pagerUrlWithoutSorting, sortingIndex, sortingDesc, page );
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
					outputWriter.print( "<tr><td align=right>" + DECIMAL_FORMAT.format( ++recordCounter ) );
					final int playerId = resultSet.getInt( 1 );
					outputWriter.print( "<td>" + getPlayerDetailsHtmlLink( playerId, resultSet.getString( 2 ), getPlayerName( playerId, connection )[ 1 ] != null, null ) );
					outputWriter.print( "<td align=center>" + ( player1 == null ? getGameListOfPlayerHtmlLink ( playerId, DECIMAL_FORMAT.format( resultSet.getInt( 3 ) ), false )
							                                                    : getGameListOfPlayersHtmlLink( player1, playerId, DECIMAL_FORMAT.format( resultSet.getInt( 3 ) ), includeAkas ) + ( hasAka1 ? " *" : "" ) ) );
					final Date firstGameDate = resultSet.getDate( 4 );
					final Date lastGameDate  = resultSet.getDate( 5 );
					outputWriter.print( "<td>" + ( firstGameDate == null ? UNKOWN_HTML_STRING : SIMPLE_DATE_FORMAT.format( firstGameDate ) ) );
					outputWriter.print( "<td>" + ( lastGameDate  == null ? UNKOWN_HTML_STRING : SIMPLE_DATE_FORMAT.format( lastGameDate  ) ) );
					outputWriter.print( "<td align=center>" + ReplayHeader.formatLongFrames( resultSet.getLong( 6 ), new StringBuilder(), true ) );
				}
				if ( queryParam == null )
					statement.close();
				else
					statement2.close();
				outputWriter.println( "</table>" );
				if ( hasAka1 )
					outputWriter.println( "<span class='note'>(* AKAs from the listed player are not included, only from " + encodeHtmlString( player1Name ) + ", the actual games count might be higher)</span>" );
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
				outputWriter.println( "AKA groups count: <b>" + DECIMAL_FORMAT.format( akaGroupsCount ) + "</b><br>" );
				outputWriter.flush();
				
				// Filter section
				renderFiltersSection( outputWriter, "Filter by name", nameFilter, page, pagerUrlWithoutNameFilter );
				
				// Pager links section
				pagerUrlBuilder.append( '&' ).append( PN_REQUEST_PARAM_NAME_PAGE ).append( '=' );
				renderPagerLinks( outputWriter, page, maxPage, pagerUrlBuilder.toString() );
				
				outputWriter.println( "</p>" );
				
				// Aka group data section
				int recordCounter = ( page - 1 ) * PAGE_SIZE;
				String query;
				if ( nameFilter == null )
					query = "SELECT id, comment FROM aka_group";
				else
					query = "SELECT DISTINCT aka_group.id, comment FROM aka_group JOIN player on aka_group.id=player.aka_group WHERE player.name LIKE ?";
				
				query += " ORDER BY " + tableHeader.sortingColumns[ sortingIndex ] + ( sortingDesc ? " DESC" : "" )
			       + " LIMIT " + PAGE_SIZE + " OFFSET " + recordCounter;
				
				outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2 width=700>" );
				renderSortingTableHeaderRow( outputWriter, tableHeader, pagerUrlWithoutSorting, sortingIndex, sortingDesc, page );
				if ( nameFilter == null ) {
					statement = connection.createStatement();
					resultSet = statement.executeQuery( query );
				}
				else {
					statement3 = connection.prepareStatement( query );
					statement3.setString( 1, queryParam );
					resultSet = statement3.executeQuery();
				}
				statement2 = connection.prepareStatement( "SELECT id, name, " + IS_HACKER_QUERY_PART + " FROM player WHERE aka_group=?" );
				while ( resultSet.next() ) {
					outputWriter.print( "<tr><td align=right>" + DECIMAL_FORMAT.format( ++recordCounter ) );
					final int akaGroupId = resultSet.getInt( 1 );
					
					outputWriter.print( "<td>" + encodeHtmlString( resultSet.getString( 2 ) ) );
					
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
		catch ( final SQLException se ) {
			se.printStackTrace();
		}
		finally {
			if ( statement3 != null ) try { statement3.close(); } catch ( final SQLException se ) {}
			if ( resultSet2 != null ) try { resultSet2.close(); } catch ( final SQLException se ) {}
			if ( statement2 != null ) try { statement2.close(); } catch ( final SQLException se ) {}
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
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
		outputWriter.print( "<tr class=" + TABLE_HEADER_STYLE_NAME + ">" );
		
		for ( int i = 0; i < tableHeader.headers.length; i++ )
			if ( tableHeader.sortingColumns[ i ] == null )
				outputWriter.print( "<th class=" + NON_SORTING_COLUMN_STYLE_NAME + ">" + tableHeader.headers[ i ] );
			else {
				outputWriter.print( "<th class=" + SORTING_COLUMN_STYLE_NAME + " onclick=\"javascript:window.location='" 
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
			getPlayerDetailsHtmlLink( resultSet.getInt( 1 ), resultSet.getString( 2 ), resultSet.getObject( 3 ) != null, playersBuilder );
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
	 * @param request    http request
	 * @param response   http response
	 * @param entity     entity to be detailed
	 * @param entityId   id of entity to be detailed
	 * @param entityName optional, if id is not known, name can be provided in some cases; if provided (not <code>null</code>), entityId is irrelevant
	 */
	private void handleDetails( final HttpServletRequest request, final HttpServletResponse response, final String entity, int entityId, final String entityName ) throws IOException {
		PrintWriter outputWriter = null;
		Connection  connection   = null;
		Statement   statement    = null;
		ResultSet   resultSet    = null;
		Statement   statement2   = null;
		ResultSet   resultSet2   = null;
		
		try {
			connection = dataSource.getConnection();
			
			outputWriter = response.getWriter();
			
			renderHeader( request, outputWriter );
			
			if ( entity.equals( ENTITY_GAME ) ) {
				
				outputWriter.println( "<h3>Details of game id=" + entityId + " </h3>" );
				
				statement = connection.createStatement();
				resultSet = statement.executeQuery( "SELECT engine, frames, save_time, name, map_width, map_height, type, creator_name, map_name, COALESCE(gateway,-1), version FROM game WHERE id=" + entityId );
				
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
					final int gateway        = resultSet.getInt( colCounter++ );
					
					outputWriter.println( "<table border=1 cellspacing=0 cellpadding=2>" );
					outputWriter.println( "<tr><th align=left>Game engine:<td>" + replayHeader.getGameEngineString() );
					outputWriter.println( "<tr><th align=left>Version:<td>" + ( replayHeader.saveTime == null ? UNKOWN_HTML_STRING : replayHeader.guessVersionFromDate() ) );
					outputWriter.println( "<tr><th align=left>Duration:<td>" + replayHeader.getDurationString( false ) );
					outputWriter.println( "<tr><th align=left>Saved on:<td>" + ( replayHeader.saveTime == null ? UNKOWN_HTML_STRING : FULL_DATE_FORMAT.format( replayHeader.saveTime ) ) );
					outputWriter.println( "<tr><th align=left>Reported on:<td>" + FULL_DATE_FORMAT.format( resultSet.getTimestamp( colCounter++ ) ) );
					outputWriter.println( "<tr><th align=left>Game name:<td>" + replayHeader.gameName );
					outputWriter.println( "<tr><th align=left>Map name:<td>" + getGameListWithMapHtmlLink( replayHeader.mapName, replayHeader.mapName, null, null, false ) );
					outputWriter.println( "<tr><th align=left>Map size:<td>" + replayHeader.getMapSize() );
					outputWriter.println( "<tr><th align=left>Creator name:<td>" + replayHeader.creatorName );
					outputWriter.println( "<tr><th align=left>Game type:<td>" + getGameTypeName( replayHeader.gameType, true ) );
					if ( gateway >= 0 && gateway < GATEWAYS.length )
						outputWriter.print( "<tr><th align=left>Reported gateway:<td class=" + ( gateway < GATEWAY_STYLE_NAMES.length ? GATEWAY_STYLE_NAMES[ gateway ] : UNKNOWN_GATEWAY_STYLE_NAME ) + ">" + GATEWAYS[ gateway ] );
					
					final int seconds = replayHeader.getDurationSeconds();
					statement2 = connection.createStatement();
					resultSet2 = statement2.executeQuery( "SELECT player.id, player.name, game_player.race, game_player.actions_count, game_player.color, " + IS_HACKER_QUERY_PART + " FROM game_player JOIN player on game_player.player=player.id WHERE game_player.game=" + entityId + " ORDER BY game_player.actions_count DESC" );
					outputWriter.print( "<tr><td colspan=2 align=center><table border=0 cellspacing=6><tr><th>Player<th>Race<th>Actions<th>APM<th>Color" );
					while ( resultSet2.next() ) {
						outputWriter.print( "<tr><td>" );
						outputWriter.print( getPlayerDetailsHtmlLink( resultSet2.getInt( 1 ), resultSet2.getString( 2 ), resultSet2.getObject( 6 ) != null, null ) );
						String colorName;
						try {
							colorName = ReplayHeader.IN_GAME_COLOR_NAMES[ resultSet2.getInt( 5 ) ];
						}
						catch ( final Exception e ) {
							colorName = UNKOWN_HTML_STRING;
						}
						outputWriter.print( "<td>" + ( resultSet2.getInt( 3 ) < ReplayHeader.RACE_NAMES.length ? ReplayHeader.RACE_NAMES[ resultSet2.getInt( 3 ) ] : UNKOWN_HTML_STRING ) );
						outputWriter.print( "<td align=right>" + DECIMAL_FORMAT.format( resultSet2.getInt( 4 ) ) );
						outputWriter.println( "<td align=right>" + resultSet2.getInt( 4 ) * 60 / Math.max( seconds, 1 ) + "<td>" + colorName );
					}
					outputWriter.println( "</table>" );
					resultSet2.close();
					statement2.close();
					
					outputWriter.print( "</table>" );
				}
				else
					outputWriter.println( "<p><b><i><font color='red'>The referred game could not be found!</font></i></b></p>" );
				resultSet.close();
				statement.close();
				
			} else if ( entity.equals( ENTITY_PLAYER ) ) {
				
				if ( entityName != null ) {
					final PreparedStatement ps = connection.prepareStatement( "SELECT id FROM player WHERE name=?" );
					statement = ps; // Just so it will be closed in case of errors
					ps.setString( 1, entityName.toLowerCase() );
					resultSet = ps.executeQuery();
					entityId = resultSet.next() ? resultSet.getInt( 1 ) : -1;
					resultSet.close();
					ps.close();
					statement = null;
				}
				
				final String[] playerNames = getPlayerName( entityId, connection );
				final String playerName     = playerNames[ 0 ];
				final String playerNameHtml = encodeHtmlString( playerName );
				outputWriter.println( "<h3>Details of player " + playerNameHtml + ( playerNames[ 1 ] == null ? "" : createHackerTagHtml( playerName ) ) + "</h3>" );
				
				statement  = connection.createStatement();
				String query = "SELECT COUNT(*), MIN(save_time), MAX(save_time), SUM(frames), SUM(CASE WHEN race=0 THEN 1 END), SUM(CASE WHEN race=1 THEN 1 END), SUM(CASE WHEN race=2 THEN 1 END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " THEN 1 END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " THEN frames END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " AND actions_count*" + OBS_MODE_FPA_LIMIT + ">frames THEN frames END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " AND actions_count*" + OBS_MODE_FPA_LIMIT + ">frames THEN actions_count END) FROM game_player JOIN game on game.id=game_player.game";
				
				resultSet  = statement.executeQuery( query + " WHERE player=" + entityId );
				final String akaIdList = getPlayerAkaIdList( entityId, connection );
				final boolean hasAka = akaIdList != null;
				if ( hasAka ) {
					statement2 = connection.createStatement();
					resultSet2 = statement2.executeQuery( "SELECT id, name, " + IS_HACKER_QUERY_PART + " FROM player WHERE id IN (" + akaIdList + ") AND id!=" + entityId );
					outputWriter.println( "<p>AKAs: " + generatePlayerHtmlLinkListFromResultSet( resultSet2 ) + "</p>" );
					resultSet2.close();
					
					resultSet2 = statement2.executeQuery( query + " WHERE player IN (" + akaIdList + ")" );
				}
				
				// Player statistics table
				outputWriter.print( "<table border=1 cellspacing=0 cellpadding=2>" );
				if ( hasAka ) outputWriter.print( "<tr><td><th>Details of " + playerNameHtml + "<th>Details with AKAs included" );
				
				int gamesCount  = 1;
				int gamesCount2 = 1;
				int averageApm  = 0;
				int averageApm2 = 0;
				if ( resultSet.next() ) {
					if ( hasAka )
						resultSet2.next();
					
					gamesCount  = resultSet.getInt( 1 );
					gamesCount2 = hasAka ? resultSet2.getInt( 1 ) : 0;
					
					outputWriter.print( "<tr><th align=left>Games count:<td>" + getGameListOfPlayerHtmlLink ( entityId, DECIMAL_FORMAT.format( gamesCount ), false ) );
					if ( hasAka ) outputWriter.print( "<td>" + getGameListOfPlayerHtmlLink ( entityId, DECIMAL_FORMAT.format( gamesCount2 ), true ) );
					outputWriter.print( "<tr><th align=left>Player list:<td>" + getPlayerListWhoPlayedWithAPlayerHtmlLink( entityId, "Who played with " + playerNameHtml + "?", false ) );
					if ( hasAka ) outputWriter.print( "<td>" + getPlayerListWhoPlayedWithAPlayerHtmlLink( entityId, "Players with AKAs included", true ) );
					
					final Date firstGameDate = resultSet.getDate( 2 );
					outputWriter.print( "<tr><th align=left>First game:<td>" + ( firstGameDate == null ? UNKOWN_HTML_STRING : SIMPLE_DATE_FORMAT.format( firstGameDate ) ) );
					Date firstGameDate2 = null;
					if ( hasAka ) { firstGameDate2 = resultSet2.getDate( 2 ); outputWriter.print( "<td>" + ( firstGameDate2 == null ? UNKOWN_HTML_STRING : SIMPLE_DATE_FORMAT.format( firstGameDate2 ) ) ); }
					final Date lastGameDate = resultSet.getDate( 3 );
					outputWriter.print( "<tr><th align=left>Last game:<td>" + ( lastGameDate == null ? UNKOWN_HTML_STRING : SIMPLE_DATE_FORMAT.format( lastGameDate ) ) );
					Date lastGameDate2 = null;
					if ( hasAka ) { lastGameDate2 = resultSet2.getDate( 3 ); outputWriter.print( "<td>" + ( lastGameDate2 == null ? UNKOWN_HTML_STRING : SIMPLE_DATE_FORMAT.format( lastGameDate2 ) ) ); }
					int days = 0, days2 = 0;
					if ( firstGameDate != null )  // if first game date not null => last not null
						days  = 1 + (int) ( ( lastGameDate.getTime() - firstGameDate.getTime() ) / (1000l*60l*60l*24l) );
					if ( firstGameDate2 != null ) // if first game date not null => last not null
						days2 = hasAka ? 1 + (int) ( ( lastGameDate2.getTime() - firstGameDate2.getTime() ) / (1000l*60l*60l*24l) ) : 0;
					outputWriter.print( "<tr><th align=left>Presence:<td>" + ( firstGameDate == null ? UNKOWN_HTML_STRING : formatDays( days ) ) );
					if ( hasAka ) outputWriter.print( "<td>" + ( firstGameDate2 == null ? UNKOWN_HTML_STRING : formatDays( days2 ) ) );
					
					outputWriter.print( "<tr><th align=left>Total time in games:<td>" + ReplayHeader.formatLongFrames( resultSet.getLong( 4 ), new StringBuilder(), true ) );
					if ( hasAka ) outputWriter.print( "<td>" + ReplayHeader.formatLongFrames( resultSet2.getLong( 4 ), new StringBuilder(), true ) );
					outputWriter.print( "<tr><th align=left>Average games per day:<td>" + ( firstGameDate == null ? UNKOWN_HTML_STRING : new Formatter( Locale.ENGLISH ).format( "%.2f", ( resultSet.getInt( 1 ) / (float) days ) ) ) );
					if ( hasAka ) outputWriter.print( "<td>" + ( firstGameDate2 == null ? UNKOWN_HTML_STRING : new Formatter( Locale.ENGLISH ).format( "%.2f", ( resultSet2.getInt( 1 ) / (float) days2 ) ) ) );
					outputWriter.print( "<tr><th align=left>Average game length:<td>" + ( resultSet.getInt( 8 ) > 0 ? ReplayHeader.formatLongFrames( resultSet.getLong( 9 ) / resultSet.getInt( 8 ), new StringBuilder(), true ) + " *" : UNKOWN_HTML_STRING ) );
					if ( hasAka ) outputWriter.print( "<td>" + ( resultSet2.getInt( 8 ) > 0 ? ReplayHeader.formatLongFrames( resultSet2.getLong( 9 ) / resultSet2.getInt( 8 ), new StringBuilder(), true ) + " *" : UNKOWN_HTML_STRING ) );
					averageApm = (int) (resultSet.getLong( 10 ) > 0 ? ( resultSet.getInt( 11 ) * 60l /  ReplayHeader.convertLongFramesToSeconds( resultSet.getLong( 10 ) ) ) : 0l );
					outputWriter.print( "<tr><th align=left>Average APM:<td>" + ( averageApm > 0 ? averageApm + " **" : UNKOWN_HTML_STRING ) );
					if ( hasAka ) {
						averageApm2 = (int) (resultSet2.getLong( 10 ) > 0 ? ( resultSet2.getInt( 11 ) * 60l /  ReplayHeader.convertLongFramesToSeconds( resultSet2.getLong( 10 ) ) ) : 0l );
						outputWriter.print( "<td>" + ( averageApm2 > 0 ? averageApm2 + " **" : UNKOWN_HTML_STRING ) );
					}
					outputWriter.print( "<tr><th align=left>Race distribution:<td>Zerg: " + (int) ( resultSet.getInt( 5 ) * 100.0f / gamesCount + 0.5f ) + "%, Terran: " + (int) ( resultSet.getInt( 6 ) * 100.0f / gamesCount + 0.5f ) + "%, Protoss: " + (int) ( resultSet.getInt( 7 ) * 100.0f / gamesCount + 0.5f ) + "%" );
					if ( hasAka ) outputWriter.print( "<td>Zerg: " + (int) ( resultSet2.getInt( 5 ) * 100.0f / gamesCount2 + 0.5f ) + "%, Terran: " + (int) ( resultSet2.getInt( 6 ) * 100.0f / gamesCount2 + 0.5f ) + "%, Protoss: " + (int) ( resultSet2.getInt( 7 ) * 100.0f / gamesCount2 + 0.5f ) + "%" );
					
					final int RANDOM_O_METER_WIDTH  = 80;
					final int RANDOM_O_METER_HEIGHT = 40;
					final String RANDOM_O_METER_URL = "http://chart.apis.google.com/chart?chs=" + RANDOM_O_METER_WIDTH + "x" + RANDOM_O_METER_HEIGHT + "&amp;cht=gom&amp;chf=bg,s,ffffff00&amp;chd=t:";
					outputWriter.print( "<tr><th align=left>Random-o-meter&trade;: <span style='font-size:80%'>(<a href='http://code.google.com/p/bwhf/wiki/PlayersNetwork#Player_details'>?</a>)<td><img src='" + RANDOM_O_METER_URL + calculateRandomOMeter( resultSet.getInt( 5 ), resultSet.getInt( 6 ), resultSet.getInt( 7 ), gamesCount ) + "' width=" + RANDOM_O_METER_WIDTH + " height=" + RANDOM_O_METER_HEIGHT + ">" );
					if ( hasAka ) outputWriter.print( "<td><img src='" + RANDOM_O_METER_URL + calculateRandomOMeter( resultSet2.getInt( 5 ), resultSet2.getInt( 6 ), resultSet2.getInt( 7 ), gamesCount2 ) + "' width=" + RANDOM_O_METER_WIDTH + " height=" + RANDOM_O_METER_HEIGHT + ">" ); 
				}
				else
					outputWriter.println( "<p><b><i><font color='red'>The referred player could not be found!</font></i></b></p>" );
				
				if ( hasAka )
					resultSet2.close();
				resultSet.close();
				
				// Gateway distribution section
				resultSet  = statement.executeQuery( "SELECT gateway, COUNT(*) FROM game_player JOIN game on game.id=game_player.game WHERE player=" + entityId + " GROUP BY gateway ORDER BY gateway" );
				if ( akaIdList != null )
					resultSet2  = statement2.executeQuery( "SELECT gateway, COUNT(*) FROM game_player JOIN game on game.id=game_player.game WHERE player IN (" + akaIdList + ") GROUP BY gateway ORDER BY gateway" );
				
				outputWriter.print( "<tr><th align=left>Gateway distribution:<td>" );
				outputWriter.print( generateGatewayDistributionImageHtml( resultSet ) );
				if ( akaIdList != null ) {
					outputWriter.print( "<td>" );
					outputWriter.print( generateGatewayDistributionImageHtml( resultSet2 ) );
				}
				
				if ( hasAka )
					resultSet2.close();
				resultSet.close();
				
				outputWriter.flush();
				
				// Top maps section
				final int TOP_COUNT = 15;
				resultSet  = statement.executeQuery( "SELECT map_name, COUNT(*) FROM game_player JOIN game on game.id=game_player.game WHERE player=" + entityId + " GROUP BY map_name ORDER BY COUNT(*) DESC LIMIT " + TOP_COUNT );
				if ( akaIdList != null )
					resultSet2  = statement2.executeQuery( "SELECT map_name, COUNT(*) FROM game_player JOIN game on game.id=game_player.game WHERE player IN (" + akaIdList + ") GROUP BY map_name ORDER BY COUNT(*) DESC LIMIT " + TOP_COUNT );
				
				outputWriter.println( "<tr><th align=left>Top " + TOP_COUNT + " maps:<td valign=top><table border=0 width=100%>" );
				int rowCounter = 1;
				while ( resultSet.next() ) {
					outputWriter.println( "<tr" + ( (rowCounter & 0x01) == 1 ? " style='background:#cacaca'" : "" ) + "><td align=right>" + rowCounter + "<td>" + getGameListWithMapHtmlLink( resultSet.getString( 1 ), resultSet.getString( 1 ), null, null, false ) );
					outputWriter.println( "<td align=right>" + getGameListWithMapHtmlLink( resultSet.getString( 1 ), DECIMAL_FORMAT.format( resultSet.getInt( 2 ) ), entityId, null, false ) + "<td align=right>" + (int) ( resultSet.getInt( 2 ) * 100.0f / gamesCount + 0.5f ) + "%" );
					rowCounter++;
				}
				outputWriter.println( "</table>" );
				if ( hasAka ) {
					outputWriter.println( "<td valign=top><table border=0 width=100%>" );
					rowCounter = 1;
					while ( resultSet2.next() ) {
						outputWriter.println( "<tr" + ( (rowCounter & 0x01) == 1 ? " style='background:#cacaca'" : "" ) + "><td align=right>" + rowCounter + "<td>" + getGameListWithMapHtmlLink( resultSet2.getString( 1 ), resultSet2.getString( 1 ), null, null, false ) );
						outputWriter.println( "<td align=right>" + getGameListWithMapHtmlLink( resultSet2.getString( 1 ), DECIMAL_FORMAT.format( resultSet2.getInt( 2 ) ), entityId, null, true ) + "<td align=right>" + (int) ( resultSet2.getInt( 2 ) * 100.0f / gamesCount2 + 0.5f ) + "%" );
						rowCounter++;
					}
					outputWriter.println( "</table>" );
				}
				
				if ( hasAka )
					resultSet2.close();
				resultSet.close();
				
				outputWriter.println( "</table>" );
				outputWriter.println( "<div class='note'>(* games with less than 2 minutes are excluded)<br>" );
				outputWriter.println( "(** games with less than 2 minutes and with less than 30 APM are excluded, the shown value is a weighted average)</div>" ); 
				
				outputWriter.flush();
				
				// CHARTS
				resultSet = statement.executeQuery( "SELECT date_trunc('month',game.save_time), COUNT(*), SUM(CASE WHEN race=0 THEN 1 END), SUM(CASE WHEN race=1 THEN 1 END), SUM(CASE WHEN race=2 THEN 1 END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " AND actions_count*" + OBS_MODE_FPA_LIMIT + ">frames THEN frames END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " AND actions_count*" + OBS_MODE_FPA_LIMIT + ">frames THEN actions_count END) FROM game_player JOIN game on game.id=game_player.game WHERE player=" + entityId + "AND game.save_time IS NOT NULL GROUP BY date_trunc('month',game.save_time) ORDER BY date_trunc('month',game.save_time)" );
				if ( hasAka )
					resultSet2 = statement2.executeQuery( "SELECT date_trunc('month',game.save_time), COUNT(*), SUM(CASE WHEN race=0 THEN 1 END), SUM(CASE WHEN race=1 THEN 1 END), SUM(CASE WHEN race=2 THEN 1 END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " AND actions_count*" + OBS_MODE_FPA_LIMIT + ">frames THEN frames END), SUM(CASE WHEN frames>" + SHORT_GAME_FRAME_LIMIT + " AND actions_count*" + OBS_MODE_FPA_LIMIT + ">frames THEN actions_count END) FROM game_player JOIN game on game.id=game_player.game WHERE player IN (" + akaIdList + ") AND game.save_time IS NOT NULL GROUP BY date_trunc('month',game.save_time) ORDER BY date_trunc('month',game.save_time)" ); 
				
				final List< Object[] > chartData  = new ArrayList< Object[] >();
				final List< Object[] > chartData2 = new ArrayList< Object[] >();
				while ( resultSet.next() )
					chartData.add( new Object[] { resultSet.getDate( 1 ), resultSet.getInt( 2 ), resultSet.getInt( 3 ), resultSet.getInt( 4 ), resultSet.getInt( 5 ), resultSet.getInt( 6 ), resultSet.getInt( 7 ) } );
				if ( hasAka ) {
					while ( resultSet2.next() )
						chartData2.add( new Object[] { resultSet2.getDate( 1 ), resultSet2.getInt( 2 ), resultSet2.getInt( 3 ), resultSet2.getInt( 4 ), resultSet2.getInt( 5 ), resultSet2.getInt( 6 ), resultSet2.getInt( 7 ) } );
					// First and last date must match in both data lists...
					if ( ( (Date) chartData2.get( 0 )[ 0 ] ).before( (Date) chartData.get( 0 )[ 0 ] ) )
						chartData.add( 0, new Object[] { chartData2.get( 0 )[ 0 ], 0, 0, 0, 0, 0, 0 } );
					if ( ( (Date) chartData2.get( chartData2.size() - 1 )[ 0 ] ).after( (Date) chartData.get( chartData.size() - 1 )[ 0 ] ) )
						chartData.add( new Object[] { chartData2.get( chartData2.size() - 1 )[ 0 ], 0, 0, 0, 0, 0, 0 } );
				}
				
				if ( hasAka )
					resultSet2.close();
				resultSet.close();
				
				// Fill up empty months...
				int monthsCount = 1;
				for ( final List< Object[] > dataList : hasAka ? new List[] { chartData, chartData2 } : new List[] { chartData } ) {
					GregorianCalendar calendar = null;
					for ( int i = dataList.size() - 1; i >= 0; i-- ) { // Downward is a must becase we might insert new elements
						final Object[] data = dataList.get( i );
						if ( calendar != null ) {
							calendar.add( Calendar.MONTH, -1 );
							if ( dataList == chartData ) monthsCount++;
							final Date prevMonth = (Date) data[ 0 ];
							while ( calendar.getTime().after( prevMonth ) ) {
								// Add empty month
								dataList.add( i + 1, new Object[] { calendar.getTime(), 0, 0, 0, 0, 0, 0 } );
								calendar.add( Calendar.MONTH, -1 );
								if ( dataList == chartData ) monthsCount++;
							}
						}
						else {
							calendar = new GregorianCalendar();
							calendar.setTime( (Date) data[ 0 ] );
						}
					}
				}
				
				if ( chartData.isEmpty() && chartData2.isEmpty() )
					outputWriter.println( "<p>Charts for this player are not available!</p>" );
				else {
					final int CHART_WIDTH  = 900;
					final int CHART_HEIGHT = 333;
					
					// Activity chart
					final StringBuilder activityChartUrlBuilder = new StringBuilder( "http://chart.apis.google.com/chart?cht=lc&amp;chf=bg,s,ffffff00&amp;chdlp=t&amp;chxtc=0,0&amp;chs=" );
					activityChartUrlBuilder.append( CHART_WIDTH ).append( 'x' ).append( CHART_HEIGHT ).append( "&amp;chtt=BWHF+Activity+of+" ).append( playerNameHtml ).append( "+(games/month)" );
					if ( hasAka )
						activityChartUrlBuilder.append( "&amp;chdl=" + playerNameHtml + "|with+AKAs+included" );
					activityChartUrlBuilder.append( "&amp;chd=t:" );
					int maxMonthGamesCount = 0;
					for ( final List< Object[] > dataList : hasAka ? new List[] { chartData, chartData2 } : new List[] { chartData } ) {
						if ( dataList == chartData2 )
							activityChartUrlBuilder.append( '|' );
						
						if ( chartData.size() == 1 ) // If presence is less than 1 month, make chart look nicer...
							activityChartUrlBuilder.append( "0,0,0," );
						
						boolean isFirst = true;
						for ( final Object[] data : dataList ) {
							if ( isFirst )
								isFirst = false;
							else
								activityChartUrlBuilder.append( ',' );
							final int monthGamesCount = (Integer) data[ 1 ];
							if ( monthGamesCount > maxMonthGamesCount )
								maxMonthGamesCount = monthGamesCount;
							activityChartUrlBuilder.append( monthGamesCount );
						}
						
						if ( chartData.size() == 1 ) // If presence is less than 1 month, make chart look nicer...
							activityChartUrlBuilder.append( ",0,0,0" );
					}
					activityChartUrlBuilder.append( "&amp;chds=0," ).append( maxMonthGamesCount );
					if ( hasAka ) 
						activityChartUrlBuilder.append( ",0," ).append( maxMonthGamesCount );
					
					activityChartUrlBuilder.append( "&amp;chco=FF0000" );
					if ( hasAka )
						activityChartUrlBuilder.append( ",00BB00" );
					
					activityChartUrlBuilder.append( "&amp;chxt=x,y&amp;chxl=0:|" );
					final GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime( (Date) chartData.get( 0 )[ 0 ] );
					final int monthsJump = monthsCount / 15 + 1;
					for ( int i = 0; i < monthsCount; i += monthsJump ) {
						activityChartUrlBuilder.append( ACTIVITY_DATE_FORMAT.format( calendar.getTime() ) ).append( '|' );
						calendar.add( Calendar.MONTH, monthsJump );
					}
					activityChartUrlBuilder.append( "1:" );
					int maxYLabelsCount = maxMonthGamesCount > 10 ? 10 : maxMonthGamesCount;
					for ( int i = 0; i <= maxYLabelsCount; i++ )
						activityChartUrlBuilder.append( '|' ).append( maxMonthGamesCount * i / maxYLabelsCount );
					
					
					activityChartUrlBuilder.append( "&amp;chxp=0" );
					for ( int i = 0; i < monthsCount; i += monthsJump )
						activityChartUrlBuilder.append( ',' ).append( monthsCount == 1 ? 50 : i * 100 / ( monthsCount - 1 ) ); // If one month only, put it in center
					
					activityChartUrlBuilder.append( "&amp;chg=" );
					activityChartUrlBuilder.append( monthsCount == 1 ? "50," : new Formatter( Locale.ENGLISH ).format( "%.2f,", monthsJump * 100.0f / ( monthsCount - 1 ) ) );
					new Formatter( activityChartUrlBuilder,  Locale.ENGLISH ).format( "%.2f", 100.0f / maxYLabelsCount );
					
					outputWriter.println( "<p><img src='" + activityChartUrlBuilder.toString() + "' width=" + CHART_WIDTH + " height=" + CHART_HEIGHT + " title='BWHF Activity of " + playerNameHtml + " (games/month)'></p>" );
					outputWriter.flush();
					
					// APM development chart
					final StringBuilder apmDevelChartUrlBuilder = new StringBuilder( "http://chart.apis.google.com/chart?cht=lc&amp;chf=bg,s,ffffff00&amp;chdlp=t&amp;chs=" );
					apmDevelChartUrlBuilder.append( CHART_WIDTH ).append( 'x' ).append( CHART_HEIGHT ).append( "&amp;chtt=BWHF+APM+development+of+" ).append( playerNameHtml ).append( "+over+time" );
					if ( hasAka )
						apmDevelChartUrlBuilder.append( "&amp;chdl=" + playerNameHtml + "|with+AKAs+included" );
					apmDevelChartUrlBuilder.append( "&amp;chd=t:" );
					int maxMonthApm = 0;
					for ( final List< Object[] > dataList : hasAka ? new List[] { chartData, chartData2 } : new List[] { chartData } ) {
						if ( dataList == chartData2 )
							apmDevelChartUrlBuilder.append( '|' );
						
						if ( chartData.size() == 1 ) // If presence is less than 1 month, make chart look nicer...
							apmDevelChartUrlBuilder.append( "0,0,0," );
						
						boolean isFirst = true;
						for ( final Object[] data : dataList ) {
							if ( isFirst )
								isFirst = false;
							else
								apmDevelChartUrlBuilder.append( ',' );
							final int actionsCount = (Integer) data[ 6 ];
							final int framesCount  = (Integer) data[ 5 ];
							final int monthApm     = (int) ( framesCount > 0 ? ( actionsCount * 60l /  ReplayHeader.convertFramesToSeconds( framesCount ) ) : 0l );
							if ( monthApm > maxMonthApm )
								maxMonthApm = monthApm;
							apmDevelChartUrlBuilder.append( monthApm );
						}
						
						if ( chartData.size() == 1 ) // If presence is less than 1 month, make chart look nicer...
							apmDevelChartUrlBuilder.append( ",0,0,0" );
					}
					apmDevelChartUrlBuilder.append( "&amp;chds=0," ).append( maxMonthApm );
					if ( hasAka ) 
						apmDevelChartUrlBuilder.append( ",0," ).append( maxMonthApm );
					
					apmDevelChartUrlBuilder.append( "&amp;chco=FF0000" );
					if ( hasAka )
						apmDevelChartUrlBuilder.append( ",00BB00" );
					
					apmDevelChartUrlBuilder.append( "&amp;chxt=x,y,r&amp;chxl=0:|" );
					calendar.setTime( (Date) chartData.get( 0 )[ 0 ] );
					for ( int i = 0; i < monthsCount; i += monthsJump ) {
						apmDevelChartUrlBuilder.append( ACTIVITY_DATE_FORMAT.format( calendar.getTime() ) ).append( '|' );
						calendar.add( Calendar.MONTH, monthsJump );
					}
					apmDevelChartUrlBuilder.append( "2:|avg|" );
					if ( averageApm2 > 0 && Math.abs( averageApm - averageApm2 ) > 5 ) // Only render AKA average if the 2 averages are not too close
						apmDevelChartUrlBuilder.append( "avg2|" );
					apmDevelChartUrlBuilder.append( "1:" );
					maxYLabelsCount = maxMonthApm > 10 ? 10 : maxMonthApm == 0 ? 1 : maxMonthApm;
					for ( int i = 0; i <= maxYLabelsCount; i++ )
						apmDevelChartUrlBuilder.append( '|' ).append( maxMonthApm * i / maxYLabelsCount );
					
					apmDevelChartUrlBuilder.append( "&amp;chxp=0" );
					for ( int i = 0; i < monthsCount; i += monthsJump )
						apmDevelChartUrlBuilder.append( ',' ).append( monthsCount == 1 ? 50 : i * 100 / ( monthsCount - 1 ) ); // If one month only, put it in center
					apmDevelChartUrlBuilder.append( "|2," );
					new Formatter( apmDevelChartUrlBuilder, Locale.ENGLISH ).format( "%.2f", averageApm * 100f / (maxMonthApm == 0 ? 1 : maxMonthApm) );
					if ( averageApm2 > 0 && Math.abs( averageApm - averageApm2 ) > 5 ) // Only render AKA average if the 2 averages are not too close
						new Formatter( apmDevelChartUrlBuilder, Locale.ENGLISH ).format( ",%.2f", averageApm2 * 100f / (maxMonthApm == 0 ? 1 : maxMonthApm) );
					
					apmDevelChartUrlBuilder.append( "&amp;chg=" );
					apmDevelChartUrlBuilder.append( monthsCount == 1 ? "50," : new Formatter( Locale.ENGLISH ).format( "%.2f,", monthsJump * 100.0f / ( monthsCount - 1 ) ) );
					new Formatter( apmDevelChartUrlBuilder, Locale.ENGLISH ).format( "%.2f", 100.0f / maxYLabelsCount );
					
					apmDevelChartUrlBuilder.append( "&amp;chxs=2,2020FF,10,-1,t,2020FF&amp;chxtc=0,0|2,-" ).append( CHART_WIDTH );
					
					outputWriter.println( "<p><img src='" + apmDevelChartUrlBuilder.toString() + "' width=" + CHART_WIDTH + " height=" + CHART_HEIGHT + " title='BWHF APM development of " + playerNameHtml + " over time'></p>" );
					outputWriter.flush();
					
					// Race distribution over time charts
					for ( final List< Object[] > dataList : hasAka ? new List[] { chartData, chartData2 } : new List[] { chartData } ) {
						final StringBuilder raceChartUrlBuilder = new StringBuilder( "http://chart.apis.google.com/chart?cht=bvs&amp;chf=bg,s,ffffff00&amp;chdlp=t&amp;chbh=a,2&amp;chxtc=0,0&amp;chs=" );
						raceChartUrlBuilder.append( CHART_WIDTH ).append( 'x' ).append( CHART_HEIGHT ).append( "&amp;chtt=BWHF+Race+distribution+of+" ).append( playerNameHtml ).append( "+over+time" );
						if ( dataList == chartData2 )
							raceChartUrlBuilder.append( " (with+AKAs+included)" );
						raceChartUrlBuilder.append( "&amp;chdl=Zerg|Terran|Protoss" );
						raceChartUrlBuilder.append( "&amp;chd=t:" );
						final Formatter raceDistroFormatter = new Formatter( raceChartUrlBuilder, Locale.ENGLISH );
						for ( int race=0; race < 3; race++ ) {
							if ( race > 0 )
								raceChartUrlBuilder.append( '|' );
							if ( dataList.size() == 1 ) // If presence is less than 1 month, make chart look nicer...
								raceChartUrlBuilder.append( "0,0,0," );
							boolean isFirst = true;
							for ( final Object[] data : dataList ) {
								if ( isFirst )
									isFirst = false;
								else
									raceChartUrlBuilder.append( ',' );
								if ( (Integer) data[ 1 ] == 0 )
									raceChartUrlBuilder.append( 0 );
								else
									raceDistroFormatter.format( "%.1f", (Integer) data[ 2 + race ] * 100.0f / (Integer) data[ 1 ] );
							}
							if ( dataList.size() == 1 ) // If presence is less than 1 month, make chart look nicer...
								raceChartUrlBuilder.append( ",0,0,0" );
						}
						
						raceChartUrlBuilder.append( "&amp;chco=FF0000,0000FF,00DD00" );
						
						raceChartUrlBuilder.append( "&amp;chxt=x,y&amp;chxl=0:|" );
						calendar.setTime( (Date) chartData.get( 0 )[ 0 ] );
						for ( int i = 0; i < monthsCount; i += monthsJump ) {
							raceChartUrlBuilder.append( ACTIVITY_DATE_FORMAT.format( calendar.getTime() ) ).append( '|' );
							calendar.add( Calendar.MONTH, monthsJump );
						}
						raceChartUrlBuilder.append( "1:|0%|33%|66%|100%" );
						
						raceChartUrlBuilder.append( "&amp;chxp=0" );
						for ( int i = 0; i < monthsCount; i += monthsJump )
							raceChartUrlBuilder.append( ',' ).append( monthsCount == 1 ? 50 : i * 100 / ( monthsCount - 1 ) ); // If one month only, put it in center
						
						raceChartUrlBuilder.append( "&amp;chg=" );
						raceChartUrlBuilder.append( monthsCount == 1 ? "50," : new Formatter( Locale.ENGLISH ).format( "%.2f,", monthsJump * 100.0f / ( monthsCount - 1 ) ) );
						raceChartUrlBuilder.append( "33.33,1,0" );
						
						outputWriter.println( "<p><img src='" + raceChartUrlBuilder.toString() + "' width=" + CHART_WIDTH + " height=" + CHART_HEIGHT + " title='BWHF Race distribution of " + playerNameHtml + " over time" + ( dataList == chartData2 ? " (with AKAs included)" : "" ) + "'></p>" );
						outputWriter.flush();
					}
					
				}
				
				if ( hasAka )
					statement2.close();
				statement.close();
				
			}
			
			renderFooter( request, outputWriter );
		}
		catch ( final SQLException se ) {
			se.printStackTrace();
		}
		finally {
			if ( resultSet2 != null ) try { resultSet2.close(); } catch ( final SQLException se ) {}
			if ( statement2 != null ) try { statement2.close(); } catch ( final SQLException se ) {}
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Handles player record checks.
	 * @param response     http response
	 * @param agentVersion version of the agent
	 * @param playerNames  names of the players whose record to check
	 */
	private void handleRecordCheck( final HttpServletResponse response, final String agentVersion, final String[] playerNames ) throws IOException {
		PrintWriter       outputWriter = null;
		Connection        connection   = null;
		PreparedStatement statement    = null;
		ResultSet         resultSet    = null;
		
		try {
			connection = dataSource.getConnection();
			
			statement = connection.prepareStatement( "SELECT games_count FROM player WHERE name=?" );
			
			response.setContentType( "text/plain" );
			outputWriter = response.getWriter();
			outputWriter.println( ServerApiConsts.CHECK_RECORDS_OK );
			
			for ( int i = 0; i < playerNames.length; i++ )
				if ( playerNames[ i ] != null ) {
					statement.setString( 1, playerNames[ i ] );
					resultSet = statement.executeQuery();
					outputWriter.println( i + " " + ( resultSet.next() ? resultSet.getInt( 1 ) : 0 ) );
					resultSet.close();
				}
			
			outputWriter.flush();
		}
		catch ( final SQLException se ) {
			se.printStackTrace();
		}
		finally {
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
			if ( connection != null ) try { connection.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Calculates the randomness of the race distribution.
	 * Result is 100 if races are equal, 0 if all games belong to 1 race.
	 * @param zergGames    number of zerg games
	 * @param terranGames  number of terran games
	 * @param protossGames number of protoss games
	 * @param gamesCount   number of total games
	 * @return the randomness of the race distribution
	 */
	private static int calculateRandomOMeter( final float zergGames, final float terranGames, final float protossGames, final float gamesCount ) {
		return 100 - (int) ( ( Math.abs( zergGames / gamesCount - 1f/3f ) + Math.abs( terranGames / gamesCount - 1f/3f ) + Math.abs( protossGames / gamesCount - 1f/3f ) ) * 75f );
	}
	
	/**
	 * Generates and returns a gateway distribution chart image html.
	 * @param resultSet result set containing the numbers for the gateways
	 * @return a gateway distribution chart image html
	 * @throws SQLException thrown if using the resultSet throws SQLException
	 */
	private static String generateGatewayDistributionImageHtml( final ResultSet resultSet ) throws SQLException {
		final List< int[] > dataList = new ArrayList< int[] >( GATEWAYS.length + 1 ); // +1 optional for missing gateway
		int allKnownGames = 0;
		
		while ( resultSet.next() ) {
			final boolean unknownGateway = resultSet.getObject( 1 ) == null;
			final int     gamesCount     = resultSet.getInt( 2 );
			dataList.add( new int[] { unknownGateway ? -1 : resultSet.getInt( 1 ), gamesCount } );
			if ( !unknownGateway )
				allKnownGames += gamesCount;
		}
		
		final int gatewaysCount = dataList.size();
		if ( allKnownGames > 0 ) { // If there were data...
			final int GATEWAY_CHART_WIDTH  = 80;
			final int GATEWAY_CHART_HEIGHT = 50;
			
			StringBuilder resultBuilder = new StringBuilder( "<img src='http://chart.apis.google.com/chart?cht=p3&amp;chf=bg,s,ffffff00&amp;chs=" );
			resultBuilder.append( GATEWAY_CHART_WIDTH ).append( 'x' ).append( GATEWAY_CHART_HEIGHT ).append( "&amp;chco=" );
			for ( int i = 0; i < gatewaysCount; i++ ) {
				if ( dataList.get( i )[ 0 ] < 0 ) // Missing gateway
					continue;
				if ( i > 0 )
					resultBuilder.append( ',' );
				resultBuilder.append( GATEWAY_COLORS[ dataList.get( i )[ 0 ] ] );
			}
			resultBuilder.append( "&amp;chd=t:" );
			final Formatter dataFormatter = new Formatter( resultBuilder, Locale.ENGLISH );
			for ( int i = 0; i < gatewaysCount; i++ ) {
				if ( dataList.get( i )[ 0 ] < 0 ) // Missing gateway
					continue;
				if ( i > 0 )
					resultBuilder.append( ',' );
				dataFormatter.format( "%.2f", dataList.get( i )[ 1 ] * 100.0f / allKnownGames );
			}
			
			resultBuilder.append( "' width=" ).append( GATEWAY_CHART_WIDTH ).append( " height=" ).append( GATEWAY_CHART_HEIGHT ).append( " title='" );
			for ( int i = 0; i < gatewaysCount; i++ ) {
				if ( i > 0 )
					resultBuilder.append( ", " );
				final int[] data = dataList.get( i );
				resultBuilder.append( data[ 0 ] < 0 ? "Unknown" : GATEWAYS[ data[ 0 ] ] ).append( ": " ).append( data[ 1 ] );
			}
			resultBuilder.append( "'>" );
			
			return resultBuilder.toString();
		}
		else
			return UNKOWN_HTML_STRING;
	}
	
	/**
	 * Safe method to get the name of a game type. Returns <code>UNKOWN_HTML_STRING</code> if game type is invalid/unknown.
	 * @param gameType game type whose name to be return
	 * @param longName tells if long or short name is to be returned
	 * @return the name of a game type
	 */
	private static String getGameTypeName( final short gameType, final boolean longName ) {
		try {
			final String gameTypeName = ( longName ? ReplayHeader.GAME_TYPE_NAMES : ReplayHeader.GAME_TYPE_SHORT_NAMES )[ gameType ];
			return gameTypeName == null ? UNKOWN_HTML_STRING : gameTypeName;
		}
		catch ( ArrayIndexOutOfBoundsException aioobe ) {
			// This case should be very rare that's why I didn't check for array index range.
			return UNKOWN_HTML_STRING;
		}
	}
	
	/**
	 * Renders the filter section.
	 * @param outputWriter output writer to use
	 * @param displayText  display text to render for the filter
	 * @param nameFilter   value of the name filter
	 * @param page         current page
	 * @param maxPage      max page
	 * @param pagerUrl     url to be used for the pager links; ends with '=' and only the target page number have to be appended
	 */
	private static void renderFiltersSection( final PrintWriter outputWriter, final String displayText, final String nameFilter, final int page, final String pagerUrlWithoutNameFilter ) {
		outputWriter.println( displayText + ": <input type=text id='8347' onkeydown=\"javascritp:if(event.keyCode==13) document.getElementById('4358').onclick()\""
				+ ( nameFilter == null ? "" : " value='" + encodeHtmlString( nameFilter ) + "'" )
				+ "> <a id='4358' href='#' onclick=\"javascript:window.location='" + pagerUrlWithoutNameFilter
				+ '&' + PN_REQUEST_PARAM_NAME_PAGE + "=" + page
				+ "&" + PN_REQUEST_PARAM_NAME_NAME_FILTER + "='+escape(document.getElementById('8347').value);\">Apply</a>&nbsp;&nbsp;<a href=\"javascript:window.location='" + pagerUrlWithoutNameFilter
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
		outputWriter.print( "&nbsp;&nbsp;|&nbsp;&nbsp;Page <b>" + DECIMAL_FORMAT.format( page ) + "</b> out of <b>" + DECIMAL_FORMAT.format( maxPage ) + "</b>.&nbsp;&nbsp;|&nbsp;&nbsp;" );
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
	 * Returns the name of the player specified by its id.<br> It also returns if the player is a hacker.
	 * @param id         id of the player
	 * @param connection connection to be used
	 * @return an array with 2 values: the name of the player specified by its id, and null if the player is not a hacker or if there is a hacker with same name, his/her id as a string
	 * @throws SQLException if SQLException occurs
	 */
	private static String[] getPlayerName( final int id, final Connection connection ) throws SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery( "SELECT name, " + IS_HACKER_QUERY_PART + " FROM player WHERE id=" + id );
			if ( resultSet.next() )
				return new String[] { resultSet.getString( 1 ), resultSet.getString( 2 ) };
			else
				return new String[] { "", null };
		}
		finally {
			if ( resultSet != null ) try { resultSet.close(); } catch ( final SQLException se ) {}
			if ( statement != null ) try { statement.close(); } catch ( final SQLException se ) {}
		}
	}
	
	/**
	 * Generates and returns an HTML link to the game list page of a map (and optionally of a player optionally akas included).<br>
	 * An HTML anchor tag will be returned whose text is <code>text</code>.
	 * @param mapName     name of the map
	 * @param text        text to appear in the link
	 * @param player1     optional id of the player, if specified, link to the player's games
	 * @param player2     optional id of the player, if specified, link to the player's games (common games of player1 and player 2)
	 * @param includeAkas tells if akas should be included (adds an extra parameter), only used if playerId is provided
	 * @return an HTML link to the details page of the player
	 */
	private static String getGameListWithMapHtmlLink( final String mapName, final String text, final Integer player1, final Integer player2, final boolean includeAkas ) {
		try {
			return "<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION   + '=' + PN_OPERATION_LIST
								 + '&' + PN_REQUEST_PARAM_NAME_ENTITY      + '=' + ENTITY_GAME
								 + ( player1 == null ? "" : '&' + PN_REQUEST_PARAM_NAME_PLAYER1 + '=' + player1
										 + ( player2 == null ? "" : '&' + PN_REQUEST_PARAM_NAME_PLAYER2 + '=' + player2 )
										 + ( includeAkas ? '&' + PN_REQUEST_PARAM_NAME_INCLUDE_AKAS : "" ) )
								 + '&' + PN_REQUEST_PARAM_NAME_NAME_FILTER + '=' + URLEncoder.encode( '"' + mapName + '"', "UTF-8" ) + "'>" + encodeHtmlString( text ) + "</a>";
		} catch ( final UnsupportedEncodingException use ) {
			// This will never happen.
			throw new RuntimeException( "Unsupported UTF-8 encoding?" ); 
		}
	}
	
	/**
	 * Generates and returns an HTML link to the details page of a player.<br>
	 * An HTML anchor tag will be returned whose text is the name of the player.<br>
	 * If <code>builder</code> is null, a new <code>StringBuilder</code> will be created and returned.
	 * @param id         id of the player
	 * @param playerName name of the player
	 * @param isHacker   tells if the player is a hacker
	 * @param builder    builder to use to build the link
	 * @return the builder used to build the an HTML link to the details page of the player
	 */
	public static StringBuilder getPlayerDetailsHtmlLink( final int id, final String playerName, final boolean isHacker, StringBuilder builder ) {
		if ( builder == null )
			builder = new StringBuilder();
		
		builder.append( "<a href='players?" ).append( PN_REQUEST_PARAM_NAME_OPERATION ).append( '=' ).append( PN_OPERATION_DETAILS )
							   .append( '&' ).append( PN_REQUEST_PARAM_NAME_ENTITY    ).append( '=' ).append( ENTITY_PLAYER )
							   .append( '&' ).append( PN_REQUEST_PARAM_NAME_ENTITY_ID ).append( '=' ).append( id ).append( "'>" ).append( encodeHtmlString( playerName ) ).append( "</a>" );
		if ( isHacker )
			builder.append( createHackerTagHtml( playerName ) );
		
		return builder;
	}
	
	/**
	 * Generates and returns an HTML link to the details page of a game.<br>
	 * An HTML anchor tag will be returned whose text is the value of <code>text</code>.
	 * @param id   id of the game
	 * @param text text to appear in the link
	 * @return an HTML link to the details page of the player
	 */
	protected static String getGameDetailsHtmlLink( final int id, final String text ) {
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
	
	/** Menu HTML code to be sent. */
	private static final String MENU_HTML = "<p><a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_GAME   + "'>Game list</a>"
					+ "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_PLAYER + "'>Player list</a>"
					+ "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='players?" + PN_REQUEST_PARAM_NAME_OPERATION + '=' + PN_OPERATION_LIST + '&' + PN_REQUEST_PARAM_NAME_ENTITY + '=' + ENTITY_AKA    + "'>AKA list</a>"
					+ "&nbsp;&nbsp;|&nbsp;&nbsp;<a href='http://code.google.com/p/bwhf/wiki/PlayersNetwork'>Help</a></p>";
	
	/**
	 * Renders the header for the output pages.
	 * @param request      http request
	 * @param outputWriter writer to be used to render
	 */
	private static void renderHeader( final HttpServletRequest request, final PrintWriter outputWriter ) {
		request.setAttribute( "startTimeNanos", System.nanoTime() ); 
		outputWriter.println( "<html><head>" );
		outputWriter.println( COMMON_HTML_HEADER_ELEMENTS );
		outputWriter.println( "<title>BWHF Players' Network</title>" );
		outputWriter.println( "</head><body><center>" );
		outputWriter.println( getCurrentTimeCode() );
		outputWriter.println( "<h2>BWHF Players' Network</h2>" );
		outputWriter.println( MENU_HTML );
		outputWriter.println( GOOGLE_AD_HTML_HEADER );
	}
	
	/**
	 * Renders the footer for the output pages.
	 * @param request      http request
	 * @param outputWriter writer to be used to render
	 */
	private static void renderFooter( final HttpServletRequest request, final PrintWriter outputWriter ) {
		final int executionMs = (int) ( ( System.nanoTime() - (Long) request.getAttribute( "startTimeNanos" ) ) / 1000000l );
		
		outputWriter.println( "<hr><table border=0 width='100%'><tr><td width='40%' align='left'><a href='http://code.google.com/p/bwhf/'>BWHF Agent home page</a>&nbsp;&nbsp;<a href='hackers'>BWHF Hacker Database</a>"
							+ "<td align=center width='20%'><i>Served in " + (executionMs / 1000) + " sec, " + (executionMs % 1000) + " ms</i>"
							+ "<td align=right width='40%'><i>&copy; Andr&aacute;s Belicza, 2008-2010</i></table>" );
		outputWriter.println( "</center>" );
		outputWriter.println( GOOGLE_ANALYTICS_TRACKING_CODE );
		outputWriter.println( "</body></html>" );
		outputWriter.flush();
	}
	
}
