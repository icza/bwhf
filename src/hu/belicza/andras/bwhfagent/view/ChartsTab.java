package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.charts.ChartsComponent;
import hu.belicza.andras.bwhfagent.view.charts.ChartsComponent.ChartType;

import java.io.File;

import swingwt.awt.Color;
import swingwt.awt.Dimension;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.event.ChangeEvent;
import swingwtx.swing.event.ChangeListener;

/**
 * Charts tab.
 * 
 * @author Andras Belicza
 */
public class ChartsTab extends Tab {
	
	/** Button to display game chat from the last replay.      */
	private final JButton   openLastReplayButton = new JButton( "Open 'LastReplay.rep'" );
	/** Button to select files to extract game chat.           */
	private final JButton   selectFileButton     = new JButton( "Select file to open...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to open previous replay from replay search tab. */
	private final JButton   previousReplayButton = new JButton( "Previous replay", IconResourceManager.ICON_ARROW_LEFT );
	/** Button to open next replay from replay search tab. */
	private final JButton   nextReplayButton     = new JButton( "Next replay", IconResourceManager.ICON_ARROW_RIGHT );
	/** Label to display the loaded replay.                    */
	private final JLabel    loadedReplayLabel    = new JLabel( "No replay loaded." );
	
	/** Wrapper for the index of the opened replay from the result list of the replay search tab. */
	private final Integer[] openedIndexFromResultListWrapper = new Integer[ 1 ];
	
	/** Combobox to select the chart type.                                   */
	public final JComboBox chartTypeComboBox                  = new JComboBox( ChartsComponent.ChartType.values() );
	/** Checkbox to enable/disable putting all players on one chart.         */
	public final JCheckBox allPlayersOnOneChartCheckBox       = new JCheckBox( "All players on 1 chart", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_ALL_PLAYERS_ON_ONE_CHART ) ) );
	/** Checkbox to enable/disable using players' in-game colors for charts. */
	public final JCheckBox usePlayersColorsCheckBox           = new JCheckBox( "Use players' in-game colors", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_USE_PLAYERS_IN_GAME_COLORS ) ) );
	/** Checkbox to auto-disable inactive players.                           */
	public final JCheckBox autoDisableInactivePlayersCheckBox = new JCheckBox( "Auto-disable players < 30 APM", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTO_DISABLE_INACTIVE_PLAYERS ) ) );
	/** Checkbox to display actions in seconds.                              */
	public final JCheckBox displayActionsInSecondsCheckBox    = new JCheckBox( "Display actions in seconds", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DISPLAY_ACTIONS_IN_SECONDS ) ) );
	
	/** The component visualizing the charts. */
	private final ChartsComponent chartsComponent = new ChartsComponent( this );
	
	/**
	 * Creates a new ChartsTab.
	 */
	public ChartsTab() {
		super( "Charts", IconResourceManager.ICON_CHARTS );
		
		buildGUI();
		
		chartTypeComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_CHART_TYPE ) ) );
		chartsComponent.setChartType( (ChartType) chartTypeComboBox.getSelectedItem() );
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		contentBox.add( Utils.wrapInPanel( loadedReplayLabel ) );
		
		final Box buttonsPanel = Box.createHorizontalBox();
		JPanel panel = Utils.createWrapperPanel();
		panel.setMaximumSize( new Dimension( Integer.MAX_VALUE, 100 ) );
		openLastReplayButton.setMnemonic( 'l' );
		openLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				setReplayFile( new File( MainFrame.getInstance().generalSettingsTab.starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ) );
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
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					setReplayFile( fileChooser.getSelectedFile() );
			}
		} );
		panel.add( selectFileButton );
		buttonsPanel.add( panel );
		panel = Utils.createWrapperPanel();
		previousReplayButton.setMnemonic( 'p' );
		previousReplayButton.setEnabled( false );
		previousReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				setReplayFile( MainFrame.getInstance().replaySearchTab.getPreviousReplayFile( openedIndexFromResultListWrapper ) );
			}
		} );
		panel.add( previousReplayButton );
		nextReplayButton.setMnemonic( 'n' );
		nextReplayButton.setEnabled( false );
		nextReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				setReplayFile( MainFrame.getInstance().replaySearchTab.getNextReplayFile( openedIndexFromResultListWrapper ) );
			}
		} );
		panel.add( nextReplayButton );
		buttonsPanel.add( panel );
		// We wrap it in another panel so it gets some border space;
		// This is needed because SwingWT resizes components wrongly when window is maximized and de-maximized
		// The extra space still lets the content be seen.
		contentBox.add( buttonsPanel );
		
		final JPanel chartsCommonControlPanel = Utils.createWrapperPanel();
		chartsCommonControlPanel.setBorder( BorderFactory.createTitledBorder( "General chart settings:" ) );
		chartsCommonControlPanel.add( new JLabel( "Chart type:" ) );
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
		contentBox.add( chartsCommonControlPanel );
		
		contentBox.add( chartsComponent.getContentPanel() );
	}
	
	@Override
	public void initializationEnded() {
		super.initializationEnded();
		chartsComponent.initializationEnded();
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
		
		final Replay replay = BinRepParser.parseReplay( file, true, false );
		
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
		previousReplayButton.setEnabled( hasResultReplay );
		nextReplayButton    .setEnabled( hasResultReplay );
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CHART_TYPE                   , Integer.toString( chartTypeComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ALL_PLAYERS_ON_ONE_CHART     , Boolean.toString( allPlayersOnOneChartCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_USE_PLAYERS_IN_GAME_COLORS   , Boolean.toString( usePlayersColorsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTO_DISABLE_INACTIVE_PLAYERS, Boolean.toString( autoDisableInactivePlayersCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DISPLAY_ACTIONS_IN_SECONDS   , Boolean.toString( displayActionsInSecondsCheckBox.isSelected() ) );
		
		chartsComponent.assignUsedProperties();
	}
	
}
