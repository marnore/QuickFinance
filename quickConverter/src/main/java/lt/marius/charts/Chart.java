package lt.marius.charts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import lt.marius.charts.Legend.CheckedListener;
import lt.marius.converter.R;
import lt.marius.converter.utils.UIUtils;

public class Chart extends FrameLayout {
	
	public Chart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	public Chart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public Chart(Context context) {
		super(context);
	}
	
	
	
	/**
	 * initialize components with values from xml
	 * @param attrs
	 */
	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomChart);
		color = a.getColor(R.styleable.CustomChart_android_color, Color.WHITE);
		a.recycle();
		series = new ArrayList<Series>();
		scaleDetector = new ScaleGestureDetector(getContext(), new OnScaleGestureListener() {
			
			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {
//				scaleX *= detector.getScaleFactor();
				scaleY *= detector.getScaleFactor();
				scaleFactor = 1;
				postInvalidate();
			}
			
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				return true;
			}
			
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				float s = detector.getScaleFactor();
				if (s > 0.1 && s < 10) {
					
					scaleFactor = s;
					postInvalidate();
					return false;
				}
				return true;
			}
		});
		paddingLeft = UIUtils.dpToPx(2, getContext());
        paddingRight = UIUtils.dpToPx(8, getContext());
        paddingTop = UIUtils.dpToPx(8, getContext());
        paddingBottom = UIUtils.dpToPx(2, getContext());

        MOVE_THRESHOLD = UIUtils.dpToPx(20, getContext());
	}

    private float sx, sy, dx, dy;
    private boolean canClick;

    private float MOVE_THRESHOLD;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sx = event.getX();
                sy = event.getY();
                canClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (canClick) {
                    dx = event.getX() - sx;
                    dy = event.getY() - sy;
                    if (dx > MOVE_THRESHOLD || dy > MOVE_THRESHOLD) {
                        canClick = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (canClick) {
                    dx = event.getX() - sx;
                    dy = event.getY() - sy;
                    if (dx <= MOVE_THRESHOLD || dy <= MOVE_THRESHOLD) {
                        performClick();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                canClick = false;
        }
        if (scaleDetector.isInProgress()) {
            canClick = false;
        }
        return true;
	}

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
//        scaleDetector.onTouchEvent(event);
        return false;
    }

    private ScaleGestureDetector scaleDetector;
	private float scaleFactor = 1;
	
	private Axis xAxis;
	private Axis yAxis;
	private int color;
	private List<Series> series;
	private int paddingLeft;
	private int paddingRight;
	private int paddingTop;
	private int paddingBottom;
	private Legend legend;

	private RectF b = new RectF();
	private RectF bounds = new RectF();
	
	private float scaleX, scaleY;
//	@Override
//	protected void onDraw(Canvas canvas) {
//		if (xAxis == null || yAxis == null) return;
//		bounds.set(canvas.getClipBounds());
//		float w = bounds.width();
//		float h = bounds.height();
//		b.set(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom);
//		canvas.drawColor(color);
//		
//		//Draw axis
//		float scaleX;
//		float scaleY;
//		if (xAxis.isVisible()) {
//			scaleX = (b.width() - XAxis.TEXT_OFFEST) / (xAxis.getMaxValue() - xAxis.getMinValue());
//		} else {
//			scaleX = (b.width()) / (xAxis.getMaxValue() - xAxis.getMinValue());
//		}
//		if (yAxis.isVisible()) {
//			scaleY = (b.height() - YAxis.TEXT_OFFSET) / (yAxis.getMaxValue() - yAxis.getMinValue());
//		} else {
//			scaleY = (b.height()) / (yAxis.getMaxValue() - yAxis.getMinValue());
//		}
//		if (xAxis.isVisible()) {
//			xAxis.draw(canvas, b, scaleX, scaleY);
//		}
//		if (yAxis.isVisible()) {
//			yAxis.draw(canvas, b, scaleX, scaleY);
//		}
//		
//		RectF bb = new RectF(b.left + XAxis.TEXT_OFFEST, b.top, b.right, b.bottom - YAxis.TEXT_OFFSET);
//		for (Series s : series) {
//			s.draw(canvas, bb, scaleX, scaleY);
//		}
////		Matrix m = new Matrix();
////		m.setScale(sx, sy);
////		tempCanvas.setMatrix(m);
//		
//		
////		xAxis.draw();
//	}
	
	private float yAxisOffest = UIUtils.dpToPx(YAxis.TEXT_OFFSET, getContext());
	private float xAxisOffset = UIUtils.dpToPx(XAxis.TEXT_OFFSET, getContext());
	
	//wtf RelativeLayout? Not funny..
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (xAxis == null || yAxis == null) return;
//		RectF bounds = new RectF(canvas.getClipBounds());
		float w = getMeasuredWidth();
		float h = getMeasuredHeight();
		RectF b = new RectF(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom);
		canvas.drawColor(color);
		
		//Draw axis
//		float scaleX;
//		float scaleY;
		if (scaleX == 0) {
			if (xAxis.isVisible()) {
				scaleX = (b.width() - yAxisOffest) / (xAxis.getMaxValue() - xAxis.getMinValue());
			} else {
				scaleX = (b.width()) / (xAxis.getMaxValue() - xAxis.getMinValue());
			}
		}
		if (scaleY == 0) {
			if (yAxis.isVisible()) {
				scaleY = (b.height() - xAxisOffset) / (yAxis.getMaxValue() - yAxis.getMinValue());
			} else {
				scaleY = (b.height()) / (yAxis.getMaxValue() - yAxis.getMinValue());
			}
		}
		if (xAxis.isVisible()) {
			xAxis.draw(canvas, b, scaleX, scaleY * scaleFactor);
		}
		if (yAxis.isVisible()) {
			yAxis.draw(canvas, b, scaleX, scaleY * scaleFactor);
		}
		
		RectF bb = new RectF(b.left + yAxisOffest, b.top, b.right, b.bottom - xAxisOffset);
		for (Series s : series) {
			if (s.isVisible()) {
				s.draw(canvas, bb, scaleX, scaleY * scaleFactor);
			}
		}
		super.dispatchDraw(canvas);
	}
	
	public void showLegend(boolean folded) {
		if (legend == null) {
			legend = new Legend(getContext());
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP);
			params.topMargin = 10;
			params.rightMargin = 10;
			params.bottomMargin = 25;
			addView(legend, params);
		}
		legend.setSeries(series);
		legend.setFolded(folded);
		legend.setCheckedChangeListener(legendCheckChangeListener);
	}
	
	private CheckedListener legendCheckChangeListener = new CheckedListener() {

		@Override
		public void onSeriesChecked(Series series, boolean checked) {
			series.setVisible(checked);
			invalidate();
		}
		
	};
	
	public void addSeries(Series series) {
		this.series.add(series);
	}
	
	public void removeSeries(Series series) {
		this.series.remove(series);
	}
	
	public void setXAxis(Axis a) {
		xAxis = a;
		
	}
	
	public void setYAxis(Axis a) {
		yAxis = a;
	}

	public void clearSeries() {
		scaleX = 0;
		scaleY = 0;
		series = new ArrayList<Series>();
	}


}
