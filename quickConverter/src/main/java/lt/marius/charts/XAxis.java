package lt.marius.charts;

import lt.marius.converter.utils.UIUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

public class XAxis extends Axis {

	public static final float TEXT_OFFSET = 15f;
	private float dp, minCellWidth;
	private float xTextOffset, yTextOffset;
	private float minTextWidth;
	
	public XAxis(float minValue, float maxValue, float step, Context c) {
		super(minValue, maxValue, step, c);
		xTextOffset = UIUtils.dpToPx(TEXT_OFFSET, c);
		yTextOffset = UIUtils.dpToPx(YAxis.TEXT_OFFSET, c);

		dp = UIUtils.dpToPx(2.75f, getContext());
		minCellWidth = UIUtils.dpToPx(7.5f, getContext());
		minTextWidth = UIUtils.dpToPx(15.f, getContext());
	}

	@Override
	public void draw(Canvas canvas, RectF b, float scaleX, float scaleY) {
		float number = getMinValue();
		float step = getStep() * scaleX;
		if (step < minCellWidth) {
			setStep(getStep() + 1);
			step = getStep() * scaleX;
		}
		float w = minTextWidth;
		for (float i = b.left + yTextOffset; i <= b.right; i += step) {
			canvas.drawLine(i, b.bottom - xTextOffset,
					i, b.top, getPaint());
			w += step;
			if (w >= minTextWidth) {
				getPaint().setTextSize(xTextOffset - dp - dp);
				canvas.drawText(String.format("%2.0f", number), i - step / 2,
						b.bottom - dp, getPaint());
				w = 0;
			}
			number += getStep();
		}
	}

}
