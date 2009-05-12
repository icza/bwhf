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
import swingwtx.swing.JPanel;


/**
 * Game chat tab.
 * 
 * @author Andras Belicza
 */
public class GameChatTab extends ProgressLoggedTab {
	
	/** Log file name for game chat. */
	private static final String LOG_FILE_NAME = "game_chat.log";
	
	/** Button to display game chat from the last replay.  */
	protected final JButton   displayFromLastReplayButton    = new JButton( "Display game chat from 'LastReplay.rep'", IconResourceManager.ICON_LASTREPLAY );
	/** Button to select a file to display game chat from. */
	private   final JButton   selectFileToDisplayFromButton  = new JButton( "Select file to display game chat from...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to select files to extract game chat from.  */
	private   final JButton   selectFilesToExtractFromButton = new JButton( "Select files to extract game chat from...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Checkbox to tell whether to include replay header. */
	private   final JCheckBox includeReplayHeaderCheckBox    = new JCheckBox( "Also include replay header information", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_INCLUDE_REPLAY_HEADER ) ) );
	
	/**
	 * Creates a new PcxConverterTab.
	 */
	public GameChatTab() {
		super( "Game chat", IconResourceManager.ICON_GAME_CHATS, LOG_FILE_NAME );
		
		buildGUI();
	}
	
	@Override
	protected void buildGUI() {
		final JPanel panel = Utils.createWrapperPanel();
		displayFromLastReplayButton.setMnemonic( 'l' );
		displayFromLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				showGameChatFromReplay( new File( MainFrame.getInstance().generalSettingsTab.starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ) );
			}
		} );
		panel.add( displayFromLastReplayButton );
		selectFileToDisplayFromButton.setMnemonic( 'd' );
		selectFileToDisplayFromButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( MainFrame.getInstance().generalSettingsTab.getReplayStartFolder() );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( Utils.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				fileChooser.setMultiSelectionEnabled( false );
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					showGameChatFromReplay( fileChooser.getSelectedFile() );
			}
		} );
		panel.add( selectFileToDisplayFromButton );
		contentBox.add( Utils.wrapInPanel( panel ) );
		
		selectFilesToExtractFromButton.setMnemonic( 'f' );
		selectFilesToExtractFromButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( MainFrame.getInstance().generalSettingsTab.getReplayStartFolder() );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( Utils.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				fileChooser.setMultiSelectionEnabled( true );
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					extractGameChatFromFiles( fileChooser.getSelectedFiles() );
			}
		} );
		contentBox.add( Utils.wrapInPanel( selectFilesToExtractFromButton ) );
		
		contentBox.add( Utils.wrapInPanel( includeReplayHeaderCheckBox ) );
		
		super.buildGUI();
	}
	
	/**
	 * Shows the game chat from a replay file.
	 * @param replayFile replay file to be show game chat from
	 */
	public void showGameChatFromReplay( final File replayFile ) {
		if ( !selectFileToDisplayFromButton.isEnabled() )
			return;
		
		selectFilesToExtractFromButton.setEnabled( false );
		selectFileToDisplayFromButton.setEnabled( false );
		displayFromLastReplayButton.setEnabled( false );
		try {
			progressBar.setValue( 0 );
			progressBar.setMaximum( 1 );
			final Replay replay = BinRepParser.parseReplay( replayFile, true, true );
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
		finally {
			selectFilesToExtractFromButton.setEnabled( true );
			selectFileToDisplayFromButton.setEnabled( true );
			displayFromLastReplayButton.setEnabled( true );
		}
	}
	
	/**
	 * Extracts game chat from the specified files and writes them to text files.
	 * @param replayFiles replay files to extract game chat from
	 */
	public void extractGameChatFromFiles( final File[] replayFiles ) {
		if ( !selectFilesToExtractFromButton.isEnabled() )
			return;
		
		selectFilesToExtractFromButton.setEnabled( false );
		selectFileToDisplayFromButton.setEnabled( false );
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
						final Replay replay = BinRepParser.parseReplay( replayFile, true, true );
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
					selectFilesToExtractFromButton.setEnabled( true );
					selectFileToDisplayFromButton.setEnabled( true );
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
