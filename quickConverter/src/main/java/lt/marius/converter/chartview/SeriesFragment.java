package lt.marius.converter.chartview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lt.marius.converter.R;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.DividerItemDecoration;

/**
 * Created by marius on 8/2/14.
 */
public class SeriesFragment extends DialogFragment {

    public static final String GROUP_PARENT_IDS = "group_parent_id";
    public static final String INITIAL_CHECHKED_STATE = "initial_checked_state";

    public SeriesFragment() {}

    public interface SeriesFragmentCallback {
        void onItemChecked(TransactionsGroup group, boolean checked);
    }

    private RecyclerView recycler;
    private ItemsAdapter mAdapter;

    private View buildView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_series, null, false);
        recycler = (RecyclerView) layout.findViewById(R.id.recyclerView);

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recycler.setHasFixedSize(true);

        // use a linear layout manager
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        recycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        int[] parentIds = getArguments().getIntArray(GROUP_PARENT_IDS);

        RuntimeExceptionDao<TransactionsGroup, Integer> dao
                = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
        List<TransactionsGroup> items = null;
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < parentIds.length; i++) {
            list.add(parentIds[i]);
        }
        try {
            items = dao.queryBuilder().where().in(TransactionsGroup.GROUP_ID, list).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // specify an adapter (see also next example)
        if (items != null) {
            mAdapter = new ItemsAdapter(items, getArguments().getBooleanArray(INITIAL_CHECHKED_STATE));
        }
        recycler.setAdapter(mAdapter);

        if (getParentFragment() instanceof SeriesFragmentCallback) {
            listener = (SeriesFragmentCallback) getParentFragment();
            mAdapter.setListener(listener);
        }

        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(buildView(LayoutInflater.from(getActivity()), savedInstanceState));

        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setTitle(R.string.legend);
        builder.setCancelable(true);

        return builder.create();
    }


    private SeriesFragmentCallback listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


    }

    private static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

        private List<TransactionsGroup> items;
        private boolean[] checkedItems;
        private SeriesFragmentCallback listener;

        public ItemsAdapter(List<TransactionsGroup> items, boolean[] initialStates) {
            this.items = items;
            checkedItems = new boolean[items.size()];
            for (int i = 0; i < checkedItems.length; i++) {
                checkedItems[i] = initialStates == null || initialStates[i];
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_chart_group, null));
        }

        private View.OnClickListener currencyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        };

        private CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                if (listener != null) {
                    ViewHolder holder = (ViewHolder) view.getTag();
                    listener.onItemChecked(items.get(holder.getPosition()), checked);
                }
            }
        };

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.view.setOnClickListener(currencyClickListener);
//            viewHolder.checkBox.setOnCheckedChangeListener(checkListener);
            TransactionsGroup g = items.get(i);

            Context context = viewHolder.flagImage.getContext();
            viewHolder.flagImage.setImageBitmap(BitmapFactory.decodeFile(g.getImagePath()));
            viewHolder.currencyTitle.setText(g.getName());
            viewHolder.checkBox.setOnCheckedChangeListener(null);
            viewHolder.checkBox.setChecked(checkedItems[i]);
            viewHolder.checkBox.setTag(viewHolder);
            viewHolder.checkBox.setOnCheckedChangeListener(checkListener);
            viewHolder.view.setTag(viewHolder);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setListener(SeriesFragmentCallback listener) {
            this.listener = listener;
        }


        static class ViewHolder extends RecyclerView.ViewHolder {

            private ViewGroup view;
            private ImageView flagImage;
            private TextView currencyTitle;
            private CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                this.view = (ViewGroup) itemView;
                flagImage = (ImageView) view.findViewById(R.id.iv_group_icon);
                currencyTitle = (TextView) view.findViewById(R.id.text_group_title);
                checkBox = (CheckBox) view.findViewById(R.id.check_group);
            }
        }



    }
}