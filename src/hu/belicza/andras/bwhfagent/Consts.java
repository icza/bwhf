package hu.belicza.andras.bwhfagent;

import java.util.Properties;

/**
 * Application wide constants.
 * 
 * @author Belicza Andras
 */
public class Consts {
	
	/** Name of the application.                                       */
	public static final String APPLICATION_NAME                 = "BWHF Agent";
	/** Author of the application.                                     */
	public static final String APPLICATION_AUTHOR               = "András Belicza";
	
	/** Home page url string.                                          */
	public static final String HOME_PAGE_URL                    = "http://code.google.com/p/bwhf/";
	/** Name of the current version resource file.                     */
	public static final String VERSION_RESOURCE_NAME            = "current_version.txt";
	/** URL pointing to the latest stable version text.                */
	public static final String LATEST_STABLE_VERSION_TEXT_URL   = "http://bwhf.googlecode.com/svn/trunk/latest_stable_version.txt";
	
	/** URL of the BWHF hacker data base server.                       */
	public static final String BWHF_HACKER_DATA_BASE_SERVER_URL = "http://94.199.240.39/hackerdb/hackers";
	
	/** Name of the resource containing the about html template.       */
	public static final String ABOUT_TEMLATE_RESOURCE_NAME      = "about_template.html";
	
	/** Default Starcraft directory.                                   */
	public static final String DEFAULT_STARCRAFT_DIRECTORY      = "C:/Program Files/Starcraft";
	/** Starcraft replay folder relative to the Starcraft folder.      */
	public static final String STARCRAFT_REPLAY_FOLDER          = "maps/replays";
	/** Name of the last replay file relative to the starcraft folder. */
	public static final String LAST_REPLAY_FILE_NAME            = "maps/replays/LastReplay.rep";
	/** Name of the Starcraft executable file.                         */
	public static final String STARCRAFT_EXECUTABLE_FILE_NAME   = "StarCraft.exe";
	/** Name of directory containing sound samples.                    */
	public static final String SOUNDS_DIRECTORY_NAME            = "sounds";
	/** Name of directory containing utility programs.                 */
	public static final String UTILS_DIRECTORY_NAME             = "utils";
	
	/** Replay converter utility to extract actions from a replay.     */
	public static final String REPLAY_CONVERTER_EXECUTABLE_FILE;
	static {
		final String osName = System.getProperty( "os.name" );
		
		if ( osName != null && osName.startsWith( "Windows" ) )
			REPLAY_CONVERTER_EXECUTABLE_FILE = UTILS_DIRECTORY_NAME + "/repextractor.exe";
		else
			REPLAY_CONVERTER_EXECUTABLE_FILE = null;
	}
	
	/** Labels for the possible values of flag hacker replays position. */
	public static final String[] FLAG_HACKER_REPS_POSITION_LABELS = new String[] { "beginning", "end" };
	/** Index for the beginning flag hacker reps position. */
	public static final int FLAG_HACKER_REPS_POSITION_BEGINNING = 0;
	/** Index for the end flag hacker reps position.       */
	public static final int FLAG_HACKER_REPS_POSITION_END       = 1;
	
	
	/** Name of the settings properties file. */
	public static final String SETTINGS_PROPERTIES_FILE = "settings.properites";
	
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
	/** Check updates on startup property.       */
	public static final String PROPERTY_CHECK_UPDATES_ON_STARTUP       = "checkUpdatesOnStartup";
	/** Skip latter actions of hackers property. */
	public static final String PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS = "skipLatterActionsOfHackers";
	/** Sound volume property.                   */
	public static final String PROPERTY_SOUND_VOLUME                   = "soundVolume";
	
	/** Properties holding the default settings. */
	public static final Properties DEFAULT_SETTINGS_PROPERTIES = new Properties();
	static {
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_STARCRAFT_FOLDER              , "C:/Program Files/Starcraft" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTOSCAN_ENABLED              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_HACKER_REPS              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_HACKER_REPS_DESTINATION       , "c:/replays/hackerreps" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SAVE_ALL_REPS                 , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_ALL_REPS_DESTINATION          , "c:/replays/allreps" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_PLAY_SOUND                    , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_FOUND_HACKS_WAV_FILE          , SOUNDS_DIRECTORY_NAME + "/falling.wav" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_BRING_TO_FRONT                , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_REPORT_HACKERS                , "false" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_GATEWAY                       , "0" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_AUTHORIZATION_KEY             , "" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_FLAG_HACKER_REPS              , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_FLAG_HACKER_REPS_POSITION     , Integer.toString( FLAG_HACKER_REPS_POSITION_END ) );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_CHECK_UPDATES_ON_STARTUP      , "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SKIP_LATTER_ACTIONS_OF_HACKERS, "true" );
		DEFAULT_SETTINGS_PROPERTIES.setProperty( PROPERTY_SOUND_VOLUME                  , "70" );
	}
	
}
