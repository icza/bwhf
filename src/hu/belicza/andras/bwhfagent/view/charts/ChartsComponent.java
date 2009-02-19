package hu.belicza.andras.bwhfagent.view.charts;

import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.Replay;

import java.util.List;

import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.Graphics;
import swingwt.awt.Graphics2D;
import swingwtx.swing.JPanel;

/**
 * Component to visialise charts.
 * 
 * @author Andras Belicza
 */
public class ChartsComponent extends JPanel {
	
	/** Background color for charts.          */
	private static final Color CHART_BACKGROUND_COLOR = Color.BLACK;
	/** Color to use for axis and info texts. */
	private static final Color CHART_INFO_COLOR       = Color.YELLOW;
	/** Color to use for indicating hacks.    */
	private static final Color CHART_HACK_COLOR       = Color.RED;
	
	/** Chart granularity in pixels. */
	private static final int CHART_GRANULARITY = 15;
	
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
	
	/** Type of chart to be painted.             */
	private ChartType               chartType = ChartType.APM;
	/** Replay whose charts to be visualized.    */
	private Replay                  replay;
	/** List of hack descriptions of the replay. */
	private List< HackDescription > hackDescriptionList;
	
	/**
	 * Creates a new ChartsComponent.
	 */
	public ChartsComponent() {
		super( new BorderLayout() );
	}
	
	/**
	 * Sets the chart type to be painted.
	 * @param chartType chart type to be painted
	 */
	public void setChartType( final ChartType chartType ) {
		this.chartType = chartType;
		repaint();
	}
	
	/**
	 * Sets the replay whose charts to be visualized.
	 * @param replay replay whose charts to be visualized
	 */
	public void setReplay( final Replay replay ) {
		this.replay = replay;
		if ( replay != null )
			hackDescriptionList = ReplayScanner.scanReplayForHacks( replay.replayActions, false );
		else
			hackDescriptionList = null;
		
		repaint();
	}
	
	@Override
	public void paintComponent( final Graphics graphics ) {
		( (Graphics2D) graphics ).setBackground( CHART_BACKGROUND_COLOR );
		graphics.clearRect( 0, 0, getWidth(), getHeight() );
		
		if ( replay != null )
			switch ( chartType ) {
				case APM :
					paintApmCharts( graphics );
					break;
			}
	}
	
	private void paintApmCharts( final Graphics graphics ) {
		if ( getWidth() < CHART_GRANULARITY )
			return;
		
		final int maxX = getWidth () - 1;
		final int maxY = getHeight() - 1;
		
		graphics.setColor( CHART_INFO_COLOR );
		final int frames = replay.replayHeader.gameFrames;
		
		final int[] xPoints = new int[ maxX / CHART_GRANULARITY + 1 ];
		final int[] yPoints = new int[ xPoints.length ];
		yPoints[ 0 ] = xPoints[ 0 ] = 0;
		
		final int frameGranularity = frames * CHART_GRANULARITY / getWidth();
		int pointIndex = 0;
		for ( int frame = 0; frame <= frames; frame += frameGranularity, pointIndex++ ) {
			
		}
	}
	
}
