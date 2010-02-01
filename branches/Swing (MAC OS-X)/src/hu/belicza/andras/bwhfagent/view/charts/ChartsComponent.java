package hu.belicza.andras.bwhfagent.view.charts;

import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.MapData;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhf.model.Action.Size;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.ChartsTab;
import hu.belicza.andras.bwhfagent.view.IconResourceManager;
import hu.belicza.andras.bwhfagent.view.MainFrame;
import hu.belicza.andras.bwhfagent.view.Utils;
import hu.belicza.andras.bwhfagent.view.PlayerCheckerTab.ListedAs;
import hu.belicza.andras.hackerdb.ServerApiConsts;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Component to visualize charts.
 * 
 * @author Andras Belicza
 */
public class ChartsComponent extends JPanel {
	
	/** Value of in-game colors. */
	private static final Color[] IN_GAME_COLORS = new Color[] {
		new Color( 244,   4, 4 ), new Color(  12,  72, 204 ), new Color(  44, 180, 148 ), new Color( 136,  64, 156 ), new Color( 248, 140,  20 ), new Color( 112,  48,  20 ), new Color( 204, 224, 208 ), new Color( 252, 252,  56 ),
		new Color(   8, 128, 8 ), new Color( 252, 252, 124 ), new Color( 236, 196, 176 ), new Color(  64, 104, 212 ), new Color( 116, 164, 124 ), new Color( 144, 144, 184 ), new Color( 252, 252, 124 ), new Color(   0, 228, 252 )
	};
	
	/** Background color for charts.                             */
	private static final Color  CHART_BACKGROUND_COLOR         = Color.BLACK;
	/** Color to use for axis lines and axis titles.             */
	private static final Color  CHART_AXIS_COLOR               = Color.YELLOW;
	/** Default color to use for the chart curve.                */
	private static final Color  CHART_DEFAULT_COLOR            = Color.WHITE;
	/** Color to use for axis and info texts.                    */
	private static final Color  CHART_ASSIST_LINES_COLOR       = new Color( 60, 60, 60 );
	/** Color to use for axis labels.                            */
	private static final Color  CHART_AXIS_LABEL_COLOR         = Color.CYAN;
	/** Color to use for player descriptions.                    */
	private static final Color  CHART_PLAYER_DESCRIPTION_COLOR = Color.GREEN;
	/** Color to use for indicating hacks.                       */
	private static final Color  CHART_HACK_COLOR               = Color.RED;
	/** 2nd color to use for indicating hacks.                   */
	private static final Color  CHART_HACK_COLOR2              = Color.YELLOW;
	/** 2nd color to use for indicating hacks.                   */
	private static final Color  CHART_MARKER_COLOR             = new Color( 150, 150, 255 );
	/** Font to use to draw descriptions and titles.             */
	private static final Font   CHART_MAIN_FONT                = new Font( "Times New Roman", Font.BOLD, 13 );
	/** Font to use to draw axis labels.                         */
	private static final Font   CHART_AXIS_LABEL_FONT          = new Font( "Courier New", Font.PLAIN, 11 );
	/** Font to use to draw hack markers.                        */
	private static final Font   HACK_MARKER_FONT               = new Font( "Courier New", Font.BOLD, 16 );
	/** Font to use to draw texts as part of charts.             */
	private static final Font   CHART_PART_TEXT_FONT           = new Font( "Courier New", Font.PLAIN, 11 );
	/** Double width stroke (for charts for example.             */
	private static final Stroke STROKE_DOUBLE                  = new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
	/** Normal stroke.                                           */
	private static final Stroke STROKE_NORMAL                  = new BasicStroke( 1.0f );
	/** Number of assist lines to be painted in each chart.      */
	private static final int    ASSIST_LINES_COUNT             = 5;
	/** Number of time labels to be painted in each chart.       */
	private static final int    TIME_LABELS_COUNT              = 8;
	/** Auto disabling APM limit.                                */
	private static final int    AUTO_DISABLING_APM_LIMIT       = 30;
	
	/** Date format to format replay dates. */
	private static final DateFormat REPLAY_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
	
	/** Main buildings of the different races. */
	private static short[] RACE_MAIN_BUILDINGS = new short[] { Action.BUILDING_NAME_INDEX_HATCHERY, Action.BUILDING_NAME_INDEX_COMMAND_CENTER, Action.BUILDING_NAME_INDEX_NEXUS };
	
	/**
	 * The supported types of charts.
	 * @author Andras Belicza
	 */
	public enum ChartType {
		/** APM charts of the players.              */
		APM             ( "APM/EAPM"         ),
		/** Hotkeys charts of the players.          */
		HOTKEYS         ( "Hotkeys"          ),
		/** Build order charts of the players.      */
		BUILD_ORDER     ( "Build order"      ),
		/** Strategy charts of the players.         */
		STRATEGY        ( "Strategy"         ),
		/** Overall APM charts of the players.      */
		OVERALL_APM     ( "Overall APM/EAPM" ),
		/** Action Sequences charts of the players. */
		ACTION_SEQUENCES( "Action Sequences" ),
		/** Map view charts of the players.         */
		MAP_VIEW        ( "Map view"         );
		
		private final String name;
		private ChartType( final String name ) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	/** Reference to the charts tab.                                  */
	private final ChartsTab         chartsTab;
	/** Panel containing the chart canvas and other control elements. */
	private final JPanel            contentPanel      = new JPanel( new BorderLayout() );
	/** Panel containing options of the selected chart type.          */
	private final JPanel            chartOptionsPanel = Utils.createWrapperPanel();
	/** Game details label.                                           */
	private final JLabel            gameDetailsLabel  = new JLabel( "<Game info>", JLabel.CENTER );
	/** Panel containing checkboxes of players.                       */
	private final JPanel            playersPanel      = Utils.createWrapperPanel();
	/** Replay whose charts to be visualized.                         */
	private Replay                  replay;
	/** List of hack descriptions of the replay.                      */
	private List< HackDescription > hackDescriptionList;
	/** List of player indices to be shown.                           */
	private List< Integer >         playerIndexToShowList;
	
	/** Detail levels.                 */
	private static final Object[] DETAIL_LEVELS                 = { 1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100 }; 
	/** Display levels.                */
	private static final Object[] DISPLAY_LEVELS                = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }; 
	/** Max frame delays in sequences. */
	private static final Object[] MAX_FRAME_DELAYS_IN_SEQUENCES = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50 }; 
	
	/** APM chart detail level in pixels combo box.         */
	private final JComboBox apmChartDetailLevelComboBox          = new JComboBox( DETAIL_LEVELS );
	/** Show select hotkeys checkbox.                       */
	private final JCheckBox showEapmCheckBox                     = new JCheckBox( "Show EAPM", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_EAPM ) ) );
	/** Show select hotkeys checkbox.                       */
	private final JCheckBox showSelectHotkeysCheckBox            = new JCheckBox( "Show select hotkeys", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_SELECT_HOTKEYS ) ) );
	/** Build order display levels combo box.               */
	private final JComboBox buildOrderDisplayLevelComboBox       = new JComboBox( DISPLAY_LEVELS );
	/** Show units on build order checkbox.                 */
	private final JCheckBox showUnitsOnBuildOrderCheckBox        = new JCheckBox( "Show units", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_UNITS_ON_BUILD_ORDER ) ) );
	/** Hide worker units checkbox.                         */
	private final JCheckBox hideWorkerUnitsCheckBox              = new JCheckBox( "Hide worker units", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_WORKER_UNITS ) ) );
	/** Build order display levels combo box.               */
	private final JComboBox strategyDisplayLevelComboBox         = new JComboBox( DISPLAY_LEVELS );
	/** Overall APM chart detail level in pixels combo box. */
	private final JComboBox overallApmChartDetailLevelComboBox   = new JComboBox( DETAIL_LEVELS );
	/** Show select hotkeys checkbox.                       */
	private final JCheckBox showOverallEapmCheckBox              = new JCheckBox( "Show overall EAPM", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_OVERALL_EAPM ) ) );
	/** Hide non-hotkey sequences checkbox.                 */
	private final JCheckBox hideNonHotkeySequencesCheckBox       = new JCheckBox( "Hide non-hotkey sequences", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_NON_HOTKEY_SEQUENCES ) ) );
	/** Max frames delay in sequences combo box.            */
	private final JComboBox maxFramesDelayInSequenceComboBox     = new JComboBox( MAX_FRAME_DELAYS_IN_SEQUENCES );
	/** Show player names on map checkbox.                  */
	private final JCheckBox showPlayerNamesOnMapCheckBox         = new JCheckBox( "Show player names on map", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_PLAYER_NAMES_ON_MAP ) ) );
	/** Show player names on map checkbox.                  */
	private final JCheckBox showBuildingImagesCheckBox           = new JCheckBox( "Show building images", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_BUILDING_IMAGES ) ) );
	/** Hide overlapped buildings checkbox.                 */
	private final JCheckBox hideOverlappedBuildingsCheckBox      = new JCheckBox( "Hide overlapped buildings", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_OVERLAPPED_BUILDINGS ) ) );
	
	/** Scroll bar to scroll the zoomed charts.             */
	private final JScrollBar            chartScrollBar           = new JScrollBar( JScrollBar.HORIZONTAL );
	/** Vertical scroll bar to scroll the zoomed map view charts.                */
	private final JScrollBar            chartVScrollBar          = new JScrollBar( JScrollBar.VERTICAL );
	/** Split pane to display the charts component and the players' action list. */
	private final JSplitPane            splitPane                = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true );
	/** List of the displayable actions, pairs of action+palyer name.            */
	private final ArrayList< Object[] > actionList               = new ArrayList< Object[] >();
	/** This is where we first construct actionsListTextArea's content.          */
	private final StringBuilder         actionsListTextBuilder   = new StringBuilder();
	/** Text area to show the players' actions.                                  */
	private final JTextArea             actionsListTextArea      = new JTextArea();
	/** Label before the filter text field.                                      */
	private final JLabel                filterActionsLabel       = new JLabel( "Filter actions:" );
	
	/** To jump to a specific iteration.                                         */
	private final JTextField            jumpToIterationTextField = new JTextField( 1 );
	/** To search a specific text.                                               */
	private final JTextField            searchTextField          = new JTextField( 1 );
	/** To filter actions.                                                       */
	private final JTextField            filterTextField          = new JTextField( 1 );
	
	/** Position of the marker.                                                           */
	private int markerPosition      = -1;
	/** Index of the selected action (in the actionList list) in case of Map view chart. */
	private int selectedActionIndex = -1;
	
	/** X coordinate of the mouse drag start.       */
	private int dragStartX;
	/** Y coordinate of the mouse drag start.       */
	private int dragStartY;
	/** X coordinate of the mouse drag destination. */
	private int dragDestinationX;
	/** Y coordinate of the mouse drag destination. */
	private int dragDestinationY;
	
	/** Reference to the last charts params object. */
	private ChartsParams chartsParams;
	
	/** Cache for the replay map view cache.     */
	private BufferedImage replayMapViewImage;
	/** Zoom value of the replay map view cache. */
	private int           replayMapViewZoom = -1;
	
	/**
	 * Creates a new ChartsComponent.
	 */
	public ChartsComponent( final ChartsTab chartsTab ) {
		setBackground( CHART_BACKGROUND_COLOR );
		setMaximumSize( Utils.getMaxDimension() );
		this.chartsTab = chartsTab;
		
		buildConentGUI();
		
		apmChartDetailLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_APM_CHART_DETAIL_LEVEL ) ) );
		buildOrderDisplayLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_BUILD_ORDER_DISPLAY_LEVELS ) ) );
		strategyDisplayLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_STRATEGY_DISPLAY_LEVELS ) ) );
		overallApmChartDetailLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_OVERALL_APM_CHART_DETAIL_LEVEL ) ) );
		maxFramesDelayInSequenceComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_MAX_FRAME_DELAY_IN_SEQUENCES ) ) );
	}
	
	/**
	 * Builds the GUI of the content panel.
	 */
	private void buildConentGUI() {
		final Box controlBox = Box.createVerticalBox();
		controlBox.add( chartOptionsPanel );
		gameDetailsLabel.setFont( Utils.DEFAULT_BOLD_FONT );
		controlBox.add( Utils.wrapInPanel( gameDetailsLabel ) );
		controlBox.add( playersPanel );
		contentPanel.add( controlBox, BorderLayout.NORTH );
		
		addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				if ( chartsParams != null ) {
					dragStartX = event.getX();
					dragStartY = event.getY();
					dragDestinationX = chartsParams.dx;
					dragDestinationY = chartsParams.dy;
					syncMarkerFromChartToActionList( event.getX() + chartsParams.dx );
					repaint();
				}
			}
		} );
		addMouseMotionListener( new MouseMotionAdapter() {
			@Override
			public void mouseDragged( final MouseEvent event ) {
				final int mx = event.getX();
				final int my = event.getY();
				if ( chartsParams != null ) {
					if ( ( event.getModifiers() & InputEvent.BUTTON1_MASK ) != 0 ) {
						// If the delta is less than the scroll unit, it won't scroll, and the little delta would vanish, so we accumulate them 
						dragDestinationX -= ( mx - dragStartX ) * 2;
						dragDestinationY -= ( my - dragStartY ) * 2;
						scrollToPoint( dragDestinationX, dragDestinationY );
						dragStartX = mx;
						dragStartY = my;
						repaint();
					}
				}
				updateTooltipText( mx, my );
			}
			@Override
			public void mouseMoved( final MouseEvent event ) {
				updateTooltipText( event.getX(), event.getY() );
			};
		} );
		ToolTipManager.sharedInstance().setInitialDelay( 0 );
		final JPanel chartsHolderPanel = new JPanel( new BorderLayout() );
		chartsHolderPanel.add( this, BorderLayout.CENTER );
		chartScrollBar.addAdjustmentListener( new AdjustmentListener() {
			public void adjustmentValueChanged( final AdjustmentEvent event ) {
				repaint();
			}
		} );
		chartsHolderPanel.add( chartScrollBar, BorderLayout.SOUTH );
		chartVScrollBar.addAdjustmentListener( new AdjustmentListener() {
			public void adjustmentValueChanged( final AdjustmentEvent event ) {
				repaint();
			}
		} );
		chartsHolderPanel.add( chartVScrollBar, BorderLayout.EAST );
		splitPane.setTopComponent( chartsHolderPanel );
		
		final Box actionListBox = Box.createHorizontalBox();
		actionsListTextArea.setFont( new Font( "Courier New", Font.PLAIN, 11 ) );
		actionsListTextArea.addKeyListener( new KeyAdapter() {
			@Override
			public void keyReleased( final KeyEvent event ) {
				if ( !event.isShiftDown() && !event.isControlDown() )
					syncMarkerFromActionListToChart();
			}
		} );
		actionsListTextArea.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				if ( event.getButton() == MouseEvent.BUTTON1 )
					syncMarkerFromActionListToChart();
			}
		} );
		actionsListTextArea.setEditable( false );
		actionsListTextArea.setBackground( Color.WHITE );
		actionsListTextArea.setForeground( Color.BLACK );
		actionListBox.add( Utils.wrapInBorderLayoutPanel( new JScrollPane( actionsListTextArea ) ) );
		
		final Box optionsBox = Box.createVerticalBox();
		final JPanel actionListOptionsPanel = new JPanel( new GridLayout( 3, 2 ) );
		actionListOptionsPanel.add( new JLabel( "Jump to iteration:" ) );
		jumpToIterationTextField.addKeyListener( new KeyAdapter() {
			@Override
			public void keyPressed( final KeyEvent event ) {
				if ( replay != null && event.getKeyCode() == KeyEvent.VK_ENTER ) {
					try {
						int iteration = Integer.parseInt( jumpToIterationTextField.getText() );
						final int index = searchActionForIteration( iteration );
						actionsListTextArea.setCaretPosition( actionsListTextArea.getText().indexOf( ( (Action) actionList.get( index )[ 0 ] ).toString( (String) actionList.get( index )[ 1 ], chartsTab.displayActionsInSecondsCheckBox.isSelected() ) ) + 1 );
						syncMarkerFromActionListToChart();
					}
					catch ( final Exception e ) {
					}
				}
			}
		} );
		actionListOptionsPanel.add( jumpToIterationTextField );
		actionListOptionsPanel.add( new JLabel( "Search text:" ) );
		searchTextField.addKeyListener( new KeyAdapter() {
			@Override
			public void keyPressed( final KeyEvent event ) {
				if ( replay != null && event.getKeyCode() == KeyEvent.VK_ENTER && searchTextField.getText().length() > 0 ) {
					if ( actionsListTextBuilder.length() == 0 )
						actionsListTextBuilder.append( actionsListTextArea.getText().toLowerCase() );
					
					int foundIndex = actionsListTextBuilder.indexOf( searchTextField.getText().toLowerCase(), indexOfLineEnd( actionsListTextBuilder, actionsListTextArea.getCaretPosition() ) );
					if ( foundIndex < 0 ) // Repeat search from beginning
						foundIndex = actionsListTextBuilder.indexOf( searchTextField.getText().toLowerCase(), indexOfLineEnd( actionsListTextBuilder, 0 ) );
					if ( foundIndex >= 0 ) {
						actionsListTextArea.setCaretPosition( foundIndex );
						syncMarkerFromActionListToChart();
					}
				}
			}
		} );
		actionListOptionsPanel.add( searchTextField );
		filterActionsLabel.setOpaque( true ); // Needed for the background to take effect
		actionListOptionsPanel.add( filterActionsLabel );
		filterTextField.addKeyListener( new KeyAdapter() {
			@Override
			public void keyPressed( final KeyEvent event ) {
				if ( event.getKeyCode() == KeyEvent.VK_ENTER )
					loadPlayerActionsIntoList();
			}
		} );
		actionListOptionsPanel.add( filterTextField );
		
		optionsBox.add( actionListOptionsPanel );
		// I put clear filter button in a different grid panel, because buttons' height is significantly greater than texfields'.
		final JPanel clearFilterButtonPanel = new JPanel( new GridLayout( 1, 2 ) );
		clearFilterButtonPanel.add( new JLabel() );
		final JButton clearFilterButton = new JButton( "Clear filter", IconResourceManager.ICON_UNDO );
		clearFilterButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				filterTextField.setText( "" );
				loadPlayerActionsIntoList();
			}
		} );
		clearFilterButtonPanel.add( clearFilterButton );
		optionsBox.add( clearFilterButtonPanel );
		optionsBox.add( Utils.wrapInPanel( new JLabel( "You can use OR between filter words." ) ) );
		final JComponent verticalFillerComponent = new JLabel();
		verticalFillerComponent.setMaximumSize( new Dimension( 1, Integer.MAX_VALUE ) );
		optionsBox.add( verticalFillerComponent );
		actionListBox.add( optionsBox );
		
		splitPane.setBottomComponent( actionListBox );
		contentPanel.add( splitPane, BorderLayout.CENTER );
		
		final ActionListener repainterActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				repaint();
			}
		};
		apmChartDetailLevelComboBox.addActionListener( repainterActionListener );
		showEapmCheckBox.addActionListener( repainterActionListener );
		showSelectHotkeysCheckBox.addActionListener( repainterActionListener );
		buildOrderDisplayLevelComboBox.addActionListener( repainterActionListener );
		showUnitsOnBuildOrderCheckBox.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				hideWorkerUnitsCheckBox.setEnabled( showUnitsOnBuildOrderCheckBox.isSelected() );
			}
		} );
		showUnitsOnBuildOrderCheckBox.addActionListener( repainterActionListener );
		hideWorkerUnitsCheckBox.addActionListener( repainterActionListener );
		hideWorkerUnitsCheckBox.setEnabled( showUnitsOnBuildOrderCheckBox.isSelected() );
		strategyDisplayLevelComboBox.addActionListener( repainterActionListener );
		overallApmChartDetailLevelComboBox.addActionListener( repainterActionListener );
		showOverallEapmCheckBox.addActionListener( repainterActionListener );
		hideNonHotkeySequencesCheckBox.addActionListener( repainterActionListener );
		maxFramesDelayInSequenceComboBox.addActionListener( repainterActionListener );
		showPlayerNamesOnMapCheckBox.addActionListener( repainterActionListener );
		showBuildingImagesCheckBox.addActionListener( repainterActionListener );
		hideOverlappedBuildingsCheckBox.addActionListener( repainterActionListener );
	}
	
	/**
	 * Updates the tooltip text over the chart.
	 * @param x x coordinate of the point to show tooltip for
	 * @param y y coordinate of the point to show tooltip for
	 */
	private void updateTooltipText( final int x, final int y ) {
		if ( chartsParams == null )
			return;
		
		final ChartType chartType = (ChartType) chartsTab.chartTypeComboBox.getSelectedItem();
		if ( chartType == ChartType.MAP_VIEW ) {
			final int mapWidth   = replay.replayHeader.mapWidth  * 32; // 1 tile is 32 pixel width  in the game (this is independent of our tile width )
			final int mapHeight  = replay.replayHeader.mapHeight * 32; // 1 tile is 32 pixel height in the game (this is independent of our tile height)
			
			final int mapCoordX = ( chartsParams.dx + x ) * ChartsTab.MAX_ZOOM / chartsParams.zoom;
			final int mapCoordY = ( chartsParams.dy + y ) * ChartsTab.MAX_ZOOM / chartsParams.zoom;
			
			if ( mapCoordX < mapWidth && mapCoordY < mapHeight ) {
				final int tileWidth  = MapImagesManager.TILE_IMAGE_WIDTH  * chartsParams.zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
				final int tileHeight = MapImagesManager.TILE_IMAGE_HEIGHT * chartsParams.zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
				setToolTipText( "Position: " + mapCoordX + "," + mapCoordY + " (" + ( (chartsParams.dx + x) / tileWidth ) + "," + ( (chartsParams.dy + y) / tileHeight ) + ")" );
			}
			else
				setToolTipText( null );
		}
		else {
			final int coordX = chartsParams.dx - chartsParams.x1 + x;
			if ( coordX >= 0 ) {
				final int maxX = chartsParams.componentWidth * chartsParams.zoom - chartsParams.x1;
				final int iteration = ( coordX * replay.replayHeader.gameFrames / maxX );
				
				final StringBuilder tooltipTextBuilder = new StringBuilder( "Frame: " ).append( iteration ).append( ", time: " );
				setToolTipText( ReplayHeader.formatFrames( iteration, tooltipTextBuilder, true ).toString() );
			}
			else
				setToolTipText( null );
		}
	}
	
	/**
	 * Sets the zoom level.
	 * @param zoom zoom level to be set.
	 */
	public void setZoom( final int zoom ) {
		chartScrollBar.setVisible( zoom > 1 );
		chartVScrollBar.setVisible( zoom > 1 && ( (ChartType) chartsTab.chartTypeComboBox.getSelectedItem() ) == ChartType.MAP_VIEW );
		chartScrollBar.getParent().validate();
		
		// Scroll to the marker in the new zoom view.
		// Since we're in a swing thread right now, we do this "later".
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				syncMarkerFromActionListToChart();
			}
		} );
	}
	
	/**
	 * Synchronizes the marker from the chart to the action list text area.
	 * @param x x coordinate on the chart
	 */
	private void syncMarkerFromChartToActionList( final int x ) {
		if ( replay == null || playerIndexToShowList.isEmpty() || chartsParams == null || (ChartType) chartsTab.chartTypeComboBox.getSelectedItem() == ChartType.MAP_VIEW )
			return;
		
		final int iteration = chartsParams.getIterationForX( x, replay.replayHeader.gameFrames );
		if ( iteration >= 0 ) {
			markerPosition = x;
			
			// Find an action for that iteration
			final int index = searchActionForIteration( iteration );
			
			if ( iteration >= 0 && index >= 0 ) {
				final String actionString = ( (Action) actionList.get( index )[ 0 ] ).toString( (String) actionList.get( index )[ 1 ], chartsTab.displayActionsInSecondsCheckBox.isSelected() );
				// We get position in the textarea's text, because this might differ from the position in acitonsListTextBuilder
				final int actionCaretPosition = actionsListTextArea.getText().indexOf( actionString );
				actionsListTextArea.setCaretPosition( actionCaretPosition );
				// First clear previous selection:
				actionsListTextArea.setSelectionStart( -1 );
				actionsListTextArea.setSelectionEnd  ( -1 );
				// Selection end has to be set first, or else it doesn't work for the first line (doesn't select it).
				actionsListTextArea.setSelectionEnd( actionCaretPosition + actionString.length() );
				actionsListTextArea.setSelectionStart( actionCaretPosition );
			}
		}
	}
	
	/**
	 * Searches an action for the given iteration.<br>
	 * If no such action exists, returns the one that precedes it. If no such iteration precedes it,
	 * returns the first iteration.<br>
	 * Uses binary search algorithm.
	 * @param iteration iteration to be searched for
	 * @return a preceding action for the given iteration or the first action if no preceding iteration exists; or -1 if action list is empty
	 */
	private int searchActionForIteration( final int iteration ) {
		if ( actionList.isEmpty() )
			return -1;
		
		// Binary search
		int minIndex = 0, maxIndex = actionList.size() - 1;
		if ( iteration >= ( (Action) actionList.get( maxIndex )[ 0 ] ).iteration)
			return maxIndex;
		
		int lastIndex = -1;
		while ( true ) {
			int       index      = ( minIndex + maxIndex ) / 2;
			final int iteration2 = ( (Action) actionList.get( index )[ 0 ] ).iteration;
			
			if ( iteration2 == iteration || lastIndex == index )
				return index;
			else if ( iteration < iteration2 )
				maxIndex = index;
			else
				minIndex = index;
			
			lastIndex = index;
		}
	}
	
	/**
	 * Synchronizes the marker from the chart to the action list text area.
	 */
	private void syncMarkerFromActionListToChart() {
		if ( chartsParams == null )
			return;
		final String actionListText = actionsListTextArea.getText();
		if ( actionsListTextArea.getText().length() == 0 )
			return;
		
		int caretPosition = actionsListTextArea.getCaretPosition();
		
		if ( caretPosition < 0 )
			caretPosition = 0;
		if ( caretPosition >= actionListText.length() )
			caretPosition = actionListText.length() - 1;
		if ( actionListText.charAt( caretPosition ) == '\n' )
			caretPosition--;
		
		final int actionFirstPosition = indexOfLineStart( actionListText, caretPosition );
		final int actionLastPosition  = indexOfLineEnd( actionListText, caretPosition  );
		
		// First clear previous selection:
		actionsListTextArea.setSelectionStart( -1 );
		actionsListTextArea.setSelectionEnd  ( -1 );
		// Selection end has to be set first, or else it doesn't work for the first line (doesn't select it).
		actionsListTextArea.setSelectionEnd( actionLastPosition );
		actionsListTextArea.setSelectionStart( actionFirstPosition );
		
		if ( (ChartType) chartsTab.chartTypeComboBox.getSelectedItem() == ChartType.MAP_VIEW ) {
			selectedActionIndex = 0;
			for ( int i = caretPosition; i >= 0; i -- )
				if ( actionListText.charAt( i ) == '\n' )
					selectedActionIndex++;
			if ( selectedActionIndex >= actionList.size() ) // Sometimes it counts up to the size...
				selectedActionIndex  = actionList.size() - 1;
			
			// If target point is not visible, scroll to it
			final int   zoom        = MapImagesManager.TILE_IMAGE_WIDTH * chartsParams.zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
			final int[] targetPoint = getActionTargetPoint( (Action) actionList.get( selectedActionIndex )[ 0 ], zoom );
			if ( targetPoint != null ) {
				final int x = targetPoint[ 0 ];
				final int y = targetPoint[ 1 ];
				
				if ( x < chartsParams.dx || x >= chartsParams.dx + chartsParams.componentWidth || y < chartsParams.dy || y >= chartsParams.dy + chartsParams.componentHeight )
					scrollToPoint( x - chartsParams.componentWidth / 2, y - chartsParams.componentHeight / 2 );
			}
		}
		else {
			final StringTokenizer timeTokenizer = new StringTokenizer( (String) actionListText.substring( actionFirstPosition, actionLastPosition ) );
			int time;
			int maxTime;
			if ( chartsTab.displayActionsInSecondsCheckBox.isSelected() ) {
				time    = 3600 * Integer.parseInt( timeTokenizer.nextToken( ":" ) ) + 60 * Integer.parseInt( timeTokenizer.nextToken( ":" ) ) + Integer.parseInt( timeTokenizer.nextToken( ": " ) );
				maxTime = replay.replayHeader.getDurationSeconds();
			}
			else {
				time    = Integer.parseInt( timeTokenizer.nextToken() );
				maxTime = replay.replayHeader.gameFrames;
			}
			markerPosition = chartsParams.getXForTime( time, maxTime );
			
			// If marker is not visible, scroll to it
			if ( markerPosition < chartsParams.dx || markerPosition >= chartsParams.dx + chartsParams.componentWidth )
				scrollToPoint( markerPosition - chartsParams.componentWidth / 2, 0 );
		}
		
		repaint();
	}
	
	/**
	 * Scrolls to the given point
	 * @param x pixel x coordinate to scroll to
	 * @param y pixel y coordinate to scroll to
	 */
	private void scrollToPoint( final int x, final int y ) {
		if ( chartsParams == null )
			return;
		
		if ( (ChartType) chartsTab.chartTypeComboBox.getSelectedItem() == ChartType.MAP_VIEW ) {
			final int tileWidth      = MapImagesManager.TILE_IMAGE_WIDTH  * chartsParams.zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
			final int tileHeight     = MapImagesManager.TILE_IMAGE_HEIGHT * chartsParams.zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
			final int mapPixelWidth  = replay.replayHeader.mapWidth  * tileWidth;
			final int mapPixelHeight = replay.replayHeader.mapHeight * tileHeight;
			
			if ( mapPixelWidth > chartsParams.componentWidth )
				chartScrollBar .setValue( x * ( chartScrollBar .getMaximum() - chartScrollBar .getVisibleAmount() ) / ( mapPixelWidth  - chartsParams.componentWidth  ) );
			if ( mapPixelHeight > chartsParams.componentHeight )
				chartVScrollBar.setValue( y * ( chartVScrollBar.getMaximum() - chartVScrollBar.getVisibleAmount() ) / ( mapPixelHeight - chartsParams.componentHeight ) );
		}
		else {
			final int divider = ( chartsParams.componentWidth * chartsParams.zoom - chartsParams.componentWidth );
			if ( divider > 0 ) // to avoid ArithmeticException 
				chartScrollBar.setValue( x * ( chartScrollBar.getMaximum() - chartScrollBar.getVisibleAmount() ) / divider );
		}
	}
	
	/**
	 * Returns the index of the start of the line specified by pos.
	 * @param text text in which to search
	 * @param pos  pos pointing somewhere in the line
	 * @return the index of the start of the line specified by pos
	 */
	private static int indexOfLineStart( final String text, int pos ) {
		// Backward search
		while ( pos > 0 && text.charAt( pos ) != '\n' )
			pos--;
		if ( text.charAt( pos ) == '\n' )
			pos++;
		
		return pos;
	}
	
	/**
	 * Returns the index of the end of the line specified by pos.
	 * @param text text in which to search; must be a {@link String} or {@link StringBuilder} 
	 * @param pos  pos pointing somewhere in the line
	 * @return the index of the start of the line specified by pos
	 */
	private static int indexOfLineEnd( final CharSequence text, int pos ) {
		if ( text instanceof String )
			pos = ( (String) text ).indexOf( '\n', pos );
		else if ( text instanceof StringBuilder )
			pos = ( (StringBuilder) text ).indexOf( "\n", pos );
		else
			throw new IllegalArgumentException( "Illegal text parameter, only String and StringBuilder are supported!" );
		
		if ( pos < 0 )
			pos = text.length();
		
		return pos;
	}
	
	/**
	 * Builds the panel containing the chart component and
	 */
	public JPanel getContentPanel() {
		return contentPanel;
	}
	
	/**
	 * Receives a notification of the end of initialization.
	 */
	public void initializationEnded() {
		splitPane.setDividerLocation( 0.78 );
	}
	
	public void setChartType( final ChartType chartType ) {
		if ( chartType == null )
			return;
		
		// Scrollbar visibility policy might change here: only the Map view chart has vertical scrollbar
		if ( (Integer) chartsTab.zoomComboBox.getSelectedItem() > 1 
				&& !( chartVScrollBar.isVisible() ^ chartType != ChartType.MAP_VIEW ) ) {
			setZoom( (Integer) chartsTab.zoomComboBox.getSelectedItem() );
		}
		
		// We store values on the options panel before we remove the components, they might lost their values in SwingWT
		assignUsedProperties();
		// removeAll() does not work properly in SwingWT, we remove components manually!
		while ( chartOptionsPanel.getComponentCount() > 0 )
			chartOptionsPanel.remove( chartOptionsPanel.getComponentCount() - 1 );
		
		switch ( chartType ) {
			case APM :
				chartOptionsPanel.add( new JLabel( "Detail level: " ) );
				chartOptionsPanel.add( apmChartDetailLevelComboBox );
				chartOptionsPanel.add( new JLabel( " pixels." ) );
				chartOptionsPanel.add( showEapmCheckBox );
				break;
			case HOTKEYS :
				chartOptionsPanel.add( showSelectHotkeysCheckBox );
				break;
			case BUILD_ORDER :
				chartOptionsPanel.add( new JLabel( "Display levels: " ) );
				chartOptionsPanel.add( buildOrderDisplayLevelComboBox );
				chartOptionsPanel.add( new JLabel( " " ) );
				chartOptionsPanel.add( showUnitsOnBuildOrderCheckBox );
				chartOptionsPanel.add( hideWorkerUnitsCheckBox );
				break;
			case STRATEGY :
				chartOptionsPanel.add( new JLabel( "Display levels: " ) );
				chartOptionsPanel.add( strategyDisplayLevelComboBox );
				break;
			case OVERALL_APM :
				chartOptionsPanel.add( new JLabel( "Detail level: " ) );
				chartOptionsPanel.add( overallApmChartDetailLevelComboBox );
				chartOptionsPanel.add( new JLabel( " pixels." ) );
				chartOptionsPanel.add( showOverallEapmCheckBox );
				break;
			case ACTION_SEQUENCES :
				chartOptionsPanel.add( hideNonHotkeySequencesCheckBox );
				chartOptionsPanel.add( new JLabel( "Max frame delay in sequences: " ) );
				chartOptionsPanel.add( maxFramesDelayInSequenceComboBox );
				break;
			case MAP_VIEW :
				chartOptionsPanel.add( showPlayerNamesOnMapCheckBox );
				chartOptionsPanel.add( showBuildingImagesCheckBox );
				chartOptionsPanel.add( hideOverlappedBuildingsCheckBox );
				break;
		}
		
		// We restore the values
		apmChartDetailLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_APM_CHART_DETAIL_LEVEL ) ) );
		showSelectHotkeysCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_SELECT_HOTKEYS ) ) );
		showUnitsOnBuildOrderCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_UNITS_ON_BUILD_ORDER ) ) );
		buildOrderDisplayLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_BUILD_ORDER_DISPLAY_LEVELS ) ) );
		hideWorkerUnitsCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_WORKER_UNITS ) ) );
		buildOrderDisplayLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_STRATEGY_DISPLAY_LEVELS ) ) );
		overallApmChartDetailLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_OVERALL_APM_CHART_DETAIL_LEVEL ) ) );
		hideNonHotkeySequencesCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_NON_HOTKEY_SEQUENCES ) ) );
		maxFramesDelayInSequenceComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_MAX_FRAME_DELAY_IN_SEQUENCES ) ) );
		showPlayerNamesOnMapCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_PLAYER_NAMES_ON_MAP ) ) );
		showBuildingImagesCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_BUILDING_IMAGES ) ) );
		hideOverlappedBuildingsCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_OVERLAPPED_BUILDINGS ) ) );
		
		contentPanel.validate();
		// Chart and action list might be differently synced in case of different chart type (like map view and not map view), so we resync now.
		syncMarkerFromActionListToChart(); // This also calls repaint(), so we don't have to do that here
	}
	
	/**
	 * Sets the replay whose charts to be visualized.
	 * @param replay replay whose charts to be visualized
	 */
	public void setReplay( final Replay replay ) {
		this.replay = replay;
		
		markerPosition      = -1;
		replayMapViewZoom   = -1;
		replayMapViewImage  = null;
		selectedActionIndex = -1;
		
		// removeAll() does not work properly in SwingWT, we remove previous checkboxes manually!
		while ( playersPanel.getComponentCount() > 1 )
			playersPanel.remove( playersPanel.getComponentCount() - 1 );
		playersPanel.getParent().validate();
		
		if ( replay != null ) {
			final ReplayHeader replayHeader = replay.replayHeader;
			
			final StringBuilder gameInfoBuilder = new StringBuilder();
			// character 0x255 is used instead of spaces (because it is interpreted as HTML and multiple spaces will appear only as 1 space!
			gameInfoBuilder.append( "Ver.: " ).append( replayHeader.gameEngine < ReplayHeader.GAME_ENGINE_SHORT_NAMES.length ? ReplayHeader.GAME_ENGINE_SHORT_NAMES[ replayHeader.gameEngine ] : "" ).append( ' ' ).append( replayHeader.guessVersionFromDate() );
			gameInfoBuilder.append( "  |  Saved on: " ).append( REPLAY_DATE_FORMAT.format( replayHeader.saveTime ) );
			final String gameTypeName = replayHeader.gameType < ReplayHeader.GAME_TYPE_SHORT_NAMES.length ? ReplayHeader.GAME_TYPE_SHORT_NAMES[ replayHeader.gameType ] : null;
			gameInfoBuilder.append( "  |  Creator: " ).append( replayHeader.creatorName );
			gameInfoBuilder.append( "  |  Name: " ).append( replayHeader.gameName );
			gameInfoBuilder.append( "  |  Type: " ).append( gameTypeName == null ? "N/A" : gameTypeName );
			gameInfoBuilder.append( "  |  Map: " ).append( replayHeader.mapName );
			gameInfoBuilder.append( "  |  Size: " ).append( replayHeader.getMapSize() );
			gameDetailsLabel.setText( gameInfoBuilder.toString() );
			
			hackDescriptionList = ReplayScanner.scanReplayForHacks( replay, false );
			
			final PlayerActions[] playerActions = replay.replayActions.players;
			
			if ( playersPanel.getComponentCount() == 0 ) // If players label has not yet been added
				playersPanel.add( new JLabel( "Players: " ) );
			
			// To store player checkboxes and their index
			final Object[][] players = new Object[ playerActions.length ][ 2 ];
			
			final ActionListener playerCheckBoxActionListener = new ActionListener() {
				public void actionPerformed( final ActionEvent event ) {
					playerIndexToShowList.clear();
					for ( int i = 0; i < players.length; i++ )
						if ( ( (JCheckBox) players[ i ][ 0 ] ).isSelected() )
							playerIndexToShowList.add( (Integer) players[ i ][ 1 ] );
					
					repaint();
					loadPlayerActionsIntoList();
					syncMarkerFromChartToActionList( markerPosition );
				}
			};
			for ( int i = 0; i < players.length; i++ ) {
				final String playerName = playerActions[ i ].playerName;
				players[ i ][ 0 ] = new JCheckBox( playerName, replayHeader.gameFrames < ReplayHeader.FRAMES_IN_TWO_MINUTES || !chartsTab.autoDisableInactivePlayersCheckBox.isSelected() || replayHeader.getPlayerApm( replayHeader.getPlayerIndexByName( playerActions[ i ].playerName ) ) >= AUTO_DISABLING_APM_LIMIT );
				final ListedAs listedAs = MainFrame.getInstance().playerCheckerTab.isPlayerListed( playerActions[ i ].playerName );
				final JCheckBox playerCheckBox = ( (JCheckBox) players[ i ][ 0 ] );
				if ( listedAs == ListedAs.HACKER ) {
					playerCheckBox.setBackground( new Color( 255, 100, 100 ) );
					playerCheckBox.setToolTipText( "There are hacker(s) reported with this name." );
				}
				else if ( listedAs == ListedAs.CUSTOM ) {
					playerCheckBox.setBackground( new Color( 220, 220, 50 ) );
					playerCheckBox.setToolTipText( "This name is on your custom list." );
				}
				players[ i ][ 1 ] = i;
				playerCheckBox.addActionListener( playerCheckBoxActionListener );
				playerCheckBox.addMouseListener( new MouseAdapter() {
					private JPopupMenu playerMenu;
					@Override
					public void mouseClicked( final MouseEvent event ) {
						if ( event.getButton() == MouseEvent.BUTTON3 ) {
							if ( playerMenu == null ) {
								playerMenu = new JPopupMenu();
								
								final JMenuItem bwhfProfileMenuItem = new JMenuItem( "View BWHF Player profile", IconResourceManager.ICON_BWHF );
								bwhfProfileMenuItem.addActionListener( new ActionListener() {
									public void actionPerformed( final ActionEvent event ) {
										try {
											Utils.showURLInBrowser( Consts.PLAYERS_NETWORK_PAGE_URL + "?" + ServerApiConsts.PN_REQUEST_PARAM_NAME_OPERATION + "=" + ServerApiConsts.PN_OPERATION_DETAILS
													+ "&" + ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY + "=" + ServerApiConsts.ENTITY_PLAYER + "&" + ServerApiConsts.PN_REQUEST_PARAM_NAME_ENTITY_NAME + "="
													+ URLEncoder.encode( playerName, "UTF-8" ) );
										} catch ( final UnsupportedEncodingException uee ) {}
									}
								} );
								playerMenu.add( bwhfProfileMenuItem );
								
								final JMenuItem iccupProfileMenuItem = new JMenuItem( "View iCCup Player profile", IconResourceManager.ICON_ICCUP );
								iccupProfileMenuItem.addActionListener( new ActionListener() {
									public void actionPerformed( final ActionEvent event ) {
										try {
											Utils.showURLInBrowser( "http://www.iccup.com/gamingprofile/" + URLEncoder.encode( playerName, "UTF-8" ) + ".html" );
										} catch ( final UnsupportedEncodingException uee ) {}
									}
								} );
								playerMenu.add( iccupProfileMenuItem );
								
								final JMenuItem starcraftdreamProfileMenuItem = new JMenuItem( "View StarCraft Dream Player profile", IconResourceManager.ICON_STARCRAFTDREAM );
								starcraftdreamProfileMenuItem.addActionListener( new ActionListener() {
									public void actionPerformed( final ActionEvent event ) {
										try {
											Utils.showURLInBrowser( "http://www.starcraftdream.com/forums/member.php?uname=" + URLEncoder.encode( playerName, "UTF-8" ) );
										} catch ( final UnsupportedEncodingException uee ) {}
									}
								} );
								playerMenu.add( starcraftdreamProfileMenuItem );
								
								final JMenuItem vilegamingProfileMenuItem = new JMenuItem( "View Vile Gaming Player profile", IconResourceManager.ICON_VILEGAMING );
								vilegamingProfileMenuItem.addActionListener( new ActionListener() {
									public void actionPerformed( final ActionEvent event ) {
										try {
											Utils.showURLInBrowser( "http://vilegaming.com/users.php?m=details&bwhfa=" + URLEncoder.encode( playerName, "UTF-8" ) );
										} catch ( final UnsupportedEncodingException uee ) {}
									}
								} );
								playerMenu.add( vilegamingProfileMenuItem );
								
								final JMenuItem teamliquidProfileMenuItem = new JMenuItem( "View Team Liquid Player profile", IconResourceManager.ICON_TEAMLIQUID );
								teamliquidProfileMenuItem.addActionListener( new ActionListener() {
									public void actionPerformed( final ActionEvent event ) {
										try {
											Utils.showURLInBrowser( "http://www.teamliquid.net/forum/profile.php?user=" + URLEncoder.encode( playerName, "UTF-8" ) );
										} catch ( final UnsupportedEncodingException uee ) {}
									}
								} );
								playerMenu.add( teamliquidProfileMenuItem );
								
								final JMenuItem gosugamersProfileMenuItem = new JMenuItem( "View GosuGamers Player profile", IconResourceManager.ICON_GOSUGAMERS );
								gosugamersProfileMenuItem.addActionListener( new ActionListener() {
									public void actionPerformed( final ActionEvent event ) {
										try {
											Utils.showURLInBrowser( "http://www.gosugamers.net/starcraft/members/" + URLEncoder.encode( playerName, "UTF-8" ) );
										} catch ( final UnsupportedEncodingException uee ) {}
									}
								} );
								playerMenu.add( gosugamersProfileMenuItem );
								
								if ( listedAs == ListedAs.HACKER ) {
									playerMenu.addSeparator();
									final JMenuItem bwhfHackerRerortsMenuItem = new JMenuItem( "View BWHF Hacker reports", IconResourceManager.ICON_BWHF );
									bwhfHackerRerortsMenuItem.addActionListener( new ActionListener() {
										public void actionPerformed( final ActionEvent event ) {
											try {
												Utils.showURLInBrowser( Consts.BWHF_HACKER_DATA_BASE_SERVER_URL + "?" + ServerApiConsts.FILTER_NAME_NAME + "=" + URLEncoder.encode( '"' + playerName + '"', "UTF-8" ) );
											} catch ( final UnsupportedEncodingException uee ) {}
										}
									} );
									playerMenu.add( bwhfHackerRerortsMenuItem );
								}
							}
							
							playerMenu.show( event.getComponent(), event.getX(), event.getY() );
						}
					}
				} );
			}
			// Order players by the number of their actions
			Arrays.sort( players, new Comparator< Object[] >() {
				public int compare( final Object[] o1, final Object[] o2 ) {
					return -Integer.valueOf( playerActions[ (Integer) o1[ 1 ] ].actions.length )
							.compareTo( playerActions[ (Integer) o2[ 1 ] ].actions.length );
				}
			} );
			
			for ( final Object[] player : players )
				playersPanel.add( (JCheckBox) player[ 0 ] );
			
			playerIndexToShowList = new ArrayList< Integer >( players.length );
			// Set the initially visible players:
			playerCheckBoxActionListener.actionPerformed( null ); // This also repaints.
			contentPanel.validate();
		}
		else {
			gameDetailsLabel.setText( "<Game info>" );
			hackDescriptionList = null;
			repaint();
			loadPlayerActionsIntoList();
		}
	}
	
	/**
	 * Loads the player actions into the action list text area.
	 */
	public void loadPlayerActionsIntoList() {
		final String[][] filterGroups = createFilterGroups();
		
		filterActionsLabel.setBackground( filterGroups == null ? filterActionsLabel.getParent().getBackground() : Color.GREEN );
		
		actionList.clear();
		actionsListTextBuilder.setLength( 0 );
		if ( replay != null ) {
			final PlayerActions[] playerActionss = replay.replayActions.players;
			
			int actionsCount = 0;
			for ( final int playerIndex : playerIndexToShowList )
				actionsCount += playerActionss[ playerIndex ].actions.length;
			
			actionList.ensureCapacity( actionsCount );
			for ( final int playerIndex : playerIndexToShowList ) {
				final String playerName = playerActionss[ playerIndex ].playerName;
				for ( final Action action : playerActionss[ playerIndex ].actions ) {
					if ( filterGroups != null ) {
						final String actionString = action.toString( playerName, chartsTab.displayActionsInSecondsCheckBox.isSelected() ).toLowerCase();
						for ( final String[] filterGroup : filterGroups ) {
							boolean filterApplies = true;
							for ( final String filter : filterGroup )
								if ( !actionString.contains( filter ) ) {
									filterApplies = false;
									break;
								}
							if ( filterApplies ) {
								actionList.add( new Object[] { action, playerName } );
								break;
							}
						}
					}
					else
						actionList.add( new Object[] { action, playerName } );
				}
			}
			
			Collections.sort( actionList, new Comparator< Object[] >() {
				public int compare( Object[] action1, Object[] action2 ) {
					return ( (Action) action1[ 0 ] ).compareTo( (Action) action2[ 0 ] );
				}
			} );
			
			final boolean displayActionsInSeconds = chartsTab.displayActionsInSecondsCheckBox.isSelected();
			for ( final Object[] action : actionList )
				actionsListTextBuilder.append( ( (Action) action[ 0 ] ).toString( (String) action[ 1 ], displayActionsInSeconds ) ).append( '\n' );
			if ( actionsListTextBuilder.length() > 0 ) // remove the last '\n'
				actionsListTextBuilder.setLength( actionsListTextBuilder.length() - 1 );
		}
		actionsListTextArea.setText( actionsListTextBuilder.toString() );
		actionsListTextArea.setCaretPosition( 0 );
		actionsListTextBuilder.setLength( 0 ); // To indicate that this does not yet contain the lowercased version for searching
		
		if ( markerPosition >= 0 )
			syncMarkerFromChartToActionList( markerPosition );
	}
	
	/**
	 * Creates the filter groups from the <code>filterTextField</code>.<br>
	 * Filters in a group are connected with logical AND condition, and the groups are connected
	 * with logical OR condition.
	 * @return the filter group created from <code>filterTextField</code>; or null if no filter was specified
	 */
	private String[][] createFilterGroups() {
		if ( filterTextField.getText().length() == 0 )
			return null;
		
		final List< List< String > > filterGroupList = new ArrayList< List< String > >();
		
		final StringTokenizer filterTokenizer = new StringTokenizer( filterTextField.getText().toLowerCase() );
		
		List< String > filterGroup = null;
		while ( filterTokenizer.hasMoreTokens() ) {
			final String filterToken = filterTokenizer.nextToken();
			if ( filterToken.equals( "or" ) ) {
				// Next token is in a new filter group
				filterGroup = null;
			}
			else if ( filterToken.equals( "and" ) ) {
				// Do nothing, next token is in the same group
			}
			else {
				if ( filterGroup == null ) {
					filterGroup = new ArrayList< String >( 2 );
					filterGroupList.add( filterGroup );
				}
				filterGroup.add( filterToken );
			}
		}
		
		final String[][] filterGroups = new String[ filterGroupList.size() ][];
		for ( int i = 0; i < filterGroups.length; i++ )
			filterGroups[ i ] = filterGroupList.get( i ).toArray( new String[ 0 ] );
		
		return filterGroups;
	}
	
	@Override
	public void paintComponent( final Graphics graphics ) {
		( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
		graphics.clearRect( 0, 0, getWidth(), getHeight() );
		
		if ( replay != null && playerIndexToShowList.size() > 0 && replay.replayHeader.gameFrames != 0 ) {
			final ChartType chartType = (ChartType) chartsTab.chartTypeComboBox.getSelectedItem();
			
			final int zoom = (Integer) chartsTab.zoomComboBox.getSelectedItem();
			int dx, dy;
			if ( zoom > 1 && chartType == ChartType.MAP_VIEW ) {
				final int tileWidth  = MapImagesManager.TILE_IMAGE_WIDTH  * zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
				final int tileHeight = MapImagesManager.TILE_IMAGE_HEIGHT * zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
				dx = ( replay.replayHeader.mapWidth  * tileWidth  - getWidth () ) * chartScrollBar .getValue() / ( chartScrollBar .getMaximum() - chartScrollBar .getVisibleAmount() );
				dy = ( replay.replayHeader.mapHeight * tileHeight - getHeight() ) * chartVScrollBar.getValue() / ( chartVScrollBar.getMaximum() - chartVScrollBar.getVisibleAmount() );
			}
			else {
				dx = ( getWidth() * zoom - getWidth() ) * chartScrollBar.getValue() / ( chartScrollBar.getMaximum() - chartScrollBar.getVisibleAmount() );
				dy = 0;
			}
			if ( dx < 0 ) dx = 0;
			if ( dy < 0 ) dy = 0;
			chartsParams = new ChartsParams( chartsTab, replay.replayHeader.gameFrames, playerIndexToShowList.size(), this, dx, dy );
			
			graphics.translate( -dx, -dy );
			
			switch ( (ChartType) chartsTab.chartTypeComboBox.getSelectedItem() ) {
				case APM :
					paintApmCharts( graphics, false );
					break;
				case HOTKEYS :
					paintHotkeysCharts( graphics );
					break;
				case BUILD_ORDER :
					paintBuildOrderCharts( graphics );
					break;
				case STRATEGY :
					paintStrategyCharts( graphics );
					break;
				case OVERALL_APM :
					paintApmCharts( graphics, true );
					break;
				case ACTION_SEQUENCES :
					paintActionSequencesCharts( graphics );
					break;
				case MAP_VIEW :
					paintMapViewCharts( graphics );
					break;
			}
			
			if ( chartType != ChartType.MAP_VIEW && markerPosition >= 0 ) {
				graphics.setColor( CHART_MARKER_COLOR );
				graphics.drawLine( markerPosition, 0, markerPosition, getHeight() - 1 );
			}
		}
	}
	
	/**
	 * Paints the APM charts of the players.
	 * @param graphics     graphics to be used for painting
	 * @param overall      tells if we have to paint momentary or overall APM charts
	 */
	private void paintApmCharts( final Graphics graphics, final boolean overall ) {
		final int chartGranularity = chartsParams.zoom * (Integer) ( overall? overallApmChartDetailLevelComboBox : apmChartDetailLevelComboBox ).getSelectedItem();
		if ( getWidth() < chartGranularity )
			return;
		
		final boolean eapm = ( overall ? showOverallEapmCheckBox : showEapmCheckBox ).isSelected();
		
		final int     chartPoints  = chartsParams.maxXInChart / chartGranularity + 1;
		
		final int[]   xPoints      = new int[ chartPoints + 1 ];
		final int[][] yPointss     = new int[ chartsParams.playersCount ][ chartPoints + 1 ];
		final int[][] yPointssEapm = eapm ? new int[ chartsParams.playersCount ][ chartPoints + 1 ] : null;
		int pointIndex = 0;
		for ( int x = chartsParams.x1; pointIndex < xPoints.length ; pointIndex++, x+= chartGranularity )
			xPoints[ pointIndex ] = x;
		
		graphics.setFont( CHART_MAIN_FONT );
		graphics.setColor( CHART_AXIS_COLOR );
		graphics.drawString( overall ? "Overall APM" : "APM", chartsParams.dx + 1, 10 );
		
		// First count the actions
		final int[] effectiveActionsCounts = eapm ? new int[ chartsParams.playersCount ] : null;
		
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final PlayerActions playerActions = replay.replayActions.players[ playerIndexToShowList.get( i ) ];
			final int[] yPoints     = yPointss[ i ];
			final int[] yPointsEapm = eapm ? yPointssEapm[ i ] : null;
			
			int lastPointIndex = 1, lastPointIndexEapm = 1;
			final Action[] allActions = playerActions.actions;
			final int allActionsCount = allActions.length;
			for ( int ai = 0; ai < allActionsCount; ai++ )
				try {
					final Action action = allActions[ ai ];
					pointIndex = 1 + action.iteration * chartPoints / chartsParams.frames;
					if ( overall && lastPointIndex < pointIndex ) {
						final int actionsCount = yPoints[ lastPointIndex ];
						while ( lastPointIndex < pointIndex )
							yPoints[ ++lastPointIndex ] = actionsCount;
						if ( eapm ) {
							final int actionsCountEapm = yPointsEapm[ lastPointIndexEapm ];
							while ( lastPointIndexEapm < pointIndex )
								yPointsEapm[ ++lastPointIndexEapm ] = actionsCountEapm;
						}
					}
					yPoints[ pointIndex ]++;
					if ( eapm && EapmUtil.isActionEffective( allActions, ai, action ) ) {
						yPointsEapm[ pointIndex ]++;
						if ( action.iteration > ReplayHeader.FRAMES_IN_TWO_MINUTES )
							effectiveActionsCounts[ i ]++;
					}
				} catch ( final ArrayIndexOutOfBoundsException aioobe ) {
					// The last few actions might be over the last domain, we ignore them.
				}
		}
		
		// Next calculate max actions (this does not include/effect eapm which cannot be higher than apm)
		final int[] maxActionss = new int[ chartsParams.playersCount ];
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final int[] yPoints     = yPointss[ i ];
			final int[] yPointsEapm = eapm ? yPointssEapm[ i ] : null;
			int         maxActions  = maxActionss[ i ]; 
			for ( pointIndex = yPoints.length - 1; pointIndex > 0; pointIndex-- ) {
				final int actionsInDomain = overall ? yPoints[ pointIndex ] /= pointIndex : yPoints[ pointIndex ];
				if ( overall && eapm )
					yPointsEapm[ pointIndex ] /= pointIndex;
				if ( maxActions < actionsInDomain )
					maxActions = actionsInDomain;
			}
			
			if ( chartsParams.allPlayersOnOneChart ) { // If all players on one chart, we have a global maxActions
				for ( int j = 0; j < maxActionss.length; j++ )
					if ( maxActions > maxActionss[ j ] )
						maxActionss[ j ] = maxActions;
			}
			else
				maxActionss[ i ] = maxActions;
		}
		
		// Normalize charts to their heights
		final int maxY = chartsParams.maxYInChart;
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final int[] yPoints     = yPointss[ i ];
			final int[] yPointsEapm = eapm ? yPointssEapm[ i ] : null;
			final int   maxActions  = maxActionss[ i ];
			final int   y1          = chartsParams.getY1ForChart( i );
			if ( maxActions > 0 ) {
				for ( pointIndex = yPoints.length - 1; pointIndex > 0; pointIndex-- ) {
					yPoints[ pointIndex ] = y1 + maxY - yPoints[ pointIndex ] * maxY / maxActions;
					if ( eapm )
						yPointsEapm[ pointIndex ] = y1 + maxY - yPointsEapm[ pointIndex ] * maxY / maxActions;
				}
				// Chart should not start from zero, we "double" the first point:
				yPoints[ 0 ] = yPoints[ 1 ];
				if ( eapm )
					yPointsEapm[ 0 ] = yPointsEapm[ 1 ];
			}
			else {
				// No actions, we cannot divide by zero, just fill with maxY
				Arrays.fill( yPoints    , y1 + maxY );
				Arrays.fill( yPointsEapm, y1 + maxY );
			}
		}
		
		// Finally draw the axis, labels, player descriptions and charts
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final PlayerActions playerActions = replay.replayActions.players[ playerIndexToShowList.get( i ) ];
			final int[]         yPoints       = yPointss[ i ];
			final int[]         yPointsEapm   = eapm ? yPointssEapm[ i ] : null;
			final int           y1            = chartsParams.getY1ForChart( i );
			final Color         inGameColor   = getPlayerInGameColor( playerActions );
			
			drawAxisAndTimeLabels( graphics, i );
			
			// Draw assist lines
			if ( !chartsParams.allPlayersOnOneChart || i == 0 ) { // We draw assist lines once if all players are on one chart
				graphics.setFont( CHART_AXIS_LABEL_FONT );
				// If no actions, let's define axis labels from zero to ASSIST_LINES_COUNT
				for ( int j = 1; j <= ASSIST_LINES_COUNT; j++ ) {
					final int y = y1 + chartsParams.maxYInChart - ( chartsParams.maxYInChart * j / ASSIST_LINES_COUNT );
					graphics.setColor( CHART_ASSIST_LINES_COLOR );
					graphics.drawLine( chartsParams.x1 + 1, y, chartsParams.x1 + chartsParams.maxXInChart, y );
				}
			}
			
			// Draw the charts
			final Color chartColor = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
			// Draw eapm first if we have 
			if ( eapm ) {
				graphics.setColor( getBrighterColor( chartColor ) );
				graphics.drawPolyline( xPoints, yPointsEapm, xPoints.length - 1 ); // Last point is excluded, it might not be a whole domain
			}
			// Now the apm
			( (Graphics2D) graphics ).setStroke( STROKE_DOUBLE );
			graphics.setColor( chartColor );
			graphics.drawPolyline( xPoints, yPoints, xPoints.length - 1 ); // Last point is excluded, it might not be a whole domain
			( (Graphics2D) graphics ).setStroke( STROKE_NORMAL );
			
			// Mark hack occurences
			if ( hackDescriptionList != null ) {
				graphics.setColor( chartColor.getRed() > 200 && chartColor.getGreen() < 100 && chartColor.getBlue() < 100 ? CHART_HACK_COLOR2 : CHART_HACK_COLOR );
				graphics.setFont( HACK_MARKER_FONT );
				for ( final HackDescription hackDescription : hackDescriptionList )
					if ( hackDescription.playerName.equalsIgnoreCase( playerActions.playerName ) ) {
						pointIndex = hackDescription.iteration * chartPoints / chartsParams.frames;
						if ( pointIndex == chartPoints )
							pointIndex = chartPoints - 1;
						final float position = (float) ( hackDescription.iteration - pointIndex * chartsParams.frames / chartPoints ) * chartPoints / chartsParams.frames;
						graphics.drawString( "!",
								interpolate( xPoints[ pointIndex ], xPoints[ pointIndex + 1 ], position ) - 4,
								interpolate( yPoints[ pointIndex ], yPoints[ pointIndex + 1 ], position ) - 4 );
					}
			}
			
			String eapmString = null;
			if ( eapm ) {
				final ReplayHeader replayHeader = replay.replayHeader;
				final int playerIndex = replayHeader.getPlayerIndexByName( playerActions.playerName );
				final int playerActionsCount = replayHeader.playerIdActionsCounts[ replayHeader.playerIds[ playerIndex ] ] - replayHeader.playerIdActionsCountBefore2Mins[ replayHeader.playerIds[ playerIndex ] ];
				eapmString = "EAPM: " + replayHeader.getPlayerApmForActionsCount( playerIndex, effectiveActionsCounts[ i ] ) + ", redundancy: ";
				if ( playerActionsCount > 0 )
					eapmString += 100 * ( playerActionsCount - effectiveActionsCounts[ i ] ) / playerActionsCount + "%";
				else
					eapmString += "0%";
			}
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor, eapmString );
			
			// Draw APM axis labels
			if ( !chartsParams.allPlayersOnOneChart || i == 0 ) { // We draw labels once if all players are on one chart
				graphics.setFont( CHART_AXIS_LABEL_FONT );
				// If no actions, let's define axis labels from zero to ASSIST_LINES_COUNT
				final int maxApm = maxActionss[ i ] > 0 ? maxActionss[ i ] * ( chartPoints - 1 ) * 60 / Math.max( ReplayHeader.convertFramesToSeconds( chartsParams.frames ), 1 ) : ASSIST_LINES_COUNT;
				for ( int j = 0; j <= ASSIST_LINES_COUNT; j++ ) {
					final int y   = y1 + chartsParams.maxYInChart - ( chartsParams.maxYInChart * j / ASSIST_LINES_COUNT );
					final int apm = maxApm * j / ASSIST_LINES_COUNT;
					graphics.setColor( CHART_BACKGROUND_COLOR );
					graphics.fillRect( chartsParams.dx + 1, y - 4, 20, 9 );
					graphics.setColor( CHART_AXIS_LABEL_COLOR );
					graphics.drawString( ( apm < 100 ? ( apm < 10 ? "  " : " " ) : "" ) + apm, chartsParams.dx + 1, y + 4 );
				}
			}
		}
	}
	
	/**
	 * Interpolates a value between <code>x1</code> and <code>x2</code>.
	 * Position is defined by <code>position</code> between <code>x1</code> and <code>x2</code> with a range of 0..1.
	 * @param x1       lowest value of interpolation
	 * @param x2       highest value of interpolation
	 * @param position position between the domain, a value between 0.0 and 1.0
	 * @return an interpolated value between the limits
	 */
	private int interpolate( final int x1, final int x2, final float position ) {
		return x1 + (int) ( ( x2 - x1 ) * position );
	}
	
	/**
	 * Paints the hotkeys charts of the players.
	 * @param graphics     graphics to be used for painting
	 */
	private void paintHotkeysCharts( final Graphics graphics ) {
		final boolean showSelectHotkeys = showSelectHotkeysCheckBox.isSelected();
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final PlayerActions playerActions = replay.replayActions.players[ playerIndexToShowList.get( i ) ];
			final int           y1            = chartsParams.getY1ForChart( i );
			final Color         inGameColor   = getPlayerInGameColor( playerActions );
			final Color         chartColor    = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
			
			drawAxisAndTimeLabels( graphics, i );
			
			graphics.setFont( CHART_PART_TEXT_FONT );
			
			for ( final Action action : playerActions.actions ) {
				final Color chartColor2 = new Color( ~chartColor.getRed() & 0xff, ~chartColor.getGreen() & 0xff, ~chartColor.getBlue() & 0xff );
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY ) {
					final String[] params         = action.parameters.split( "," );
					final boolean  isHotkeyAssign = params[ 0 ].equals( Action.HOTKEY_ACTION_PARAM_NAME_ASSIGN );
					
					if ( isHotkeyAssign || showSelectHotkeys ) {
						final int hotkey = Integer.parseInt( params[ 1 ] ) % 10; // Hotkey 10 is displayed where hotkey 0
						
						if ( isHotkeyAssign ) {
							graphics.setColor( chartColor );
							graphics.fillRect( chartsParams.getXForIteration( action.iteration ) - 1, y1 + hotkey * ( chartsParams.maxYInChart - 14 ) / 9, 10, 13 );
							graphics.setColor( chartColor2 );
						}
						else {
							( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
							graphics.setColor( chartColor );
						}
						
						graphics.drawString( params[ 1 ],
								chartsParams.getXForIteration( action.iteration ),
								y1 + hotkey * ( chartsParams.maxYInChart - 14 ) / 9 + 10 );
					}
				}
			}
			( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor, null );
		}
	}
	
	/**
	 * Paints the build order charts of the players.
	 * @param graphics     graphics to be used for painting
	 */
	private void paintBuildOrderCharts( final Graphics graphics ) {
		final boolean     showUnits               = showUnitsOnBuildOrderCheckBox.isSelected();
		final int         buildOrderDisplayLevels = (Integer) buildOrderDisplayLevelComboBox.getSelectedItem() - 1;
		final boolean     hideWorkerUnits         = hideWorkerUnitsCheckBox.isSelected();
		final FontMetrics fontMetrics             = graphics.getFontMetrics( CHART_PART_TEXT_FONT );
		
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final int           playerIndex   = playerIndexToShowList.get( i );
			final PlayerActions playerActions = replay.replayActions.players[ playerIndex ];
			final int           y1            = chartsParams.getY1ForChart( i );
			final int           y2            = y1 + chartsParams.maxYInChart - 1;
			final Color         inGameColor   = getPlayerInGameColor( playerActions );
			final Color         chartColor    = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
			
			drawAxisAndTimeLabels( graphics, i );
			
			graphics.setFont( CHART_PART_TEXT_FONT );
			
			int buildOrderLevel = chartsParams.allPlayersOnOneChart ? buildOrderDisplayLevels - i * 2 : buildOrderDisplayLevels;
			while ( buildOrderLevel < 0 )
				buildOrderLevel += buildOrderDisplayLevels;
			for ( final Action action : playerActions.actions ) {
				graphics.setColor( chartColor );
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD
				  || showUnits && ( action.actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN || action.actionNameIndex == Action.ACTION_NAME_INDEX_HATCH ) ) {
					final short unitId = action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD ? action.parameterBuildingNameIndex : action.parameterUnitNameIndex;
					if ( !( hideWorkerUnits && ( unitId == Action.UNIT_NAME_INDEX_DRONE || unitId == Action.UNIT_NAME_INDEX_PROBE || unitId == Action.UNIT_NAME_INDEX_SCV ) ) ) {
						String buildingName = Action.UNIT_ID_NAME_MAP.get( unitId );
						buildingName = buildingName == null ? "" : buildingName;
						
						final int x = chartsParams.getXForIteration( action.iteration );
						final int y = y1 + 14 + buildOrderLevel * ( chartsParams.maxYInChart - 23 ) / buildOrderDisplayLevels;
						
						graphics.drawLine( x, y, x, y2 );
						graphics.drawString( buildingName, x - fontMetrics.stringWidth( buildingName ) / 2 + 2, y - 3 );
						
						if ( --buildOrderLevel < 0 )
							buildOrderLevel = buildOrderDisplayLevels;
					}
				}
			}
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor, null );
		}
	}
	
	/**
	 * Paints the strategy charts of the players.
	 * @param graphics graphics to be used for painting
	 */
	private void paintStrategyCharts( final Graphics graphics ) {
		final int         strategyDisplayLevels = (Integer) strategyDisplayLevelComboBox.getSelectedItem() - 1;
		final FontMetrics fontMetrics           = graphics.getFontMetrics( CHART_PART_TEXT_FONT );
		
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final int           playerIndex   = playerIndexToShowList.get( i );
			final PlayerActions playerActions = replay.replayActions.players[ playerIndex ];
			final int           y1            = chartsParams.getY1ForChart( i );
			final int           y2            = y1 + chartsParams.maxYInChart - 1;
			final Color         inGameColor   = getPlayerInGameColor( playerActions );
			final Color         chartColor    = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
			
			drawAxisAndTimeLabels( graphics, i );
			
			graphics.setFont( CHART_PART_TEXT_FONT );
			
			int strategyLevel = chartsParams.allPlayersOnOneChart ? strategyDisplayLevels - i * 2 : strategyDisplayLevels;
			while ( strategyLevel < 0 )
				strategyLevel += strategyDisplayLevels + 1;
			for ( final Action action : playerActions.actions ) {
				graphics.setColor( chartColor );
				String strategyName = null;
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_UNLOAD_ALL || action.actionNameIndex == Action.ACTION_NAME_INDEX_UNLOAD || action.subactionNameIndex == Action.SUBACTION_NAME_INDEX_UNLOAD )
					strategyName = "Drop";
				else if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD ) {
					if ( action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_HATCHERY || action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_NEXUS || action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_COMMAND_CENTER )
						strategyName = "Expand";
					else if ( action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_BUNKER || action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_PHOTON_CANNON )
						strategyName = "Defense";
					else if ( action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_NYDUS_CANAL )
						strategyName = "Nydus";
				}
				else if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_MORPH && action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_SUNKEN_COLONY )
					strategyName = "Defense";
				else if ( action.subactionNameIndex == Action.SUBACTION_NAME_INDEX_RECALL )
					strategyName = "Recall";
				else if ( action.subactionNameIndex == Action.SUBACTION_NAME_INDEX_LAUNCH_NUKE )
					strategyName = "Nuke";
				
				if ( strategyName != null ) {
					final int x = chartsParams.getXForIteration( action.iteration );
					final int y = y1 + 14 + strategyLevel * ( chartsParams.maxYInChart - 23 ) / strategyDisplayLevels;
					
					graphics.drawLine( x, y, x, y2 );
					graphics.drawString( strategyName, x - fontMetrics.stringWidth( strategyName ) / 2 + 2, y - 3 );
					
					if ( --strategyLevel < 0 )
						strategyLevel = strategyDisplayLevels;
				}
			}
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor, null );
		}
	}
	
	/**
	 * Paints the Action Sequences charts of the players.
	 * @param graphics graphics to be used for painting
	 */
	@SuppressWarnings("unchecked")
	private void paintActionSequencesCharts( final Graphics graphics ) {
		final boolean hideNonHotkeySequences = hideNonHotkeySequencesCheckBox.isSelected();
		final int     maxActionDelay         = (Integer) maxFramesDelayInSequenceComboBox.getSelectedItem();
		
		graphics.setFont( CHART_MAIN_FONT );
		graphics.setColor( CHART_AXIS_COLOR );
		graphics.drawString( "Pairs/sec", chartsParams.dx + 1, 10 );
		
		// First identify Action Sequences
		final List< int[] >[] actionSequenceLists = new List[ chartsParams.playersCount ]; // An action sequence has 4 parameters: its first frame; its last frame; pairs count, and whether it is hotkey sequence (0) or not (1) 
		final float[] maxValues     = new float[ chartsParams.playersCount ]; // We calculate max values for normalization in one step 
		final int[]   summaPairs    = new int[ chartsParams.playersCount ];   // For the average pairs/sec 
		final int[]   summaDuration = new int[ chartsParams.playersCount ];   // For the average pairs/sec 
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final List< int[] > actionSequenceList = actionSequenceLists[ i ] = new ArrayList< int[] >();
			
			final int      playerIndex = playerIndexToShowList.get( i );
			final Action[] actions     = replay.replayActions.players[ playerIndex ].actions;
			
			final int maxActionIndex = actions.length - 4;
			for ( int actionIndex = 0; actionIndex < maxActionIndex; actionIndex++ ) {
				final Action  action            = actions[ actionIndex ];
				final boolean isSelect          = action.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT;
				final boolean isHotkeySelect    = action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.startsWith( "Select" );
				final Action  action2           = actions[ actionIndex + 1 ];
				final boolean isSelectOrHotkey2 = action2.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT || action2.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY;
				
				if ( ( !hideNonHotkeySequences && isSelect || isHotkeySelect ) && !isSelectOrHotkey2 && action2.iteration - action.iteration <= maxActionDelay ) {
					final int  sequenceStart       = action.iteration;
					final byte command             = action2.actionNameIndex;
					int        pairsCount          = 1;
					int        prevActionIteration = action2.iteration;
					
					do {
						if ( actionIndex + 3 >= actions.length )
							break;
						actionIndex += 2;
						final Action action3 = actions[ actionIndex ];
						if ( action3.iteration - prevActionIteration > maxActionDelay )
							break;
						
						final boolean isSelect3       = action3.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT;
						final boolean isHotkeySelect3 = action3.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action3.parameters.startsWith( "Select" );
						final Action  action4         = actions[ actionIndex + 1 ];
						
						if ( ( !hideNonHotkeySequences && isSelect3 || isHotkeySelect3 ) && ( isSelect == isSelect3 || isHotkeySelect == isHotkeySelect3 ) && action4.actionNameIndex == command && action4.iteration - action3.iteration <= maxActionDelay ) {
							pairsCount++;
							prevActionIteration = action4.iteration;
						}
						else
							break;
					} while ( true );
					
					if ( pairsCount > 1 ) {
						final int sequenceDuration = ( prevActionIteration == sequenceStart ? 2 : prevActionIteration - sequenceStart );
						actionSequenceList.add( new int[] { sequenceStart, prevActionIteration, pairsCount, isSelect ? 1 : 0 } );
						summaPairs   [ i ] += pairsCount;
						summaDuration[ i ] += sequenceDuration;
						final float pairsPerSec =  pairsCount * 1000f / ( sequenceDuration * 42f );
						if ( maxValues[ i ] < pairsPerSec )
							maxValues[ i ] = pairsPerSec;
					}
					actionIndex--;
				}
			}
		}
		
		if ( chartsParams.allPlayersOnOneChart ) { // If all players are on one chart, we have a global maximum
			float globalMaxValue = 0;
			for ( int i = 0; i < chartsParams.playersCount; i++ )
				if ( globalMaxValue < maxValues[ i ] )
					globalMaxValue = maxValues[ i ];
			Arrays.fill( maxValues, globalMaxValue );
		}
		
		// Now draw the Axis, labels and charts
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final int           playerIndex   = playerIndexToShowList.get( i );
			final PlayerActions playerActions = replay.replayActions.players[ playerIndex ];
			final int           y1            = chartsParams.getY1ForChart( i );
			final int           y2            = y1 + chartsParams.maxYInChart - 1;
			final Color         inGameColor   = getPlayerInGameColor( playerActions );
			final Color         chartColor    = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
			
			drawAxisAndTimeLabels( graphics, i );
			
			// Draw assist lines
			if ( !chartsParams.allPlayersOnOneChart || i == 0 ) { // We draw assist lines once if all players are on one chart
				graphics.setFont( CHART_AXIS_LABEL_FONT );
				for ( int j = 1; j <= ASSIST_LINES_COUNT; j++ ) {
					final int y = y1 + chartsParams.maxYInChart - ( chartsParams.maxYInChart * j / ASSIST_LINES_COUNT );
					graphics.setColor( CHART_ASSIST_LINES_COLOR );
					graphics.drawLine( chartsParams.x1 + 1, y, chartsParams.x1 + chartsParams.maxXInChart, y );
				}
			}
			
			// Now draw the bars of the action sequences
			// If no action sequences, let's define axis labels from zero to ASSIST_LINES_COUNT
			final float maxValue = maxValues[ i ] > 0 ? maxValues[ i ] : ASSIST_LINES_COUNT;
			for ( final int[] actionSequence : actionSequenceLists[ i ] ) {
				final int x1               = chartsParams.getXForIteration( actionSequence[ 0 ] );
				final int x2               = chartsParams.getXForIteration( actionSequence[ 1 ] );
				final int sequenceDuration = ( actionSequence[ 1 ] == actionSequence[ 0 ] ? 2 : actionSequence[ 1 ] - actionSequence[ 0 ] );
				final int height           = (int) ( actionSequence[ 2 ] * 1000f * chartsParams.chartHeight / ( sequenceDuration * 42f * maxValue ) );
				graphics.setColor( actionSequence[ 3 ] == 0 ? chartColor : getBrighterColor( chartColor ) );
				graphics.fillRect( x1, y2 - height + 1, x2 > x1 ? x2 - x1 : 1, height );
			}
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor, new Formatter( Locale.ENGLISH ).format( "Avg %.1f pairs/sec", summaDuration[ i ] > 0 ? summaPairs[ i ] * 1000f / ( summaDuration[ i ] * 42f ) : 0f ).toString() );
			
			// Draw Pairs/sec axis labels
			if ( !chartsParams.allPlayersOnOneChart || i == 0 ) { // We draw labels once if all players are on one chart
				graphics.setFont( CHART_AXIS_LABEL_FONT );
				for ( int j = 0; j <= ASSIST_LINES_COUNT; j++ ) {
					final int   y     = y1 + chartsParams.maxYInChart - ( chartsParams.maxYInChart * j / ASSIST_LINES_COUNT );
					final float value = maxValue * j / ASSIST_LINES_COUNT;
					graphics.setColor( CHART_BACKGROUND_COLOR );
					graphics.fillRect( chartsParams.dx + 1, y - 4, 20, 9 );
					graphics.setColor( CHART_AXIS_LABEL_COLOR );
					graphics.drawString( new Formatter( Locale.ENGLISH ).format( "%.1f", value ).toString(), chartsParams.dx + 1, y + 3 );
				}
			}
			
		}
	}
	
	/**
	 * Paints the Map view charts of the players.
	 * @param graphics graphics to be used for painting
	 */
	private void paintMapViewCharts( final Graphics graphics ) {
		final MapData mapData = replay.mapData;
		final short[] tiles = mapData == null ? null : mapData.tiles;
		
		if ( tiles != null ) {
			final int zoom = MapImagesManager.TILE_IMAGE_WIDTH * chartsParams.zoom / ChartsTab.MAX_ZOOM; // At max zoom tiles are shown in real size
			
			final int mapWidth  = replay.replayHeader.mapWidth;
			final int mapHeight = replay.replayHeader.mapHeight;
			
			if ( replayMapViewZoom != zoom ) {
				
				replayMapViewImage = new BufferedImage( mapWidth * zoom, mapHeight * zoom, BufferedImage.TYPE_INT_RGB );
				final Graphics2D      cacheGraphics       = replayMapViewImage.createGraphics();
				final int             tileSet             = mapData.tileSet < 0 ? 0 : mapData.tileSet & 0x07;
				final BufferedImage[] tileSetScaledImages = MapImagesManager.getTileSetScaledImages( tileSet, zoom );
				
				for ( int y = 0; y < mapHeight; y++ ) {
					final int rowStartPos = y * mapWidth;
					if ( rowStartPos + mapWidth  <= tiles.length ) // If we have the whole line
						for ( int x = 0; x < mapWidth; x++ ) {
							final short tile = tiles[ rowStartPos + x ];
							final int borderConfig = tile >> 9 & 0x1f;
							final int pixelX = x * zoom;
							final int pixelY = y * zoom;
							
							// TODO: handle borderconfig to draw border between solid tiles
							if ( borderConfig == 0 )
								cacheGraphics.drawImage( tileSetScaledImages[ tile >> 5 & 0x0f ], pixelX, pixelY, null ); // Solid tile
							else {
								cacheGraphics.drawImage( tileSetScaledImages[ borderConfig & 0x0f ], pixelX, pixelY, null ); // Border
								final BufferedImage borderImage = tileSetScaledImages[ ( (borderConfig & 0x0f)+1 )&0x0f ]; // Usually the border is between [borderCofing] and [borderConfig+1] tiles
								
								for ( int i = zoom - 1; i >= 0; i -= 2 )
									cacheGraphics.drawImage( borderImage, pixelX, pixelY + i, pixelX + zoom, pixelY + i + 1, 0, i, zoom, i + 1, null );
							}
						}
				}
				
				// Mineral fields
				cacheGraphics.setColor( new Color( 90, 90, 255 ) );
				for ( final short[] mineral : mapData.mineralFieldList )
					cacheGraphics.fillRect( ( mineral[ 0 ] -  MapImagesManager.TILE_IMAGE_WIDTH ) * zoom / MapImagesManager.TILE_IMAGE_WIDTH, ( mineral[ 1 ] -  MapImagesManager.TILE_IMAGE_HEIGHT ) * zoom / MapImagesManager.TILE_IMAGE_HEIGHT, 2*zoom, 2*zoom ); // Size of mineral fields are 2x2
				// Vespene geysers
				cacheGraphics.setColor( new Color( 20, 180, 20 ) );
				for ( final short[] geyser : mapData.geyserList )
					cacheGraphics.fillRect( ( geyser[ 0 ] - 2*MapImagesManager.TILE_IMAGE_WIDTH ) * zoom / MapImagesManager.TILE_IMAGE_WIDTH, ( geyser[ 1 ] -  MapImagesManager.TILE_IMAGE_HEIGHT ) * zoom / MapImagesManager.TILE_IMAGE_HEIGHT, 4*zoom, 2*zoom ); // Size of vespene geysers are 4x2
				
				replayMapViewZoom = zoom;
			}
			
			if ( replayMapViewImage != null )
				graphics.drawImage( replayMapViewImage, 0, 0, null );
			
			final boolean showPlayerNames         = showPlayerNamesOnMapCheckBox   .isSelected();
			final boolean showBuildingImages      = showBuildingImagesCheckBox     .isSelected();
			final boolean hideOverlappedBuildings = hideOverlappedBuildingsCheckBox.isSelected();
			
			final List< int[] > buildingList = hideOverlappedBuildings ? new ArrayList< int[] >() : null; // Building rectangles (in pixel coordinates) and the action index
			if ( hideOverlappedBuildings && selectedActionIndex >= 0 ) {
				// Show buildings up to this time
				final int maxActionIndex = Math.min( selectedActionIndex, actionList.size() );
				for ( int i = 0; i < maxActionIndex; i++ ) {
					final Object[] actionObjects = actionList.get( i );
					final Action   action        = (Action) actionObjects[ 0 ];
					if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD ) {
						final int[]  targetPoint = getActionTargetPoint( action, zoom );
						if ( targetPoint != null ) {
							final int x1 = targetPoint[ 0 ];
							final int y1 = targetPoint[ 1 ];
							
							final Size size = Action.BUILDING_ID_SIZE_MAP.get( action.parameterBuildingNameIndex );
							final int x2 = x1 + size.width  * zoom - 1;
							final int y2 = y1 + size.height * zoom - 1;
							
							for ( int j = 0; j < buildingList.size(); j++ ) {
								final int[] buildLoc = buildingList.get( j );
								if ( buildLoc[ 2 ] < x1 || buildLoc[ 0 ] > x2 || buildLoc[ 3 ] < y1 || buildLoc[ 1 ] > y2 )
									continue;
								else {
									buildingList.remove( j );
									j--;
								}
							}
							
							buildingList.add( new int[] { x1, y1, x2, y2, i } );
						}
					}
				}
			}
			
			// Show start locations
			for ( int i = 0; i < chartsParams.playersCount; i++ ) {
				final int           playerIndex_  = playerIndexToShowList.get( i );
				final PlayerActions playerActions = replay.replayActions.players[ playerIndex_ ];
				final int           playerIndex   = replay.replayHeader.getPlayerIndexByName( replay.replayActions.players[ playerIndex_ ].playerName );
				final Color         inGameColor   = getPlayerInGameColor( playerActions );
				final Color         chartColor    = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
				
				graphics.setColor( chartColor );
				
				final int   race           = replay.replayHeader.playerRaces[ playerIndex ] & 0xff;
				final short mainBuildingId = race >= 0 && race < RACE_MAIN_BUILDINGS.length ? RACE_MAIN_BUILDINGS[ race ] : Action.BUILDING_NAME_INDEX_NEXUS;
				final Size  size           = Action.BUILDING_ID_SIZE_MAP.get( mainBuildingId );
				startLocCycle:
				for ( final int[] loc : mapData.startLocationList )
					if ( loc[ 2 ] == playerIndex ) {
						final int x = ( loc[ 0 ] - size.width  * MapImagesManager.TILE_IMAGE_WIDTH  / 2 ) * zoom / MapImagesManager.TILE_IMAGE_WIDTH;
						final int y = ( loc[ 1 ] - size.height * MapImagesManager.TILE_IMAGE_HEIGHT / 2 ) * zoom / MapImagesManager.TILE_IMAGE_HEIGHT;
						
						if ( buildingList != null ) {
							final int x2 = x + size.width  * zoom - 1;
							final int y2 = y + size.height * zoom - 1;
							for ( int j = 0; j < buildingList.size(); j++ ) {
								final int[] buildLoc = buildingList.get( j );
								if ( buildLoc[ 2 ] < x || buildLoc[ 0 ] > x2 || buildLoc[ 3 ] < y || buildLoc[ 1 ] > y2 )
									continue;
								else
									continue startLocCycle;
							}
						}
						
						// We always paint rectangle, because even if we have to draw images, we would have to draw a rectangle. DrawRect is extremely slow in SwingWT, fillRect is much faster :S
						graphics.fillRect( x, y, size.width * zoom+1, size.height * zoom+1 );
						if ( showBuildingImages ) {
							final BufferedImage buildingImage = MapImagesManager.getBuildingScaledImage( race, mainBuildingId, zoom );
							if ( buildingImage != null )
								graphics.drawImage( buildingImage, x+1, y+1, null );
						}
						
						if ( showPlayerNames ) {
							final String playerName = replay.replayActions.players[ playerIndexToShowList.get( i ) ].playerName;
							graphics.setColor( chartColor );
							graphics.fillRect( loc[ 0 ] * zoom / MapImagesManager.TILE_IMAGE_WIDTH, loc[ 1 ] * zoom / MapImagesManager.TILE_IMAGE_HEIGHT, graphics.getFontMetrics().stringWidth( playerName ) + 2, graphics.getFontMetrics().getHeight() );
							graphics.setColor( Color.BLACK );
							graphics.drawString( playerName, loc[ 0 ] * zoom / MapImagesManager.TILE_IMAGE_WIDTH + 1, loc[ 1 ] * zoom / MapImagesManager.TILE_IMAGE_HEIGHT + 11 );
						}
						break;
					}
			}
			
			// Show game state up to the selection
			if ( selectedActionIndex >= 0 ) {
				// Show buildings up to this time
				final int maxActionIndex = Math.min( selectedActionIndex, actionList.size() );
				actionCycle:
				for ( int i = 0; i < maxActionIndex; i++ ) {
					final Object[] actionObjects = actionList.get( i );
					final Action   action        = (Action) actionObjects[ 0 ];
					if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD ) {
						final int[]  targetPoint = getActionTargetPoint( action, zoom );
						if ( targetPoint != null ) {
							final int x = targetPoint[ 0 ];
							final int y = targetPoint[ 1 ];
							
							final Size size = Action.BUILDING_ID_SIZE_MAP.get( action.parameterBuildingNameIndex );
							
							if ( buildingList != null ) {
								final int x2 = x + size.width  * zoom - 1;
								final int y2 = y + size.height * zoom - 1;
								for ( int j = 0; j < buildingList.size(); j++ ) {
									final int[] buildLoc = buildingList.get( j );
									if ( buildLoc[ 4 ] == i || ( buildLoc[ 2 ] < x || buildLoc[ 0 ] > x2 || buildLoc[ 3 ] < y || buildLoc[ 1 ] > y2 ) )
										continue;
									else
										continue actionCycle;
								}
							}
							
							final int playerIndex = replay.replayHeader.getPlayerIndexByName( (String) actionObjects[ 1 ] );
							
							Color playerColor = null;
							try {
								playerColor = chartsTab.usePlayersColorsCheckBox.isSelected() ? IN_GAME_COLORS[ replay.replayHeader.playerColors[ playerIndex ] ] : CHART_DEFAULT_COLOR;
							}
							catch ( final Exception e ) {
								playerColor = CHART_DEFAULT_COLOR;
							}
							graphics.setColor( playerColor );
							
							// We always paint rectangle, because even if we have to draw images, we would have to draw a border rectangle. DrawRect is extremely slow in SwingWT, fillRect is much faster :S
							graphics.fillRect( x, y, size.width * zoom+1, size.height * zoom+1 );
							if ( showBuildingImages ) {
								final int race = replay.replayHeader.playerRaces[ playerIndex ] & 0xff;
								final BufferedImage buildingImage = MapImagesManager.getBuildingScaledImage( race, action.parameterBuildingNameIndex, zoom );
								if ( buildingImage != null )
									graphics.drawImage( buildingImage, x+1, y+1, null );
							}
						}
					}
				}
				
				// And the current action's target point
				try { // To avoid the hassle with string -> int parsing
					if ( selectedActionIndex < actionList.size() ) {
						final Action action      = (Action) actionList.get( selectedActionIndex )[ 0 ];
						final int[]  targetPoint = getActionTargetPoint( action, zoom );
						if ( targetPoint != null ) {
							final int x = targetPoint[ 0 ];
							final int y = targetPoint[ 1 ];
							if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD ) {
								graphics.setColor( new Color( 255, 50, 50 ) );
								( (Graphics2D) graphics ).setStroke( STROKE_DOUBLE );
								final Size size = Action.BUILDING_ID_SIZE_MAP.get( action.parameterBuildingNameIndex );
								graphics.drawRect( x, y, size.width * zoom, size.height * zoom );
								( (Graphics2D) graphics ).setStroke( STROKE_NORMAL );
								graphics.setColor( Color.WHITE );
								graphics.drawRect( x+1, y+1, size.width * zoom-3, size.height * zoom-3 );
							}
							else {
								graphics.setColor( new Color( 255, 50, 50 ) );
								( (Graphics2D) graphics ).setStroke( STROKE_DOUBLE );
								graphics.drawLine( x - zoom, y - zoom, x + zoom, y + zoom );
								graphics.drawLine( x - zoom, y + zoom, x + zoom, y - zoom );
								graphics.setColor( Color.WHITE );
								graphics.drawOval( x - 3*zoom/2, y - 3*zoom/2, 3*zoom, 3*zoom );
								( (Graphics2D) graphics ).setStroke( STROKE_NORMAL );
							}
						}
					}
				}
				catch ( final Exception e ) {
					e.printStackTrace();
				}
			}
			
			if ( chartsParams.zoom == 1 ) {
				graphics.setColor( CHART_BACKGROUND_COLOR );
				graphics.fillRect( -5, -5, 1, 1 ); // To set the background color...
				graphics.setColor( new Color( 200, 200, 200 ) );
				graphics.drawString( "Tip: try zooming this chart!", mapWidth * zoom + 5, 10 );
			}
		}
		else {
			graphics.setColor( CHART_DEFAULT_COLOR );
			graphics.drawString( "A Map view is not available for this replay!", chartsParams.dx, chartsParams.dy );
		}
	}
	
	/**
	 * Returns the target point of an action in pixel coordinates.
	 * @param action action whose target is to be returned
	 * @param zoom   zoom value to be used for coordinate calculation
	 * @return the target point of an action in pixel coordinates
	 */
	private static int[] getActionTargetPoint( final Action action, final int zoom ) {
		if ( Action.ACTION_NAME_INDICES_WITH_POINT_TARGET_SET.contains( action.actionNameIndex ) ) {
			final StringTokenizer paramsTokenizer = new StringTokenizer( action.parameters, "," );
			if ( paramsTokenizer.countTokens() == 2 ) {
				final int x = Integer.parseInt( paramsTokenizer.nextToken() ) * zoom / MapImagesManager.TILE_IMAGE_WIDTH;
				final int y = Integer.parseInt( paramsTokenizer.nextToken() ) * zoom / MapImagesManager.TILE_IMAGE_HEIGHT;
				return new int[] { x, y };
			}
		}
		else if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD ) {
			final StringTokenizer paramsTokenizer = new StringTokenizer( action.parameters.substring( action.parameters.indexOf( '(' ) + 1, action.parameters.indexOf( ')' ) ), "," );
			if ( paramsTokenizer.countTokens() == 2 ) {
				final int x = Integer.parseInt( paramsTokenizer.nextToken() ) * zoom;
				final int y = Integer.parseInt( paramsTokenizer.nextToken() ) * zoom;
				return new int[] { x, y };
			}
		}
		
		return null;
	}
	
	/**
	 * Determines the specified player's in-game color 
	 * @param playerActions player actions of the player being queried
	 * @return the specified player's in-game color (returns <code>null</code> if it's not handled)
	 */
	private Color getPlayerInGameColor( final PlayerActions playerActions ) {
		Color inGameColor = null;
		
		if ( chartsTab.allPlayersOnOneChartCheckBox.isSelected() || chartsTab.usePlayersColorsCheckBox.isSelected() )
			try {
				final int headerPlayerIndex = replay.replayHeader.getPlayerIndexByName( playerActions.playerName );
				inGameColor = IN_GAME_COLORS[ replay.replayHeader.playerColors[ headerPlayerIndex ] ];
			}
			catch ( final Exception e ) {
			}
		
		return inGameColor;
	}
	
	/**
	 * Draws the axis and time labels.
	 * @param graphics     graphics to be used for painting
	 * @param chartIndex   index of chart being queried
	 */
	private void drawAxisAndTimeLabels( final Graphics graphics, final int chartIndex ) {
		if ( !chartsParams.allPlayersOnOneChart || chartIndex == 0 ) { // We draw axis and labels once if all players are on one chart
			final int frames = replay.replayHeader.gameFrames;
			final int y1     = chartsParams.getY1ForChart( chartIndex );
			
			// Draw the axis
			graphics.setColor( CHART_AXIS_COLOR );
			graphics.drawLine( chartsParams.x1, y1, chartsParams.x1, y1 + chartsParams.chartHeight );
			graphics.drawLine( chartsParams.x1, y1 + chartsParams.maxYInChart, chartsParams.x1 + chartsParams.maxXInChart, y1 + chartsParams.maxYInChart );
			
			// Draw time axis labels
			graphics.setColor( CHART_AXIS_LABEL_COLOR );
			graphics.setFont( CHART_AXIS_LABEL_FONT );
			final int labelsCount = TIME_LABELS_COUNT * chartsParams.zoom;
			for ( int j = 0; j <= labelsCount; j++ ) {
				final String timeString = ReplayHeader.formatFrames( frames * j / labelsCount, new StringBuilder(), false ).toString();
				final int x = chartsParams.x1 + ( chartsParams.maxXInChart * j / labelsCount )
								- ( j == 0 ? 0 : ( j == labelsCount ? timeString.length() * 7 : timeString.length() * 7 / 2 ) );
				graphics.drawString( timeString, x, y1 + chartsParams.maxYInChart + 11 );
			}
		}
	}
	
	/**
	 * Draws a player's name and description.
	 * @param graphics     graphics to be used for painting
	 * @param chartsParams parameters of the charts to be drawn
	 * @param chartIndex   index of chart being queried
	 * @param inGameColor  in-game color of the player
	 * @param extraInfo    extra info to be displayed; can be <code>null</code>
	 */
	private void drawPlayerDescription( final Graphics graphics, final ChartsParams chartsParams, final int chartIndex, final Color inGameColor, final String extraInfo ) {
		graphics.setFont( CHART_MAIN_FONT );
		graphics.setColor( inGameColor == null ? CHART_PLAYER_DESCRIPTION_COLOR : inGameColor );
		final String description = replay.replayHeader.getPlayerDescription( replay.replayActions.players[ playerIndexToShowList.get( chartIndex ) ].playerName );
		graphics.drawString( extraInfo == null ? description : description + ", " + extraInfo,
				             chartsParams.x1 + 2,
				             chartsParams.getY1ForChart( chartIndex ) + ( chartsParams.allPlayersOnOneChart ? chartIndex * 14 - 11 : -1 ) );
	}
	
	/**
	 * Creates and returns a brighter version of the specified color.<br>
	 * If the color is white, it returns a bright green color.
	 * @param color color whose brighter version to be returned
	 * @return a brighter version of the specified color
	 */
	private static Color getBrighterColor( final Color color ) {
		if ( color.getRed() + color.getGreen() + color.getBlue() > 600 )
			return new Color( 100, 255, 100 );
		else
			return new Color( Math.min( 255, color.getRed() + 50 ), Math.min( 255, color.getGreen() + 50 ), Math.min( 255, color.getBlue() + 50 ) ).brighter().brighter();
		
	}
	
	/**
	 * Assigns the used properties of this component.
	 */
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_APM_CHART_DETAIL_LEVEL        , Integer.toString( apmChartDetailLevelComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_EAPM                     , Boolean.toString( showEapmCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_SELECT_HOTKEYS           , Boolean.toString( showSelectHotkeysCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_BUILD_ORDER_DISPLAY_LEVELS    , Integer.toString( buildOrderDisplayLevelComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_UNITS_ON_BUILD_ORDER     , Boolean.toString( showUnitsOnBuildOrderCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HIDE_WORKER_UNITS             , Boolean.toString( hideWorkerUnitsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_STRATEGY_DISPLAY_LEVELS       , Integer.toString( strategyDisplayLevelComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_OVERALL_APM_CHART_DETAIL_LEVEL, Integer.toString( overallApmChartDetailLevelComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_OVERALL_EAPM             , Boolean.toString( showOverallEapmCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HIDE_NON_HOTKEY_SEQUENCES     , Boolean.toString( hideNonHotkeySequencesCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_MAX_FRAME_DELAY_IN_SEQUENCES  , Integer.toString( maxFramesDelayInSequenceComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_PLAYER_NAMES_ON_MAP      , Boolean.toString( showPlayerNamesOnMapCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_BUILDING_IMAGES          , Boolean.toString( showBuildingImagesCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HIDE_OVERLAPPED_BUILDINGS     , Boolean.toString( hideOverlappedBuildingsCheckBox.isSelected() ) );
	}
	
}
