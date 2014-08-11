package lt.marius.converter.utils;

import java.util.ArrayList;
import java.util.List;

public class BackKeyHandler {

	private static final BackKeyHandler INSTANCE = new BackKeyHandler();
	
	public interface BackKeyListener {
		public boolean onBackPressed();
	}
	
	public static BackKeyHandler get() {
		return INSTANCE;
	}
	
	
	private List<BackKeyListener> listeners = new ArrayList<BackKeyListener>();
	
	public void subscribe(BackKeyListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}
	
	public void unsubscribe(BackKeyListener l) {
		listeners.remove(l);
	}
	
	public boolean backPressed() {
		boolean res = false;
		for (BackKeyListener l : listeners) {
			res |= l.onBackPressed();
		}
		return res;
	}
	
}
