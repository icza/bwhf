package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.hackerdb.ServerApiConsts;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import swingwt.awt.BorderLayout;
import swingwt.awt.Component;
import swingwt.awt.Dimension;
import swingwt.awt.FlowLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JComponent;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JPanel;
import swingwtx.swing.JTextField;
import swingwtx.swing.SwingWTUtils;
import swingwtx.swing.filechooser.FileFilter;

/**
 * Utility methods related to view and GUI.
 * 
 * @author Belicza Andras
 */
public class Utils {
	
	/** Date format to create timestamps for file names.       */
	public static final DateFormat DATE_FORMAT       = new SimpleDateFormat( "yyyy-MM-dd HH-mm-ss" );
	/** Date format to create short timestamps for file names. */
	public static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat( "yyMMdd HHmmss" );
	
	/** Size of buffer to use to play wav files.  */
	private static final int WAV_BUFFER_SIZE       = 64*1024;
	/** Size of buffer to use to copy files.      */
	private static final int FILE_COPY_BUFFER_SIZE = 4*1024;
	
	/** Replay file filter. */
	public static final FileFilter SWING_REPLAY_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File file ) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith( ".rep" );
		}
		@Override
		public String getDescription() {
			return "Replay files (*.rep)";
		}
	};
	
	/** Replay file filter. */
	public static final FileFilter SWING_TEXT_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File file ) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith( ".txt" );
		}
		@Override
		public String getDescription() {
			return "Text files (*.txt)";
		}
	};
	
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
		} catch ( final Exception e ) {
		}
	}
	
	public static Dimension getMaxDimension() {
		return new Dimension( 10000, 10000 );
	}
	
	/**
	 * Creates a panel with FlowLayout, and sets its maximum to a relatively high value
	 * to be big enough for all screen resolution.<br>
	 * This is required, because SwingWT does not size "floating" panels to take all the remaining space.
	 * @return a panel to be used to wrap other elements
	 */
	public static JPanel createWrapperPanel() {
		final JPanel panel = new JPanel();
		panel.setMaximumSize( getMaxDimension() );
		return panel;
	}
	
	/**
	 * Creates a panel with FlowLayout aligning to left, and sets its maximum to a relatively high value
	 * to be big enough for all screen resolution.<br>
	 * This is required, because SwingWT does not size "floating" panels to take all the remaining space.
	 * @return a panel to be used to wrap other elements
	 */
	public static JPanel createWrapperPanelLeftAligned() {
		final JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		panel.setMaximumSize( getMaxDimension() );
		return panel;
	}
	
	/**
	 * Wraps a component into a {@link JPanel} with a {@link FlowLayout}.
	 * @param component component to be wrapped
	 * @return the panel wrapping the component
	 */
	public static JPanel wrapInPanel( final JComponent component ) {
		final JPanel panel = createWrapperPanel();
		panel.add( component );
		return panel;
	}
	
	/**
	 * Wraps a component into a {@link JPanel} with a {@link FlowLayout} aligning components to left.
	 * @param component component to be wrapped
	 * @return the panel wrapping the component
	 */
	public static JPanel wrapInPanelLeftAligned( final JComponent component ) {
		final JPanel panel = createWrapperPanelLeftAligned();
		panel.add( component );
		return panel;
	}
	
	/**
	 * Wraps a component into a {@link JPanel} with a {@link BorderLayout}.
	 * @param component component to be wrapped
	 * @return the panel wrapping the component
	 */
	public static JPanel wrapInBorderLayoutPanel( final JComponent component ) {
		final JPanel panel = new JPanel( new BorderLayout() );
		panel.add( component, BorderLayout.CENTER );
		return panel;
	}
	
	/**
	 * Creates and returns a button with a registered action listener which opens a file chooser
	 * with the specified file selection mode, and on approved returned option stores the selected file
	 * into the target text field. 
	 * @param parent                 component to be used as parent for the file chooser dialog
	 * @param targetTextField        text field to be updated if file/folder is selected
	 * @param fileSelectionMode      the type of files to be displayed
	 * 								 <ul>
	 * 									<li>JFileChooser.FILES_ONLY
	 * 									<li>JFileChooser.DIRECTORIES_ONLY
	 * 									<li>JFileChooser.FILES_AND_DIRECTORIES
	 * 								 </ul>
	 * @param choosableFileFilter    file filter to add as a choosable file filter for Swing
	 * @param choosableFileFilterSWT file filter to add as a choosable file filter for SwingWT
	 * @param taskOnApprove          task to be executed after selection if approve is performed, can be null
	 * @return a button handling the file chooser
	 */
	public static JButton createFileChooserButton( final Component parent, final JTextField targetTextField, final int fileSelectionMode, final FileFilter choosableFileFilter, final String[][] choosableFileFilterSWT, final Runnable taskOnApprove ) {
		final JButton chooseButton = new JButton( "Choose...", fileSelectionMode == JFileChooser.DIRECTORIES_ONLY ? IconResourceManager.ICON_FOLDER_CHOOSER : IconResourceManager.ICON_FILE_CHOOSER );
		
		chooseButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( targetTextField.getText() );
				
				if ( choosableFileFilter != null )
					fileChooser.addChoosableFileFilter( choosableFileFilter ); 
				if ( choosableFileFilterSWT != null )
					fileChooser.setExtensionFilters( choosableFileFilterSWT[ 0 ], choosableFileFilterSWT[ 1 ] );
				
				fileChooser.setFileSelectionMode( fileSelectionMode );
				if ( fileChooser.showOpenDialog( parent ) == JFileChooser.APPROVE_OPTION )
					targetTextField.setText( fileChooser.getSelectedFile().getAbsolutePath() );
				
				if ( taskOnApprove != null )
					taskOnApprove.run();
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
			boolean useOwnMethod = true;
			if ( Desktop.isDesktopSupported() )
				try {
					Desktop.getDesktop().browse( new URL( url ).toURI() );
					useOwnMethod = false;
				}
				catch ( final Exception e ) {
				}
			
			if ( useOwnMethod ) {
				String[] cmdArray = null;
				if ( SwingWTUtils.isWindows() ) {
					cmdArray = new String[] { "rundll32", "url.dll,FileProtocolHandler", url };
				}
				else {
					// Linux
					final String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
					for ( final String browser : browsers )
						if ( Runtime.getRuntime().exec( new String[] { "which", browser } ).waitFor() == 0 ) {
							cmdArray = new String[] { browser, url };
							break;
						}
				}
				
				if ( cmdArray != null )
					Runtime.getRuntime().exec( cmdArray );
			}
		} catch ( final Exception e ) {
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
	 * @param wavFile the wav file to play
	 * @param waitPlayEnd tells if have to wait the end of the play
	 * @return true if the file was started playing; false if error occurred
	 */
	public static boolean playWavFile( final File wavFile, final boolean waitPlayEnd ) {
		try {
			final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( wavFile );
			final AudioFormat      audioFormat      = audioInputStream.getFormat();
			final SourceDataLine   audioLine        = (SourceDataLine) AudioSystem.getLine( new DataLine.Info( SourceDataLine.class, audioFormat ) );
			
			audioLine.open( audioFormat );
			
			if ( audioLine.isControlSupported( FloatControl.Type.MASTER_GAIN ) ) {
				final FloatControl volume = (FloatControl) audioLine.getControl( FloatControl.Type.MASTER_GAIN );
	            volume.setValue( (float) ( 20.0*Math.log10( MainFrame.getInstance().generalSettingsTab.soundVolumeSlider.getValue() / 100.0 ) ) );
			}
			
			final Thread wavPlayerThread = new Thread() {
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
			};
			wavPlayerThread.start();
			
			if ( waitPlayEnd )
				wavPlayerThread.join();
			
			return true;
		}
		catch ( final Exception e ) {
			return false;
		}
	}
	
	/**
	 * Copies the source file to the destination folder using the specified destination file name.<br>
	 * If destination folder does not exist, tries to create it (recursively).
	 * 
	 * @param sourceFile          source file to be copied
	 * @param destinationFolder   destination folder to copy to
	 * @param destinationFileName destination file name to be used for the copied file
	 * @return true if copying was successful; false otherwise
	 */
	public static boolean copyFile( final File sourceFile, final File destinationFolder, final String destinationFileName ) {
		if ( !destinationFolder.exists() )
			destinationFolder.mkdirs();
		if ( destinationFolder.exists() && destinationFolder.isDirectory() )
			return Utils.copyFile( sourceFile, new File( destinationFolder, destinationFileName ) );
		else
			return false;
	}
	
	/**
	 * Copies the source file to the destination file.
	 * @param sourceFile      source file to be copied
	 * @param destinationFile destination file to copy to
	 * @return true if copying was successful; false otherwise
	 */
	public static boolean copyFile( final File sourceFile, final File destinationFile ) {
		FileInputStream  inputStream  = null;
		FileOutputStream outputStream = null;
		try {
			inputStream  = new FileInputStream ( sourceFile      );
			outputStream = new FileOutputStream( destinationFile );
			
			final byte[] buffer =  new byte[ FILE_COPY_BUFFER_SIZE ];
			
			int bytesRead;
			while ( ( bytesRead = inputStream.read( buffer ) ) > 0 )
				outputStream.write( buffer, 0, bytesRead );
			outputStream.flush();
			
			return true;
		} catch ( final Exception e ) {
			e.printStackTrace();
			return false;
		}
		finally {
			if ( outputStream != null )
				try { outputStream.close(); } catch ( final Exception e ) {}
			if ( inputStream != null )
				try { inputStream.close(); } catch ( final Exception e ) {}
		}
	}
	
	/**
	 * Checks an authorization key.
	 * @param key key to be checked
	 * @return true if the key is valid; false if key is invalid; or null if check failed
	 */
	public static Boolean checkAuthorizationKey( final String key ) {
		BufferedReader input = null;
		try {
			input = new BufferedReader( new InputStreamReader( new URL( Consts.BWHF_HACKER_DATA_BASE_SERVER_URL + "?" + ServerApiConsts.REQUEST_PARAMETER_NAME_OPERATION + "=" + ServerApiConsts.OPERATION_CHECK + "&" + ServerApiConsts.REQUEST_PARAMETER_NAME_KEY + "=" + URLEncoder.encode( key, "UTF-8" ) ).openStream() ) );
			return Boolean.parseBoolean( input.readLine() );
		} catch ( final Exception e ) {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) {}
			return null;
		}
	}
	
	/**
	 * Sends a report to the BWHF hacker data base server.
	 * @param gateway       gateway of the reported players
	 * @param key           authorization key to be used
	 * @param gameEngine    game engine
	 * @param mapName       map name
	 * @param playerNameSet set of player names that were found hacking
	 * @return null if report succeeded; an error message otherwise
	 */
	public static String sendHackerReport( final String key, final int gateway, final int gameEngine, final String mapName, final Set< String > playerNameSet ) {
		BufferedReader input = null;
		try {
			final StringBuilder reportURLBuilder = new StringBuilder( Consts.BWHF_HACKER_DATA_BASE_SERVER_URL );
			reportURLBuilder.append( '?' ).append( ServerApiConsts.REQUEST_PARAMETER_NAME_OPERATION     ).append( '=' ).append( ServerApiConsts.OPERATION_REPORT      )
							.append( '&' ).append( ServerApiConsts.REQUEST_PARAMETER_NAME_KEY           ).append( '=' ).append( URLEncoder.encode( key, "UTF-8" )     )
							.append( '&' ).append( ServerApiConsts.REQUEST_PARAMETER_NAME_GATEWAY       ).append( '=' ).append( gateway                               )
							.append( '&' ).append( ServerApiConsts.REQUEST_PARAMETER_NAME_GAME_ENGINE   ).append( '=' ).append( gameEngine                            )
							.append( '&' ).append( ServerApiConsts.REQUEST_PARAMETER_NAME_MAP_NAME      ).append( '=' ).append( URLEncoder.encode( mapName, "UTF-8" ) )
							.append( '&' ).append( ServerApiConsts.REQUEST_PARAMETER_NAME_AGENT_VERSION ).append( '=' ).append( URLEncoder.encode( MainFrame.getInstance().applicationVersion, "UTF-8" ) );
			
			int i = 0;
			for ( final String playerName : playerNameSet )
				reportURLBuilder.append( '&' ).append( ServerApiConsts.REQUEST_PARAMETER_NAME_PLAYER ).append( i++ ).append( '=' ).append( URLEncoder.encode( playerName, "UTF-8" ) );
			
			input = new BufferedReader( new InputStreamReader( new URL( reportURLBuilder.toString() ).openStream() ) );
			final String message = input.readLine();
			
			return message.equals( ServerApiConsts.REPORT_ACCEPTED_MESSAGE ) ? null : message;
		} catch ( final Exception e ) {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) {}
			
			return "Error connecting to the BWHF data base server!";
		}
	}
	
	/**
	 * Opens the specified file with the editor program set in the general settigns tab.
	 * @param fileName name of file to be edited
	 */
	public static void editFile( final String fileName ) {
		if ( fileName.length() == 0 )
			return;
		
		final String editorProgram = MainFrame.getInstance().generalSettingsTab.editorProgramTextField.getText();
		
		if ( editorProgram.length() > 0 ) {
			try {
				Runtime.getRuntime().exec( new String[] { editorProgram, fileName } );
			} catch ( final IOException ie ) {
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Calculates the MD5 digest of a file.
	 * @param file file whose MD5 to be calculated
	 * @return the calculated MD5 digest of the file
	 */
	public static String calculateFileMd5( final File file ) {
        FileInputStream input = null;
        try {
        	final MessageDigest md = MessageDigest.getInstance( "MD5" );
        	
        	input = new FileInputStream( file );
        	final byte[] buffer = new byte[ 16*1024 ];
        	
        	int bytesRead;
        	while ( ( bytesRead = input.read( buffer ) ) > 0 )
        		md.update( buffer, 0, bytesRead );
        	
        	final byte[] md5Bytes = md.digest();
        	final StringBuilder hexBuilder = new StringBuilder( md5Bytes.length * 2 );
        	for ( final byte b : md5Bytes )
        		hexBuilder.append( Integer.toHexString( ( b & 0xff ) >> 4 ) ).append( Integer.toHexString( b & 0x0f ) );
        	
	        return hexBuilder.toString();
        }
        catch ( final Exception e ) {
        	return "";
        }
        finally {
        	if ( input != null )
				try { input.close(); } catch (IOException e) {}
        }
	}
	
}
