package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JPanel;

/**
 * AKA finder tab.
 * 
 * @author Andras Belicza
 */
public class AkaFinderTab extends ProgressLoggedTab {
	
	/** Log file name for autoscan. */
	private static final String LOG_FILE_NAME = "aka_finder.log";
	
	/** Button to select folders to analyze. */
	private final JButton selectFoldersButton = new JButton( "Select folders to analyze recursively...", IconResourceManager.ICON_FOLDER_CHOOSER );
	/** Button to select files to analyze.   */
	private final JButton selectFilesButton   = new JButton( "Select files to analyze...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to stop analysing.            */
	private final JButton stopSendingButton   = new JButton( "Stop analyzing", IconResourceManager.ICON_STOP );
	
	/** Variable to store stop requests of analyzing. */
	private volatile boolean requestedToStop;
	
	/**
	 * Creates a new PlayersNetworkTab.
	 */
	public AkaFinderTab() {
		super( "AKA finder", IconResourceManager.ICON_AKA_FINDER, LOG_FILE_NAME );
		
		buildGUI();
	}
	
	@Override
	protected void buildGUI() {
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
					analyzeFilesAndFolders( event.getSource() == selectFoldersButton ? new File[] { fileChooser.getSelectedFile() } : fileChooser.getSelectedFiles() );
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
	 * Analyses files and folders to find AKAs..
	 * @param files files and folders to be analyzed
	 */
	protected void analyzeFilesAndFolders( final File[] files ) {
		if ( !selectFilesButton.isEnabled() )
			return;
		
		requestedToStop = false;
		
		selectFoldersButton.setEnabled( false );
		selectFilesButton  .setEnabled( false );
		stopSendingButton  .setEnabled( true  );
		
		new NormalThread() {
			/** List of replay files to be analyzed. */
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
					
					final String analysingMessage = "Analysing " + replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" );
					logMessage( analysingMessage + "..." );
					
					final long startNanoTime = System.nanoTime();
					
					int counter = 0;	
					int skippedRepsCount = 0;
					for ( final File replayFile : replayFileList ) {
						if ( requestedToStop )
							return;
						
						final String replayFileAbsolutePath = replayFile.getAbsolutePath();
						
						final Replay replay = BinRepParser.parseReplay( replayFile, true, false );
						
						if ( replay == null ) {
							logMessage( "Could not parse " + replayFileAbsolutePath + "!" );
							skippedRepsCount++;
						}
						else {
							
						}
						
						progressBar.setValue( ++counter );
					}
					
					final long endNanoTime = System.nanoTime();
					
					logMessage( analysingMessage + " done in " + Utils.formatNanoTimeAmount( endNanoTime - startNanoTime ) );
					logMessage( "\tSkipped/failed " + skippedRepsCount + " replay" + ( skippedRepsCount == 1 ? "" : "s" ) + ".", false );
				}
				finally {
					if ( requestedToStop )
						logMessage( "Analysing was manually aborted!" );
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
	
	@Override
	public void assignUsedProperties() {
	}
	
}
