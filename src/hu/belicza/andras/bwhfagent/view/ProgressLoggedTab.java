package hu.belicza.andras.bwhfagent.view;

import swingwtx.swing.Icon;
import swingwtx.swing.JProgressBar;


/**
 * A tab with a progress bar.
 * 
 * @author Andras Belicza
 */
public abstract class ProgressLoggedTab extends LoggedTab {
	
	/** The progress bar component. */
	protected final JProgressBar progressBar = new JProgressBar();
	
	/**
	 * Creates a new ProgressLoggedTab.
	 * @param title       title of the tab
	 * @param icon        icon of the tab
	 * @param logFileName name of the log file
	 */
	public ProgressLoggedTab( final String title, final Icon icon, final String logFileName ) {
		super( title, icon, logFileName );
	}
	
	@Override
	protected void buildGUI() {
		progressBar.setMaximumSize( Utils.getMaxDimension() );
		contentBox.add( progressBar );
		
		super.buildGUI();
	}
	
}
