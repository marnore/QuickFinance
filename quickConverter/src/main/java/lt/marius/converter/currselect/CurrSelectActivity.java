package lt.marius.converter.currselect;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import lt.marius.converter.MainActivity.PagerTabsListener;
import lt.marius.converter.MainActivity.TabsAdapter;
import lt.marius.converter.MainActivity.TabsAdapter.FragmentInfo;
import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.settings.SettingsProvider.Setting;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.UIUtils;
import lt.marius.pinchtoclose.PinchToClose;
import lt.marius.pinchtoclose.PinchToClose.CustomFinishCallback;

public class CurrSelectActivity extends ActionBarActivity {

	private static final int PAGER_ID = R.id.tabs_pager;
	public static final String USER_CURRENCIES_FRAGMENT_TAG = UIUtils.makeFragmentName(PAGER_ID, 1);
	public static final String ALL_CURRENCIES_FRAGMENT_TAG = UIUtils.makeFragmentName(PAGER_ID, 0);
	
	private SearchView search;
	private ArrayList<CurrSelectChangeListener> listeners;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
//		UIUtils.setLocale("lt", getBaseContext());
		requestWindowFeature(Window.FEATURE_PROGRESS);
		getSupportActionBar().setTitle(R.string.choose_your_currencies);
		DatabaseUtils.initDatabse(getApplicationContext());
		TransactionsGroupsController.init(getApplicationContext());
		
		listeners = new ArrayList<CurrSelectChangeListener>();
		
//		boolean landscape = getResources().getBoolean(R.bool.landscape);
		boolean landscape = false;
		if (landscape) {
			LinearLayout l = new LinearLayout(this);
			l.setId(PAGER_ID);
			l.setOrientation(LinearLayout.HORIZONTAL);
//			FrameLayout fm1 = new FrameLayout(this);
//			FrameLayout fm2 = new FrameLayout(this);
//			fm1.setId(0x7f110001);
//			fm2.setId(0x7f110002);
//			l.addView(fm1, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f));
//			l.addView(fm2, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f));
			setContentView(l);
			Fragment cf = getSupportFragmentManager().findFragmentByTag(ALL_CURRENCIES_FRAGMENT_TAG);
			if (cf == null) {
				cf = new CurrenciesSelectionFragment();
			}
				getSupportFragmentManager().beginTransaction()
				.remove(cf)
				.add(PAGER_ID, cf, ALL_CURRENCIES_FRAGMENT_TAG)
				.commit();
			Fragment uf = getSupportFragmentManager().findFragmentByTag(USER_CURRENCIES_FRAGMENT_TAG);
//			if (uf == null) {
//				uf = new UserCurrenciesFragment();
//			}
//			if (!uf.isAdded()) {
//				getSupportFragmentManager().beginTransaction()
//				.add(fm2.getId(), uf, USER_CURRENCIES_FRAGMENT_TAG)
//				.commit();
//			}
		} else {
			ViewPager pager = new ViewPager(this);
			pager.setId(PAGER_ID);
			setContentView(pager);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			
			PagerTabsListener pagerTabsListener = new PagerTabsListener(pager);
			List<FragmentInfo> fragments = new ArrayList<FragmentInfo>();
			
			Bundle args = new Bundle();
			
			ActionBar.Tab tab = getSupportActionBar().newTab();
			tab.setText(getString(R.string.tab_all_currencies));
			tab.setTabListener(pagerTabsListener);
	//		tab.setTabListener(new TabsListener<MyFragment>(MainActivity.this, "first", MyFragment.class, args));
			getSupportActionBar().addTab(tab);
	
			fragments.add(new FragmentInfo(CurrenciesSelectionFragment.class, ALL_CURRENCIES_FRAGMENT_TAG, args, tab));

			tab = getSupportActionBar().newTab();
			tab.setText(getString(R.string.tab_user_currencies));
			tab.setTabListener(pagerTabsListener);
	//		tab.setTabListener(new TabsListener<MyFragment>(MainActivity.this, "secnd", MyFragment.class, args));
			getSupportActionBar().addTab(tab);
			fragments.add(new FragmentInfo(UserCurrenciesFragment.class, USER_CURRENCIES_FRAGMENT_TAG, args, tab));
			
			TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager(), this, fragments);
			pager.setAdapter(adapter);
			pager.setOnPageChangeListener(adapter);
//			pager.setPageTransformer(true, new SlidePageTransformer());
		}
		PinchToClose.init(this, false, new CustomFinishCallback() {
			
			@Override
			public void finish(Activity activity) {
				setResult(Activity.RESULT_OK);
		    	SettingsProvider.getStored().putSetting(Setting.REFRESH_CURR_VIEW, true);
				activity.finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void addListener(CurrSelectChangeListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}
	
	public void removeListener(CurrSelectChangeListener l) {
		listeners.remove(l);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Used to put dark icons on light action bar
        boolean isLight = false;

        //Create the search view
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint(getString(R.string.search_currency));

        menu.add(0, 1, 1, getString(R.string.menu_search))
            .setIcon(R.drawable.ic_action_search)
            .setActionView(searchView)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                search = (SearchView) item.getActionView();
                search.setOnQueryTextListener(textListener);
                search.setOnCloseListener(closeListener);
//                search.addTextChangedListener(filterTextWatcher);
//                search.requestFocus();
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return true;
            case android.R.id.home:
            	setResult(Activity.RESULT_OK);
            	SettingsProvider.getStored().putSetting(Setting.REFRESH_CURR_VIEW, true);
            	finish();
            	return true;
        }   
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_OK);
    	SettingsProvider.getStored().putSetting(Setting.REFRESH_CURR_VIEW, true);
		finish();
	}
	
	public void currencyAdded(Currency c) {
		for (CurrSelectChangeListener l : listeners) {
        	l.onCurrencyAdded(c);
        }
	}
	
	public void currencyRemoved(Currency c) {
		for (CurrSelectChangeListener l : listeners) {
        	l.onCurrencyRemoved(c);
        }
	}
	
	private SearchView.OnCloseListener closeListener = new SearchView.OnCloseListener() {
		
		@Override
		public boolean onClose() {
			for (CurrSelectChangeListener l : listeners) {
	        	l.onSearchQuery("");
	        }
			return false;
		}
	};
	
	private SearchView.OnQueryTextListener textListener = new SearchView.OnQueryTextListener() {
		
		@Override
		public boolean onQueryTextSubmit(String query) {
			for (CurrSelectChangeListener l : listeners) {
	        	l.onSearchQuery("");
	        }
			return false;
		}
		
		@Override
		public boolean onQueryTextChange(String newText) {
			for (CurrSelectChangeListener l : listeners) {
	        	l.onSearchQuery(newText);
	        }
			return false;
		}
	};
	
	private TextWatcher filterTextWatcher = new TextWatcher() {
	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	        for (CurrSelectChangeListener l : listeners) {
	        	l.onSearchQuery(s.toString());
	        }
	    }

	};
	
	@Override
	protected void onDestroy() {
		DatabaseUtils.closeDatabase();
		super.onDestroy();
	}
}
