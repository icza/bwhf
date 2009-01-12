package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.control.HackDescription;
import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JPanel;
import swingwtx.swing.filechooser.FileFilter;


/**
 * Manual scan tab.
 * 
 * @author Andras Belicza
 */
public class ManualScanTab extends LoggedTab {
	
	/** Log file name for autoscan.     */
	private static final String LOG_FILE_NAME         = "manual_scan.log";
	/** Replay file extension.          */
	private static final String REPLAY_FILE_EXTENSION = ".rep";
	/** Appendable hacker reps postfix. */
	private static final String HACKER_REPS_POSTFIX   = " - hack";
	
	/** Replay file filter. */
	private static final FileFilter SWING_REPLAY_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File file ) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith( REPLAY_FILE_EXTENSION );
		}
		@Override
		public String getDescription() {
			return "Replay files (*.rep)";
		}
	};
	
	/** Button to scan the last replay.   */
	private final JButton   scanLastReplayButton   = new JButton( "Scan 'LastReplay.rep'" );
	/** Button to select folders to scan. */
	private final JButton   selectFoldersButton    = new JButton( "Select folders to scan recursively" );
	/** Button to select files to scan.   */
	private final JButton   selectFilesButton      = new JButton( "Select files to scan" );
	/** Button to stop the current scan.  */
	private final JButton   stopScanButton         = new JButton( "Stop current scan" );
	/** Flag hacker reps checkbox.        */
	private final JCheckBox flagHackerRepsCheckBox = new JCheckBox( "Flag hacker replays by appending '" + HACKER_REPS_POSTFIX + "' to the end of their names", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_FLAG_HACKER_REPS ) ) );
	
	/** Variable to store stop requests of scan.    */
	private volatile boolean requestedToStop;
	
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
				scanFilesAndFolders( new File[] { new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ) }, true );
			}
		} );
		contentBox.add( Utils.wrapInPanel( scanLastReplayButton ) );
		
		final ActionListener selectFilesAndFoldersActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ) );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( event.getSource() == selectFoldersButton ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				// SwingWT does not support selecting multiple directories yet, getSelectedFiles() returns null so I have to call getSelectedFile() in case of folders.
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					scanFilesAndFolders( event.getSource() == selectFoldersButton ? new File[] { fileChooser.getSelectedFile() } : fileChooser.getSelectedFiles(), false );
			}
		};
		
		final JPanel selectButtonsPanel = new JPanel();
		selectFoldersButton.setMnemonic( 'd' );
		selectFoldersButton.addActionListener( selectFilesAndFoldersActionListener );
		selectButtonsPanel.add( selectFoldersButton );
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( selectFilesAndFoldersActionListener );
		selectButtonsPanel.add( selectFilesButton );
		contentBox.add( selectButtonsPanel );
		
		stopScanButton.setEnabled( false );
		stopScanButton.setMnemonic( 't' );
		stopScanButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				requestedToStop = true; // Access to a volatile variable is automatically synchronized from Java 5.0
				stopScanButton.setEnabled( false );
			}
		} );
		contentBox.add( Utils.wrapInPanel( stopScanButton ) );
		
		contentBox.add( Utils.wrapInPanel( flagHackerRepsCheckBox ) );
		
		super.buildGUI();
	}
	
	/**
	 * Helper class to wrap a modifiable integer into an object.
	 * @author Andras Belicza
	 */
	private static class IntWrapper {
		public int value;
	}
	
	/**
	 * Scans the specified files and folders.
	 * @param files        files and folders to be scanned
	 * @param isLastReplay tells whether the lastreplay scan button was activated
	 */
	private void scanFilesAndFolders( final File[] files, final boolean isLastReplay ) {
		requestedToStop = false;
		scanLastReplayButton.setEnabled( false );
		selectFoldersButton .setEnabled( false );
		selectFilesButton   .setEnabled( false );
		stopScanButton      .setEnabled( true  );
		
		new NormalThread() {
			/** List of replay files to be scanned. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				try {
					final boolean skipLatterActionsOfHackers = MainFrame.getInstance().generalSettingsTab.skipLatterActionsOfHackersCheckBox.isSelected();
					
					logMessage( "\n", false ); // Prints 2 empty lines
					if ( !isLastReplay )
						logMessage( "Counting replays..." );
					
					chooseReplayFiles( files );
					
					if ( requestedToStop )
						return;
					
					final long startTimeNanons = System.nanoTime();
					
					final String scanningMessage = "Scanning " 
						+ ( isLastReplay ? new File( Consts.LAST_REPLAY_FILE_NAME ).getName() : replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" ) );
					logMessage( scanningMessage + "..." );
					
					int hackerRepsCount  = 0;
					int skippedRepsCount = 0;
					
					final Map< String, IntWrapper > playerHackerRepsCountMap = new HashMap< String, IntWrapper >(); // We count the replays for every hacker in which he was caught
					
					for ( final File replayFile : replayFileList ) {
						if ( requestedToStop )
							return;
						
						List< HackDescription > hackDescriptionList = null; 
						final Replay replay = BinRepParser.parseReplay( replayFile );
						if ( replay != null )
							hackDescriptionList = ReplayScanner.scanReplayForHacks( replay.replayActions, skipLatterActionsOfHackers );
						
						if ( hackDescriptionList == null ) {
							skippedRepsCount++;
							logMessage( "Could not scan " + replayFile.getAbsolutePath() + "!" );
						}
						else
							if ( !hackDescriptionList.isEmpty() ) {
								hackerRepsCount++;
								logMessage( "Found " + hackDescriptionList.size() + " hack" + (hackDescriptionList.size() == 1 ? "" : "s" ) + " in " + replayFile.getAbsolutePath() + ":" );
								final Set< String > hackersOfReplaySet = new HashSet< String >();
								for ( final HackDescription hackDescription : hackDescriptionList ) {
									logMessage( "\t" + hackDescription.description, false );
									
									final String lowerCasedHackerName = hackDescription.playerName.toLowerCase();
									if ( !hackersOfReplaySet.contains( lowerCasedHackerName ) ) {
										// Only count a player once per replay in the overall statistics
										hackersOfReplaySet.add( lowerCasedHackerName );
										IntWrapper playerHackerRepsCount = playerHackerRepsCountMap.get( lowerCasedHackerName );
										if ( playerHackerRepsCount == null )
											playerHackerRepsCountMap.put( lowerCasedHackerName, playerHackerRepsCount = new IntWrapper() );
										playerHackerRepsCount.value++;
									}
								}
								if ( flagHackerRepsCheckBox.isSelected() && !isLastReplay ) {
									final String replayName = replayFile.getName().substring( 0, replayFile.getName().length() - REPLAY_FILE_EXTENSION.length() );
									if ( !replayName.endsWith( HACKER_REPS_POSTFIX ) )
										replayFile.renameTo( new File( replayFile.getParent(), replayName + HACKER_REPS_POSTFIX + REPLAY_FILE_EXTENSION ) );
								}
							}
							else
								logMessage( "Found no hacks in " + replayFile.getAbsolutePath() + "." );
					}
					
					final long endTimeNanons = System.nanoTime();
					logMessage( scanningMessage + " done in " + Utils.formatNanoTimeAmount( endTimeNanons - startTimeNanons ) );
					logMessage( "\tFound " + hackerRepsCount + " hacker replay" + ( hackerRepsCount == 1 ? "" : "s" ) + ".", false );
					logMessage( "\tSkipped " + skippedRepsCount + " replay" + ( skippedRepsCount == 1 ? "" : "s" ) + ".", false );
					if ( !playerHackerRepsCountMap.isEmpty() ) {
						final StringBuilder hackersBuilder = new StringBuilder( "\tThe following player" + ( playerHackerRepsCountMap.size() == 1 ? " was" : "s were" ) + " found hacking: " );
						boolean firstHacker = true;
						// First me make a TreeMap so we will list hackers sorted by their name
						for ( final Entry< String, IntWrapper > playerHackerRepsCount : new TreeMap< String, IntWrapper >( playerHackerRepsCountMap ).entrySet() ) {
							if ( firstHacker )
								firstHacker = false;
							else
								hackersBuilder.append( ", " );
							hackersBuilder.append( playerHackerRepsCount.getKey() );
							if ( playerHackerRepsCount.getValue().value > 1 )
								hackersBuilder.append( " (x" ).append( playerHackerRepsCount.getValue().value ).append( ')' );
						}
						logMessage( hackersBuilder.toString(), false );
					}
				}
				finally {
					if ( requestedToStop )
						logMessage( "Scan was manually aborted!" );
					stopScanButton      .setEnabled( false );
					selectFilesButton   .setEnabled( true  );
					selectFoldersButton .setEnabled( true  );
					scanLastReplayButton.setEnabled( true  );
				}
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
		Utils.settingsProperties.setProperty( Consts.PROPERTY_FLAG_HACKER_REPS, Boolean.toString( flagHackerRepsCheckBox.isSelected() ) );
	}
	
}
