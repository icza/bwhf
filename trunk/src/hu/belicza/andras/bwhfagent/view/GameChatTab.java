package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JFileChooser;


/**
 * Game chat tab.
 * 
 * @author Andras Belicza
 */
public class GameChatTab extends ProgressLoggedTab {
	
	/** Log file name for game chat. */
	private static final String LOG_FILE_NAME = "game_chat.log";
	
	/** Button to display game chat from the last replay.  */
	private final JButton   displayFromLastReplayButton = new JButton( "Display game chat from 'LastReplay.rep'" );
	/** Button to select files to extract game chat.       */
	private final JButton   selectFilesButton           = new JButton( "Select files to extract game chat from" );
	/** Checkbox to tell whether to include replay header. */
	private final JCheckBox includeReplayHeaderCheckBox = new JCheckBox( "Also include replay header information", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_INCLUDE_REPLAY_HEADER ) ) );
	
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
		displayFromLastReplayButton.setMnemonic( 'l' );
		displayFromLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				progressBar.setValue( 0 );
				progressBar.setMaximum( 1 );
				final Replay replay = BinRepParser.parseReplay( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ), true );
				logMessage( "\n", false ); // Prints 2 empty lines
				if ( replay == null )
					logMessage( "Could not extract game chat from '" + Consts.LAST_REPLAY_FILE_NAME + "'!" );
				else {
					if ( includeReplayHeaderCheckBox.isSelected() ) {
						logMessage( "Replay header information of '" + Consts.LAST_REPLAY_FILE_NAME + "':" );
						final StringWriter headerInfoWriter = new StringWriter();
						replay.replayHeader.printHeaderInformation( new PrintWriter( headerInfoWriter ) );
						logMessage( headerInfoWriter.toString(), false );
						logMessage( "", false ); // Prints an empty line
					}
					logMessage( "Game chat of '" + Consts.LAST_REPLAY_FILE_NAME + "':" );
					logMessage( replay.gameChat, false );
					logMessage( "End of game chat of '" + Consts.LAST_REPLAY_FILE_NAME + "'." );
				}
				progressBar.setValue( 1 );
			}
		} );
		contentBox.add( Utils.wrapInPanel( displayFromLastReplayButton ) );
		
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ) );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( ManualScanTab.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				fileChooser.setMultiSelectionEnabled( true );
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					extractGameChatFromFiles( fileChooser.getSelectedFiles() );
			}
		} );
		contentBox.add( Utils.wrapInPanel( selectFilesButton ) );
		
		contentBox.add( Utils.wrapInPanel( includeReplayHeaderCheckBox ) );
		
		super.buildGUI();
	}
	
	/**
	 * Extracts game chat from the specified files and writes them to text files.
	 * @param replayFiles replay files to extract game chat from
	 */
	private void extractGameChatFromFiles( final File[] replayFiles ) {
		selectFilesButton.setEnabled( false );
		displayFromLastReplayButton.setEnabled( false );
		new NormalThread() {
			@Override
			public void run() {
				try {
					progressBar.setValue( 0 );
					progressBar.setMaximum( replayFiles.length );
					logMessage( "\n", false ); // Prints 2 empty lines
					final String extractingMessage = "Extracting game chat from " + replayFiles.length + " replay" + ( replayFiles.length == 1 ? "" : "s" );
					logMessage( extractingMessage + "..." );
					logMessage( "Game chats will be written to text files named after the replays." );
					final long startTimeNanons = System.nanoTime();
					
					int counter = 0;
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
								if ( includeReplayHeaderCheckBox.isSelected() ) {
									output.println( "Replay header information of '" + absoluteReplayPath + "':" );
									replay.replayHeader.printHeaderInformation( output );
									output.println();
								}
								output.println( "Game chat of '" + absoluteReplayPath + "':" );
								output.println( replay.gameChat );
								output.flush();
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
						progressBar.setValue( ++counter );
					}
					
					final long endTimeNanons = System.nanoTime();
					logMessage( extractingMessage + " done in " + Utils.formatNanoTimeAmount( endTimeNanons - startTimeNanons ) );
				}
				finally {
					selectFilesButton.setEnabled( true );
					displayFromLastReplayButton.setEnabled( true );
				}
			}
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_INCLUDE_REPLAY_HEADER, Boolean.toString( includeReplayHeaderCheckBox.isSelected() ) );
	}
	
}
