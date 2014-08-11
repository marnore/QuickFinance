package lt.marius.converter.views;

import java.sql.SQLException;
import java.util.List;

import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.UIUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.AndroidCharacter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.RuntimeExceptionDao;

public class CurrenciesDialog extends DialogFragment {
	
	public static final String TITLE = "title";
	final static public String DEFAULT_CURR_ID = "default_curr_id";
	private String defaultCurr;
	
	public interface CurrenciesDialogListener {
		void onCurrencySelected(Currency currency);
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putBoolean("dismiss", true);	//FIXME so far just dismiss it since it is tricky to save state listener
		super.onSaveInstanceState(bundle);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = savedInstanceState == null ? getArguments() : savedInstanceState;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("dismiss", false)) {
				UIUtils.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						dismiss();
					}
				});
				return builder.create();
			}
		}
		defaultCurr = args.getString(DEFAULT_CURR_ID);
		builder.setTitle(args.getString(TITLE));
		builder.setCancelable(true);
		ListView list = new ListView(getActivity());
		CurrenciesAdapter adapter = new CurrenciesAdapter();
		list.setAdapter(adapter);
		list.setOnItemClickListener(clickListener);
		builder.setView(list);
		int position = -1;
		for (int i = 0, n = adapter.getCount(); i < n; i++) {
			if (adapter.getItem(i).getCurrencyCodeShort().equals(defaultCurr)) {
				position = i;
			}
		}
		if (position != -1) list.setSelection(position);
		
		return builder.create();
	}
	
	//bad design for fragments..
	private CurrenciesDialogListener listener;
	
	public void setListener(CurrenciesDialogListener l) {
		listener = l;
	}
	
	private OnItemClickListener clickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if (listener != null && arg1.getTag() != null) {
				listener.onCurrencySelected((Currency) arg1.getTag());
			}
			dismiss();
		}
	};
	
	
	
	private class CurrenciesAdapter extends BaseAdapter {

		private List<Currency> currencies;
		
		public CurrenciesAdapter() {
			RuntimeExceptionDao<Currency, Integer> dao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
			try {
				currencies = dao.queryBuilder().orderBy(Currency.CURRENCY_CODE_SHORT, true).query();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		@Override
		public int getCount() {
			return currencies == null ? 0 : currencies.size();
		}

		@Override
		public Currency getItem(int position) {
			return currencies == null ? null : currencies.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_curr_dialog, parent, false);
			}
			Currency cur = getItem(position);
			ImageView iv = (ImageView)convertView.findViewById(R.id.iv_flag);
			iv.setImageBitmap(UIUtils.getFlagBitmap(getActivity().getApplicationContext(), cur.getShortCode()));
			TextView tv = (TextView)convertView.findViewById(R.id.text_currency_name);
			tv.setText(cur.getCurrencyTitle() + " (" + cur.getShortCode() + ") ");
			if (cur.getShortCode().equals(defaultCurr)) {
				convertView.setBackgroundColor(Color.parseColor("#6033b5e5"));
			} else {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
					convertView.setBackground(null);
				} else {
					convertView.setBackgroundDrawable(null);
				}
			}
			convertView.setTag(cur);
			return convertView;
		}
		
	}
}
