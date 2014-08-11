package lt.marius.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import lt.marius.converter.utils.UIUtils;

public class YAxis extends Axis {

	public static final float TEXT_OFFSET = 20f;
	private float textSize;
	private float minStep, maxStep;
	private float xTextOffset, yTextOffset;
	private float step;
	private float offsets[];
	
	public YAxis(float minValue, float maxValue, float step, Context c) {
		super(minValue, maxValue, step, c);
		xTextOffset = UIUtils.dpToPx(XAxis.TEXT_OFFSET, c);
		yTextOffset = UIUtils.dpToPx(YAxis.TEXT_OFFSET, c);
		textSize = UIUtils.dpToPx(10, c);
		minStep = UIUtils.dpToPx(25, c);
		maxStep = UIUtils.dpToPx(35, c);
		Paint p = getPaint();
		float padding = UIUtils.dpToPx(2, c);
		String txt = "0";
		offsets = new float[4];
		for (int i = 0; i < offsets.length; i++) {
			offsets[i] = yTextOffset - p.measureText(txt) - padding;
			txt += "0";
			if (offsets[i] < 0) offsets[i] = 0;
		}
	}

	private float getTextOffset(float number) {
		int length = 0;
		int nr = (int)number;
		while (nr >= 10 && length < offsets.length) {
			nr = nr / 10;
			length++;
		}
		if (length == offsets.length){
			return 0;
		}
		return offsets[length];
	}
	
	@Override
	public void draw(Canvas canvas, RectF b, float scaleX, float scaleY) {
		if (step == 0) {
			step = getStep();
		}
		float number = getMinValue();
		float dy = scaleY * step;
		while (dy > maxStep) {
			step = (int)step - 5;
			dy = scaleY * step;
		}
		while (dy < minStep) {
			step = (int)step + 5;
			dy = scaleY * step;
		}
		//TODO dynamic space for Y axis labels
//		float max = b.bottom / dy * step;
//		float width = getPaint().measureText(text)
		for (float i = b.bottom - xTextOffset; i >= 0; i -= dy) {
			canvas.drawLine(b.left + yTextOffset, i,
					b.right, i, getPaint());
			getPaint().setTextSize(textSize);
			canvas.drawText(getNumberText(number), b.left + getTextOffset(number), i, getPaint());
			number += step;
		}
	}
	
	private String getNumberText(float number) {
		if (number > 1e6) {
			return String.format("%.1fM", number / 1e6);
		}
		if (number > 1e5) {
			return String.format("%.0fK", number / 1000);
		}
		if (number > 1e4) {
			return String.format("%.1fK", number / 1000);
		}
		return String.format("%.0f", number);
	}

}
