package lt.marius.converter.test;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewSwapper<O extends View, N extends View> {

	O oldView;
	N newView;
	ViewGroup parent;
	ViewGroup.LayoutParams oldParams;
	private int viewIndex;
	
	public ViewSwapper(O oldView, N newView) {
		this.oldView = oldView;
		this.newView = newView;
	}
	
	private int findViewIndex(View v, ViewGroup parent) {
		int index = 0;
		for (index = 0; index < parent.getChildCount(); index++) {
			if (v == parent.getChildAt(index)){
				break;
			}
		}
		return index;
	}
	
	public O swap() {
		parent = (ViewGroup) oldView.getParent();
		oldParams = oldView.getLayoutParams();
		
		viewIndex = findViewIndex(oldView, parent);
		parent.removeViewAt(viewIndex);
		if (viewIndex < parent.getChildCount()) {
			parent.addView(newView, viewIndex, oldParams);
		} else {
			parent.addView(newView, oldParams);
		}
		return oldView;
	}
	
	public N swapBack() {
		parent.removeViewAt(viewIndex);
		if (viewIndex < parent.getChildCount()) {
			parent.addView(oldView, viewIndex, oldParams);
		} else {
			parent.addView(oldView, oldParams);
		}
		return newView;
	}

	public O getOld() {
		return oldView;
	}
	
	public N getNew() {
		return newView;
	}
	
}
