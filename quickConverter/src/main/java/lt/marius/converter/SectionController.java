package lt.marius.converter;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class SectionController extends DragSortController {

    private int mPos;
    private int mDivPos;

    private SectionAdapter mAdapter;

    DragSortListView mDslv;

    public SectionController(DragSortListView dslv, SectionAdapter adapter) {
        super(dslv, /*R.id.text*/0, DragSortController.ON_DOWN, 0);
        setRemoveEnabled(false);
        mDslv = dslv;
        mAdapter = adapter;
        mDivPos = adapter.getDivPosition();
    }

    @Override
    public int startDragPosition(MotionEvent ev) {
        int res = super.dragHandleHitPosition(ev);
        if (res == mDivPos) {
            return DragSortController.MISS;
        }

        int width = mDslv.getWidth();

        if ((int) ev.getX() < width / 3) {
            return res;
        } else {
            return DragSortController.MISS;
        }
    }

    @Override
    public View onCreateFloatView(int position) {
        mPos = position;

        View v = mAdapter.getView(position, null, mDslv);
        if (position < mDivPos) {
//            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_handle_section1));
        } else {
//            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_handle_section2));
        }
        v.getBackground().setLevel(10000);
        return v;
    }

    private int origHeight = -1;

    @Override
    public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
        final int first = mDslv.getFirstVisiblePosition();
        final int lvDivHeight = mDslv.getDividerHeight();

        if (origHeight == -1) {
            origHeight = floatView.getHeight();
        }

        View div = mDslv.getChildAt(mDivPos - first);

        if (touchPoint.x > mDslv.getWidth() / 2) {
            float scale = touchPoint.x - mDslv.getWidth() / 2;
            scale /= (float) (mDslv.getWidth() / 5);
            ViewGroup.LayoutParams lp = floatView.getLayoutParams();
            lp.height = Math.max(origHeight, (int) (scale * origHeight));
            floatView.setLayoutParams(lp);
        }

        if (div != null) {
            if (mPos > mDivPos) {
                // don't allow floating View to go above
                // section divider
                final int limit = div.getBottom() + lvDivHeight;
                if (floatPoint.y < limit) {
                    floatPoint.y = limit;
                }
            } else {
                // don't allow floating View to go below
                // section divider
                final int limit = div.getTop() - lvDivHeight - floatView.getHeight();
                if (floatPoint.y > limit) {
                    floatPoint.y = limit;
                }
            }
        }
    }

    @Override
    public void onDestroyFloatView(View floatView) {
        //do nothing; block super from crashing
    }

}
