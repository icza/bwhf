package hu.belicza.andras.bwhfagent.view;

import swingwtx.swing.Box;
import swingwtx.swing.JComponent;

/**
 * Defines a tab to be added to the main frame.
 * 
 * @author Belicza Andras
 */
public abstract class Tab {
	
	/** A box to hold the content of the tab. */
	protected final Box contentBox = Box.createVerticalBox();
	
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
	 * Returns the content component.
	 * @return the content component
	 */
	public JComponent getContent() {
		return contentBox;
	}
	
	/**
	 * Sets the new values of properties used by this tab.
	 */
	public abstract void assignUsedProperties();
	
	/**
	 * Called when this tab is selected.<br>
	 * Because of SwingWT, this won't be called on startup for the initially selected tab. 
	 */
	public void onSelected() {
	}
	
	/**
	 * Receives a notification of the end of initialization.
	 * Main frame is now visible.
	 */
	public void initializationEnded() {
	}
	
}
