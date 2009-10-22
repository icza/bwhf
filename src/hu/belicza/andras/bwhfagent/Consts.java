package hu.belicza.andras.bwhfagent;

import java.util.Properties;

/**
 * Application wide constants.
 * 
 * @author Belicza Andras
 */
public class Consts {
	
	/** Name of the application.                                               */
	public static final String APPLICATION_NAME                        = "BWHF Agent";
	/** Author of the application.                                             */
	public static final String APPLICATION_AUTHOR                      = "András Belicza";
	/** Email of the author.                                                   */
	public static final String AUTHOR_EMAIL                            = new String( new char[] { 'i', 'c', 'z', 'a', 'a', 'a', '@', 'g', 'm', 'a', 'i', 'l', '.', 'c', 'o', 'm' } );
	
	/** Home page url string.                                                  */
	public static final String HOME_PAGE_URL                           = "http://code.google.com/p/bwhf/";
	/** Name of the current version resource file.                             */
	public static final String VERSION_RESOURCE_NAME                   = "current_version.txt";
	/** URL pointing to the latest stable version text.                        */
	public static final String LATEST_STABLE_VERSION_TEXT_URL          = "http://bwhf.googlecode.com/svn/branches/Swing%20(MAC%20OS-X)/latest_stable_version.txt";
	/** Search help page url string.                                           */
	public static final String SEARCH_HELP_PAGE_URL                    = "http://code.google.com/p/bwhf/wiki/ReplaySearchInBWHFAgent";
	/** Player matcher help page url string.                                   */
	public static final String PLAYER_MATCHER_HELP_PAGE_URL            = "http://code.google.com/p/bwhf/wiki/PlayerMatcherInBWHFAgent";
	/** Players' Network page url string.                                      */
	public static final String PLAYERS_NETWORK_PAGE_URL                = "http://bwhf.net/hackerdb/players";
	
	/** URL of the BWHF hacker data base server.                               */
	public static final String BWHF_HACKER_DATA_BASE_SERVER_URL        = "http://bwhf.net/hackerdb/hackers";
	/** URL of the Players' Network processor server.                          */
	public static final String PLAYERS_NETWORK_DATA_BASE_URL           = "http://bwhf.net/hackerdb/players";
	
	/** Name of the resource containing the about html template.               */
	public static final String ABOUT_TEMLATE_RESOURCE_NAME             = "about_template.html";
	
	/** Default Starcraft directory.                                           */
	public static final String DEFAULT_STARCRAFT_DIRECTORY             = "/Applications/Starcraft Files";
	/** Starcraft replay folder relative to the Starcraft folder.              */
	public static final String STARCRAFT_REPLAY_FOLDER                 = "maps/replays";
	/** Starcraft replay folder relative to the Starcraft folder.              */
	public static final String DEFAULT_REPLAY_LISTS_FOLDER             = "Replay lists";
	/** Name of the last replay file relative to the starcraft folder.         */
	public static final String LAST_REPLAY_FILE_NAME                   = "maps/replays/LastReplay.rep";
	/** Name of the Starcraft executable file.                                 */
	public static final String STARCRAFT_EXECUTABLE_FILE_NAME          = "Starcraft (Carbon)";
	/** Name of directory containing sound samples.                            */
	public static final String SOUNDS_DIRECTORY_NAME                   = "sounds";
	/** Name of directory containing utility programs.                         */
	public static final String UTILS_DIRECTORY_NAME                    = "utils";
	/** Name of directory containing the HTML summary reports of manual scans. */
	public static final String HTML_REPORT_DIRECTORY_NAME              = "HTML Reports";
	/** Name of directory containing cache of the hacker list.                 */
	public static final String HACKER_LIST_DIRECTORY_NAME              = "Hacker list cache";
	
	/** Labels for the possible values of flag hacker replays position. */
	public static final String[] FLAG_HACKER_REPS_POSITION_LABELS      = new String[] { "beginning", "end" };
	/** Index for the beginning flag hacker reps position. */
	public static final int FLAG_HACKER_REPS_POSITION_BEGINNING        = 0;
	/** Index for the end flag hacker reps position.       */
	public static final int FLAG_HACKER_REPS_POSITION_END              = 1;
	
	
	/** Name of the settings properties file. */
	public static final String SETTINGS_PROPERTIES_FILE = "settings.properties";
	
	/** Starcraft folder property.               */
	public static final String PROPERTY_STARCRAFT_FOLDER               = "starcraftFolder";
	/** Autoscan enabled property.               */
	public static final String PROPERTY_AUTOSCAN_ENABLED               = "autoscanEnabled";
	/** Save hacker reps property.               */
	public static final String PROPERTY_SAVE_HACKER_REPS               = "saveHackerReps";
	/** Hacker reps destination property.        */
	public static final String PROPERTY_HACKER_REPS_DESTINATION        = "hackerRepsDestination";
	/** Save all reps property.                  */
	public static final String PROPERTY_SAVE_ALL_REPS                  = "saveAllReps";
	/** All reps destination property.           */
	public static final String PROPERTY_ALL_REPS_DESTINATION           = "allRepsDestination";
	/** Auto convert PCX enabled property.       */
	public static final String PROPERTY_AUTO_CONVERT_PCX_ENABLED       = "autoConvertPcxEnabled";
	/** PCX output format property.              */
	public static final String PROPERTY_PCX_OUTPUT_FORMAT              = "pcxOutputFormat";
	/** Resize converted images property.        */
	public static final String PROPERTY_RESIZE_CONVERTED_IMAGES        = "resizeConvertedImages";
	/** Resized image width property.            */
	public static final String PROPERTY_RESIZED_IMAGE_WIDTH            = "resizedImageWidth";
	/** Include replay header property.          */
	public static final String PROPERTY_INCLUDE_REPLAY_HEADER          = "includeReplayHeader";
	/** Use short names for autosave property.   */
	public static final String PROPERTY_USE_SHORT_NAMES_FOR_AUTOSAVE   = "useShortNamesForAutosave";
	/** Play sound if found hackers property.    */
	public static final String PROPERTY_PLAY_SOUND_IF_FOUND_HACKERS    = "playSoundIfFoundHackers";
	/** Bring to front property.                 */
	public static final String PROPERTY_BRING_TO_FRONT                 = "bringToFront";
	/** Report hackers property.                 */
	public static final String PROPERTY_REPORT_HACKERS                 = "reportHackers";
	/** Gateway property.                        */
	public static final String PROPERTY_GATEWAY                        = "gateway";
	/** Authorization key property.              */
	public static final String PROPERTY_AUTHORIZATION_KEY              = "authorizationKey";
	/** Flag hacker reps property.               */
	public static final String PROPERTY_FLAG_HACKER_REPS               = "flagHackerReps";
	/** Flag hacker reps property.               */
	public static final String PROPERTY_FLAG_HACKER_REPS_POSITION      = "flagHackerRepsPosition";
	/** Clean 'hack' flag property.              */
	public static final String PROPERTY_CLEAN_HACK_FLAG                = "cleanHackFlag";
	/** Create HTML summary report property.     */
	public static final String PROPERTY_CREATE_HTML_SUMMARY_REPORT     = "createHtmlSummaryReport";
	/** Check updates on startup property.       */
	public static final String PROPERTY_CHECK_UPDATES_ON_STARTUP       = "checkUpdatesOnStartup";
	/** Skip latter actions of hackers property. */
	public static final String PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS = "skipLatterActionsOfHackers";
	/** Sound volume property.                   */
	public static final String PROPERTY_SOUND_VOLUME                   = "soundVolume";
	/** Default replay start folder property.    */
	public static final String PROPERTY_REPLAY_START_FOLDER            = "defaultReplayStartFolder";
	/** Default replay lists folder property.    */
	public static final String PROPERTY_DEFAULT_REPLAY_LISTS_FOLDER    = "defaultReplayListsFolder";
	/** Enable system tray icon property.        */
	public static final String PROPERTY_ENABLE_SYSTEM_TRAY_ICON        = "enableSystemTrayIcon";
	/** Enable system tray icon property.        */
	public static final String PROPERTY_ALWAYS_MINIMIZE_TO_TRAY        = "alwaysMinimizeToTray";
	/** Start agent minimized to tray property.  */
	public static final String PROPERTY_START_AGENT_MINIMIZED_TO_TRAY  = "startAgentMinimizedToTray";
	/** Save window position property.           */
	public static final String PROPERTY_SAVE_WINDOW_POSITION           = "saveWindowPosition";
	/** Window position property.                */
	public static final String PROPERTY_WINDOW_POSITION                = "windowPosition";
	/** Chart type property.                     */
	public static final String PROPERTY_CHART_TYPE                     = "chartType";
	/** All players on one chart property.       */
	public static final String PROPERTY_ALL_PLAYERS_ON_ONE_CHART       = "allPlayersOnOneChart";
	/** Use players' in-game colors property.    */
	public static final String PROPERTY_USE_PLAYERS_IN_GAME_COLORS     = "usePlayersInGameColors";
	/** Auto-disable inactive players property.  */
	public static final String PROPERTY_AUTO_DISABLE_INACTIVE_PLAYERS  = "autoDisableInactivePlayers";
	/** Display actions in seconds property.     */
	public static final String PROPERTY_DISPLAY_ACTIONS_IN_SECONDS     = "displayActionsInSeconds";
	/** APM chart detail level property.         */
	public static final String PROPERTY_APM_CHART_DETAIL_LEVEL         = "apmChartDetailLevel";
	/** Show EAPM property.                      */
	public static final String PROPERTY_SHOW_EAPM                      = "showEapm";
	/** Show select hotkeys property.            */
	public static final String PROPERTY_SHOW_SELECT_HOTKEYS            = "showSelectHotkeys";
	/** Build order display levels property.     */
	public static final String PROPERTY_BUILD_ORDER_DISPLAY_LEVELS     = "buildOrderDisplayLevels";
	/** Show units on build order property.      */
	public static final String PROPERTY_SHOW_UNITS_ON_BUILD_ORDER      = "showUnitsOnBuildOrder";
	/** Hide worker units property.              */
	public static final String PROPERTY_HIDE_WORKER_UNITS              = "hideWorkerUnits";
	/** Strategy display levels property.        */
	public static final String PROPERTY_STRATEGY_DISPLAY_LEVELS        = "strategyDisplayLevels";
	/** Overall APM chart detail level property. */
	public static final String PROPERTY_OVERALL_APM_CHART_DETAIL_LEVEL = "overallApmChartDetailLevel";
	/** Show overall EAPM property.              */
	public static final String PROPERTY_SHOW_OVERALL_EAPM              = "showOverallEapm";
	/** Player checker enabled property.         */
	public static final String PROPERTY_HIDE_SEARCH_FILTERS            = "hideSearchFilters";
	/** Player checker enabled property.         */
	public static final String PROPERTY_APPEND_RESULTS_TO_TABLE        = "appendResultsToTable";
	/** Player checker enabled property.         */
	public static final String PROPERTY_PLAYER_CHECKER_ENABLED         = "playerCheckerEnabled";
	/** Hacker list update interval property.    */
	public static final String PROPERTY_HACKER_LIST_UPDATE_INTERVAL    = "hackerListUpdateInterval";
	/** Say "clean" property.                    */
	public static final String PROPERTY_SAY_CLEAN                      = "sayClean";
	/** Delete game lobby screenshots property.  */
	public static final String PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS  = "deleteGameLobbyScreenshots";
	/** Echo recognized player names property.   */
	public static final String PROPERTY_ECHO_RECOGNIZED_PLAYER_NAMES   = "echoRecognizedPlayerNames";
	/** Include custom player list property.     */
	public static final String PROPERTY_INCLUDE_CUSTOM_PLAYER_LIST     = "includeCustomPlayerList";
	/** Custom player list file property.        */
	public static final String PROPERTY_CUSTOM_PLAYER_LIST_FILE        = "customPlayerListFile";
	/** Replay list to load on startup property. */
	public static final String PROPERTY_REPLAY_LIST_TO_LOAD_ON_STARTUP = "replayListToLoadOnStartup";
	/** Editor program property.                 */
	public static final String PROPERTY_EDITOR_PROGRAM                 = "editorProgram";
	/** Replay column model indices property.    */
	public static final String PROPERTY_REPLAY_COLUMN_MODEL_INDICES    = "replayColumnModelIndices";
	/** Auto send info about lastrep property.   */
	public static final String PROPERTY_AUTO_SEND_INFO_ABOUT_LAST_REP  = "autoSendInfoAboutLastRep";
	/** Don't compare same names property.       */
	public static final String PROPERTY_DONT_COMPARE_SAME_NAMES        = "dontCompareSameNames";
	/** Authoritativeness threshold property.    */
	public static final String PROPERTY_AUTHORITATIVENESS_THRESHOLD    = "authoritativenessThreshold";
	/** Matching probability threshold property. */
	public static final String PROPERTY_MATCHING_PROBABILITY_THRESHOLD = "matchingProbabilityThreshold";
	/** Max displayable results property.        */
	public static final String PROPERTY_MAX_DISPLAYABLE_RESULTS        = "maxDisplayableResults";
	/** Monitor re-check time interval property. */
	public static final String PROPERTY_MONITOR_RECHECK_INTERVAL       = "monitorRecheckInterval";
	/** NavigationBarCollapsed property.         */
	public static final String PROPERTY_NAVIGATION_BAR_COLLAPSED       = "navigationBarCollapsed";
	
	/** Properties holding the default settings. */
	public static final Properties DEFAULT_SETTINGS_PROPERTIES = new Properties();
	static {
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_STARCRAFT_FOLDER              , DEFAULT_STARCRAFT_DIRECTORY );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTOSCAN_ENABLED              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_HACKER_REPS              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_HACKER_REPS_DESTINATION       , System.getProperty( "user.home" ) + "/replays/hackerreps" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_ALL_REPS                 , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ALL_REPS_DESTINATION          , System.getProperty( "user.home" ) + "/replays/allreps" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTO_CONVERT_PCX_ENABLED      , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_PCX_OUTPUT_FORMAT             , "JPG" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_RESIZE_CONVERTED_IMAGES       , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_RESIZED_IMAGE_WIDTH           , "320" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_INCLUDE_REPLAY_HEADER         , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_USE_SHORT_NAMES_FOR_AUTOSAVE  , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_PLAY_SOUND_IF_FOUND_HACKERS   , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_BRING_TO_FRONT                , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_REPORT_HACKERS                , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_GATEWAY                       , "0" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTHORIZATION_KEY             , "" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_FLAG_HACKER_REPS              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_FLAG_HACKER_REPS_POSITION     , Integer.toString( FLAG_HACKER_REPS_POSITION_END ) );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_CLEAN_HACK_FLAG               , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_CREATE_HTML_SUMMARY_REPORT    , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_CHECK_UPDATES_ON_STARTUP      , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS, "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SOUND_VOLUME                  , "70" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_REPLAY_START_FOLDER           , STARCRAFT_REPLAY_FOLDER );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ENABLE_SYSTEM_TRAY_ICON       , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ALWAYS_MINIMIZE_TO_TRAY       , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_START_AGENT_MINIMIZED_TO_TRAY , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_WINDOW_POSITION          , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_WINDOW_POSITION               , "5,10,1150,830" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_CHART_TYPE                    , "0" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ALL_PLAYERS_ON_ONE_CHART      , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_USE_PLAYERS_IN_GAME_COLORS    , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTO_DISABLE_INACTIVE_PLAYERS , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_DISPLAY_ACTIONS_IN_SECONDS    , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_APM_CHART_DETAIL_LEVEL        , "4" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SHOW_EAPM                     , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SHOW_SELECT_HOTKEYS           , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_BUILD_ORDER_DISPLAY_LEVELS    , "4" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SHOW_UNITS_ON_BUILD_ORDER     , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_HIDE_WORKER_UNITS             , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_STRATEGY_DISPLAY_LEVELS       , "4" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_OVERALL_APM_CHART_DETAIL_LEVEL, "7" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SHOW_OVERALL_EAPM             , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_HIDE_SEARCH_FILTERS           , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_APPEND_RESULTS_TO_TABLE       , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_PLAYER_CHECKER_ENABLED        , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_HACKER_LIST_UPDATE_INTERVAL   , "2" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAY_CLEAN                     , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ECHO_RECOGNIZED_PLAYER_NAMES  , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_INCLUDE_CUSTOM_PLAYER_LIST    , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_CUSTOM_PLAYER_LIST_FILE       , "" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_DEFAULT_REPLAY_LISTS_FOLDER   , DEFAULT_REPLAY_LISTS_FOLDER );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_REPLAY_LIST_TO_LOAD_ON_STARTUP, "" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_EDITOR_PROGRAM                , "open" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_REPLAY_COLUMN_MODEL_INDICES   , "" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTO_SEND_INFO_ABOUT_LAST_REP , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_DONT_COMPARE_SAME_NAMES       , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTHORITATIVENESS_THRESHOLD   , "3" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_MATCHING_PROBABILITY_THRESHOLD, "6" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_MAX_DISPLAYABLE_RESULTS       , "2" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_MONITOR_RECHECK_INTERVAL      , "2" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_NAVIGATION_BAR_COLLAPSED      , "false" );
	}
	
}
