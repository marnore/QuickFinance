package lt.marius.converter.network;

import java.util.HashMap;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

/**
 * A wrapper for a broadcast receiver that provides network connectivity state
 * information, independent of network type (mobile, Wi-Fi, etc.).
 * 
 */
public class NetworkConnectivityListener {

	/* Context to register Connectivity Broadcast Receiver */
	private Context mContext;
	/* Handler to notify when network state changes */
	private HashMap<Handler, Integer> mHandlers = new HashMap<Handler, Integer>();
	/* Flag to identify when listening for changes */
	private boolean mListening;
	/* Network connectivity information */
	private NetworkInfo mNetworkInfo;
	
	/* Broadcast Receiver to Receive network changes */
	private ConnectivityBroadcastReceiver mReceiver;
	private static class ConnectivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//
//			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
//					|| mListening == false) {
//				return;
//			}
//			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//			mNetworkInfo = cm.getActiveNetworkInfo();
//			Iterator<Handler> it = mHandlers.keySet().iterator();
//			while (it.hasNext()) {
//				Handler target = it.next();
//				Message message = Message.obtain(target, mHandlers.get(target));
//				target.sendMessage(message);
//			}
		}
	};

	/**
	 * Create a new NetworkConnectivityListener.
	 */
	public NetworkConnectivityListener() {
		mReceiver = new ConnectivityBroadcastReceiver();
	}

	/**
	 * This method starts listening for network connectivity state changes.
	 * 
	 * @param context
	 */
	public synchronized void startListening(Context context) {
		if (!mListening) {
			mContext = context;

			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			context.registerReceiver(mReceiver, filter);
			mListening = true;
		}
	}

	/**
	 * This method stops this class from listening for network changes.
	 */
	public synchronized void stopListening() {
		if (mListening) {
			mContext.unregisterReceiver(mReceiver);
			mContext = null;
			mNetworkInfo = null;
			mListening = false;
		}
	}

	/**
	 * This methods registers a Handler to be called back onto with the
	 * specified what code when the network connectivity state changes.
	 * 
	 * @param target
	 *            The target handler.
	 * @param what
	 *            The what code to be used when posting a message to the
	 *            handler.
	 */
	public void registerHandler(Handler target, int what) {
		mHandlers.put(target, what);
	}

	/**
	 * This methods unregisters the specified Handler.
	 * 
	 * @param target
	 */
	public void unregisterHandler(Handler target) {
		mHandlers.remove(target);
	}

	/**
	 * Return the NetworkInfo associated with the most recent connectivity
	 * event.
	 * 
	 * @return {@code NetworkInfo} for the network that had the most recent
	 *         connectivity event.
	 */
	public NetworkInfo getNetworkInfo() {
		return mNetworkInfo;
	}
}
