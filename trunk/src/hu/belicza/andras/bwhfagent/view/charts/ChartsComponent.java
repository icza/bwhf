package hu.belicza.andras.bwhfagent.view.charts;

import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.ChartsTab;
import hu.belicza.andras.bwhfagent.view.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import swingwt.awt.BasicStroke;
import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.Dimension;
import swingwt.awt.Font;
import swingwt.awt.FontMetrics;
import swingwt.awt.Graphics;
import swingwt.awt.Graphics2D;
import swingwt.awt.Stroke;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.event.ChangeEvent;
import swingwtx.swing.event.ChangeListener;

/**
 * Component to visialize charts.
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
	private static final Color  CHART_ASSIST_LINES_COLOR       = new Color( 80, 80, 80 );
	/** Color to use for axis labels.                            */
	private static final Color  CHART_AXIS_LABEL_COLOR         = Color.CYAN;
	/** Color to use for player descriptions.                    */
	private static final Color  CHART_PLAYER_DESCRIPTION_COLOR = Color.GREEN;
	/** Color to use for indicating hacks.                       */
	private static final Color  CHART_HACK_COLOR               = Color.RED;
	/** 2nd color to use for indicating hacks.                   */
	private static final Color  CHART_HACK_COLOR2              = Color.YELLOW;
	/** Font to use to draw descriptions and titles.             */
	private static final Font   CHART_MAIN_FONT                = new Font( "Times", Font.BOLD, 10 );
	/** Font to use to draw axis labels.                         */
	private static final Font   CHART_AXIS_LABEL_FONT          = new Font( "Courier New", Font.PLAIN, 8 );
	/** Font to use to draw hack markers.                        */
	private static final Font   HACK_MARKER_FONT               = new Font( "Courier New", Font.BOLD, 13 );
	/** Font to use to draw texts as part of charts.             */
	private static final Font   CHART_PART_TEXT_FONT           = new Font( "Courier New", Font.PLAIN, 8 );
	/** Stroke to be used to draw charts.                        */
	private static final Stroke CHART_STROKE                   = new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
	/** Stroke to be used to draw everything else.               */
	private static final Stroke CHART_REST_STROKE              = new BasicStroke( 1.0f );
	/** Number of assist lines to be painted in each chart.      */
	private static final int    ASSIST_LINES_COUNT             = 5;
	/** Number of time lables to be painted in each chart.       */
	private static final int    TIME_LABELS_COUNT              = 8;
	
	/**
	 * The supported types of charts.
	 * @author Andras Belicza
	 */
	public enum ChartType {
		/** APM charts of the players of the replay.         */
		APM( "APM" ),
		/** Hotkeys charts of the players of the replay.     */
		HOTKEYS( "Hotkeys" ),
		/** Build order charts of the players of the replay. */
		BUILD_ORDER( "Build order" );
		
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
	private final JPanel            chartOptionsPanel = new JPanel();
	/** Panel containing checkboxes of players.                       */
	private final JPanel            playersPanel      = new JPanel();
	/** Replay whose charts to be visualized.                         */
	private Replay                  replay;
	/** List of hack descriptions of the replay.                      */
	private List< HackDescription > hackDescriptionList;
	/** List of player indices to be shown.                           */
	private List< Integer >         playerIndexToShowList;
	
	/** APM chart detail level in pixels combo box. */
	private final JComboBox apmChartDetailLevelComboBox    = new JComboBox( new Object[] { 1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100 } );
	/** Show select hotkeys checkbox.               */
	private final JCheckBox showSelectHotkeysCheckBox      = new JCheckBox( "Show select hotkeys", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_SELECT_HOTKEYS ) ) );
	/** Build order display levels combo box.       */
	private final JComboBox buildOrderDisplayLevelComboBox = new JComboBox( new Object[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 } );
	/** Show units on build order checkbox.         */
	private final JCheckBox showUnitsOnBuildOrderCheckBox  = new JCheckBox( "Show units", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_UNITS_ON_BUILD_ORDER ) ) );
	/** Hide worker units checkbox.                 */
	private final JCheckBox hideWorkerUnitsCheckBox        = new JCheckBox( "Hide worker units", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_WORKER_UNITS ) ) );
	
	/**
	 * Creates a new ChartsComponent.
	 */
	public ChartsComponent( final ChartsTab chartsTab ) {
		setBackground( CHART_BACKGROUND_COLOR );
		setMaximumSize( new Dimension( 10000, 10000 ) );
		this.chartsTab = chartsTab;
		
		buildConentGUI();
		
		apmChartDetailLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_APM_CHART_DETAIL_LEVEL ) ) );
		buildOrderDisplayLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_BUILD_ORDER_DISPLAY_LEVELS ) ) );
	}
	
	/**
	 * Builds the GUI of the content panel.
	 */
	private void buildConentGUI() {
		final JPanel controlPanel = new JPanel( new BorderLayout() );
		controlPanel.add( chartOptionsPanel, BorderLayout.NORTH );
		controlPanel.add( playersPanel, BorderLayout.CENTER );
		contentPanel.add( controlPanel, BorderLayout.NORTH );
		
		contentPanel.add( this, BorderLayout.CENTER );
		
		final ChangeListener repainterChangeListener = new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				repaint();
			}
		};
		apmChartDetailLevelComboBox.addChangeListener( repainterChangeListener );
		showSelectHotkeysCheckBox.addChangeListener( repainterChangeListener );
		buildOrderDisplayLevelComboBox.addChangeListener( repainterChangeListener );
		showUnitsOnBuildOrderCheckBox.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				hideWorkerUnitsCheckBox.setEnabled( showUnitsOnBuildOrderCheckBox.isSelected() );
			}
		} );
		showUnitsOnBuildOrderCheckBox.addChangeListener( repainterChangeListener );
		hideWorkerUnitsCheckBox.addChangeListener( repainterChangeListener );
		hideWorkerUnitsCheckBox.setEnabled( showUnitsOnBuildOrderCheckBox.isSelected() );
	}
	
	/**
	 * Builds the panel containing the chart component and
	 */
	public JPanel getContentPanel() {
		return contentPanel;
	}
	
	public void setChartType( final ChartType chartType ) {
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
		}
		
		// We restore the values
		apmChartDetailLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_APM_CHART_DETAIL_LEVEL ) ) );
		showSelectHotkeysCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_SELECT_HOTKEYS ) ) );
		showUnitsOnBuildOrderCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SHOW_UNITS_ON_BUILD_ORDER ) ) );
		buildOrderDisplayLevelComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_BUILD_ORDER_DISPLAY_LEVELS ) ) );
		hideWorkerUnitsCheckBox.setSelected( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_WORKER_UNITS ) ) );
		
		contentPanel.doLayout();
		repaint();
	}
	
	/**
	 * Sets the replay whose charts to be visualized.
	 * @param replay replay whose charts to be visualized
	 */
	public void setReplay( final Replay replay ) {
		this.replay = replay;
		
		// removeAll() does not work properly in SwingWT, we remove previous checkboxes manually!
		while ( playersPanel.getComponentCount() > 1 )
			playersPanel.remove( playersPanel.getComponentCount() - 1 );
		
		if ( replay != null ) {
			hackDescriptionList = ReplayScanner.scanReplayForHacks( replay.replayActions, false );
			
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
				}
			};
			for ( int i = 0; i < players.length; i++ ) {
				players[ i ][ 0 ] = new JCheckBox( playerActions[ i ].playerName, true );
				players[ i ][ 1 ] = i;
				( (JCheckBox) players[ i ][ 0 ] ).addActionListener( playerCheckBoxActionListener );
			}
			// Order players by the number of their actions
			Arrays.sort( players, new Comparator< Object[] >() {
				public int compare( final Object[] o1, final Object[] o2 ) {
					return -Integer.valueOf( playerActions[ (Integer) o1[ 1 ] ].actions.length )
							.compareTo( playerActions[ (Integer) o2[ 1 ] ].actions.length );
				}
			} );
			
			playerIndexToShowList = new ArrayList< Integer >( players.length );
			for ( final Object[] player : players ) {
				playerIndexToShowList.add( (Integer) player[ 1 ] );
				playersPanel.add( (JCheckBox) player[ 0 ] );
			}
		}
		else
			hackDescriptionList = null;
		
		contentPanel.doLayout();
		repaint();
	}
	
	@Override
	public void paintComponent( final Graphics graphics ) {
		( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
		graphics.clearRect( 0, 0, getWidth(), getHeight() );
		
		if ( replay != null && playerIndexToShowList.size() > 0 ) {
			final ChartsParams chartsParams = new ChartsParams( chartsTab, replay.replayHeader.gameFrames, playerIndexToShowList.size(), this );
			switch ( (ChartType) chartsTab.chartTypeComboBox.getSelectedItem() ) {
				case APM :
					paintApmCharts( graphics, chartsParams );
					break;
				case HOTKEYS :
					paintHotkeysCharts( graphics, chartsParams );
					break;
				case BUILD_ORDER :
					paintBuildOrderCharts( graphics, chartsParams );
					break;
			}
		}
	}
	
	/**
	 * Paints the APM charts of the players.
	 * @param graphics     graphics to be used for painting
	 * @param chartsParams parameters of the charts to be drawn
	 */
	private void paintApmCharts( final Graphics graphics, final ChartsParams chartsParams ) {
		final int chartGranularity = (Integer) apmChartDetailLevelComboBox.getSelectedItem();
		if ( getWidth() < chartGranularity )
			return;
		
		final int     chartPoints = chartsParams.maxXInChart / chartGranularity + 1;
		
		final int[]   xPoints     = new int[ chartPoints + 1 ];
		final int[][] yPointss    = new int[ chartsParams.playersCount ][ chartPoints + 1 ];
		int pointIndex = 0;
		for ( int x = chartsParams.x1; pointIndex < xPoints.length ; pointIndex++, x+= chartGranularity )
			xPoints[ pointIndex ] = x;
		
		graphics.setFont( CHART_MAIN_FONT );
		graphics.setColor( CHART_AXIS_COLOR );
		graphics.drawString( "APM", 1, 0 );
		
		// First count the actions
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final PlayerActions playerActions = replay.replayActions.players[ playerIndexToShowList.get( i ) ];
			final int[] yPoints = yPointss[ i ];
			
			for ( final Action action : playerActions.actions )
				try {
					yPoints[ 1 + action.iteration * chartPoints / chartsParams.frames ]++;
				} catch ( final ArrayIndexOutOfBoundsException aioobe ) {
					// The last few actions might be over the last domain, we ignore them.
				}
		}
		
		// Next calculate max actions
		final int[] maxActionss = new int[ chartsParams.playersCount ];
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final int[] yPoints    = yPointss[ i ];
			int         maxActions = maxActionss[ i ]; 
			for ( final int actionsInDomain : yPoints )
				if ( maxActions < actionsInDomain )
					maxActions = actionsInDomain;
			
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
			final int[] yPoints    = yPointss[ i ];
			final int   maxActions = maxActionss[ i ];
			final int   y1         = chartsParams.getY1ForChart( i );
			if ( maxActions > 0 ) {
				for ( pointIndex = yPoints.length - 1; pointIndex > 0; pointIndex-- )
					yPoints[ pointIndex ] = y1 + maxY - yPoints[ pointIndex ] * maxY / maxActions;
				// Chart should not start from zero, we "double" the first point:
				yPoints[ 0 ] = yPoints[ 1 ];
			}
			else
				Arrays.fill( yPoints, y1 + maxY ); // No actions, we cannot divide by zero, just fill with maxY
		}
		
		// Finally draw the axis, labels, player descriptions and charts
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final PlayerActions playerActions = replay.replayActions.players[ playerIndexToShowList.get( i ) ];
			final int[]         yPoints       = yPointss[ i ];
			final int           y1            = chartsParams.getY1ForChart( i );
			final Color         inGameColor   = getPlayerInGameColor( playerActions );
			
			drawAxisAndTimeLabels( graphics, chartsParams, i );
			
			// Draw APM axis labels
			if ( !chartsParams.allPlayersOnOneChart || i == 0 ) { // We draw labels once if all players are on one chart
				graphics.setFont( CHART_AXIS_LABEL_FONT );
				// if no actions, let's define axis labels from zero to ASSIST_LINES_COUNT
				final int maxApm = maxActionss[ i ] > 0 ? maxActionss[ i ] * ( chartPoints - 1 ) * 60 / ReplayHeader.convertFramesToSeconds( chartsParams.frames ) : ASSIST_LINES_COUNT;
				for ( int j = 0; j <= ASSIST_LINES_COUNT; j++ ) {
					final int y   = y1 + chartsParams.maxYInChart - ( chartsParams.maxYInChart * j / ASSIST_LINES_COUNT );
					final int apm = maxApm * j / ASSIST_LINES_COUNT;
					graphics.setColor( CHART_AXIS_LABEL_COLOR );
					graphics.drawString( ( apm < 100 ? ( apm < 10 ? "  " : " " ) : "" ) + apm, 1, y - 7 );
					if ( j > 0 ) {
						graphics.setColor( CHART_ASSIST_LINES_COLOR );
						graphics.drawLine( chartsParams.x1 + 1, y, chartsParams.x1 + chartsParams.maxXInChart, y );
					}
				}
			}
			
			// Draw the charts
			final Color chartColor = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
			graphics.setColor( chartColor );
			( (Graphics2D) graphics ).setStroke( CHART_STROKE );
			graphics.drawPolyline( xPoints, yPoints, xPoints.length - 1 ); // Last point is excluded, it might not be a whole domain
			( (Graphics2D) graphics ).setStroke( CHART_REST_STROKE );
			
			// Mark hack occurences
			if ( hackDescriptionList != null ) {
				graphics.setColor( chartColor.getRed() > 200 && chartColor.getGreen() < 100 && chartColor.getBlue() < 100 ? CHART_HACK_COLOR2 : CHART_HACK_COLOR );
				graphics.setFont( HACK_MARKER_FONT );
				for ( final HackDescription hackDescription : hackDescriptionList )
					if ( hackDescription.playerName.equalsIgnoreCase( playerActions.playerName ) ) {
						pointIndex = hackDescription.iteration * chartPoints / chartsParams.frames;
						final float position = (float) ( hackDescription.iteration - pointIndex * chartsParams.frames / chartPoints ) * chartPoints / chartsParams.frames;
						graphics.drawString( "!",
								interpolate( xPoints[ pointIndex ], xPoints[ pointIndex + 1 ], position ) - 4,
								interpolate( yPoints[ pointIndex ], yPoints[ pointIndex + 1 ], position ) - 18, true );
					}
			}
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor );
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
	 * @param chartsParams parameters of the charts to be drawn
	 */
	private void paintHotkeysCharts( final Graphics graphics, final ChartsParams chartsParams ) {
		final boolean showSelectHotkeys = showSelectHotkeysCheckBox.isSelected();
		for ( int i = 0; i < chartsParams.playersCount; i++ ) {
			final PlayerActions playerActions = replay.replayActions.players[ playerIndexToShowList.get( i ) ];
			final int           y1            = chartsParams.getY1ForChart( i );
			final Color         inGameColor   = getPlayerInGameColor( playerActions );
			final Color         chartColor    = inGameColor == null ? CHART_DEFAULT_COLOR : inGameColor;
			
			drawAxisAndTimeLabels( graphics, chartsParams, i );
			
			graphics.setFont( CHART_PART_TEXT_FONT );
			
			for ( final Action action : playerActions.actions ) {
				final Color chartColor2 = new Color( ~chartColor.getRed() & 0xff, ~chartColor.getGreen() & 0xff, ~chartColor.getBlue() & 0xff );
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY ) {
					final String[] params         = action.parameters.split( "," );
					final boolean  isHotkeyAssign = params[ 0 ].equals( Action.HOTKEY_ACTION_PARAM_NAME_ASSIGN );
					
					if ( isHotkeyAssign || showSelectHotkeys ) {
						final int hotkey = Integer.parseInt( params[ 1 ] ) % 10; // Hotkey 10 is displayed where hotkey 0
						
						if ( isHotkeyAssign ) {
							( (Graphics2D) graphics ).setBackground( chartColor );
							graphics.setColor( chartColor2 );
						}
						else {
							( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
							graphics.setColor( chartColor );
						}
						
						graphics.drawString( params[ 1 ],
								chartsParams.getXForIteration( action.iteration ),
								y1 + hotkey * ( chartsParams.maxYInChart - 14 ) / 9,
								!isHotkeyAssign );
					}
				}
			}
			( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor );
		}
	}
	
	/**
	 * Paints the build order charts of the players.
	 * @param graphics     graphics to be used for painting
	 * @param chartsParams parameters of the charts to be drawn
	 */
	private void paintBuildOrderCharts( final Graphics graphics, final ChartsParams chartsParams ) {
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
			
			drawAxisAndTimeLabels( graphics, chartsParams, i );
			
			graphics.setFont( CHART_PART_TEXT_FONT );
			
			int buildOrderLevel = chartsParams.allPlayersOnOneChart ? playerIndex % ( buildOrderDisplayLevels + 1 ) : buildOrderDisplayLevels;
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
						graphics.drawString( buildingName, x - fontMetrics.stringWidth( buildingName ) / 2 - 4, y - 14, true );
						
						if ( --buildOrderLevel < 0 )
							buildOrderLevel = buildOrderDisplayLevels;
					}
				}
			}
			
			drawPlayerDescription( graphics, chartsParams, i, inGameColor );
		}
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
	 * @param chartsParams parameters of the charts to be drawn
	 * @param chartIndex   index of chart being queried
	 */
	private void drawAxisAndTimeLabels( final Graphics graphics, final ChartsParams chartsParams, final int chartIndex ) {
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
			for ( int j = 0; j <= TIME_LABELS_COUNT; j++ ) {
				final StringBuilder timeBuilder = new StringBuilder();
				ReplayHeader.formatFrames( frames * j / TIME_LABELS_COUNT, timeBuilder );
				final int x = chartsParams.x1 + ( chartsParams.maxXInChart * j / TIME_LABELS_COUNT )
								- ( j == 0 ? 0 : ( j == TIME_LABELS_COUNT ? timeBuilder.length() * 7 : timeBuilder.length() * 7 / 2 ) );
				graphics.drawString( timeBuilder.toString(), x, y1 + chartsParams.maxYInChart + 1 );
			}
		}
	}
	
	/**
	 * Draws a player's name and description.
	 * @param graphics     graphics to be used for painting
	 * @param chartsParams parameters of the charts to be drawn
	 * @param chartIndex   index of chart being queried
	 * @param inGameColor  in-game color of the player
	 */
	private void drawPlayerDescription( final Graphics graphics, final ChartsParams chartsParams, final int chartIndex, final Color inGameColor ) {
		graphics.setFont( CHART_MAIN_FONT );
		graphics.setColor( inGameColor == null ? CHART_PLAYER_DESCRIPTION_COLOR : inGameColor );
		graphics.drawString( replay.replayHeader.getPlayerDescription( replay.replayActions.players[ playerIndexToShowList.get( chartIndex ) ].playerName ),
				             chartsParams.x1,
				             chartsParams.getY1ForChart( chartIndex ) + ( chartsParams.allPlayersOnOneChart ? chartIndex * 14 - 22 : -12 ) );
	}
	
	/**
	 * Assignes the used properties of this component.
	 */
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_APM_CHART_DETAIL_LEVEL    , Integer.toString( apmChartDetailLevelComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_SELECT_HOTKEYS       , Boolean.toString( showSelectHotkeysCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_BUILD_ORDER_DISPLAY_LEVELS, Integer.toString( buildOrderDisplayLevelComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SHOW_UNITS_ON_BUILD_ORDER , Boolean.toString( showUnitsOnBuildOrderCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HIDE_WORKER_UNITS         , Boolean.toString( hideWorkerUnitsCheckBox.isSelected() ) );
	}
	
}
