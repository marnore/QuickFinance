package lt.marius.converter.test;

import lt.marius.converter.MainActivity;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

//import com.robotium.solo.Solo;

public class QuickConverterTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public QuickConverterTest() {
		super(MainActivity.class);
	}

//	private Solo solo;
	
	@Override
	protected void setUp() throws Exception {
//		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	
	
	public void testSomething() throws Exception {
				
		StringDatabase db = new StringDatabase("", lt.marius.converter.R.string.class);
		db.init(getActivity());
		
//		final TextView tv = solo.getText("Finance");
		Handler handler = new Handler(Looper.getMainLooper());
//		handler.post(new Runnable() {
//			
//			@Override
//			public void run() {
//				tv.setText("Test");
//				
//			}
//		});
		
//		ClickListener<TextView> textClickListener = new ClickListener<TextView>() {
//
//			@Override
//			public String getText(TextView view) {
//				return view.getText().toString();
//			}
//
//			@Override
//			public void setText(TextView view, String text) {
//				view.setText(text);
//			}
//
//		};
//
//		for (final View v : solo.getCurrentViews()) {
//			if (v.getClass().equals(TextView.class)) {
//				v.setOnClickListener(textClickListener);
//				handler.post(new Runnable() {
//
//					@Override
//					public void run() {
//						v.setBackgroundColor(Color.parseColor("#80FF0000"));
//					}
//				});
//			}
//		}
//		solo.waitForText("12345", 1, 20000);
//		synchronized(solo) {
//			solo.wait();
//		}
		
//		ht = new HandlerThread("runner");
//		ht.start();
//		handler = new Handler(ht.getLooper()) {
//			@Override
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//				case 0:
//					//do stuff here :)
//					TextView tv = solo.getText("Finance");
//					tv.setText("Lol wrong thread");
//					sendEmptyMessage(1);
//				case 1:
//					ht.quit();
//					break;
//				}
//			}
//		};
//		handler.sendEmptyMessage(0);
//		ht.join();
		
//		Handler h = new Handler(Looper.myLooper()) {
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//				case 0:
//					//do stuff here :)
//					TextView tv = solo.getText("Finance");
//					tv.setText("Lol wrong thread");
//					sendEmptyMessage(1);
//				case 1:
//					Looper.myLooper().quit();
//					break;
//				}
//			};
//		};
//		h.sendEmptyMessage(0);
		assertTrue("some error", true); 
		
//		solo.sleep(5000);
	}
	
	private abstract class ClickListener<V extends View> implements OnClickListener {
		
		public abstract String getText(V view);
		public abstract void setText(V view, String text);
		
		@Override
		public void onClick(View v) {
			
			final V view = (V)v;
			
			EditText editText = new EditText(getActivity());
			final ViewSwapper<V, EditText> swapper = new ViewSwapper<V, EditText>(view, editText);

			editText.setText(getText(view));
			editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			editText.setOnEditorActionListener(new OnEditorActionListener() {
				
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
//						synchronized (solo) {
//							solo.notifyAll();
//						}
						EditText et = swapper.swapBack();
						String newText = et.getText().toString();
						//change the text of the view
						setText(view, newText);
						return true;
					}
					return false;
				}
			});
			swapper.swap();
			
		}
		
	}
	
	private OnClickListener textClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TextView tv = ((TextView)v);				
			EditText editText = new EditText(getActivity());
			final ViewSwapper<TextView, EditText> swapper = new ViewSwapper<TextView, EditText>(tv, editText);

			editText.setText(tv.getText());
			editText.setImeOptions(EditorInfo.IME_NULL);
			editText.setOnEditorActionListener(new OnEditorActionListener() {
				
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_NULL
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
//						synchronized (solo) {
//							solo.notifyAll();
//						}
						EditText et = swapper.swapBack();
						swapper.getOld().setText(et.getText().toString());
					}
					return false;
				}
			});
			swapper.swap();

		}
	};
	
	private HandlerThread ht;
	private Handler handler;
	
	@Override
	protected void tearDown() throws Exception {
//		solo.finishOpenedActivities();
	}
	
}
