package lt.marius.converter.curview;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import lt.marius.converter.MainActivity;
import lt.marius.converter.Option;
import lt.marius.converter.R;
import lt.marius.converter.chartview.ChartFragment;
import lt.marius.converter.curview.CurrencyView.OnValueChangeListener;
import lt.marius.converter.curview.CurrencyView.OnValueStoredListener;
import lt.marius.converter.data.CurrencyProvider;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.groupview.GroupsController;
import lt.marius.converter.network.NetworkUtils;
import lt.marius.converter.transactions.CurrencyStored;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.user.UsersController;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.UIUtils;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.j256.ormlite.dao.RuntimeExceptionDao;


class CurrencyController implements OnValueChangeListener, OnValueStoredListener {
	
	private Context mContext;
	private ViewGroup mLayoutHolder;
	private Vector<Currency> mModels;
	private Vector<CurrencyView> mViews;
	private int baseCurrencyID;
	
	private String mLastValue = null;
	private CurrencyView mLastUpdatedView = null;
	private RuntimeExceptionDao<Currency, Integer> currencyDao;
	private RuntimeExceptionDao<CurrencyStored, Integer> currencyStoreDao;
	private List<Option> incomeOptions;
	private List<Option> expensesOptions;
	private Fragment ownerFragment;
	
	public CurrencyController(Context c, ViewGroup layoutHolder, Fragment ownerFragment) {
		mLayoutHolder = layoutHolder;
		mContext = c;
		mModels = new Vector<Currency>();
		mViews = new Vector<CurrencyView>();
		currencyDao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
		currencyStoreDao = DatabaseUtils.getHelper().getCachedDao(CurrencyStored.class);
		this.ownerFragment = ownerFragment;
	}
	
	
	public void addCurrency(Currency currency) {
		mModels.add(currency);
	}
	
	public void remove(int position) {
		mModels.remove(position);
		View v = mViews.get(position).getView();
		((ViewGroup)v.getParent()).removeView(v);
		mViews.remove(position);
	}
	
	public void insert(Currency currency, int pos) {
		mModels.insertElementAt(currency, pos);
		CurrencyView cv = new CurrencyView(mContext, currency, mLayoutHolder);
		cv.setOnValueChangeListener(CurrencyController.this);
		cv.setOnValueStoredListener(CurrencyController.this);
		mViews.insertElementAt(cv, pos);
	}
	
	public Currency getModel(int pos) {
		return mModels.get(pos);
	}
	
	public int getModelCount() {
		return mModels.size();
	}
	
	/**
	 * Fresh view for adapter. Without parent
	 * @param position
	 * @param convertView
	 * @return
	 */
	public View requestView(int position, View convertView) {
		View v = mViews.get(position).getView();
		if (v.getParent() != null) {
			((ViewGroup)v.getParent()).removeView(v);
		}
		return v;
	}

	/**
	 * Initializes user selected currencies to the layout
	 * @return number of currencies initialized
	 */
	public int initCurrencies() {
		UsersController users = UsersController.getInstance();
		List<Currency> currencies = users.getUserCurrencies(null);

		for (Currency curr : currencies) {
			addCurrency(curr);
		}
		
		TransactionsGroupsController tgc = TransactionsGroupsController.getInstance();
		incomeOptions = new ArrayList<Option>();
		TransactionsGroup incomeGroup = tgc.getDefaultIncomeGroup();
		incomeOptions.add(new Option("Default income", GroupsController.getGroupImage(incomeGroup, mContext), 0, incomeGroup));
		for (TransactionsGroup group : tgc.getGroups(incomeGroup)){
			incomeOptions.add(new Option(group.getName(), GroupsController.getGroupImage(group, mContext), 0, group));
		}
		
		expensesOptions = new ArrayList<Option>();
		TransactionsGroup expensesGroup = tgc.getDefaultExpensesGroup();
		
		expensesOptions.add(new Option("Default expense", GroupsController.getGroupImage(expensesGroup, mContext), 0, expensesGroup));
		for (TransactionsGroup group : tgc.getGroups(expensesGroup)){
			expensesOptions.add(new Option(group.getName(), GroupsController.getGroupImage(group, mContext), 0, group));
		}
		return currencies.size();
	}
	
	private List<Currency> initDefaultCurrencies() {
		UsersController users = UsersController.getInstance();
		Currency curr = new Currency(BigDecimal.ONE, "EUR", "Euro");
		currencyDao.create(curr);
		users.addCurrency(curr, null, 1);
		curr = new Currency(new BigDecimal(0.11627907), "SEK", "Kronos");
		currencyDao.create(curr);
		users.addCurrency(curr, null, 2);
		curr = new Currency(new BigDecimal(0.289620019), "LTL", "Litai");
		currencyDao.create(curr);
		users.addCurrency(curr, null, 3);
		return currencyDao.queryForAll();
	}
	
	public void initViews() {
		
		for (Currency c : mModels) {
			CurrencyView cv = new CurrencyView(mContext, c, mLayoutHolder);
			cv.setIncomeOptions(incomeOptions);
			cv.setExpensesOptions(expensesOptions);
			cv.setOnValueChangeListener(CurrencyController.this);
			cv.setOnValueStoredListener(CurrencyController.this);
			mViews.add(cv);
		}
	}

	public void onValueChanged(CurrencyView view, String newValue) {
		try {
			BigDecimal dd = new BigDecimal(newValue);
			int viewID = mViews.indexOf(view);
			if (viewID != -1) {	//get value in base units
				dd = mModels.get(viewID).getValue(dd);
			}
			CurrencyView v;	//update all the views
			for (int i = 0, n = mViews.size(); i < n; i++) {
				v = mViews.get(i);
				if (v == view) continue;
				BigDecimal d = dd.divide(mModels.get(i).getCurrency(), 20, RoundingMode.UP);
				v.updateValue(d.doubleValue());
			}
			mLastValue = newValue;	//save entered value for future updates
			mLastUpdatedView  = view;
		} catch (NumberFormatException ex) {
			
		}
	}

	@Override
	public void onValueAdded(CurrencyView view, String newValue,
			Date date, TransactionsGroup group) {
		BigDecimal value;
		try {
			value = new BigDecimal(newValue);
			storeValue(view, value, date, group);
			updateChart();
		} catch (NumberFormatException ex) {
		}
	}
	
	@Override
	public void onValueSubstracted(CurrencyView view, String newValue,
			Date date, TransactionsGroup group) {
		BigDecimal value;
		try {
			value = new BigDecimal(newValue);
			value = value.negate();		//this is expenses
			storeValue(view, value, date, group);
			updateChart();
		} catch (NumberFormatException ex) {
		}
	}
	
	private void storeValue(CurrencyView view, BigDecimal value, Date date, TransactionsGroup group) {
		int ind = mViews.indexOf(view);
		Currency model = mModels.get(ind);
		if (group == null) {
			if (value.compareTo(BigDecimal.ZERO) == 1) {	//greater
				group = TransactionsGroupsController.getInstance().getDefaultIncomeGroup();
			} else { //lesser
				group = TransactionsGroupsController.getInstance().getDefaultExpensesGroup();
			}
		}
		if (date == null) {
			date = new Date();
		}
		CurrencyStored transaction = new CurrencyStored(model, value, date, group);
		currencyStoreDao.create(transaction);
		if (value.compareTo(BigDecimal.ZERO) == 1) {
			Toast.makeText(mContext.getApplicationContext(), 
				mContext.getString(R.string.added_income, value.doubleValue(), model.getCurrencyTitle(), group.getName()),
				Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext.getApplicationContext(), 
					mContext.getString(R.string.added_expenses,
							value.negate(), model.getCurrencyTitle(), group.getName()),
					Toast.LENGTH_SHORT).show();
		}
		
		for (CurrencyView v : mViews) {
			v.clearValue();
		}
	}
	
	private void updateChart() {
		//TODO this is hacky. Needs to be fixed
		MainActivity act = (MainActivity) ownerFragment.getActivity();
		ChartFragment cf = (ChartFragment)act.getFragmentByTag(MainActivity.CHART_FRAGMENT_TAG);
		if (cf != null) {
			cf.refresh();
		}
	}
	
	public double getStoredAmount() {
		List<CurrencyStored> all = currencyStoreDao.queryForAll();
		double ret = 0;
		for (CurrencyStored cs : all) {
			ret += cs.getBaseDouble();
		}
		return ret;
	}


	public void destroy() {
		for (int i = 0; i < mViews.size(); i++) {
			remove(i);
		}
		mViews = null;
		mModels = null;
		mLayoutHolder.removeAllViews();
	}

	
}
