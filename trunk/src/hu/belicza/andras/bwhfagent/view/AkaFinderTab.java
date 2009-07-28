package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import swingwt.awt.GridLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;

/**
 * AKA finder tab.
 * 
 * @author Andras Belicza
 */
public class AkaFinderTab extends ProgressLoggedTab {
	
	/** Log file name for autoscan. */
	private static final String LOG_FILE_NAME = "aka_finder.log";
	
	/** Flag hacker reps checkbox.                       */
	private final JCheckBox dontCompareSameNamesCheckBox = new JCheckBox( "Don't compare players with same names", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DONT_COMPARE_SAME_NAMES ) ) );
	/** Authoritativeness threshold combobox.            */
	private final JComboBox  authoritativenessThresholdComboBox   = new JComboBox( AuthoritativenessExtent.values() );
	/** Matching probability treshold combobox.          */
	private final JComboBox  matchingProbabilityThresholdComboBox = new JComboBox( new Object[] { "100%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%", "0%" } );
	
	/** Button to select folders to analyze. */
	private final JButton selectFoldersButton = new JButton( "Select folders to analyze recursively...", IconResourceManager.ICON_FOLDER_CHOOSER );
	/** Button to select files to analyze.   */
	private final JButton selectFilesButton   = new JButton( "Select files to analyze...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to stop analysing.            */
	private final JButton stopSendingButton   = new JButton( "Stop analyzing", IconResourceManager.ICON_STOP );
	
	/** Variable to store stop requests of analyzing. */
	private volatile boolean requestedToStop;
	
	
	/**
	 * The extents of authoritativeness.
	 * @author Andras Belicza
	 */
	private static enum AuthoritativenessExtent {
		EXCELLENT( "Excellent" ),
		VERY_GOOD( "Very good" ),
		GOOD     ( "Good"      ),
		AVERAGE  ( "Average"   ),
		POOR     ( "Poor"      ),
		VERY_POOR( "Very poor" ),
		USELESS  ( "Useless"   );
		
		private final String displayName; 
		private AuthoritativenessExtent( final String displayName ) {
			this.displayName = displayName;
		}
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	/**
	 * Analysis of a player.
	 * @author Andras Belicza
	 */
	private static class PlayerAnalysis {
		final File   replay;
		final Date   replayDate;
		final String playerName;
		final byte   race;
		final int    frames;
		final int    realApm;
		public PlayerAnalysis( final File replay, final Date replayDate, final String playerName, final byte race, final int frames, final int realApm ) {
			this.replay     = replay;
			this.replayDate = replayDate;
			this.playerName = playerName;
			this.race       = race;
			this.frames     = frames;
			this.realApm    = realApm;
		}
	}
	
	/**
	 * Comparision result of analysis of 2 players.
	 * @author Andras Belicza
	 */
	private static class Comparision implements Comparable< Comparision > {
		final PlayerAnalysis          analysis1;
		final PlayerAnalysis          analysis2;
		final float                   matchingProbability;
		final AuthoritativenessExtent authoritativenessExtent;
		public Comparision( final PlayerAnalysis analysis1, final PlayerAnalysis analysis2 ) {
			this.analysis1 = analysis1;
			this.analysis2 = analysis2;
			this.matchingProbability = (float) Math.random() * 100.0f;
			this.authoritativenessExtent = AuthoritativenessExtent.values()[ (int) ( Math.random() * AuthoritativenessExtent.values().length ) ];
		}
		@Override
		public String toString() {
			return String.format( "%-9s %3.1f%% - %s, %s  (%s, %s)", authoritativenessExtent.toString(), matchingProbability, analysis1.playerName, analysis2.playerName, analysis1.replay.getAbsolutePath(), analysis2.replay.getAbsolutePath() );
		}
		public int compareTo( final Comparision comparision ) {
			if ( authoritativenessExtent == comparision.authoritativenessExtent )
				return -Float.compare( matchingProbability, comparision.matchingProbability );
			return authoritativenessExtent.ordinal() < comparision.authoritativenessExtent.ordinal() ? -1 : 1;
		}
	}
	
	/**
	 * Creates a new PlayersNetworkTab.
	 */
	public AkaFinderTab() {
		super( "AKA finder", IconResourceManager.ICON_AKA_FINDER, LOG_FILE_NAME );
		
		authoritativenessThresholdComboBox  .setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTHORITATIVENESS_TRESHOLD ) ) );
		matchingProbabilityThresholdComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_MATCHING_PROBABILITY_TRESHOLD ) ) );
		
		buildGUI();
	}
	
	@Override
	protected void buildGUI() {
		contentBox.add( Utils.wrapInPanel( dontCompareSameNamesCheckBox ) );
		
		final JPanel tresholdPanel = new JPanel( new GridLayout( 2, 2, 7, 0 ) );
		tresholdPanel.setBorder( BorderFactory.createTitledBorder( "Display tresholds:" ) );
		tresholdPanel.add( new JLabel( "Extent of authoritativeness:" ) );
		tresholdPanel.add( authoritativenessThresholdComboBox );
		tresholdPanel.add( new JLabel( "Matching probability:" ) );
		tresholdPanel.add( matchingProbabilityThresholdComboBox );
		contentBox.add( Utils.wrapInPanel( tresholdPanel ) );
		
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
					final boolean                 compareSameNames             = !dontCompareSameNamesCheckBox.isSelected();
					final float                   matchingProbabilityThreshold = Float.parseFloat( ( (String) matchingProbabilityThresholdComboBox.getSelectedItem() ).substring( 0, ( (String) matchingProbabilityThresholdComboBox.getSelectedItem() ).length() - 1 ) );
					final AuthoritativenessExtent authoritativenessThreshold   = (AuthoritativenessExtent) authoritativenessThresholdComboBox.getSelectedItem();
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
					final List< PlayerAnalysis > playerAnalysisList = new ArrayList< PlayerAnalysis >();
					final List< Comparision    > comparisionList    = new ArrayList< Comparision    >();
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
							// Players in one replay must and should not be compared, we add analysis of players of this replay after the replay has been processed.
							final List< PlayerAnalysis > replayPlayerAnalysisList = new ArrayList< PlayerAnalysis >( replay.replayActions.players.length );
							
							for ( final PlayerActions playerActions : replay.replayActions.players ) {
								final ReplayHeader replayHeader = replay.replayHeader;
								
								final int playerIndex = replayHeader.getPlayerIndexByName( playerActions.playerName );
								
								final int frames = playerActions.actions.length > 0 ? playerActions.actions[ playerActions.actions.length - 1 ].iteration : 0;
								final PlayerAnalysis playerAnalysis = new PlayerAnalysis( replayFile, replayHeader.saveTime, playerActions.playerName, replayHeader.playerRaces[ playerIndex ],
										frames, frames == 0 ? 0 : playerActions.actions.length * 60 / ReplayHeader.convertFramesToSeconds( frames ) );
								replayPlayerAnalysisList.add( playerAnalysis );
								
								for ( final PlayerAnalysis playerAnalysis2 : playerAnalysisList )
									if ( compareSameNames || !playerAnalysis2.playerName.equals( playerAnalysis.playerName ) ) {
										final Comparision comparision = new Comparision( playerAnalysis2, playerAnalysis );
										if ( comparision.matchingProbability >= matchingProbabilityThreshold && comparision.authoritativenessExtent.compareTo( authoritativenessThreshold ) <= 0 )
											comparisionList.add( comparision );
									}
							}
							
							playerAnalysisList.addAll( replayPlayerAnalysisList );
						}
						
						progressBar.setValue( ++counter );
					}
					
					Collections.sort( comparisionList );
					logMessage( "Comparision results:" );
					for ( final Comparision comparision : comparisionList )
						logMessage( "\t" + comparision.toString(), false );
					
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
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DONT_COMPARE_SAME_NAMES      , Boolean.toString( dontCompareSameNamesCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTHORITATIVENESS_TRESHOLD   , Integer.toString( authoritativenessThresholdComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_MATCHING_PROBABILITY_TRESHOLD, Integer.toString( matchingProbabilityThresholdComboBox.getSelectedIndex() ) );
	}
	
}
