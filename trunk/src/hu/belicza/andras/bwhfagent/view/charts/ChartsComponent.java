package hu.belicza.andras.bwhfagent.view.charts;

import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.view.ChartsTab;
import hu.belicza.andras.bwhfagent.view.MainFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import swingwt.awt.BasicStroke;
import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.Font;
import swingwt.awt.Graphics;
import swingwt.awt.Graphics2D;
import swingwt.awt.Stroke;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;

/**
 * Component to visialize charts.
 * 
 * @author Andras Belicza
 */
public class ChartsComponent extends JPanel {
	
	/** Background color for charts.                 */
	private static final Color  CHART_BACKGROUND_COLOR         = Color.BLACK;
	/** Color to use for axis lines and axis titles. */
	private static final Color  CHART_AXIS_COLOR               = Color.YELLOW;
	/** Default color to use for the chart curve.    */
	private static final Color  CHART_DEFAULT_COLOR            = Color.WHITE;
	/** Color to use for axis and info texts.        */
	private static final Color  CHART_ASSIST_LINES_COLOR       = new Color( 80, 80, 80 );
	/** Color to use for axis labels.                */
	private static final Color  CHART_AXIS_LABEL_COLOR         = Color.CYAN;
	/** Color to use for player descriptions.        */
	private static final Color  CHART_PLAYER_DESCRIPTION_COLOR = Color.GREEN;
	/** Color to use for indicating hacks.           */
	private static final Color  CHART_HACK_COLOR               = Color.RED;
	/** Font to use to draw descriptions and titles. */
	private static final Font   CHART_MAIN_FONT                = new Font( "Times", Font.BOLD, 10 );
	/** Font to use to draw axis labels.             */
	private static final Font   CHART_AXIS_LABEL_FONT          = new Font( "Courier New", Font.PLAIN, 8  );
	/** Stroke to be used to draw charts.            */
	private static final Stroke CHART_STROKE                   = new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
	/** Stroke to be used to draw everything else.   */
	private static final Stroke CHART_REST_STROKE              = new BasicStroke( 1.0f );
	/** Number of assist lines to be painted in each chart. */
	private final int           ASSIST_LINES_COUNT             = 5;
	
	/** Chart granularity in pixels. */
	private static final int CHART_GRANULARITY = 10;
	
	/**
	 * The supported types of charts.
	 * @author Andras Belicza
	 */
	public enum ChartType {
		/** APM charts of the players of the replay. */
		APM( "APM" );
		
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
	private final JPanel            contentPanel = new JPanel( new BorderLayout() );
	/** Panel containing checkboxes of players.                       */
	private final JPanel            playersPanel = new JPanel();
	/** Replay whose charts to be visualized.                         */
	private Replay                  replay;
	/** List of hack descriptions of the replay.                      */
	private List< HackDescription > hackDescriptionList;
	/** List of player indices to be shown.                           */
	private List< Integer >         playerIndexToShowList;
	
	/**
	 * Creates a new ChartsComponent.
	 */
	public ChartsComponent( final ChartsTab chartsTab ) {
		super( new BorderLayout() );
		this.chartsTab = chartsTab;
		
		buildConentGUI();
	}
	
	/**
	 * Builds the GUI of the content panel.
	 */
	private void buildConentGUI() {
		contentPanel.add( playersPanel, BorderLayout.NORTH );
		contentPanel.add( this, BorderLayout.CENTER );
	}
	
	/**
	 * Builds the panel containing the chart component and
	 */
	public JPanel getContentPanel() {
		return contentPanel;
	}
	
	/**
	 * Sets the replay whose charts to be visualized.
	 * @param replay replay whose charts to be visualized
	 */
	public void setReplay( final Replay replay ) {
		this.replay = replay;
		if ( replay != null ) {
			hackDescriptionList = ReplayScanner.scanReplayForHacks( replay.replayActions, false );
			
			final PlayerActions[] playerActions = replay.replayActions.players;
			
			// removeAll() does not work properly in SwingWT, we remove previous checkboxes manually!
			while ( playersPanel.getComponentCount() > 1 )
				playersPanel.remove( 1 );
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
		
		MainFrame.getInstance().pack();
		repaint();
	}
	
	@Override
	public void paintComponent( final Graphics graphics ) {
		( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
		graphics.clearRect( 0, 0, getWidth(), getHeight() );
		
		if ( replay != null && playerIndexToShowList.size() > 0 )
			switch ( (ChartType) chartsTab.chartTypeComboBox.getSelectedItem() ) {
				case APM :
					paintApmCharts( graphics );
					break;
			}
	}
	
	private void paintApmCharts( final Graphics graphics ) {
		if ( getWidth() < CHART_GRANULARITY )
			return;
		
		final int AXIS_SPACE_X = 23;
		final int AXIS_SPACE_Y = 20;
		
		final int chartWidth  = getWidth() - AXIS_SPACE_X;
		final int chartHeight = ( getHeight() - AXIS_SPACE_Y ) / playerIndexToShowList.size() - AXIS_SPACE_Y;
		final int x1          = AXIS_SPACE_X;
		final int maxXInChart = chartWidth  - 1;
		final int maxYInChart = chartHeight - 1;
		
		final int chartPoints       = maxXInChart / CHART_GRANULARITY + 1;
		final int frames            = replay.replayHeader.gameFrames;
		final int framesGranularity = frames / chartPoints;
		
		final int[] xPoints = new int[ chartPoints + 1 ];
		final int[] yPoints = new int[ chartPoints + 1 ];
		int pointIndex = 0;
		for ( int x = x1; pointIndex < xPoints.length ; pointIndex++, x+= CHART_GRANULARITY )
			xPoints[ pointIndex ] = x;
		
		graphics.setFont( CHART_MAIN_FONT );
		graphics.setColor( CHART_AXIS_COLOR );
		graphics.drawString( "APM", 1, 0 );
		
		graphics.setFont( CHART_AXIS_LABEL_FONT );
		
		for ( int i = 0; i < playerIndexToShowList.size(); i++ ) {
			final PlayerActions playerActions = replay.replayActions.players[ playerIndexToShowList.get( i ) ];
			
			// Draw the axis
			graphics.setColor( CHART_AXIS_COLOR );
			final int y1 = ( chartHeight + AXIS_SPACE_Y ) * i + AXIS_SPACE_Y;
			graphics.drawLine( x1, y1, x1, y1 + chartHeight );
			graphics.drawLine( x1, y1 + maxYInChart, x1 + maxXInChart, y1 + maxYInChart );
			
			Arrays.fill( yPoints, 0 );
			
			for ( final Action action : playerActions.actions )
				try {
					yPoints[ 1 + action.iteration / framesGranularity ]++;
				} catch ( final ArrayIndexOutOfBoundsException aioobe ) {
					// The last few actions might be over the last domain, we ignore them.
				}
			
			int maxActions = 0; 
			for ( final int actionsInDomain : yPoints )
				if ( maxActions < actionsInDomain )
					maxActions = actionsInDomain;
			
			final int maxY = maxYInChart;
			// Normalize chart to its height
			if ( maxActions > 0 )
				for ( pointIndex = yPoints.length - 1; pointIndex > 0; pointIndex-- )
					yPoints[ pointIndex ] = y1 + maxY - yPoints[ pointIndex ] * maxY / maxActions;
			else
				Arrays.fill( yPoints, y1 + maxY ); // No actions, we cannot divide by zero, just fill with maxY
			
			// Draw APM axis labels
			graphics.setFont( CHART_AXIS_LABEL_FONT );
			final int maxApm = maxActions * ( chartPoints - 1 ) * 60 / ReplayHeader.convertFramesToSeconds( frames );
			for ( int j = 0; j <= ASSIST_LINES_COUNT; j++ ) {
				final int y   = y1 + maxYInChart - ( maxYInChart * j / ASSIST_LINES_COUNT );
				final int apm = maxApm * j / ASSIST_LINES_COUNT;
				graphics.setColor( CHART_AXIS_LABEL_COLOR );
				graphics.drawString( ( apm < 100 ? ( apm < 10 ? "  " : " " ) : "" ) + apm, 1, y - 7 );
				if ( j > 0 ) {
					graphics.setColor( CHART_ASSIST_LINES_COLOR );
					graphics.drawLine( x1 + 1, y, x1 + maxXInChart, y );
				}
			}
			
			// Chart should not start from zero, we "double" the first point:
			yPoints[ 0 ] = yPoints[ 1 ];
			graphics.setColor( CHART_DEFAULT_COLOR );
			( (Graphics2D) graphics ).setStroke( CHART_STROKE );
			graphics.drawPolyline( xPoints, yPoints, xPoints.length - 1 ); // Last point is excluded, it might not be a whole domain
			( (Graphics2D) graphics ).setStroke( CHART_REST_STROKE );
			
			// Draw player's name and description
			graphics.setFont( CHART_MAIN_FONT );
			graphics.setColor( CHART_PLAYER_DESCRIPTION_COLOR );
			graphics.drawString( replay.replayHeader.getPlayerDescription( replay.replayActions.players[ playerIndexToShowList.get( i ) ].playerName ), AXIS_SPACE_X + 15, y1 - 14 );
		}
	}
	
}
