package lt.marius.charts;

import java.util.ArrayList;
import java.util.List;

import lt.marius.converter.R;
import lt.marius.converter.utils.UIUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;

public class Legend extends LinearLayout {

	public interface CheckedListener {
		void onSeriesChecked(Series series, boolean checked);
	}
	
	private List<Series> series;
	private ArrayList<CheckBox> views;
	private CheckedListener listener;
	private View headerView;
	private ListView container;

	public Legend(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Legend(Context context) {
		super(context);
		init();
	}

	float sx, sy;
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			sx = event.getX();
//			sy = event.getY();
//		case MotionEvent.ACTION_MOVE:
//			event.getX();
//			event.getY();
//			ViewHelper.setTranslationX(this, event.getX() - sx);
//			ViewHelper.setTranslationY(this, event.getY() - sy);
//			break;
//		}
//		return true;
////		return super.onTouchEvent(event);
//	}
	
	private float lx, ly;
	private final float CLICK_AREA_THRESHOLD = UIUtils.dpToPx(5, getContext());
	private int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	
	private OnTouchListener touchListener = new OnTouchListener() {
		private boolean canClick = false;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				sx = event.getX();
				sy = event.getY();
				canClick = true;
//				Log.d("temp", "sx " + sx + " sy " + sy);
//				lx = ViewHelper.getX(Legend.this);
//				ly = ViewHelper.getY(Legend.this);
//				Log.d("temp", "lx " + lx + " ly " + ly);
			case MotionEvent.ACTION_MOVE:
//				Log.d("temp", "ev " + event.getX() + " " + event.getY());
//				Log.d("temp", "before " + ViewHelper.getX(Legend.this) + " after " + (ViewHelper.getX(Legend.this) + event.getX() - sx));
				if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
					ViewHelper.setX(Legend.this, ViewHelper.getX(Legend.this) + event.getX() - sx);
					ViewHelper.setY(Legend.this, ViewHelper.getY(Legend.this) + event.getY() - sy);
				} else{	//poor pre honeycomb cannot really do that..
//					ViewHelper.setX(Legend.this, event.getX() - sx);
//					ViewHelper.setY(Legend.this, event.getY() - sy);
				}
				if (event.getX() - sx > CLICK_AREA_THRESHOLD ||
					event.getY() - sy > CLICK_AREA_THRESHOLD) {
					canClick = false;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (canClick) {
					float dist = (event.getX() - sx) * (event.getX() - sx) + (event.getY() - sy) * (event.getY() - sy);
					dist = FloatMath.sqrt(dist);
					if (dist <= CLICK_AREA_THRESHOLD) {
						onClick(v);
						if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB){
							((View)getParent()).invalidate();
						}
					}
				}
				break;
			}
			return true;
		}
	};
	
	private void onClick(View v) {
//		for (View vv : views) {
//			if (vv.getVisibility() == VISIBLE) {
//				vv.setVisibility(View.GONE);
//			} else {
//				vv.setVisibility(View.VISIBLE);
//			}
//		}
		if (container.getVisibility() == VISIBLE) {
			container.setVisibility(View.GONE);
		} else {
			container.setVisibility(View.VISIBLE);
		}
	}
	
	public void setFolded(boolean folded) {
		container.setVisibility(folded ? View.GONE : View.VISIBLE);
	}
	
	private void init() {
		views = new ArrayList<CheckBox>();
		container = new ListView(getContext());
		
		container.setDivider(null);
		container.setDividerHeight(0);
		container.setCacheColorHint(Color.TRANSPARENT);
		container.requestFocus(0);
		setOrientation(LinearLayout.VERTICAL);
		setOnTouchListener(touchListener);
		setBackgroundColor(Color.parseColor("#ff205060"));
//		RelativeLayout header = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.legend_header, null);
//		ImageView iv = (ImageView)header.findViewById(R.id.iv_legend_resize);
//		iv.setTag(true);
//		iv.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Boolean showing = (Boolean) v.getTag();
//				if (showing) {
//					
//				} else {
//					
//					headerView.getHeight();
//				}
//			}
//		});
//		headerView = header;
		TextView tv = new TextView(getContext());
		tv.setText(getContext().getString(R.string.legend));
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_move);
		Drawable dr = new BitmapDrawable(getResources(), bmp);
		dr.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
		tv.setCompoundDrawablesWithIntrinsicBounds(dr, null, null, null);
		tv.setTextSize(14.f);
		tv.setTextColor(Color.WHITE);
		int pad = UIUtils.dpToPx(15, getContext());
		tv.setPadding(pad, 0, pad, 0);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		headerView = tv;
		addView(headerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
	}
	
	public void setCheckedChangeListener(CheckedListener l) {
		this.listener = l;
		for (int i = 0; i < getChildCount(); i++) {
			View cb =  getChildAt(i);
			if (! (cb instanceof CheckBox) ) continue;
			((CheckBox)cb).setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (listener != null) {
						listener.onSeriesChecked((Series) buttonView.getTag(), isChecked);
					}
				}
			});
		}
	}
	
	private class LegendAdapter extends BaseAdapter {

		List<Boolean> states;
		public LegendAdapter() {
			states = new ArrayList<Boolean>();
			for (Series s : series) {
				states.add(s.isVisible());
			}
		}
		
		int paddingLeft = 0;
		@Override
		public int getCount() {
			return series.size();
		}

		@Override
		public Series getItem(int position) {
			return series.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			CheckBox cb;
			if (convertView != null) {
				cb = (CheckBox) convertView;
			} else {
				cb = new CheckBox(getContext());
				if (paddingLeft == 0) paddingLeft = cb.getPaddingLeft();
			}
			cb.setOnCheckedChangeListener(null);
			cb = createItem(getItem(position), cb, paddingLeft);
			cb.setChecked(states.get(position));
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					states.set(position, isChecked);
					if (listener != null) {
						listener.onSeriesChecked((Series) buttonView.getTag(), isChecked);
					}
				}
			});
			return cb;
		}
		
	}
	
	private CheckBox createItem(Series s, CheckBox cb, int paddingLeft) {
		int padding = UIUtils.dpToPx(5, getContext());
		cb.setText(s.getTitle());
		
		int height = (int) (cb.getPaint().getTextSize() - 2);
		Bitmap bmp = getIcon(height, height, s.getColor());
		Drawable dr = new BitmapDrawable(getResources(), bmp);
		dr.setBounds(0, 0, height, height);
		cb.setChecked(true);
		cb.setCompoundDrawablePadding(padding);
		cb.setCompoundDrawablesWithIntrinsicBounds(null, null, dr, null);
		cb.setPadding(paddingLeft + padding, 0, padding, 0);
		cb.setTag(s);
		cb.setMaxLines(2);
		cb.setChecked(s.isVisible());
		cb.setHorizontallyScrolling(true);
		return cb;
	}
	
	public void setSeries(List<Series> series) {
		this.series = series;
		
		LegendAdapter adapter = new LegendAdapter();
		container.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		//determine the width
		int width = UIUtils.dpToPx(150, getContext());
		if (getParent() != null) {
			View parent = (View) getParent();		
			int pw = parent.getWidth() / 2;
			if (width < pw) width = pw;
		}
		if (container.getParent() != null) {
			UIUtils.removeFromParent(container);
		}
		addView(container, new LayoutParams(width, LayoutParams.WRAP_CONTENT));
	}

	private Bitmap getIcon(int width, int height, int color) {
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		c.drawRect(0, 0, width, height, paint);
		paint.setColor(color);
		c.drawRect(2, 2, width - 2, height - 2, paint);
		return bmp;
	}
	
	

}
