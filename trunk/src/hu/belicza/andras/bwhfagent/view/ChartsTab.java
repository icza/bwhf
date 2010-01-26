package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.charts.ChartsComponent;
import hu.belicza.andras.bwhfagent.view.charts.ChartsComponent.ChartType;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import swingwt.awt.Color;
import swingwt.awt.Cursor;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.ComponentAdapter;
import swingwt.awt.event.ComponentEvent;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.SwingUtilities;
import swingwtx.swing.event.ChangeEvent;
import swingwtx.swing.event.ChangeListener;

/**
 * Charts tab.
 * 
 * @author Andras Belicza
 */
public class ChartsTab extends Tab {
	
	/** Replay file filter. */
	private static final FileFilter IO_REPLAY_FILE_FILTER = new FileFilter() {
		public boolean accept( final File pathname ) {
			return pathname.isFile() && pathname.getName().toLowerCase().endsWith( ".rep" );
		}
	};
	
	/** A comparator that compares files based on their last modified property. */
	private static final Comparator< File > REPLAY_LAST_MODIFIED_COMPARATOR = new Comparator< File >() {
		public int compare( final File o1, final File o2 ) {
			final long diff = o1.lastModified() - o2.lastModified();
			return diff > 0l ? 1 : diff < 0l ? -1 : 0;
		}
	};
	
	/** Max zoom value. */
	public static final int MAX_ZOOM = 16;
	
	/**
	 * Creates a clone file which is equal to the source based on the comparision provided by this comparator.<br>
	 * The returned file cannot be used as a real File object!<br>
	 * The reason for me to create and use clones is because if the user deletes or moves the replay being loaded,
	 * I want the next call to load prev-next autorep to be the right in the order. If the original file would be stored,
	 * its lastModified() method would returned 0L.
	 * @param sourceFile source file to be cloned
	 * @return a clone file which is equal to the source based on the comparision provided by this comparator
	 */
	private static File createCloneForComparision( final File sourceFile ) {
		final long lastModified = sourceFile.lastModified();
		return new File( "" ) {
			@Override
			public long lastModified() {
				return lastModified;
			}
		};
	}
	
	/** Button to open previous replay from the autoreplay folder. */
	private final JButton   previousAutoReplayButton   = new JButton( "Prev autorep", IconResourceManager.ICON_ARROW_LEFT );
	/** Button to open next replay from the autoreplay folder.     */
	private final JButton   nextAutoReplayButton       = new JButton( "Next autorep", IconResourceManager.ICON_ARROW_RIGHT );
	/** Button to display game chat from the last replay.          */
	private final JButton   openLastReplayButton       = new JButton( "Open 'LastReplay.rep'", IconResourceManager.ICON_LASTREPLAY );
	/** Button to select files to extract game chat.               */
	private final JButton   selectFileButton           = new JButton( "Select file to open...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to open previous replay from replay search tab.     */
	private final JButton   previousSearchReplayButton = new JButton( "Prev listed rep", IconResourceManager.ICON_ARROW_LEFT );
	/** Button to open next replay from replay search tab.         */
	private final JButton   nextSearchReplayButton     = new JButton( "Next listed rep", IconResourceManager.ICON_ARROW_RIGHT );
	/** Label to display the loaded replay.                        */
	private final JLabel    loadedReplayLabel          = new JLabel( "No replay loaded." );
	
	/** Wrapper for the index of the opened replay from the result list of the replay search tab. */
	private final Integer[] openedIndexFromResultListWrapper = new Integer[ 1 ];
	
	/** Reference to the clone of last autoreplay file that was loaded with the prev-next autorep buttons. */
	private File lastAutoreplayFileClone;
	
	/** Combobox to select the chart type.                                   */
	public final JComboBox chartTypeComboBox                  = new JComboBox( ChartsComponent.ChartType.values() );
	/** Checkbox to enable/disable putting all players on one chart.         */
	public final JCheckBox allPlayersOnOneChartCheckBox       = new JCheckBox( "All on 1 chart", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_ALL_PLAYERS_ON_ONE_CHART ) ) );
	/** Checkbox to enable/disable using players' in-game colors for charts. */
	public final JCheckBox usePlayersColorsCheckBox           = new JCheckBox( "Use in-game colors", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_USE_PLAYERS_IN_GAME_COLORS ) ) );
	/** Checkbox to auto-disable inactive players.                           */
	public final JCheckBox autoDisableInactivePlayersCheckBox = new JCheckBox( "Auto-disable < 30 APM", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTO_DISABLE_INACTIVE_PLAYERS ) ) );
	/** Checkbox to display actions in seconds.                              */
	public final JCheckBox displayActionsInSecondsCheckBox    = new JCheckBox( "Display in seconds", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DISPLAY_ACTIONS_IN_SECONDS ) ) );
	/** Combobox to select the chart type.                                   */
	public final JComboBox zoomComboBox                       = new JComboBox( new Integer[] { 1, 2, 4, 8, 16 } );
	
	/** The component visualizing the charts. */
	private final ChartsComponent chartsComponent = new ChartsComponent( this );
	
	private ChangeListener zoomChangeListener; // So we can make the first call when setting the saved, initial value.
	
	/**
	 * Creates a new ChartsTab.
	 */
	public ChartsTab() {
		super( "Charts", IconResourceManager.ICON_CHARTS );
		
		buildGUI();
		
		chartTypeComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_CHART_TYPE ) ) );
		zoomComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_CHART_ZOOM ) ) );
		chartsComponent.setChartType( (ChartType) chartTypeComboBox.getSelectedItem() );
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		contentBox.add( Utils.wrapInPanel( loadedReplayLabel ) );
		
		final Box buttonsPanel = Box.createHorizontalBox();
		// We need to issue a validate() if the state of a maximized window changes (SwingWT bug). 
		buttonsPanel.addComponentListener( new ComponentAdapter() {
			@Override
			public void componentResized( final ComponentEvent ce ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						buttonsPanel.getParent().validate();
					}
				} );
			}
		} );
		buttonsPanel.setMaximumSize( Utils.getMaxDimension() );
		// Previous-next replay from search tab
		JPanel panel = Utils.createWrapperPanel();
		previousAutoReplayButton.setMnemonic( 'r' );
		previousAutoReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final File[] replayFiles = getAutorepFiles();
				
				if ( replayFiles != null ) {
					if ( lastAutoreplayFileClone == null ) {
						lastAutoreplayFileClone = createCloneForComparision( replayFiles[ replayFiles.length - 1 ] );
						setReplayFile( replayFiles[ replayFiles.length - 1 ] );
					}
					else {
						int index = Arrays.binarySearch( replayFiles, lastAutoreplayFileClone, REPLAY_LAST_MODIFIED_COMPARATOR );
						if ( index < 0 )
							index = -( index + 1 );
						if ( index > 0 ) {
							lastAutoreplayFileClone = createCloneForComparision( replayFiles[ index - 1 ] );
							setReplayFile( replayFiles[ index - 1 ] );
						}
						else
							JOptionPane.showMessageDialog( contentBox, "There are no older replays in your autoreplay folder!", "Info", JOptionPane.INFORMATION_MESSAGE );
					}
				}
			}
		} );
		panel.add( previousAutoReplayButton );
		nextAutoReplayButton.setMnemonic( 'e' );
		nextAutoReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final File[] replayFiles = getAutorepFiles();
				
				if ( replayFiles != null ) {
					if ( lastAutoreplayFileClone == null ) {
						lastAutoreplayFileClone = createCloneForComparision( replayFiles[ replayFiles.length - 1 ] );
						setReplayFile( replayFiles[ replayFiles.length - 1 ] );
					}
					else {
						int index = Arrays.binarySearch( replayFiles, lastAutoreplayFileClone, REPLAY_LAST_MODIFIED_COMPARATOR );
						if ( index < 0 )
							index = -( index + 1 ) - 1;
						if ( index < replayFiles.length - 1 ) {
							lastAutoreplayFileClone = createCloneForComparision( replayFiles[ index + 1 ] );
							setReplayFile( replayFiles[ index + 1 ] );
						}
						else
							JOptionPane.showMessageDialog( contentBox, "There are no newer replays in your autoreplay folder!", "Info", JOptionPane.INFORMATION_MESSAGE );
					}
				}
			}
		} );
		panel.add( nextAutoReplayButton );
		buttonsPanel.add( panel );
		// Last replay and user selected replay
		panel = Utils.createWrapperPanel();
		openLastReplayButton.setMnemonic( 'l' );
		openLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final File lastReplayFile = new File( MainFrame.getInstance().generalSettingsTab.starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME );
				lastAutoreplayFileClone = createCloneForComparision( lastReplayFile );
				setReplayFile( lastReplayFile );
			}
		} );
		panel.add( openLastReplayButton );
		selectFileButton.setMnemonic( 'f' );
		selectFileButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( MainFrame.getInstance().generalSettingsTab.getReplayStartFolder() );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( Utils.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION ) {
					lastAutoreplayFileClone = null;
					setReplayFile( fileChooser.getSelectedFile() );
				}
			}
		} );
		panel.add( selectFileButton );
		// Previous-next replay from search tab
		buttonsPanel.add( panel );
		panel = Utils.createWrapperPanel();
		previousSearchReplayButton.setMnemonic( 'p' );
		previousSearchReplayButton.setEnabled( false );
		previousSearchReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				lastAutoreplayFileClone = null;
				setReplayFile( MainFrame.getInstance().replaySearchTab.getPreviousReplayFile( openedIndexFromResultListWrapper ) );
			}
		} );
		panel.add( previousSearchReplayButton );
		nextSearchReplayButton.setMnemonic( 'n' );
		nextSearchReplayButton.setEnabled( false );
		nextSearchReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				lastAutoreplayFileClone = null;
				setReplayFile( MainFrame.getInstance().replaySearchTab.getNextReplayFile( openedIndexFromResultListWrapper ) );
			}
		} );
		panel.add( nextSearchReplayButton );
		buttonsPanel.add( panel );
		
		contentBox.add( buttonsPanel );
		
		final JPanel chartsCommonControlPanel = Utils.createWrapperPanel();
		chartsCommonControlPanel.setBorder( BorderFactory.createTitledBorder( "General chart settings:" ) );
		chartsCommonControlPanel.add( new JLabel( "Chart:" ) );
		chartTypeComboBox.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				chartsComponent.setChartType( (ChartType) chartTypeComboBox.getSelectedItem() );
			}
		} );
		chartsCommonControlPanel.add( chartTypeComboBox );
		allPlayersOnOneChartCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				usePlayersColorsCheckBox.setEnabled( !allPlayersOnOneChartCheckBox.isSelected() );
				chartsComponent.repaint();
			}
		} );
		chartsCommonControlPanel.add( allPlayersOnOneChartCheckBox );
		usePlayersColorsCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				chartsComponent.repaint();
			}
		} );
		usePlayersColorsCheckBox.setEnabled( !allPlayersOnOneChartCheckBox.isSelected() );
		chartsCommonControlPanel.add( usePlayersColorsCheckBox );
		chartsCommonControlPanel.add( autoDisableInactivePlayersCheckBox );
		chartsCommonControlPanel.add( displayActionsInSecondsCheckBox );
		displayActionsInSecondsCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				chartsComponent.loadPlayerActionsIntoList();
			}
		} );
		final JLabel zoomLabel = new JLabel( "Zoom:" );
		chartsCommonControlPanel.add( zoomLabel );
		zoomComboBox.addChangeListener( zoomChangeListener = new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				final int zoom = (Integer) zoomComboBox.getSelectedItem();
				zoomLabel.setBackground( zoom == 1 ? zoomLabel.getParent().getBackground() : Color.GREEN );
				chartsComponent.setZoom( zoom );
				chartsComponent.repaint();
			}
		} );
		chartsCommonControlPanel.add( zoomComboBox );
		contentBox.add( chartsCommonControlPanel );
		
		chartsComponent.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		contentBox.add( chartsComponent.getContentPanel() );
	}
	
	@Override
	public void initializationEnded() {
		super.initializationEnded();
		zoomChangeListener.stateChanged( null );
		chartsComponent.initializationEnded();
	}
	
	/**
	 * Returns the replay file in the autorep folder.<br>
	 * The autorep folder is taken from the autoscan tab. The returned files are sorted by the last modified property.
	 * @return the replay file in the autorep folder; or <code>null</code> if the autorep folder does not exist or is not a folder
	 */
	private File[] getAutorepFiles() {
		final File autoRepsFolder = new File( MainFrame.getInstance().autoscanTab.allRepsDestinationTextField.getText() );
		
		if ( autoRepsFolder.exists() && autoRepsFolder.isDirectory() ) {
			final File[] autoreplayFiles = autoRepsFolder.listFiles( IO_REPLAY_FILE_FILTER );
			
			if ( autoreplayFiles == null || autoreplayFiles.length == 0 ) {
				JOptionPane.showMessageDialog( contentBox, "You do not have replays in your autoreplay folder!", "Error", JOptionPane.ERROR_MESSAGE );
				return null;
			}
			else {
				Arrays.sort( autoreplayFiles, REPLAY_LAST_MODIFIED_COMPARATOR );
				return autoreplayFiles;
			}
		}
		else {
			JOptionPane.showMessageDialog( contentBox, "Autoreplay folder does not exist!", "Error", JOptionPane.ERROR_MESSAGE );
			return null;
		}
	}
	
	/**
	 * Sets the replay file displayed on the charts tab.
	 * @param file replay file to be set
	 */
	public void setReplayFile( final File file ) {
		setReplayFile( file, null );
	}
	
	/**
	 * Sets the replay file displayed on the charts tab.
	 * @param file replay file to be set
	 * @param indexInResultList index of the file in the result list
	 */
	public void setReplayFile( final File file, final Integer indexInResultList ) {
		if ( indexInResultList != null )
			openedIndexFromResultListWrapper[ 0 ] = indexInResultList;
		
		final Replay replay = BinRepParser.parseReplay( file, true, false, true, true );
		
		if ( replay == null ) {
			loadedReplayLabel.setText( "Failed to load " + file.getAbsolutePath() + "!" );
			loadedReplayLabel.setForeground( Color.RED );
		}
		else {
			loadedReplayLabel.setText( "Loaded replay: " + file.getAbsolutePath() );
			loadedReplayLabel.setForeground( Color.BLACK );
		}
		
		chartsComponent.setReplay( replay );
	}
	
	/**
	 * Called when the result list of the replay search tab changes. 
	 * @param hasResultReplay tells if the result list has any replay
	 */
	public void onReplayResultListChange( final boolean hasResultReplay ) {
		if ( !hasResultReplay )
			openedIndexFromResultListWrapper[ 0 ] = null;
		previousSearchReplayButton.setEnabled( hasResultReplay );
		nextSearchReplayButton    .setEnabled( hasResultReplay );
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CHART_TYPE                   , Integer.toString( chartTypeComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ALL_PLAYERS_ON_ONE_CHART     , Boolean.toString( allPlayersOnOneChartCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_USE_PLAYERS_IN_GAME_COLORS   , Boolean.toString( usePlayersColorsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTO_DISABLE_INACTIVE_PLAYERS, Boolean.toString( autoDisableInactivePlayersCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DISPLAY_ACTIONS_IN_SECONDS   , Boolean.toString( displayActionsInSecondsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CHART_ZOOM                   , Integer.toString( zoomComboBox.getSelectedIndex() ) );
		
		chartsComponent.assignUsedProperties();
	}
	
}
