package lt.marius.charts;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class Series {
	
	protected String title;
	protected Paint paint;
	protected boolean visible;
    private int groupId;

    public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint paint) {
		this.paint = new Paint(paint);
	}

	public int getColor() {
		return paint.getColor();
	}

	public void setColor(int color) {
		paint.setColor(color);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
//	public abstract void draw(Canvas canvas, RectF fitInto);
	public abstract RectF getBounds();
	public abstract void draw(Canvas canvas, RectF bounds, float scaleX, float scaleY);

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }
}
