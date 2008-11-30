package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


/**
 * Manual scan tab.
 * 
 * @author Andras Belicza
 */
public class ManualScanTab extends LoggedTab {
	
	/** Log file name for autoscan. */
	private static final String LOG_FILE_NAME = "manual_scan.log";
	
	/** Replay file filter. */
	private static final FileFilter SWING_REPLAY_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File file ) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith( ".rep" );
		}
		@Override
		public String getDescription() {
			return "Replay files (*.rep)";
		}
	};
	
	/** Button to scan the last replay.             */
	final JButton scanLastReplayButton        = new JButton( "Scan 'LastReplay.rep'" );
	/** Butotn to select files and folders to scan. */
	final JButton selectFilesAndFoldersButton = new JButton( "Select files and folders to scan recursively" );
	
	/**
	 * Creates a new ManualScanTab.
	 */
	public ManualScanTab() {
		super( "Manual scan", LOG_FILE_NAME );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		scanLastReplayButton.setMnemonic( 'L' );
		scanLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				scanFilesAndFolders( new File[] { new File( Utils.settingsProperties.getProperty( Consts.PROPERTY_STARCRAFT_FOLDER ), Consts.LAST_REPLAY_FILE_NAME ) } );
			}
		} );
		contentBox.add( Utils.wrapInPanel( scanLastReplayButton ) );
		
		selectFilesAndFoldersButton.setMnemonic( 'f' );
		selectFilesAndFoldersButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( Utils.settingsProperties.getProperty( Consts.PROPERTY_STARCRAFT_FOLDER ), Consts.STARCRAFT_REPLAY_FOLDER ) );
				
				fileChooser.addChoosableFileFilter( SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				if ( fileChooser.showOpenDialog( getScrollPane() ) == JFileChooser.APPROVE_OPTION )
					scanFilesAndFolders( fileChooser.getSelectedFiles() );
			}
		} );
		contentBox.add( Utils.wrapInPanel( selectFilesAndFoldersButton ) );
		
		super.buildGUI();
	}
	
	/**
	 * Scans the specified files and folders.
	 * @param files files and folders to be scanned
	 */
	private void scanFilesAndFolders( final File[] files ) {
		scanLastReplayButton       .setEnabled( false );
		selectFilesAndFoldersButton.setEnabled( false );
		
		new Thread() {
			/** List of replay files to be scanned. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				logMessage( "Counting replays..." );
				
				chooseReplayFiles( files );
				final long startTimeNanons = System.nanoTime();
				
				final String scanningMessage = "Scanning " + replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" );
				logMessage( scanningMessage + "..." );
				
				int hackerRepsCount  = 0;
				int skippedRepsCount = 0;
				for ( final File replayFile : replayFileList ) {
					final List< String > hackDescriptionList = Utils.scanReplayFile( replayFile );
					if ( hackDescriptionList == null ) {
						skippedRepsCount++;
						logMessage( "Could not scan " + replayFile.getAbsolutePath() + "!" );
					}
					else
						if ( !hackDescriptionList.isEmpty() ) {
							hackerRepsCount++;
							logMessage( "Found " + hackDescriptionList.size() + " hack" + (hackDescriptionList.size() == 1 ? "" : "s" ) + " in " + replayFile.getAbsolutePath() + ":" );
							for ( final String hackDescription : hackDescriptionList )
								logMessage( "\t" + hackDescription, false );
						}
						else
							logMessage( "Found no hacks in " + replayFile.getAbsolutePath() + "." );
				}
				
				final long endTimeNanons = System.nanoTime();
				logMessage( scanningMessage + " done in " + Utils.formatNanoTimeAmount( endTimeNanons - startTimeNanons ) );
				logMessage( "\tFound " + hackerRepsCount + " hacker replay" + ( hackerRepsCount == 1 ? "" : "s" ) + ".", false );
				logMessage( "\tSkipped " + skippedRepsCount + " replay" + ( hackerRepsCount == 1 ? "" : "s" ) + ".", false );
				
				scanLastReplayButton       .setEnabled( true );
				selectFilesAndFoldersButton.setEnabled( true );
			}
			
			private final java.io.FileFilter IO_REPLAY_FILE_FILTER = new java.io.FileFilter() {
				public boolean accept( final File pathname ) {
					return SWING_REPLAY_FILE_FILTER.accept( pathname );
				}
			};
			
			/**
			 * Chooses the replay files in the specified files and folders.
			 * @param files files and folders to be chosen from
			 */
			private void chooseReplayFiles( final File[] files ) {
				for ( final File file : files )
					if ( file.isDirectory() )
						chooseReplayFiles( file.listFiles( IO_REPLAY_FILE_FILTER ) );
					else
						if ( IO_REPLAY_FILE_FILTER.accept( file ) )
							replayFileList.add( file );
			}
			
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
