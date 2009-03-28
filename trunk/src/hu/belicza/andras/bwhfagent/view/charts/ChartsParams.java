package hu.belicza.andras.bwhfagent.view.charts;

import swingwtx.swing.JComponent;
import hu.belicza.andras.bwhfagent.view.ChartsTab;

/**
 * Common parameters for drawing players' charts.
 * 
 * @author Andras Belicza
 */
public class ChartsParams {
	
	/** Space to be left for the x axis. */
	private static final int AXIS_SPACE_X = 23;
	/** Space to be left for the y axis. */
	private static final int AXIS_SPACE_Y = 23;
	
	/** Tells whether to draw all players on one chart. */
	public final boolean allPlayersOnOneChart;
	/** Frames of the replay.                           */
	public final int     frames;
	/** Number of visible players.                      */
	public final int     playersCount;
	/** Number of charts to be drawn.                   */
	public final int     chartsCount;
	/** Width of the charts.                            */
	public final int     chartWidth;
	/** Height of the charts.                           */
	public final int     chartHeight;
	/** x1 coordinate of the charts.                    */
	public final int     x1;
	/** Max x coordinate inside charts.                 */
	public final int     maxXInChart;
	/** Max y coordinate inside charts.                 */
	public final int     maxYInChart;
	
	public ChartsParams( final ChartsTab chartsTab, final int frames, final int playersCount, final JComponent chartsComponent ) {
		allPlayersOnOneChart = chartsTab.allPlayersOnOneChartCheckBox.isSelected();
		this.frames          = frames;
		this.playersCount    = playersCount;
		chartsCount          = allPlayersOnOneChart ? 1 : playersCount;
		chartWidth           = chartsComponent.getWidth() - AXIS_SPACE_X;
		final int chartHeight_ = ( chartsComponent.getHeight() - AXIS_SPACE_Y ) / chartsCount - AXIS_SPACE_Y;
		chartHeight          = chartHeight_ < 1 ? 1 : chartHeight_;
		x1                   = AXIS_SPACE_X;
		maxXInChart          = chartWidth  - 1;
		maxYInChart          = chartHeight - 1;
	}
	
	/**
	 * Returns the y1 coordinate of the specified chart.
	 * @param chartIndex index of chart being queried
	 * @return the y1 coordinate of the specified chart
	 */
	public int getY1ForChart( final int chartIndex ) {
		return ( chartHeight + AXIS_SPACE_Y ) * ( allPlayersOnOneChart ? 0 : chartIndex ) + AXIS_SPACE_Y;
	}
	
	/**
	 * Returns the x coordinate calculated for the given iteration.
	 * @param iteration iteration whose x coordinate to be returned
	 * @return the x coordinate calculated for the given iteration
	 */
	public int getXForIteration( final int iteration ) {
		return x1 + iteration * maxXInChart / frames;
	}
	
	/**
	 * Returns the x coordinate calculated for the given iteration.
	 * @param iteration       iteration whose x coordinate to be returned
	 * @param frames          frames count
	 * @param chartsComponent reference to the charts component
	 * @return the x coordinate calculated for the given iteration
	 */
	public static int getXForIteration( final int iteration, final int frames, final JComponent chartsComponent ) {
		return AXIS_SPACE_X + iteration * ( chartsComponent.getWidth() - AXIS_SPACE_X  - 1 ) / frames;
	}
	
	/**
	 * Returns the iteration denoted by the x coordinate on the charts component.
	 * @param x               x coordinate whose iteration is to be returned
	 * @param frames          frames count
	 * @param chartsComponent reference to the charts component
	 * @return the iteration denoted by the x coordinate on the charts component or -1 if it is outside the game iteration domain
	 */
	public static int getIterationForX( final int x, final int frames, final JComponent chartsComponent ) {
		if ( x < AXIS_SPACE_X )
			return -1;
		return ( x - AXIS_SPACE_X ) * frames / ( chartsComponent.getWidth() - AXIS_SPACE_X  - 1 );
	}
	
}