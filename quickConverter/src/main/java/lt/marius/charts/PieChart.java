package lt.marius.charts;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import lt.marius.charts.Legend.CheckedListener;
import lt.marius.converter.utils.UIUtils;

public class PieChart extends FrameLayout {

	public PieChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PieChart(Context context) {
		super(context);
		init();
	}
	
	private Paint paint;
	private RectF bounds = new RectF(), shadowBounds = new RectF();
	private Camera camera;
	private Matrix matrix;
	private int padding, shadowOffset;
	private List<SeriesPie> series;
	private Legend legend;
	
	private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setColor(Color.LTGRAY);
		paint.setAntiAlias(true);
		matrix = new Matrix();
		bounds = new RectF(0, 0, 100, 200);
		camera = new Camera();
		padding = UIUtils.dpToPx(8, getContext());
		shadowOffset = UIUtils.dpToPx(2, getContext());
	}
	
	public void setSeries(List<SeriesPie> series) {
		this.series = series;
		initSeries();
	}
	
	private void initSeries() {
		float start = 0;
		double sum = 0;
		for (SeriesPie s : series) {
			if (s.isVisible()) {
				sum += s.getValue();
			}
		}
		if (sum != 0) {
			for (SeriesPie s : this.series) {
				if (!s.isVisible()) continue;
				float degrees = (float) (360 / sum * s.getValue());
				s.setStartSweep(start, degrees);
				start += degrees;
			}
		}
	}
	
	private List<Series> legendSeries;
	public void showLegend(boolean folded) {
		if (series.isEmpty()) return;
		
		if (legend == null) {
			legend = new Legend(getContext());
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP);
			params.topMargin = 10;
			params.rightMargin = 10;
			params.bottomMargin = 25;
			addView(legend, params);
		}
		legendSeries = new ArrayList<Series>(series.size());
		for (SeriesPie s : series) {
			legendSeries.add(s);
		}
		legend.setSeries(legendSeries);
		legend.setCheckedChangeListener(legendCheckChangeListener);
		legend.setFolded(folded);
	}
	
	private CheckedListener legendCheckChangeListener = new CheckedListener() {

		@Override
		public void onSeriesChecked(Series series, boolean checked) {
			toggleSeriesVisibility(PieChart.this.series.get(legendSeries.indexOf(series)), checked);
		}
		
	};

    public void toggleSeriesVisibility(Series series, boolean visible) {
        series.setVisible(visible);
        initSeries();
        invalidate();
    }
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		int s = Math.min(bottom - top - 2 * padding, right - left - 2 * padding);
		int w = right - left;
        int h = bottom - top;

		bounds.left = (w - s) / 2;
		bounds.right = bounds.left + s;
		bounds.top = (h - s) / 2;
		bounds.bottom = bounds.top + s;
				
		shadowBounds.set(bounds);
		shadowBounds.right += shadowOffset;
		shadowBounds.bottom += shadowOffset;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	private double coef = Math.PI/180.0;
	private float rotX = 0;
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (series == null) {
			super.dispatchDraw(canvas);
			return;
		}
		canvas.save();
		
//		camera.save();
//		camera.translate(0, 50, 250);
//		camera.save();
//		camera.rotateX(rotX);
//
//		camera.getMatrix(matrix);
		float w = bounds.width();
		float h = bounds.height();
		float r = w / 2;
		float CenterX = w / 2 + bounds.left;
		float CenterY = h / 2 + bounds.top;
//        canvas.translate(CenterX, CenterY);
//		matrix.preTranslate(-CenterX, -CenterY);
//		matrix.postTranslate(CenterX, CenterY);
//		canvas.concat(matrix);
//		camera.restore();
//		camera.restore();
		//shadow
		paint.setColor(Color.LTGRAY);
        paint.setShadowLayer(UIUtils.dpToPx(4, getContext()), 0, shadowOffset, 0xFF999999);

		canvas.drawArc(bounds, 0, 360, true, paint);    //dummy bg

		
//		paint.setColor(Color.LTGRAY);
//		canvas.drawArc(bounds, -90, 130, true, paint);
//		paint.setColor(Color.RED);
//		canvas.drawArc(bounds, 40, 30, true, paint);
//		paint.setColor(Color.BLUE);
//		canvas.drawArc(bounds, 70, 80, true, paint);
//		paint.setColor(Color.WHITE);
//		canvas.drawArc(bounds, 150, 120, true, paint);
		
		for (SeriesPie s : series) {
			if (s.getSweep() > 0 && s.isVisible()) {

				canvas.drawArc(bounds, s.getStart() - 90, s.getSweep(), true, s.getPaint());
			}
		}
		
		canvas.restore();
		
		float start = 0;
		for (SeriesPie s : series ) {
			if (s.getSweep() > 0 && s.isVisible()) {
				drawText(canvas, s.getTitle(), String.format("%.2f", s.getValue()), CenterX, CenterY, r, start + s.getSweep() / 2);
				start += s.getSweep();
			}
		}
		
		super.dispatchDraw(canvas);
	}
	
	private void drawText(Canvas canvas, String text, String subtext, float centerX, float centerY, float r, float radius) {
		int textSize = UIUtils.dpToPx(16, getContext());
		paint.setTextSize(textSize);
		float tw = paint.measureText(text);
		float x = centerX + r * 0.66f * (float)Math.sin(radius * coef) - tw / 2;
		float y = centerY - r * 0.66f * (float)Math.cos(radius * coef);
//		paint.setShadowLayer(0, 0, 0, 0);
//		paint.setColor(Color.BLACK);
//		canvas.drawText(text, x+1, y+2, paint);
		
		paint.setColor(Color.WHITE);
		paint.setShadowLayer(1, 2, 2, Color.BLACK);
		canvas.drawText(text, x,  y, paint);
		tw = paint.measureText(subtext);
		x = centerX + r * 0.66f * (float)Math.sin(radius * coef) - tw / 2;
		y = centerY - r * 0.66f * (float)Math.cos(radius * coef) + textSize;
		canvas.drawText(subtext, x,  y, paint);
	}

}
