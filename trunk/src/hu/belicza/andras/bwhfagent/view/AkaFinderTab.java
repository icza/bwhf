package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private final JComboBox  authoritativenessThresholdComboBox;
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
		USELESS  ( "Useless"   ),
		VERY_POOR( "Very poor" ),
		POOR     ( "Poor"      ),
		AVERAGE  ( "Average"   ),
		GOOD     ( "Good"      ),
		VERY_GOOD( "Very good" ),
		EXCELLENT( "Excellent" );
		
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
		final long   replayDate;
		final String playerName;
		final byte   race;
		final int    frames;
		final int    realApm;
		
		public PlayerAnalysis( final File replay, final long replayDate, final String playerName, final byte race, final int frames, final int realApm ) {
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
		final AuthoritativenessExtent authoritativenessExtent;
		final float                   matchingProbability;
		final boolean                 tresholdReached;
		
		public Comparision( final PlayerAnalysis analysis1, final PlayerAnalysis analysis2, final AuthoritativenessExtent authoritativenessExtentTreshold, final float matchingProbabilityTreshold ) {
			this.analysis1 = analysis1;
			this.analysis2 = analysis2;
			authoritativenessExtent = determineAuthoritativenessExtent();
			// If authoritativeness extent is below the treshold, don't bother calculating the matching probability
			if ( authoritativenessExtent.compareTo( authoritativenessExtentTreshold ) >= 0  ) {
				matchingProbability = calculateMatchingProbability();
				tresholdReached = matchingProbability >= matchingProbabilityTreshold;
			}
			else {
				matchingProbability = 0.0f;
				tresholdReached     = false;
			}
		}
		
		@Override
		public String toString() {
			return String.format( "%-9s %3.1f%% - %s, %s  (%s, %s)", authoritativenessExtent.toString(), matchingProbability, analysis1.playerName, analysis2.playerName, analysis1.replay.getAbsolutePath(), analysis2.replay.getAbsolutePath() );
		}
		
		public int compareTo( final Comparision comparision ) {
			if ( authoritativenessExtent == comparision.authoritativenessExtent )
				return -Float.compare( matchingProbability, comparision.matchingProbability );
			return authoritativenessExtent.ordinal() < comparision.authoritativenessExtent.ordinal() ? 1 : -1;
		}
		
		private AuthoritativenessExtent determineAuthoritativenessExtent() {
			float authroitativenessExtent = 1.0f;
			
			// Comparing different races are a huge setback (the mathing algorithm uses action distribution pairing which changes with races)
			if ( analysis1.race != analysis2.race )
				authroitativenessExtent *= 0.5f;
			
			// Difference in APM is an obvious negative factor
			if ( analysis1.realApm < 10 || analysis2.realApm < 10 )
				authroitativenessExtent = 0.0f;
			else
				authroitativenessExtent *= analysis1.realApm < analysis2.realApm ? (float) analysis1.realApm / analysis2.realApm : (float) analysis2.realApm / analysis1.realApm;
			
			// If games differ in duration, the action distribution might change drastically (gather and micro focused on early, macro later on)
			authroitativenessExtent *= analysis1.frames < analysis2.frames ? (float) analysis1.frames / analysis2.frames : (float) analysis2.frames / analysis1.frames;
			
			// Playing style changes over time. Replays from different ages are not very authoritative.
			final int days  = (int) ( Math.abs( analysis1.replayDate - analysis2.replayDate ) / (1000l*60l*60l*24l) );
			authroitativenessExtent *= days < 2000 ? ( 2000.0f - days ) / 2000.0f : 0.0f;
			
			// Safety checking (might be out of range due to rounding problems or if it remains 1.0):
			final int extentOrdinal = (int) ( authroitativenessExtent * AuthoritativenessExtent.values().length );
			if ( extentOrdinal > AuthoritativenessExtent.values().length - 1 )
				return AuthoritativenessExtent.EXCELLENT;
			if ( extentOrdinal < 0 )
				return AuthoritativenessExtent.USELESS;
			return AuthoritativenessExtent.values()[ extentOrdinal ];
		}
		
		private float calculateMatchingProbability() {
			return (float) Math.random() * 100.0f;
		}
	}
	
	/**
	 * Creates a new PlayersNetworkTab.
	 */
	public AkaFinderTab() {
		super( "AKA finder", IconResourceManager.ICON_AKA_FINDER, LOG_FILE_NAME );
		
		final List< AuthoritativenessExtent > authoritativenessExtentList = Arrays.asList( AuthoritativenessExtent.values() );
		Collections.reverse( authoritativenessExtentList );
		authoritativenessThresholdComboBox = new JComboBox( authoritativenessExtentList.toArray( new AuthoritativenessExtent[ authoritativenessExtentList.size() ] ) );
		
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
								
								final int frames  = playerActions.actions.length > 0 ? playerActions.actions[ playerActions.actions.length - 1 ].iteration : 0;
								final int seconds = ReplayHeader.convertFramesToSeconds( frames );
								final PlayerAnalysis playerAnalysis = new PlayerAnalysis( replayFile, replayHeader.saveTime.getTime(), playerActions.playerName, replayHeader.playerRaces[ playerIndex ],
										frames, frames == 0 ? 0 : seconds == 0 ? 0 : playerActions.actions.length * 60 / seconds );
								replayPlayerAnalysisList.add( playerAnalysis );
								
								for ( final PlayerAnalysis playerAnalysis2 : playerAnalysisList )
									if ( compareSameNames || !playerAnalysis2.playerName.equals( playerAnalysis.playerName ) ) {
										final Comparision comparision = new Comparision( playerAnalysis2, playerAnalysis, authoritativenessThreshold, matchingProbabilityThreshold );
										if ( comparision.tresholdReached )
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
