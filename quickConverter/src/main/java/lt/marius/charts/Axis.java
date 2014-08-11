package lt.marius.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class Axis {
	private float step;
	private float minValue;
	private float maxValue;
	private int color;
	private Paint paint = null;
	private boolean isVisible = true;
	private Context context;
	
	public Axis(float minValue, float maxValue, float step, Context c) {
		if (maxValue < minValue) {
			throw new IllegalArgumentException("maxValue must be greater or equal to minValue");
		}
		context = c;
		this.step = step;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.color = Color.parseColor("#222222");
		initPaint();
	}
	
	public Context getContext() {
		return context;
	}
	
	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	private void initPaint() {
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(0);
		paint.setAntiAlias(true);
		paint.setColor(color);
	}
	
	public void setVisible(boolean visible) {
		isVisible = visible;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	
	public abstract void draw(Canvas canvas, RectF bounds, float scaleX, float scaleY);

	public float getStep() {
		return step;
	}

	public void setStep(float step) {
		this.step = step;
	}

	public float getMinValue() {
		return minValue;
	}

	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}

	public float getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
	
	
	
}
