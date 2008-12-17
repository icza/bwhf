package hu.belicza.andras.bwhfagent.view;

/**
 * This is a simple Thread setting always <code>NORM_PRIORITY</code> in its constructor.<br>
 * This is neccessary if we create a new thread form the event dispatch thread.
 * In that case a newly thread would inherit the parent's priority which in the case of event dispatch thread is higher
 * than the normal (and would cause the event dispatch thread to compete when it should have the win unconditionally). 
 * 
 * @author Andras Belicza
 */
public class NormalThread extends Thread {
	
	public NormalThread() {
		setPriority( NORM_PRIORITY );
	}
	
}
