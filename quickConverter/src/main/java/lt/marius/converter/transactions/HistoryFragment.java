package lt.marius.converter.transactions;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.groupview.GroupsController;
import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.settings.SettingsProvider.Setting;
import lt.marius.converter.transactions.HistoryItemEditFragment.HistoryItemEditCallback;
import lt.marius.converter.user.UsersController;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.GeneralUtils;
import lt.marius.converter.utils.UIUtils;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.nolanlawson.supersaiyan.OverlaySizeScheme;
import com.nolanlawson.supersaiyan.SectionedListAdapter;
import com.nolanlawson.supersaiyan.Sectionizer;
import com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView;
import static java.util.Calendar.*;

@SuppressLint("NewApi")
public class HistoryFragment extends Fragment implements HistoryItemEditCallback {

	private ListView listView;
	private HistoryAdapter adapter;
	private java.text.DateFormat sdf;
	private Currency currency;
	
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ){
			sdf = new SimpleDateFormat("yyyy LLLL", Locale.getDefault());
		} else {
			sdf = new SimpleDateFormat("yyyy MMMM", Locale.getDefault());
		}
	}
	
	private SuperSaiyanScrollView superSaiyanScrollView;
    private SectionedListAdapter<HistoryAdapter> sectionedAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		listView = new ListView(inflater.getContext());
		listView.setVerticalScrollBarEnabled(false);
		adapter  = new HistoryAdapter();
		currency = UsersController.getInstance().getUserSelectedCurrency();

		superSaiyanScrollView = new SuperSaiyanScrollView(container.getContext());
		superSaiyanScrollView.setOverlaySizeScheme(OverlaySizeScheme.Normal, true);
		sectionedAdapter = SectionedListAdapter.Builder.create(container.getContext(), adapter)
                .setSectionizer(new Sectionizer<CurrencyStored>(){

					@Override
					public CharSequence toSection(CurrencyStored input) {
						return sdf.format(input.getDate());
					}
                })
//                .sortKeys()
//                .sortValues(new Comparator<CurrencyStored>() {
//                    
//                    public int compare(CurrencyStored left, CurrencyStored right) {
//                        return left.getDate().compareTo(right.getDate());
//                    }
//                })
                .build();
        
		listView.setAdapter(sectionedAdapter);
		listView.setOnItemClickListener(clickListener);
		superSaiyanScrollView.addView(listView);
		
//		listView.setAdapter(adapter);
//		listView.setFastScrollEnabled(true);
//		listView.setFastScrollAlwaysVisible(true);
		
		return superSaiyanScrollView;
	}
	
		
	@Override
	public void onUpdated(int year, int month, int day, double value, int id) {
		adapter.closeIterator();
		CurrencyStored curr = dao.queryForId(id);
		RuntimeExceptionDao<Currency, Integer> cdao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
		cdao.refresh(curr.getCurrency());
		if (curr.getBaseDouble() < 0) {
			if (value > 0) value *= -1;
		} else {
			if (value < 0) value *= -1;
		}
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		cal.setTime(curr.getDate());
		cal.set(YEAR, year);
		cal.set(MONTH, month);
		cal.set(DAY_OF_MONTH, day);
		curr.update(cal.getTime(), value);
		dao.update(curr);
		adapter.notifyDataSetChanged();
		sectionedAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null && currency != null && !currency.equals(UsersController.getInstance().getUserSelectedCurrency())) {
			currency = UsersController.getInstance().getUserSelectedCurrency();
			sectionedAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onDeleted(int id) {
		adapter.closeIterator();
		dao.deleteById(id);
		adapter.notifyDataSetChanged();
		sectionedAdapter.notifyDataSetChanged();
	}
	
	private OnItemClickListener clickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
			HistoryItemEditFragment frag = new HistoryItemEditFragment();
			CurrencyStored c = (CurrencyStored)v.getTag();
			Bundle args = new Bundle();
			args.putInt("id", c.getId());
			Calendar cal = Calendar.getInstance(TimeZone.getDefault());
			cal.setTime(c.getDate());
			args.putInt("year", cal.get(YEAR));
			args.putInt("month", cal.get(MONTH));
			args.putInt("day", cal.get(DAY_OF_MONTH));
			args.putDouble("value", c.getAmount().doubleValue());
			
			frag.setArguments(args);
			frag.show(getChildFragmentManager(), getTag() + "#dialog");
		}
	};
	
	@Override
	public void onDestroyView() {
		adapter.destroy();
		super.onDestroyView();
	}
	
	private RuntimeExceptionDao<CurrencyStored, Integer> dao;

	private class HistoryAdapter extends BaseAdapter {

		private RuntimeExceptionDao<TransactionsGroup, Integer> groupsDao;
		private CloseableIterator<CurrencyStored> iter;
		private int currPos = 0;
		private int count;
		private java.text.DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
//		private java.text.DateFormat timeFormat = SimpleDateFormat.getTimeInstance();
		
		
		private HistoryAdapter() {
			dao = DatabaseUtils.getHelper().getCachedDao(CurrencyStored.class);
			groupsDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
			initIterator();
		}
		
		public void closeIterator() {
			destroy();
			count = 0;
			currPos = 0;
		}
		
		private void initIterator() {
			PreparedQuery<CurrencyStored> query = null;
			try {
				query = dao.queryBuilder().orderBy(CurrencyStored.COLUMN_DATE, false).prepare();
			} catch (SQLException e) {
			}
			iter = dao.iterator(query);
			currPos = 0;
			count = (int) dao.countOf();
		}
		
		@Override
		public void notifyDataSetChanged() {
			try {
				iter.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			initIterator();
			super.notifyDataSetChanged();
		}
		
		public void destroy() {
			try {
				iter.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			return count;
		}

		@Override
		public CurrencyStored getItem(int position) {
			CurrencyStored c = null;
			try {
				c = iter.moveRelative(position - currPos);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			currPos = position;
			return c;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CurrencyStored c = getItem(position);
			if (c == null) {
				return convertView;
			}
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
			}
			groupsDao.refresh(c.getGroup());
			TextView tv = (TextView)convertView.findViewById(R.id.text_datetime);
//			tv.setText(dateFormat.format(c.getDate()) + "\n" + timeFormat.format(c.getDate()));
			tv.setText(dateFormat.format(c.getDate()));
			tv = (TextView)convertView.findViewById(R.id.text_title);
			tv.setText(c.getGroup().getName());
			tv = (TextView)convertView.findViewById(R.id.text_inc);
			if (c.getBaseDouble() > 0) {
				tv.setVisibility(View.VISIBLE);
				tv.setText(String.format("%.2f", GeneralUtils.getValue(c, currency)));
			} else {
				tv.setVisibility(View.GONE);
			}
			tv = (TextView)convertView.findViewById(R.id.text_exp);
			if (c.getBaseDouble() < 0) {
				tv.setVisibility(View.VISIBLE);
				tv.setText(String.format("%.2f", GeneralUtils.getValue(c, currency)));
			} else {
				tv.setVisibility(View.GONE);
			}
			ImageView iv = (ImageView)convertView.findViewById(R.id.image);
			iv.setImageBitmap(GroupsController.getGroupImage(c.getGroup(), parent.getContext()));
			//			tv.setHeight(UIUtils.dpToPx(48, parent.getContext()));
			convertView.setTag(c);
			return convertView;
		}
		
	}
	
}
