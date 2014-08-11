package lt.marius.charts;

import android.graphics.Canvas;
import android.graphics.RectF;

public class SeriesPie extends Series {

	private double value;
	private float start, sweep;

    public SeriesPie(double value) {
		this.value = value;
	}
	
	public void setStartSweep(float start, float sweep) {
		this.start = start;
		this.sweep = sweep;
	}
	
	public float getStart() {
		return start;
	}
	
	public float getSweep() {
		return sweep;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	
	//Unused stuff
	@Override
	public RectF getBounds() {
		return null;
	}

	@Override
	public void draw(Canvas canvas, RectF bounds, float scaleX, float scaleY) {
		
	}

}
