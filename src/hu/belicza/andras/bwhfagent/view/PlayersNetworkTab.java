package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.hackerdb.ServerApiConsts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JPanel;


/**
 * Players' Network tab.
 * 
 * @author Andras Belicza
 */
public class PlayersNetworkTab extends ProgressLoggedTab {
	
	/** Log file name for autoscan. */
	private static final String LOG_FILE_NAME = "players_network.log";
	
	/** Flag hacker reps checkbox.                       */
	protected final JCheckBox autoSendInfoAboutLastReplayCheckBox = new JCheckBox( "Automatically send info about 'LastReplay.rep' to Players' Network", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTO_SEND_INFO_ABOUT_LAST_REP ) ) );
	
	/** Button to select folders to send. */
	private final JButton selectFoldersButton = new JButton( "Select folders to send recursively...", IconResourceManager.ICON_FOLDER_CHOOSER );
	/** Button to select files to send.   */
	private final JButton selectFilesButton   = new JButton( "Select files to send...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to stop sending.           */
	private final JButton stopSendingButton   = new JButton( "Stop sending", IconResourceManager.ICON_STOP );
	
	/** Variable to store stop requests of sending. */
	private volatile boolean requestedToStop;
	
	/**
	 * Creates a new PlayersNetworkTab.
	 */
	public PlayersNetworkTab() {
		super( "Players' Network", IconResourceManager.ICON_PLAYERS_NETWORK, LOG_FILE_NAME );
		
		buildGUI();
	}
	
	@Override
	protected void buildGUI() {
		final JButton visitPlayersNetworkButton = new JButton( "Visit Players' Network", IconResourceManager.ICON_WORLD_GO );
		visitPlayersNetworkButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.PLAYERS_NETWORK_PAGE_URL );
			}
		} );
		contentBox.add( Utils.wrapInPanel( visitPlayersNetworkButton ) );
		
		contentBox.add( Utils.wrapInPanel( autoSendInfoAboutLastReplayCheckBox ) );
		
		final ActionListener selectFilesAndFoldersActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( MainFrame.getInstance().generalSettingsTab.getReplayStartFolder() );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( Utils.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( event.getSource() == selectFoldersButton ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				// SwingWT does not support selecting multiple directories yet, getSelectedFiles() returns null so I have to call getSelectedFile() in case of folders.
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					sendFileAndFolderInfo( event.getSource() == selectFoldersButton ? new File[] { fileChooser.getSelectedFile() } : fileChooser.getSelectedFiles() );
			}
		};
		
		final JPanel buttonsPanel = Utils.createWrapperPanel();
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( selectFilesAndFoldersActionListener );
		buttonsPanel.add( selectFilesButton );
		selectFoldersButton.setMnemonic( 'd' );
		selectFoldersButton.addActionListener( selectFilesAndFoldersActionListener );
		buttonsPanel.add( selectFoldersButton );
		contentBox.add( buttonsPanel );
		
		stopSendingButton.setEnabled( false );
		stopSendingButton.setMnemonic( 't' );
		stopSendingButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				requestedToStop = true; // Access to a volatile variable is automatically synchronized from Java 5.0
				stopSendingButton.setEnabled( false );
			}
		} );
		contentBox.add( Utils.wrapInPanel( stopSendingButton ) );
		
		super.buildGUI();
	}
	
	/**
	 * Sends info about files and folders to the Players' Network.
	 * @param files files and folders to be sent info about
	 */
	protected void sendFileAndFolderInfo( final File[] files ) {
		if ( !selectFilesButton.isEnabled() )
			return;
		
		requestedToStop = false;
		
		selectFoldersButton.setEnabled( false );
		selectFilesButton  .setEnabled( false );
		stopSendingButton  .setEnabled( true  );
		
		new NormalThread() {
			/** List of replay files to be scanned. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				try {
					progressBar.setValue( 0 );
					
					logMessage( "\n", false ); // Prints 2 empty lines
					logMessage( "Counting replays..." );
					
					chooseReplayFiles( files );
					progressBar.setMaximum( replayFileList.size() );
					
					if ( requestedToStop )
						return;
					
					final String sendingMessage = "Sending info about " + replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" );
					logMessage( sendingMessage + "..." );
					
					final long startNanoTime = System.nanoTime();
					
					int counter = 0;	
					int skippedRepsCount = 0;
					for ( final File replayFile : replayFileList ) {
						if ( requestedToStop )
							return;
						
						if ( !sendFileInfo( replayFile, null ) )
							skippedRepsCount++;
						
						progressBar.setValue( ++counter );
					}
					
					final long endNanoTime = System.nanoTime();
					
					logMessage( sendingMessage + " done in " + Utils.formatNanoTimeAmount( endNanoTime - startNanoTime ) );
					logMessage( "\tSkipped/failed " + skippedRepsCount + " replay" + ( skippedRepsCount == 1 ? "" : "s" ) + ".", false );
				}
				finally {
					if ( requestedToStop )
						logMessage( "Sending was manually aborted!" );
					stopSendingButton  .setEnabled( false );
					selectFilesButton  .setEnabled( true  );
					selectFoldersButton.setEnabled( true  );
				}
			}
			
			private final java.io.FileFilter IO_REPLAY_FILE_FILTER = new java.io.FileFilter() {
				public boolean accept( final File pathname ) {
					return Utils.SWING_REPLAY_FILE_FILTER.accept( pathname );
				}
			};
			
			/**
			 * Chooses the replay files in the specified files and folders.
			 * @param files files and folders to be chosen from
			 */
			private void chooseReplayFiles( final File[] files ) {
				if ( files == null )
					return;
				for ( final File file : files ) {
					if ( requestedToStop )
						return;
					if ( file.isDirectory() )
						chooseReplayFiles( file.listFiles( IO_REPLAY_FILE_FILTER ) );
					else
						if ( IO_REPLAY_FILE_FILTER.accept( file ) )
							replayFileList.add( file );
				}
			}
			
		}.start();
	}
	
	/**
	 * Sends info about a file to the Players' Network.
	 * @param replayFile file be sent info about
	 * @param gateway    gateway where the game was played - optional
	 * @return true if info sent successfully; false otherwise
	 */
	protected boolean sendFileInfo( final File replayFile, final Integer gateway ) {
		final String replayFileAbsolutePath = replayFile.getAbsolutePath();
		
		logMessage( "Sending info about " + replayFileAbsolutePath + "..." );
		
		final Replay replay = BinRepParser.parseReplay( replayFile, true, false );
		
		if ( replay == null ) {
			logMessage( "Could not parse " + replayFileAbsolutePath + "!" );
			return false;
		}
		else {
			OutputStreamWriter output = null; 
			BufferedReader     input  = null;
			try {
				final URLConnection urlConnection = new URL( Consts.PLAYERS_NETWORK_DATA_BASE_URL + '?' + ServerApiConsts.PN_REQUEST_PARAM_NAME_OPERATION + '=' + ServerApiConsts.PN_OPERATION_SEND ).openConnection();
				urlConnection.setDoOutput( true );
				urlConnection.setDoInput( true );
				output = new OutputStreamWriter( urlConnection.getOutputStream() ); 
				
				final ReplayHeader  replayHeader = replay.replayHeader;
				final StringBuilder infoBuilder  = new StringBuilder();
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_ENGINE       , Byte.toString( replayHeader.gameEngine ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_FRAMES       , Integer.toString( replayHeader.gameFrames ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_SAVE_TIME    , Long.toString( replayHeader.saveTime.getTime() ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_NAME         , replayHeader.gameName );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_MAP_WIDTH    , Short.toString( replayHeader.mapWidth ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_MAP_HEIGHT   , Short.toString( replayHeader.mapHeight ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_SPEED        , Short.toString( replayHeader.gameSpeed ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_TYPE         , Short.toString( replayHeader.gameType ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_SUB_TYPE     , Short.toString( replayHeader.gameSubType ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_CREATOR_NAME , replayHeader.creatorName );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_MAP_NAME     , replayHeader.mapName );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_REPLAY_MD5   , Utils.calculateFileMd5( replayFile ) );
				appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_AGENT_VERSION, MainFrame.getInstance().applicationVersion );
				
				int playerCounter = 0;
				for ( int i = 0; i < replayHeader.playerNames.length; i++ )
					if ( replayHeader.playerNames[ i ] != null ) {
						appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_PLAYER_NAME    + playerCounter, replayHeader.playerNames[ i ] );
						appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_PLAYER_RACE    + playerCounter, Integer.toString( replayHeader.playerRaces[ i ] & 0xff ) );
						appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_PLAYER_ACTIONS + playerCounter, Integer.toString( replayHeader.playerIdActionsCounts[ replayHeader.playerIds[ i ] ] ) );
						appendKeyValue( infoBuilder, ServerApiConsts.GAME_PARAM_PLAYER_COLOR   + playerCounter, Integer.toString( replayHeader.playerColors[ i ] ) );
						playerCounter++;
					}
				
				output.write( infoBuilder.toString() );
				output.flush();
				
				input = new BufferedReader( new InputStreamReader( urlConnection.getInputStream() ) );
				final String message = input.readLine();
				
				output.close();
				
				if ( message.equals( ServerApiConsts.REPORT_ACCEPTED_MESSAGE ) ) {
					logMessage( "Successfully sent " + replayFileAbsolutePath + "." );
					return true;
				}
				else {
					logMessage( "Rejected, reason: " + message );
					return false;
				}
			} catch ( final Exception e ) {
				logMessage( "Cound not send " + replayFileAbsolutePath + "." );
				return false;
			}
			finally {
				if ( output != null )
					try { output.close(); } catch ( final IOException ie ) {}
				if ( input != null )
					try { input.close(); } catch ( final IOException ie ) {}
			}
		}
	}
	
	/**
	 * Appends a key-value pair to a builder which will be transferred over HTTP.
	 * @param builder builder to append to
	 * @param key     key of the pair
	 * @param value   value of the pair
	 */
	private static void appendKeyValue( final StringBuilder builder, final String key, final String value ) {
		if ( builder.length() > 0 )
			builder.append( '&' );
		try {
			builder.append( URLEncoder.encode( key, "UTF-8" ) ).append( '=' ).append( URLEncoder.encode( value, "UTF-8" ) );
		} catch ( final UnsupportedEncodingException ue ) {
		}
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTO_SEND_INFO_ABOUT_LAST_REP, Boolean.toString( autoSendInfoAboutLastReplayCheckBox.isSelected() ) );
	}
	
}
