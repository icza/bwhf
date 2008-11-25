package hu.belicza.andras.bwhfagent.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * About tab.
 * 
 * @author Andras Belicza
 */
public class AboutTab extends Tab {
	
	/** Name of the resource containing the about html template. */
	private static final String ABOUT_TEMLATE_RESOURCE_NAME = "about_template.html";
	
	/** A map containing the possible parameters in the template file and their values to be replaced with. */
	private final Map< String, String > templateParameterValueMap = new HashMap< String, String >();
	
	/**
	 * Creates a new AboutTab.
	 */
	public AboutTab() {
		super( "About" );
		
		templateParameterValueMap.put( "%APPLICATION_NAME%"   , MainFrame.APPLICATION_NAME + "&trade;" );
		templateParameterValueMap.put( "%APPLICATION_AUTHOR%" , MainFrame.APPLICATION_AUTHOR );
		templateParameterValueMap.put( "%APPLICATION_VERSION%", Utils.getMainFrame().applicationVersion );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final StringBuilder aboutHtmlBuilder = new StringBuilder();
		final BufferedReader input = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( ABOUT_TEMLATE_RESOURCE_NAME ) ) );
		try {
			while ( input.ready() )
				aboutHtmlBuilder.append( input.readLine() );
		} catch ( final IOException ie ) {
			ie.printStackTrace();
		}
		
		String aboutHtml = aboutHtmlBuilder.toString();
		for ( final Map.Entry< String, String > entry : templateParameterValueMap.entrySet() )
			aboutHtml = aboutHtml.replace( entry.getKey(), entry.getValue() );
		
		final JEditorPane editorPane = new JEditorPane( "text/html", aboutHtml );
		editorPane.setEditable( false );
		//editorPane.setPreferredSize( new Dimension( 200, 200 ) );
		editorPane.addHyperlinkListener( new HyperlinkListener() {
			public void hyperlinkUpdate( final HyperlinkEvent event ) {
				if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
					Utils.showURLInBrowser( event.getURL().toString() );
			}
		} );
		contentBox.add( editorPane );
//		contentBox.add( new JScrollPane( editorPane ) );
	}
	
}
