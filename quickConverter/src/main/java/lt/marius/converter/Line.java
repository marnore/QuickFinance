package lt.marius.converter;

public class Line {
	
	private float k, b;
	
	public Line(float x1, float y1, float x2, float y2) {
		float m = (y2 - y1) / (x2 - x1);
		k = m;
		b = -m * x1 + y1;
	}
	
	public boolean isPointAbove(float x, float y) {
		return y < k * x + b;
	}
	
	public boolean isPointBelow(float x, float y) {
		return y > k * x + b;
	}
	
}
