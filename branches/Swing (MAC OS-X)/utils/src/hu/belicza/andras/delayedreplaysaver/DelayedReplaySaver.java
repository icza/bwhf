package hu.belicza.andras.delayedreplaysaver;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Delayed replay saver to test replay check during a replay saving process.
 * 
 * @author Andras Belicza
 */
public class DelayedReplaySaver {
	
	/** Save time of a replay in ms to simulate. */
	private static final long   SAVE_TIME_MS   = 1500l;
	/** Replay to be copied to LastReplay.rep.   */
	private static final String REPLAY_TO_COPY = "w:/rep/1.rep";
	
	/**
	 * The entry point of the program.
	 * 
	 * @param arguments used to take arguments from the running environment, not used here
	 * @throws Exception thrown if any error occurs
	 */
	public static void main( final String[] arguments ) throws Exception {
		final Properties properties = new Properties();
		final FileInputStream propertiesInput = new FileInputStream( Consts.SETTINGS_PROPERTIES_FILE );
		properties.load( propertiesInput );
		propertiesInput.close();
		
		final File starcraftDirectory = new File( properties.getProperty( Consts.PROPERTY_STARCRAFT_FOLDER ) );
		final File lastReplayFile     = new File( starcraftDirectory, Consts.LAST_REPLAY_FILE_NAME );
		final File sourceFile         = new File( REPLAY_TO_COPY );
		final long sourceSize         = sourceFile.length();
		
		final FileInputStream  input  = new FileInputStream ( sourceFile     );
		final FileOutputStream output = new FileOutputStream( lastReplayFile );
		
		final int BUFFER_SIZE = 1024;
		final byte[] buffer = new byte[ 1024 ];
		int bytesRead;
		
		System.out.println( "Starting to save..." );
		while ( ( bytesRead = input.read( buffer ) ) >= 0 ) {
			output.write( buffer, 0, bytesRead );
			Thread.sleep( SAVE_TIME_MS * BUFFER_SIZE / sourceSize );
		}
		System.out.println( "Saving completed." );
		
		output.close();
		input.close();
	}
	
}
