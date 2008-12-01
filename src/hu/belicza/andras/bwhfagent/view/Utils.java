package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.ReplayParser;
import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhfagent.Consts;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * Utility methods related to view and GUI.
 * 
 * @author Belicza Andras
 */
public class Utils {
	
	/** Size of buffer to use to play wav files.  */
	private static final int WAV_BUFFER_SIZE       = 128*1024;
	/** Size of buffer to use to copy files.      */
	private static final int FILE_COPY_BUFFER_SIZE = 4*1024;
	
	/** Public reference to the settings properties. */
	public static final Properties settingsProperties = new Properties( Consts.DEFAULT_SETTINGS_PROPERTIES );
	static {
		try {
			settingsProperties.load( new FileInputStream( Consts.SETTINGS_PROPERTIES_FILE ) );
		} catch ( final Exception e ) {
		}
	}
	
	/**
	 * Saves the settings properties.
	 */
	public static void saveSettingsProperties() {
		try {
			settingsProperties.store( new FileOutputStream( Consts.SETTINGS_PROPERTIES_FILE ), null );
		} catch ( final Exception e) {
		}
	}
	
	/**
	 * Wraps a component into a {@link JPanel} with a {@link FlowLayout}.
	 * @param component component to be wrapped
	 * @return the panel wrapping the component
	 */
	public static JPanel wrapInPanel( final JComponent component ) {
		final JPanel panel = new JPanel();
		panel.add( component );
		return panel;
	}
	
	/**
	 * Creates and returns a button with a registered action listener which opens a file chooser
	 * with the specified file selection mode, and on approved returned option stores the selected file
	 * into the target text field. 
	 * @param parent            component to be used as parent for the file chooser dialog
	 * @param targetTextField   text field to be updated if file/folder is selected
	 * @param fileSelectionMode the type of files to be displayed
	 * 							<ul>
	 * 								<li>JFileChooser.FILES_ONLY
	 * 								<li>JFileChooser.DIRECTORIES_ONLY
	 * 								<li>JFileChooser.FILES_AND_DIRECTORIES
	 * 							</ul>
	 * @param choosableFileFilter file filter to add as a choosable file filter
	 * @return a button handling the file chooser
	 */
	public static JButton createFileChooserButton( final Component parent, final JTextField targetTextField, final int fileSelectionMode, final FileFilter choosableFileFilter ) {
		final JButton chooseButton = new JButton( "Choose..." );
		
		chooseButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( targetTextField.getText() );
				
				if ( choosableFileFilter != null )
					fileChooser.addChoosableFileFilter( choosableFileFilter ); 
				
				fileChooser.setFileSelectionMode( fileSelectionMode );
				if ( fileChooser.showOpenDialog( parent ) == JFileChooser.APPROVE_OPTION )
					targetTextField.setText( fileChooser.getSelectedFile().getAbsolutePath() );
			}
		} );
		
		return chooseButton;
	}
	
	/**
	 * Opens the web page specified by the url in the system's default browser.
	 * @param url url to be opened
	 */
	public static void showURLInBrowser( final String url ) {
		try {
			final String osName = System.getProperty( "os.name" );
			
			String[] cmdArray = null;
			if ( osName != null && osName.startsWith( "Windows" ) )
				cmdArray = new String[] { "rundll32", "url.dll,FileProtocolHandler", url };
			else
				cmdArray = new String[] { "netscape", "-remote", "openURL", url };
			
			Runtime.getRuntime().exec( cmdArray );
		} catch ( final IOException ie ) {
		}
	}
	
	/**
	 * Reads the application version string from an input stream.<br>
	 * The version string is the first line of the stream.
	 * 
	 * @param inputStream input stream to read from
	 * @return the application version; or null if read failed 
	 */
	public static String readVersionStringFromStream( final InputStream inputStream ) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
			return bufferedReader.readLine();
		} catch ( final Exception e ) {
			return null;
		}
		finally {
			if ( bufferedReader != null )
				try { bufferedReader.close(); } catch ( final IOException ie ) {}
		}
	}
	
	
	/**
	 * Formats a nano time amount to human readable.
	 * @param nanoTimeAmount amount of time in nanos
	 * @return the human readable formatted text of the specified amount of time
	 */
	public static String formatNanoTimeAmount( final long nanoTimeAmount ) {
		final long ms = nanoTimeAmount / 1000000l; 
		String formattedText = ( ms % 1000l ) + " ms";
		final long sec = ms / 1000l;
		
		if ( sec > 0 ) {
			formattedText = ( sec % 60l ) + " sec " + formattedText;
		
			final long min = sec / 60l;
			if ( min > 0 ) {
				formattedText = ( min % 60l ) + " min " + formattedText;
				
				final long hours = min / 60l;
				if ( hours > 0 )
					formattedText = hours + " hour(s) " + formattedText;
			}
		}
		
		return formattedText;
	}
	
	/**
	 * Plays a wav file.<br>
	 * Will return immediately, the wav playing will run on a new thread.
	 * 
	 * @param wavFileName name of the wav file to play
	 * @return true if the file was started playing; false if error occurred
	 */
	public static boolean playWavFile( final String wavFileName ) {
		try {
			final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( new File( wavFileName ) );
			final AudioFormat      audioFormat      = audioInputStream.getFormat();
			final SourceDataLine   audioLine        = (SourceDataLine) AudioSystem.getLine( new DataLine.Info( SourceDataLine.class, audioFormat ) );
			
			audioLine.open( audioFormat );
			
			new Thread() {
				@Override
				public void run() {
					try {
						audioLine.start();
						
						final byte[] buffer = new byte[ WAV_BUFFER_SIZE ];
						int          bytesRead;
						
						while ( ( bytesRead = audioInputStream.read( buffer ) ) > 0 )
							audioLine.write( buffer, 0, bytesRead );
						
					}
					catch ( final Exception e  ){
					}
					finally {
						audioLine.drain();
						audioLine.close();
					}
				}
			}.start();
			
			return true;
		}
		catch ( final Exception e ) {
			return false;
		}
	}
	
	/**
	 * Scans a replay file and returns the hack descriptions found in it.
	 * @param replayFile file to be scanned
	 * @return the hack descriptions found in the replay
	 */
	public static List< String > scanReplayFile( final File replayFile ) {
		final JCheckBox skipLatterActionsOfHackersCheckBox = MainFrame.getInstance().generalSettingsTab.skipLatterActionsOfHackersCheckBox;
		
		final File exportFile = new File( "rep-" + (long) ( Math.random() * 1000000l ) + "-" + new Date().getTime() + ".out" );
		try {
			final Process process = Runtime.getRuntime().exec( new String[] { Consts.REPLAY_CONVERTER_EXECUTABLE_FILE, replayFile.getAbsolutePath(), exportFile.getAbsolutePath() } );
			process.waitFor();
			
			return ReplayScanner.scanReplayForHacks( ReplayParser.parseBWChartExportFile( exportFile ), skipLatterActionsOfHackersCheckBox.isSelected() );
		} catch ( final Exception e ) {
			new Object();
			return null;
		}
		finally {
			if ( !exportFile.delete() )
				if ( exportFile.exists() )
					exportFile.deleteOnExit();
		}
	}
	
	/**
	 * Copies the source file to the destination folder using the specified destination file name.<br>
	 * If destination folder does not exist, tries to create it (recursively).
	 * 
	 * @param sourceFile          source file to be copied
	 * @param destinationFolder   destination folder to copy to
	 * @param destinationFileName destination file name to be used for the copied file
	 */
	public static void copyFile( final File sourceFile, final File destinationFolder, final String destinationFileName ) {
		if ( !destinationFolder.exists() )
			destinationFolder.mkdirs();
		if ( destinationFolder.exists() && destinationFolder.isDirectory() ) {
			Utils.copyFile( sourceFile, new File( destinationFolder, destinationFileName ) );
		}
	}
	
	/**
	 * Copies the source file to the destination file.
	 * @param sourceFile      source file to be copied
	 * @param destinationFile destination file to copy to
	 */
	public static void copyFile( final File sourceFile, final File destinationFile ) {
		FileInputStream  inputStream  = null;
		FileOutputStream outputStream = null;
		try {
			inputStream  = new FileInputStream ( sourceFile      );
			outputStream = new FileOutputStream( destinationFile );
			
			final byte[] buffer =  new byte[ FILE_COPY_BUFFER_SIZE ];
			
			int bytesRead;
			while ( ( bytesRead = inputStream.read( buffer ) ) > 0 )
				outputStream.write( buffer, 0, bytesRead );
			
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
		finally {
			if ( outputStream != null )
				try { outputStream.close(); } catch ( final Exception e ) {}
			if ( inputStream != null )
				try { inputStream.close(); } catch ( final Exception e ) {}
		}
	}
	
}
