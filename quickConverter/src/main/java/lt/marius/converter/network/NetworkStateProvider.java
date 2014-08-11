package lt.marius.converter.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.LinkedList;
import java.util.List;

public class NetworkStateProvider {
	
	/**
	 * Interface for listening to network changes
	 * @author Marius
	 *
	 */
	public interface NetworkStateListener {
			void onConnected();
			void onDisconnected();
	}
	
	/* Broadcast Receiver to Receive network changes */
	private ConnectivityBroadcastReceiver mReceiver;
	public static class ConnectivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ) {
				return;
			}
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();
			NetworkStateProvider.getInstance().handleNetworkChange(mNetworkInfo);
		}
	};
	
	public static final int STATE_UNKNOWN = 0;
	public static final int STATE_DISCONNECTED = 1;
	public static final int STATE_CONNECTED = 2;
	
	private static NetworkStateProvider instance;
	private static boolean listening = false;
	private List<NetworkStateListener> listeners = new LinkedList<NetworkStateListener>();
	
//	private NetworkConnectivityListener networkList;
	private Context mContext;
	private NetworkInfo networkInfo;
	private int connected = STATE_UNKNOWN;
	
	
	public NetworkStateProvider() {
		mReceiver = new ConnectivityBroadcastReceiver();
	}
	
	private void handleNetworkChange(NetworkInfo networkInfo) {
		if (networkInfo != null) {
			this.networkInfo = networkInfo;
			if (networkInfo.isConnected()) {
				if (connected == STATE_DISCONNECTED || connected == STATE_UNKNOWN) {
					connected = STATE_CONNECTED;
					notifyObservers();
				}
			} else {
				if (connected == STATE_CONNECTED || connected == STATE_UNKNOWN) {
					connected = STATE_DISCONNECTED;
					notifyObservers();
				}
			}
		}
	}
	
	public static synchronized NetworkStateProvider getInstance() {
		if (instance == null) {
			instance = new NetworkStateProvider();
		}
		return instance;
	}
	
	public synchronized void startListening(Context context) {
		if (!listening) {
			mContext = context;
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			context.registerReceiver(mReceiver, filter);
			
			listening = true;
			connected = STATE_UNKNOWN;
		}
	}
	
	public synchronized void stopListening() {
		if (listening) {
			listeners.clear();
			mContext.unregisterReceiver(mReceiver);
			mContext = null;
			networkInfo = null;
			listening = false;
			connected = STATE_UNKNOWN;
		}
	}
	
	public synchronized void addListener(NetworkStateListener l) {
		listeners.add(l);
	}
	
	public synchronized void removeListener(NetworkStateListener l) {
		listeners.remove(l);
	}
	
	private synchronized void notifyObservers() {
		for (NetworkStateListener l : listeners) {
			if (connected == STATE_CONNECTED) {
				l.onConnected();
			} else {
				l.onDisconnected();
			}
		}
	}

    private int getNetworkState() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            if (activeNetworkInfo.isConnected()) {
                return STATE_CONNECTED;
            } else {
                return STATE_UNKNOWN;
            }
        } else {
            return STATE_UNKNOWN;
        }
    }
	
	public synchronized boolean isConnected() {
        if (connected == STATE_UNKNOWN) {
            connected = getNetworkState();
        }
		return connected == STATE_CONNECTED;
	}
}
