package hu.belicza.andras.bwhfagent;

import java.util.Properties;

import swingwtx.swing.SwingWTUtils;

/**
 * Application wide constants.
 * 
 * @author Belicza Andras
 */
public class Consts {
	
	/** Name of the application.                                               */
	public static final String APPLICATION_NAME                 = "BWHF Agent";
	/** Author of the application.                                             */
	public static final String APPLICATION_AUTHOR               = "András Belicza";
	
	/** Home page url string.                                                  */
	public static final String HOME_PAGE_URL                    = "http://code.google.com/p/bwhf/";
	/** Name of the current version resource file.                             */
	public static final String VERSION_RESOURCE_NAME            = "current_version.txt";
	/** URL pointing to the latest stable version text.                        */
	public static final String LATEST_STABLE_VERSION_TEXT_URL   = "http://bwhf.googlecode.com/svn/trunk/latest_stable_version.txt";
	/** Search help page url string.                                           */
	public static final String SEARCH_HELP_PAGE_URL             = "http://code.google.com/p/bwhf/wiki/ReplaySearchInBWHFAgent";
	
	/** URL of the BWHF hacker data base server.                               */
	public static final String BWHF_HACKER_DATA_BASE_SERVER_URL = "http://94.199.240.39/hackerdb/hackers";
	
	/** Name of the resource containing the about html template.               */
	public static final String ABOUT_TEMLATE_RESOURCE_NAME      = "about_template.html";
	
	/** Default Starcraft directory.                                           */
	public static final String DEFAULT_STARCRAFT_DIRECTORY      = SwingWTUtils.isWindows() ? "C:/Program Files/Starcraft" : "/mnt/c/Program Files/Starcraft";
	/** Starcraft replay folder relative to the Starcraft folder.              */
	public static final String STARCRAFT_REPLAY_FOLDER          = "maps/replays";
	/** Name of the last replay file relative to the starcraft folder.         */
	public static final String LAST_REPLAY_FILE_NAME            = "maps/replays/LastReplay.rep";
	/** Name of the Starcraft executable file.                                 */
	public static final String STARCRAFT_EXECUTABLE_FILE_NAME   = "StarCraft.exe";
	/** Name of directory containing sound samples.                            */
	public static final String SOUNDS_DIRECTORY_NAME            = "sounds";
	/** Name of directory containing utility programs.                         */
	public static final String UTILS_DIRECTORY_NAME             = "utils";
	/** Name of directory containing the HTML summary reports of manual scans. */
	public static final String HTML_REPORT_DIRECTORY_NAME       = "HTML Reports";
	
	/** Labels for the possible values of flag hacker replays position. */
	public static final String[] FLAG_HACKER_REPS_POSITION_LABELS = new String[] { "beginning", "end" };
	/** Index for the beginning flag hacker reps position. */
	public static final int FLAG_HACKER_REPS_POSITION_BEGINNING = 0;
	/** Index for the end flag hacker reps position.       */
	public static final int FLAG_HACKER_REPS_POSITION_END       = 1;
	
	
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
	/** Include replay header property.          */
	public static final String PROPERTY_INCLUDE_REPLAY_HEADER          = "includeReplayHeader";
	/** Play sound property.                     */
	public static final String PROPERTY_PLAY_SOUND                     = "playSound";
	/** Found hacks wav file property.           */
	public static final String PROPERTY_FOUND_HACKS_WAV_FILE           = "foundHacksWavFile";
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
	/** APM chart detail level property.         */
	public static final String PROPERTY_APM_CHART_DETAIL_LEVEL         = "apmChartDetailLevel";
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
	
	/** Properties holding the default settings. */
	public static final Properties DEFAULT_SETTINGS_PROPERTIES = new Properties();
	static {
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_STARCRAFT_FOLDER              , DEFAULT_STARCRAFT_DIRECTORY );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTOSCAN_ENABLED              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_HACKER_REPS              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_HACKER_REPS_DESTINATION       , SwingWTUtils.isWindows() ? "c:/replays/hackerreps" : System.getProperty( "user.home" ) + "/replays/hackerreps" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_ALL_REPS                 , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ALL_REPS_DESTINATION          , SwingWTUtils.isWindows() ? "c:/replays/allreps" : System.getProperty( "user.home" ) + "/replays/allreps" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTO_CONVERT_PCX_ENABLED      , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_PCX_OUTPUT_FORMAT             , "JPG" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_INCLUDE_REPLAY_HEADER         , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_PLAY_SOUND                    , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_FOUND_HACKS_WAV_FILE          , SOUNDS_DIRECTORY_NAME + "/falling.wav" );
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
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SOUND_VOLUME                  , "60" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_REPLAY_START_FOLDER           , STARCRAFT_REPLAY_FOLDER );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ENABLE_SYSTEM_TRAY_ICON       , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ALWAYS_MINIMIZE_TO_TRAY       , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_START_AGENT_MINIMIZED_TO_TRAY , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_WINDOW_POSITION          , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_WINDOW_POSITION               , "50,20,950,700" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_CHART_TYPE                    , "0" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ALL_PLAYERS_ON_ONE_CHART      , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_USE_PLAYERS_IN_GAME_COLORS    , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTO_DISABLE_INACTIVE_PLAYERS , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_APM_CHART_DETAIL_LEVEL        , "4" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SHOW_SELECT_HOTKEYS           , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_BUILD_ORDER_DISPLAY_LEVELS    , "4" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SHOW_UNITS_ON_BUILD_ORDER     , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_HIDE_WORKER_UNITS             , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_STRATEGY_DISPLAY_LEVELS       , "4" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_OVERALL_APM_CHART_DETAIL_LEVEL, "7" );
	}
	
}
