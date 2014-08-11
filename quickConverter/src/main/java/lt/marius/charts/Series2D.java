package lt.marius.charts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.shapes.Shape;

public class Series2D extends Series {
	private float[] pointsX;
	private float[] pointsY;
	private Shape connector;
	private boolean displayValues = false;
	private boolean displayNames = false;
	private String[] names;

	private float minX, maxX;
	private float minY, maxY;
	
	public Series2D() {
		setVisible(true);
		initPaint();
	}
	
	private void initPaint() {
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3.f);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeJoin(Paint.Join.ROUND);
	}


	public Shape getConnector() {
		return connector;
	}

	public void setConnector(Shape connector) {
		this.connector = connector;
	}

	public boolean isValuesDisplayed() {
		return displayValues;
	}

	public void setDisplayValues(boolean displayValues) {
		this.displayValues = displayValues;
	}

	public boolean isNamesDisplayed() {
		return displayNames;
	}

	public void setDisplayNames(boolean displayNames) {
		this.displayNames = displayNames;
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}


	@Override
	public void draw(Canvas canvas, RectF bounds, float scaleX, float scaleY) {
//		float scaleX = fitInto.width() / (maxX - minX);
//		float scaleY = fitInto.height() / (maxY - minY);
		canvas.save();
		canvas.translate(-scaleX, 0);
		canvas.clipRect(bounds.left - scaleX, bounds.top, bounds.right + scaleX, bounds.bottom);
		for (int i = 0; i < pointsX.length - 1 && i < pointsY.length - 1; i += 1) {
			canvas.drawLine(bounds.left + pointsX[i] * scaleX, 
					bounds.bottom - pointsY[i] * scaleY,
					bounds.left + pointsX[i+1] * scaleX,
					bounds.bottom - pointsY[i+1] * scaleY, paint);
		}
		if (pointsY.length == 1) {
//			canvas.drawRect(bounds.left + pointsX[0] * scaleX, 
//					bounds.bottom - pointsY[0] * scaleY, 
//					bounds.left + pointsX[0] * scaleX + 4, 
//					bounds.bottom - pointsY[0] * scaleY + 4, paint);
			canvas.drawPoint(bounds.left + pointsX[0] * scaleX, 
					bounds.bottom - pointsY[0] * scaleY, paint);
		}
		canvas.restore();
	}
	
	public void setPointsX(float[] points) {
		pointsX = points;
		minX = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		for (int i = 0; i < pointsX.length; i++) {
			if (pointsX[i] > maxX) maxX = pointsX[i];
			if (pointsX[i] < minX) minX = pointsX[i];
		}
	}
	
	public void setPointsY(float[] points) {
		pointsY = points;
		minY = Float.MAX_VALUE;
		maxY = Float.MIN_VALUE;
		for (int i = 0; i < pointsY.length; i++) {
			if (pointsY[i] > maxY) maxY = pointsY[i];
			if (pointsY[i] < minY) minY = pointsY[i];
		}
	}
	
	public RectF getBounds() {
		return new RectF(minX, minY, maxX, maxY);
	}

	public float[] getPointsY() {
		return pointsY;
	}

    public float[] getPointsX() {
        return pointsX;
    }
}
