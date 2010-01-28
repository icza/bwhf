package hu.belicza.andras.bwhfagent.view.charts;

import javax.swing.JComponent;
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
	
	/** Width of the chartsComponent.  */
	public final int componentWidth;
	/** Height of the chartsComponent. */
	public final int componentHeight;
	
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
	
	/** Zoom level.                                                                            */
	public final int zoom;
	/** Delta x: x coordinate of the first visible chart point (it is scrollable when zoomed). */
	public final int dx;
	/** Delta y: y coordinate of the first visible chart point (it is scrollable when zoomed). */
	public final int dy;
	
	public ChartsParams( final ChartsTab chartsTab, final int frames, final int playersCount, final JComponent chartsComponent, final int dx, final int dy ) {
		componentWidth         = chartsComponent.getWidth();
		componentHeight        = chartsComponent.getHeight();
		allPlayersOnOneChart   = chartsTab.allPlayersOnOneChartCheckBox.isSelected();
		this.frames            = frames;
		this.playersCount      = playersCount;
		chartsCount            = allPlayersOnOneChart ? 1 : playersCount;
		zoom                   = (Integer) chartsTab.zoomComboBox.getSelectedItem();
		this.dx                = dx;
		this.dy                = dy;
		final int chartWidth_  = componentWidth * zoom - AXIS_SPACE_X;
		chartWidth             = chartWidth_ < 1 ? 1 : chartWidth_;
		final int chartHeight_ = ( componentHeight - AXIS_SPACE_Y ) / chartsCount - AXIS_SPACE_Y;
		chartHeight            = chartHeight_ < 1 ? 1 : chartHeight_;
		x1                     = AXIS_SPACE_X;
		maxXInChart            = chartWidth  - 1;
		maxYInChart            = chartHeight - 1;
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
	 * Returns the x coordinate calculated for the given time.
	 * @param time    time whose x coordinate to be returned (either iteration or seconds)
	 * @param maxTime max time (either frames count or duration) 
	 * @return the x coordinate calculated for the specified time
	 */
	public int getXForTime( final int time, int maxTime ) {
		if ( maxTime == 0 )
			maxTime = 1;
		return AXIS_SPACE_X + time * ( chartWidth  - 1 ) / maxTime;
	}
	
	/**
	 * Returns the time denoted by the x coordinate on the charts component.
	 * @param x       x coordinate whose time is to be returned
	 * @param maxTime max time (either frames count or duration)
	 * @return the time denoted by the x coordinate on the charts component or -1 if it is outside the game domain
	 */
	public int getIterationForX( final int x, int maxTime ) {
		if ( x < AXIS_SPACE_X )
			return -1;
		if ( maxTime == 0 )
			maxTime = 1;
		return ( x - AXIS_SPACE_X ) * maxTime / ( chartWidth  - 1 );
	}
	
}
