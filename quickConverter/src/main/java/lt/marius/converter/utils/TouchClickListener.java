package lt.marius.converter.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class TouchClickListener implements OnTouchListener {

	private OnClickListener listener;

	public TouchClickListener(OnClickListener listener) {
		this.listener = listener;
	}
	
	private float dx, dy, sx, sy;
	private boolean canClick;
	
	private static final float CLICK_THRESHOLD = 20.f;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			sx = event.getX();
			sy = event.getY();
			canClick = true;
			return true;
		case MotionEvent.ACTION_MOVE:
			dx = event.getX() - sx;
			dy = event.getY() - sy;
			if (Math.sqrt(dx * dx + dy * dy) > CLICK_THRESHOLD) {
				canClick = false;
				return false;
			}
			return true;
		case MotionEvent.ACTION_CANCEL:
			return false;
		case MotionEvent.ACTION_UP:
			dx = event.getX() - sx;
			dy = event.getY() - sy;
			if (Math.sqrt(dx * dx + dy * dy) <= CLICK_THRESHOLD && canClick) {
				if (listener != null) listener.onClick(v);
			}
			return true;
		}
		return false;
	}

}
