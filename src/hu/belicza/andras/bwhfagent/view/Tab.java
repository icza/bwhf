package hu.belicza.andras.bwhfagent.view;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * Defines a tab to be added to the main frame.<br>
 * 
 * @author Belicza Andras
 */
public abstract class Tab {
	
	/** A box to hold the content of the tab. */
	protected final Box contentBox = Box.createVerticalBox();
	
	/** The scroll panel wrapping the content box. */
	private final JScrollPane scrollPane = new JScrollPane( contentBox );
	
	/** The title of the tab. */
	private String title;
	
	/**
	 * Creates a new Tab.
	 * @param title title of the tab
	 */
	public Tab( final String title ) {
		this.title = title;
	}
	
	/**
	 * Returns the title of the tab.
	 * @return the title of the tab
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Returns the scroll panel wrapping the content box.
	 * @return the scroll panel wrapping the content box
	 */
	public JComponent getScrollPane() {
		return scrollPane;
	}
	
	/**
	 * Sets the new values of properties used by this tab.
	 */
	public abstract void assignUsedProperties();
	
}
