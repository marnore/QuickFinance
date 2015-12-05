package lt.marius.converter.currselect;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider;
import lt.marius.converter.data.CurrencyProvider.CurrencyUpdatedListener;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.network.NetworkStateProvider;
import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.settings.SettingsProvider.Setting;
import lt.marius.converter.user.UsersController;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.UIUtils;

public class CurrenciesSelectionFragment extends DialogFragment implements CurrSelectChangeListener {

	private List<Currency> allCurrencies;
	private List<Currency> userCurrencies;
	private Map<String, Currency> userCheckedCurrencies;
	
	private CurrenciesAdapter adapter;
	private ListView listView;
	private EditText searchField;
	private RuntimeExceptionDao<Currency, Integer> currDao;
	private Context appContext;
	
	@Override
	public void onDestroy() {
		currDao = null;
		((CurrSelectActivity)getActivity()).removeListener(this);
		super.onDestroy();
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		
		appContext = getActivity().getApplicationContext();
		((CurrSelectActivity)getActivity()).addListener(this);
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		UsersController usersController = UsersController.getInstance();
		userCurrencies = usersController.getUserCurrencies(null);
		userCheckedCurrencies = new HashMap<String, Currency>();
		for (Currency c : userCurrencies) {	//hash for quick access
			userCheckedCurrencies.put(c.getShortCode(), c);
		}
		
		currDao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
		try {
			allCurrencies = currDao.queryBuilder().orderBy(Currency.CURRENCY_NAME, true).query();
		} catch (SQLException e) {
			allCurrencies = new ArrayList<>();
		}

		appContext = inflater.getContext().getApplicationContext();
		View view = inflater.inflate(R.layout.fragment_curr_list, container, false);
		listView = (ListView) view.findViewById(R.id.drag_sort_list);
		
//		searchField = (EditText)view.findViewById(R.id.edit_search);
//		searchField.setVisibility(View.GONE);
//		listView.setDropListener(onDrop);
//		listView.setRemoveListener(onRemove);
		boolean initialized = SettingsProvider.getStored().getSetting(Setting.CURRENCIES_INIT, Boolean.class);
		boolean updateRunning = SettingsProvider.getStored().getSetting(Setting.CURRENCIES_UPDATE, Boolean.class);
		if (allCurrencies.isEmpty() || !initialized) {
			//Check connection first
			if (NetworkStateProvider.getInstance().isConnected()) {
				allCurrencies.clear();
//				final IsoCode[] vals = IsoCode.values();
				Currency base = new Currency(BigDecimal.ONE, IsoCode.EUR.getName(), "");
				
				final CurrencyProvider provider = new CurrencyProvider(inflater.getContext(), base);
				final IsoCode[] vals = provider.getSupportedIsoCodes();
				new Thread() {
					private int updated;
					public void run() {
						SettingsProvider.getStored().putSetting(Setting.CURRENCIES_UPDATE, true);
						updated = 0;
						UIUtils.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								if (getActivity() != null) {
									(getActivity())
									.setProgressBarVisibility(Boolean.TRUE);
									(getActivity())
										.setProgress(0);
								}
//									.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
							}
						});
						boolean success = provider.downloadAllCurrencies(vals, new CurrencyUpdatedListener() {
							
							@Override
							public void onCurrencyUpdated(final Currency cur) {
								updated++;
								UIUtils.runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										allCurrencies.add(cur);
										adapter.notifyDataSetChanged();	
										if (getActivity() != null) {
											(getActivity())
												.setProgress((int) (10000.d / vals.length * updated));
										}
									}
								});
							}
						});
						System.out.println("Updated at startup " + updated + "/" + vals.length);
						UIUtils.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								if (getActivity() != null) {
									(getActivity())
										.setProgressBarVisibility(Boolean.FALSE);
								}
							}
						});
						if (success) {
							chooseCurrencyForUser();
							if (updated == vals.length) {
								SettingsProvider.getStored().putSetting(Setting.CURRENCIES_INIT, true);
							}
						} else {
							UIUtils.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									UIUtils.showOkDialog(getActivity(), "",
										appContext.getString(R.string.error_download_failed), 
										new OnDismissListener() {
											
											@Override
											public void onDismiss(DialogInterface dialog) {
												getActivity().setResult(Activity.RESULT_CANCELED);
												getActivity().finish();
											}
										});
								}
							});
						}
						SettingsProvider.getStored().putSetting(Setting.CURRENCIES_UPDATE, false);
					}
				}.start();
			} else {
				Toast.makeText(appContext, appContext.getString(R.string.error_no_internet_on_startup), Toast.LENGTH_SHORT).show();
			}
		}
		
		adapter = new CurrenciesAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos, long arg3) {
				CheckBox cb = (CheckBox)view.findViewById(R.id.check_currency);
				if (cb != null) {
					cb.setChecked(!cb.isChecked());
				}
			}
			
		});
        UIUtils.fastScrollHack(listView);
		return view;
	}
	
	private void chooseCurrencyForUser() {
		if (DatabaseUtils.getHelper() != null) {	//DB already closed.
			String code = Locale.getDefault().getISO3Country();
			RuntimeExceptionDao<Currency, Integer> dao 
				= DatabaseUtils.getHelper().getCachedDao(Currency.class);
			List<Currency> list = dao.queryForEq(Currency.CURRENCY_CODE_SHORT, code);
			if (list.isEmpty()) {
				list = dao.queryForEq(Currency.CURRENCY_CODE_SHORT, IsoCode.EUR.getName());
			}
			if (!list.isEmpty()) {
				final Currency c = list.get(0);
				UIUtils.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if (getActivity() != null) {	//FIX crash 1
							((CurrSelectActivity)getActivity()).currencyAdded(c);
						}
					}
				});
			}
		}
	}
	
	private class CurrenciesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return allCurrencies.size();
		}

		@Override
		public Currency getItem(int position) {
			return allCurrencies.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Currency cur = getItem(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_curr_select, null);
			}
			ImageView iv = (ImageView)convertView.findViewById(R.id.iv_flag);
			iv.setImageBitmap(UIUtils.getFlagBitmap(appContext, cur.getShortCode()));
			TextView tv = (TextView)convertView.findViewById(R.id.text_currency_name);
			tv.setText(cur.getCurrencyTitle() + " (" + cur.getShortCode() + ") ");
			CheckBox cb = (CheckBox)convertView.findViewById(R.id.check_currency);
			cb.setOnCheckedChangeListener(null);
			cb.setChecked(userCheckedCurrencies.containsKey(cur.getShortCode()));
			cb.setTag(cur);
			cb.setOnCheckedChangeListener(checkChangeListener);
			return convertView;
		}
    	
    }

	private OnCheckedChangeListener checkChangeListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				((CurrSelectActivity)getActivity()).currencyAdded((Currency) buttonView.getTag());
			} else {
				((CurrSelectActivity)getActivity()).currencyRemoved((Currency) buttonView.getTag());
			}
		}
	};
	
	@Override
	public void onSearchQuery(String query) {
		if (query.length() == 0) {
            try {
                allCurrencies = currDao.queryBuilder().orderBy(Currency.CURRENCY_NAME, true).query();
            } catch (SQLException ignore) {
            }
            adapter.notifyDataSetChanged();
		} else {
			try {
				String q = "%" + query + "%";
				allCurrencies = currDao.queryBuilder()
                        .orderBy(Currency.CURRENCY_NAME, true)
						.where().like(Currency.CURRENCY_NAME, q)
						.or().like(Currency.CURRENCY_CODE_SHORT, q)
						.query();
				adapter.notifyDataSetChanged();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onCurrencyAdded(Currency currency) {
		userCheckedCurrencies.put(currency.getShortCode(), currency);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCurrencyRemoved(Currency currency) {
		userCheckedCurrencies.remove(currency.getShortCode());
		adapter.notifyDataSetChanged();
	}
}
