package lt.marius.converter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;

public class SectionAdapter extends BaseAdapter implements DragSortListView.DropListener {
    
    private final static int SECTION_DIV = 0;
    private final static int SECTION_ONE = 1;
    private final static int SECTION_TWO = 2;

    private List<String> mData;

    private int mDivPos;

    private LayoutInflater mInflater;
    private Context context;

    public SectionAdapter(Context context, List<String> names) {
        super();
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = names;
        mDivPos = names.size() / 2;
    }

    @Override
    public void drop(int from, int to) {
        if (from != to) {
            String data = mData.remove(dataPosition(from));
            mData.add(dataPosition(to), data);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mData.size() + 1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != mDivPos;
    }

    public int getDivPosition() {
        return mDivPos;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public String getItem(int position) {
        if (position == mDivPos) {
            return "Something";
        } else {
            return mData.get(dataPosition(position));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDivPos) {
            return SECTION_DIV;
        } else if (position < mDivPos) {
            return SECTION_ONE;
        } else {
            return SECTION_TWO;
        }
    }

    private int dataPosition(int position) {
        return position > mDivPos ? position - 1 : position;
    }

    public Drawable getBGDrawable(int type) {
        Drawable d = null;
//        if (type == SECTION_ONE) {
//            d = context.getResources().getDrawable(R.drawable.bg_handle_section1_selector);
//        } else {
//            d = context.getResources().getDrawable(R.drawable.bg_handle_section2_selector);
//        }
        d.setLevel(3000);
        return d;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);

        View v = null;
        if (convertView != null) {
            v = convertView;
        } else if (type != SECTION_DIV) {
//            v = mInflater.inflate(R.layout.list_item_bg_handle, parent, false);
//            v.setBackgroundDrawable(getBGDrawable(type));
        } else {
//            v = mInflater.inflate(R.layout.section_div, parent, false);
        }

        if (type != SECTION_DIV) {
            // bind data
            ((TextView) v).setText(mData.get(dataPosition(position)));
        }

        return v;
    }
}
