package lt.marius.converter;

import java.util.ArrayList;
import java.util.List;

import lt.marius.converter.utils.UIUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class OptionsButton extends Button {


	public OptionsButton(Context context) {
		super(context);
		init(context);
	}

	public OptionsButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public OptionsButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private OnOptionSelectListener listener;
	
	public interface OnOptionSelectListener {
		void onOptionSelected(Option selected);
	}
	
	public void setOnOptionSelectedListener(OnOptionSelectListener l) {
		this.listener = l;
	}
	
	public void setOptions(List<Option> options) {
		this.options.clear();
		this.options.addAll(options);
		ArrayList<Bitmap> images = new ArrayList<Bitmap>();
		for (Option o : this.options) {
			images.add(o.getImage());
		}
		view.setImages(images);
	}

	private List<Option> options = new ArrayList<Option>();
	private DrawingView view;
	private TextView textView;
	private static Bitmap cancel_icon;
	private RectF cancelBounds;
//	private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
	
	private void init(Context context) {
		if (cancel_icon == null) {
			cancel_icon = BitmapFactory.decodeResource(getResources(), R.drawable.cancel_icon);
		}
		
		textView = new TextView(context);
		textView.setTextSize(25);
		textView.setPadding(5, 5, 5, 5);
		textView.setBackgroundColor(Color.parseColor("#80AAAA00"));
		view = new DrawingView(context);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		view.setBackgroundColor(Color.parseColor("#C0111111"));
		
		OnTouchListener touchListener = new ButtonTouchListener() {
			
			@Override
			public void touched(View v) {
				highlitedOption = null;
				addOverlay();
			}
			
			@Override
			public void moved(View v, float x, float y, float dx, float dy) {
//				setText(String.format("%.4f,  %.4f", dx, dy));
//				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
//				params.topMargin = (int) y + btnTop;
//				params.leftMargin = (int) x + btnLeft;
//				view.setLayoutParams(params);
				
//				positionOverlay(getRootView(), (int)(btnTop + y), (int)(btnLeft + x));
				if (options != null && !options.isEmpty()) {
					checkTouchArea(x + btnLeft, y + btnTop);
				}
			}
			
			@Override
			public void clicked(View v) {
				ViewGroup root = (ViewGroup) v.getRootView();
				root.removeView(textView);
				root.removeView(view);
			}
			
			@Override
			public void released(View v) {
				ViewGroup root = (ViewGroup) v.getRootView();
				root.removeView(textView);
				root.removeView(view);
				if (listener != null && highlitedOption != null) {
					listener.onOptionSelected(highlitedOption);
				}
			}
		};
		setOnTouchListener(touchListener);
	}
	
	private void addOverlay() {
		if (options == null || options.isEmpty()) {
			return;
		}
		ViewGroup root = (ViewGroup) getRootView();
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		root.removeView(view);
		root.addView(view, params);
		
		params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = UIUtils.dpToPx(50, getContext());
		params.gravity = Gravity.CENTER_HORIZONTAL;
		root.removeView(textView);
		textView.setText("Select option");
		root.addView(textView, params);
		//form a square in a middle of the screen
		//portrait mode
		if (root.getHeight() > root.getWidth()) {
			positionOverlay(root, btnTop, btnLeft);
		} else {
			positionOverlay(root, 0, 0);
		}
		
	}
	
	private int btnTop;
	private int btnLeft;
	private ArrayList<Line> midLines;
	private ArrayList<Point> points;
	private int centerX;
	private int centerY;
	
	private void checkTouchArea(float x, float y) {
		if (cancelBounds == null) {
			cancelBounds = new RectF(
					btnLeft + (getMeasuredWidth() - cancel_icon.getWidth()) / 2,
					btnTop + (getMeasuredHeight() - cancel_icon.getHeight()) / 2,
					btnLeft + (getMeasuredWidth() - cancel_icon.getWidth()) / 2 + cancel_icon.getWidth(),
					btnTop + (getMeasuredHeight() - cancel_icon.getHeight()) / 2 + cancel_icon.getHeight());
		}
		view.drawCancel((int)cancelBounds.left, (int)cancelBounds.top);
//		int imgSize = options.get(0).getImage().getWidth();
		
		if (cancelBounds.contains(x, y)) {
			setHighlited(-1);
			view.invalidate();
			return;
		}
		
		Point prev = null;
		int index = -1;
		if (x > centerX + 10) {	//linear approach
			for (int i = 0, n = points.size(); i < n; i++) {
				Point p = points.get(i);
				if (y > p.y) {
					index = i;
				} else {
					if (index == -1) {
						index = 0;
					}
					setHighlited(index);
					index = -1;
					break;
				}
				
			}
			if (index != -1) {
				setHighlited(points.size() - 1);
			}
		} else {	//circular approach
			for (int i = 0, n = midLines.size(); i < n; i++) {
				if (midLines.get(i).isPointBelow(x, y)) {
					index = i;
				} else {
					setHighlited(i);
					index = -1;
					break;
				}
			}
			if (index != -1) {
				setHighlited(points.size() - 1);
			}
		}
		view.invalidate();
	}
	
	private Option highlitedOption;
	private void setHighlited(int index) {
		if (index == -1) {
//			view.drawCancel(true);
			highlitedOption = null;
			view.setHighlited(null);
			textView.setText("Cancel");
			textView.setVisibility(View.INVISIBLE);
		} else {
			view.setHighlited(new Point(points.get(index)));
			highlitedOption = options.get(index);
			textView.setText(highlitedOption.getTitle());
			textView.setVisibility(View.VISIBLE);
		}
	}
	
	private void positionOverlay(View root, int btnTop, int btnLeft) {
		view.highlight = null;
		
		int size = root.getWidth();
		int top = (root.getHeight() - size) / 2;
		int left = 0;
		if (btnTop < top) top = btnTop;
		if (btnTop + this.getMeasuredHeight() > top + size) top = btnTop - size + this.getMeasuredHeight();
		
		centerY = btnTop + getMeasuredHeight() / 2;
		centerX = 0;
		int radius;
		if (btnLeft <= size / 3) {
			centerX = 0;
			radius = size;
		} else if (btnLeft >= size / 3 * 2) {
			centerX = size - 1;
			radius = size;
		} else {
			centerX = size / 2;
			radius = size / 2;
		}
		
		//90 deg circle appraoch
//		float stepAngle =  (float) (Math.toRadians(90.f) / images.size());
//		ArrayList<Point> points = new ArrayList<Point>();
//		int x, y;
//		radius = size - 50;
//		float beta;
//		for (int i = 0; i < images.size(); i++) {
//			beta = (float) (Math.toRadians(90.f) - i * stepAngle);
//			x = (int) (size - (int)(radius * Math.sin(beta))) + left;
//			y = (int) (radius * Math.cos(beta)) + top;
//			Point p = new Point(x, y);
//			Log.d("temp", p.toString());
//			points.add(p);
//		}
		int imgSize = options.get(0).getImage().getWidth();
		radius = size / 2 + 1;
		int halfSize = (size) / 2;
//		centerX = 0 + radius + left - imgSize / 2;
//		centerY = halfSize + top;
		
		centerX = btnLeft;
		centerY = btnTop + getMeasuredHeight() / 2;
		
		midLines = new ArrayList<Line>(options.size() - 1);
		double b = (halfSize - imgSize) / (float)radius;
		float alpha1;
		float stepAngle;
		if (radius > halfSize) {
			alpha1 = (float) Math.acos(b);
			stepAngle = (float) ((Math.toRadians(180.f) - 2 * alpha1) / (options.size() - 1));
		} else {
			stepAngle = (float)(Math.toRadians(180.f)) / (float)(options.size() + 1);
			alpha1 = stepAngle;
		}
		points = new ArrayList<Point>();
		ArrayList<RectF> lines = new ArrayList<RectF>();
		int x = 0, y = 0;
		int xp = 0, yp = 0;
		float beta;
		for (int i = 0; i < options.size(); i++) {
			beta = (float) (alpha1 + i * stepAngle);
			x = (int)(radius * Math.sin(beta));		
			y = (int) (radius * Math.cos(beta));
			x = centerX - x;
			y = centerY - y - imgSize / 2;
			Point p = new Point(x, y);
			points.add(p);
//			lines.add(new RectF(x, y, centerX, centerY));
			if (i >= 0) {
				if (beta < Math.toRadians(90)) {
					midLines.add(new Line(x,  y + imgSize, centerX, centerY));
					lines.add(new RectF(x, y + imgSize, centerX, centerY));
				} else {
					midLines.add(new Line(x + imgSize, y + imgSize, centerX, centerY));
					lines.add(new RectF(x + imgSize, y + imgSize, centerX, centerY));
				}
			}
			xp = x;
			yp = y;
		}
		view.setPoints(points);
		
		view.lines = lines;
		view.center = new RectF(centerX - 5, centerY - 5, centerX + 5, centerY + 5);
		view.bounds = new RectF(0, top, size - 1, top + size - 1);
		
		view.invalidate();
	}
	
	private class DrawingView extends View {

		private int cancelLeft = -1, cancelTop = -1;
		public DrawingView(Context context) {
			super(context);
			paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(0);
			highlightPaint = new Paint();
			highlightPaint.setColor(Color.parseColor("#80FFFF80"));
			highlightPaint.setStyle(Paint.Style.FILL);
		}
		
		public void setImages(List<Bitmap> images) {
			this.images = images;
		}
		
		private List<Bitmap> images;
		
		public void setHighlited(Point p) {
			if (p == null) {
				highlight = null;
				highlightPoint = null;
			} else {
				int size = images.get(0).getWidth();
				highlightPoint = p;
				if (images != null) {
					highlight = new RectF(p.x - 5, p.y - 5, 
							p.x + size + 5, p.y + size + 5);
					highlightCorner = UIUtils.dpToPx(10, getContext());
				}
			}
		}
		
		public void setPoints(ArrayList<Point> points) {
			this.points = points;
		}
		
		public void drawCancel(int left, int top) {
			cancelLeft = left;
			cancelTop = top;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (cancelLeft != -1 && cancelTop != -1) {
				canvas.drawBitmap(cancel_icon, cancelLeft, cancelTop, paint);
			}
			
			if (bounds != null) {
//				canvas.drawRect(bounds, paint);
			}
			if (center != null) {
				canvas.drawCircle(center.centerX(), center.centerY(), center.width() / 2, paint);
			}
			if (points != null) {
				int i = 0;
				int skipped = -1;
				for (Point p : points ) {
					if (p.equals(highlightPoint)) {
						skipped = i;
					} else {
						canvas.drawBitmap(images.get(i), p.x, p.y, paint);
					}
					i++;
				}
				if (skipped != -1 ) {
					canvas.drawBitmap(images.get(skipped), highlightPoint.x, highlightPoint.y, paint);
				}
			}
			if (lines != null) {
				for (RectF r : lines ) {
//					canvas.drawLine(r.left, r.top, r.right, r.bottom, paint);
				}
			}
			if (highlight != null) {
				
				canvas.drawRoundRect(highlight, highlightCorner, highlightCorner, highlightPaint);
			}
//			if (text != null && textPosition != null) {
//				float width = paint.measureText(text);
//				int x = (int) (canvas.getWidth() - (width / 2));
//				canvas.drawText(text, x, textPosition.y, paint);
//			}
		}
		
		private RectF highlight;
		private Point highlightPoint;
		private int highlightCorner;
		private ArrayList<Point> points;
		private Paint paint, highlightPaint;
		private String text;
		private Point textPosition;
		
		RectF bounds;
		RectF center;
		ArrayList<RectF> lines;
		
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		btnTop = UIUtils.getRecursiveTop(this);
		btnLeft = UIUtils.getRecursiveLeft(this);
	}

	private class ButtonTouchListener implements OnTouchListener {

		private static final float CLICK_THRESHOLD = 25.f;
		private float sx, sy, dx, dy;
		private boolean canClick;

		public void touched(View v){}
		
		public void moved(View v, float x, float y, float dx, float dy) {}
		
		public void clicked(View v) {}
		
		public void released(View v) {}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				sx = event.getX();
				sy = event.getY();
				canClick = true;
				touched(v);
				break;
			case MotionEvent.ACTION_MOVE:
				dx = event.getX() - sx;
				dy = event.getY() - sy;
				if (Math.abs(dx) > CLICK_THRESHOLD
						|| Math.abs(dy) > CLICK_THRESHOLD) {
					canClick = false;
				}
				moved(v, event.getX(), event.getY(), dx, dy);
				break;
			case MotionEvent.ACTION_CANCEL:
				canClick = false;	//TODO check if works
				break;
			case MotionEvent.ACTION_UP:
				dx = event.getX() - sx;
				dy = event.getY() - sy;
				if (canClick && Math.abs(dx) < CLICK_THRESHOLD
						&& Math.abs(dy) < CLICK_THRESHOLD) {
					clicked(v);
					canClick = true;
					return false;
				}
				moved(v, event.getX(), event.getY(), dx, dy);
				released(v);
				return true;
			}
			return true;
		}
	};

}
