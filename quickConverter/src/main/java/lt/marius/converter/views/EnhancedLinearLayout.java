package lt.marius.converter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class EnhancedLinearLayout extends LinearLayout {

	public interface EnhancedLayoutListener {
		void onSizeChanged(int width, int height);
	}

	public EnhancedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public EnhancedLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	private EnhancedLayoutListener listener;
	
	public void setListener(EnhancedLayoutListener listener) {
		this.listener = listener;
	}
	
	private int lastWidth, lastHeight;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (listener != null) {
			if (lastHeight != getMeasuredHeight() && lastWidth != getMeasuredWidth()) {
				lastWidth = getMeasuredWidth();
				lastHeight = getMeasuredHeight();
				listener.onSizeChanged(getMeasuredWidth(), getMeasuredHeight());
			}
		}
	}
//	
//	@Override
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		if (listener != null && changed) {
//			listener.onSizeChanged(r - l, b - t);
//		}
//		super.onLayout(changed, l, t, r, b);
//	}


}
