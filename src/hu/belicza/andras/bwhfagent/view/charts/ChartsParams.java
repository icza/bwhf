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
	
	public ChartsParams( final ChartsTab chartsTab, final int playersCount, final JComponent chartsComponent ) {
		allPlayersOnOneChart = chartsTab.allPlayersOnOneChartCheckBox.isSelected();
		this.playersCount    = playersCount;
		chartsCount          = allPlayersOnOneChart ? 1 : playersCount;
		chartWidth           = chartsComponent.getWidth() - AXIS_SPACE_X;
		chartHeight          = ( chartsComponent.getHeight() - AXIS_SPACE_Y ) / chartsCount - AXIS_SPACE_Y;
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
	
}
