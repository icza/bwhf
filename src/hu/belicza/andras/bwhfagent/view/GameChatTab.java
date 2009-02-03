package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JFileChooser;


/**
 * Game chat tab.
 * 
 * @author Andras Belicza
 */
public class GameChatTab extends LoggedTab {
	
	/** Log file name for game chat. */
	private static final String LOG_FILE_NAME = "game_chat.log";
	
	/** Button to extract game chat frm the last replay. */
	private final JButton   extractFromLastReplayButton = new JButton( "Extract game chat from 'LastReplay.rep'" );
	/** Button to select files to extract game chat.     */
	private final JButton   selectFilesButton           = new JButton( "Select files to extract game chat from" );
	
	/**
	 * Creates a new PcxConverterTab.
	 */
	public GameChatTab() {
		super( "Game chat", LOG_FILE_NAME );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		extractFromLastReplayButton.setMnemonic( 'l' );
		extractFromLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final Replay replay = BinRepParser.parseReplay( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ), true );
				logMessage( "\n", false ); // Prints 2 empty lines
				if ( replay == null )
					logMessage( "Could not extract game chat from '" + Consts.LAST_REPLAY_FILE_NAME + "'!" );
				else {
					logMessage( "Game chat of '" + Consts.LAST_REPLAY_FILE_NAME + "':" );
					logMessage( replay.gameChat, false );
					logMessage( "End of game chat of '" + Consts.LAST_REPLAY_FILE_NAME + "'." );
				}
			}
		} );
		contentBox.add( Utils.wrapInPanel( extractFromLastReplayButton ) );
		
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ) );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( ManualScanTab.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				// SwingWT does not support selecting multiple directories yet, getSelectedFiles() returns null so I have to call getSelectedFile() in case of folders.
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION ) {
					final File[] replayFiles = fileChooser.getSelectedFiles();
					logMessage( "\n", false ); // Prints 2 empty lines
					final String extractingMessage = "Extracting game chat from " + replayFiles.length + " replay" + ( replayFiles.length == 1 ? "" : "s" );
					logMessage( extractingMessage + "..." );
					logMessage( "Game chats will be written to text files named after the replays." );
					final long startTimeNanons = System.nanoTime();
					
					for ( final File replayFile : replayFiles ) {
						final String absoluteReplayPath = replayFile.getAbsolutePath();
						final Replay replay = BinRepParser.parseReplay( replayFile, true );
						if ( replay == null )
							logMessage( "Could not extract game chat from '" + absoluteReplayPath + "'!" );
						else {
							final int    extensionIndex   = absoluteReplayPath.lastIndexOf( '.' );
							final String gameChatFileName = ( extensionIndex < 0 ? absoluteReplayPath : absoluteReplayPath.substring( 0, extensionIndex ) ) + ".txt";
							PrintWriter output = null;
							try {
								output = new PrintWriter( new FileWriter( gameChatFileName ) );
								output.println( replay.gameChat );
								logMessage( "Game chat from '" + absoluteReplayPath + "' successfully extracted." );
							}
							catch ( final Exception e ) {
								logMessage( "Could not write game chat to '" + gameChatFileName + "'!" );
							}
							finally {
								if ( output != null )
									output.close();
							}
						}
					}
					
					final long endTimeNanons = System.nanoTime();
					logMessage( extractingMessage + " done in " + Utils.formatNanoTimeAmount( endTimeNanons - startTimeNanons ) );
				}
			}
		} );
		contentBox.add( Utils.wrapInPanel( selectFilesButton ) );
		
		super.buildGUI();
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
