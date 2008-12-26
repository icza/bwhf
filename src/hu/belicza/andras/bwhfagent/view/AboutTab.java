package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import swingwt.awt.Dimension;
import swingwtx.swing.JEditorPane;
import swingwtx.swing.JScrollPane;

/**
 * About tab.
 * 
 * @author Andras Belicza
 */
public class AboutTab extends Tab {
	
	/** A map containing the possible parameters in the template file and their values to be replaced with. */
	private final Map< String, String > templateParameterValueMap = new HashMap< String, String >();
	
	/** Reference to the content editor pane, so it can be put in focus when the tab is selected. */
	private JEditorPane editorPane;
	
	/**
	 * Creates a new AboutTab.
	 */
	public AboutTab() {
		super( "About" );
		
		templateParameterValueMap.put( "%APPLICATION_NAME%"                , Consts.APPLICATION_NAME + "&#8482;" ); // &#8482; is the code of trade mark ('™')
		templateParameterValueMap.put( "%APPLICATION_AUTHOR%"              , Consts.APPLICATION_AUTHOR );
		templateParameterValueMap.put( "%APPLICATION_VERSION%"             , MainFrame.getInstance().applicationVersion );
		templateParameterValueMap.put( "%HOME_PAGE_URL%"                   , Consts.HOME_PAGE_URL );
		templateParameterValueMap.put( "%BWHF_HACKER_DATA_BASE_SERVER_URL%", Consts.BWHF_HACKER_DATA_BASE_SERVER_URL );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final StringBuilder aboutHtmlBuilder = new StringBuilder();
		BufferedReader input = null;
		try {
			input = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( Consts.ABOUT_TEMLATE_RESOURCE_NAME ) ) );
			while ( input.ready() )
				aboutHtmlBuilder.append( input.readLine() );
		} catch ( final IOException ie ) {
		}
		finally {
			if ( input != null )
				try { input.close(); } catch ( final IOException ie ) {}
		}
		
		String aboutHtml = aboutHtmlBuilder.toString();
		for ( final Map.Entry< String, String > entry : templateParameterValueMap.entrySet() )
			aboutHtml = aboutHtml.replace( entry.getKey(), entry.getValue() );
		
		editorPane = new JEditorPane( "text/html", aboutHtml );
		editorPane.setEditable( false );
		/*editorPane.addHyperlinkListener( new HyperlinkListener() {
			public void hyperlinkUpdate( final HyperlinkEvent event ) {
				if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
					if ( event.getURL() != null )
						Utils.showURLInBrowser( event.getURL().toString() );
			}
		} );*/
		
		// Why do we need this?
		// Not setting preferred size of the editorPane results in really wide window size after pack,
		// because the about html contains long lines which are not broke by default.
		// Setting preferred size of the editorPane results in no scrollbars around it, so its
		// full content wouldn't be visible.
		// Solution: the component added to the content box should be sized and scrolled.
		final JScrollPane wrapperScrollPane = new JScrollPane( editorPane );
		wrapperScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
		contentBox.add( wrapperScrollPane );
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
	@Override
	public void onSelected() {
		editorPane.requestFocusInWindow();
	}
	
}
