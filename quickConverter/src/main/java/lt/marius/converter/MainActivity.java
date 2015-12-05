package lt.marius.converter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lt.marius.converter.MainActivity.TabsAdapter.FragmentInfo;
import lt.marius.converter.calcview.CalcFragment;
import lt.marius.converter.chartview.ChartFragment;
import lt.marius.converter.currselect.CurrSelectActivity;
import lt.marius.converter.curview.CurrencyFragment;
import lt.marius.converter.data.Updater;
import lt.marius.converter.groupview.GroupFragment;
import lt.marius.converter.network.NetworkStateProvider;
import lt.marius.converter.network.NetworkStateProvider.NetworkStateListener;
import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.settings.SettingsProvider.Setting;
import lt.marius.converter.transactions.HistoryFragment;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.utils.BackKeyHandler;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.UIUtils;
import lt.marius.converter.views.FilePickDialog;
import lt.marius.converter.views.FilePickDialog.FilePickDialogListener;
import lt.marius.pinchtoclose.PinchToClose;

public class MainActivity extends ActionBarActivity implements NetworkStateListener,
	FilePickDialogListener {

	private ViewGroup mainLayout;
	private CurrencyFragment currenciesFragment;
	private ChartFragment chartFragment;
	private TabsAdapter adapter;
	public static final String CURRENCIES_FRAGMENT_TAG = "currencies_fragment_tag";
	public static final String CHART_FRAGMENT_TAG = "chart_fragment_tag";
	public static final String GROUP_FRAGMENT_TAG = "group_fragment_tag";
	public static final String HISTORY_FRAGMENT_TAG = "history_fragment_tag";
	public static final String CALCULATOR_FRAGMENT_TAG = "calculator_fragment_tag";
	public static final String CURR_SELECT_TAG = "curr_select_tag";
	
	private AlertDialog connErrorDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
//		UIUtils.setLocale("lt", getBaseContext());		
        DatabaseUtils.initDatabse(getApplicationContext());

    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        TransactionsGroupsController.init(getApplicationContext());
        
        setContentView(R.layout.activity_main);

        PinchToClose.init(this);
        
        mainLayout = (ViewGroup) findViewById(R.id.ll_main);
        NetworkStateProvider.getInstance().startListening(this);
		boolean initialized = SettingsProvider.getStored().getSetting(Setting.CURRENCIES_INIT, Boolean.class);
        if (!initialized ) {
        	if (!NetworkStateProvider.getInstance().isConnected()) {
	        	connErrorDialog = new AlertDialog.Builder(MainActivity.this)
				.setMessage(R.string.error_no_internet_on_startup)
				.setNeutralButton(MainActivity.this.getString(R.string.ok),
						new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								MainActivity.this.finish();
							}
						})
				.setCancelable(false)
				.show();
        	} else {
//        		startActivityForResult(new Intent(this, CurrSelectActivity.class), 1);
        	}
        }
        initMainUI();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	setProgressBarIndeterminateVisibility(Boolean.FALSE);
    	NetworkStateProvider.getInstance().addListener(this);
    }
    
    @Override
    protected void onPause() {
    	NetworkStateProvider.getInstance().removeListener(this);
    	super.onPause();
    }
    
    @Override
    public void onBackPressed() {
    	if (!BackKeyHandler.get().backPressed()) {
    		super.onBackPressed();
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    	if (requestCode == 1) {
    		if (resultCode == Activity.RESULT_OK) {
    			
//    			ViewPager pager = (ViewPager) findViewById(R.id.tabs_pager);
//    			pager.removeAllViews();
//    			getSupportActionBar().removeAllTabs();
//    			initMainUI();
    		} else if (resultCode == Activity.RESULT_CANCELED){
    			finish();
    		}
    	}
    }
    
    private void initMainUI() {
    	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ViewPager pager = (ViewPager) findViewById(R.id.tabs_pager);
		PagerTabsListener pagerTabsListener = new PagerTabsListener(pager);
		
		if (adapter == null || adapter.getCount() == 0) {
		
			List<FragmentInfo> fragments = new ArrayList<FragmentInfo>();
			FragmentManager fm = getSupportFragmentManager();
			Bundle args = new Bundle();

			Tab tab = getSupportActionBar().newTab();
			tab.setText(getString(R.string.tab_currencies));
			tab.setTabListener(pagerTabsListener);
	//		tab.setTabListener(new TabsListener<MyFragment>(MainActivity.this, "first", MyFragment.class, args));
            getSupportActionBar().addTab(tab);
			FragmentInfo fi = new FragmentInfo(CurrencyFragment.class, CURRENCIES_FRAGMENT_TAG, args, tab);
			fi.setFragment(fm.findFragmentByTag(CURRENCIES_FRAGMENT_TAG));
			fragments.add(fi);
			
			
			tab = getSupportActionBar().newTab();
			tab.setText(getString(R.string.tab_statistics));
			tab.setTabListener(pagerTabsListener);
	//		tab.setTabListener(new TabsListener<MyFragment>(MainActivity.this, "secnd", MyFragment.class, args));
			getSupportActionBar().addTab(tab);
			fi = new FragmentInfo(ChartFragment.class, CHART_FRAGMENT_TAG, args, tab);
			fi.setFragment(fm.findFragmentByTag(CHART_FRAGMENT_TAG));
			fragments.add(fi);
			
			tab = getSupportActionBar().newTab();
			tab.setText(getString(R.string.tab_groups));
			tab.setTabListener(pagerTabsListener);
	//		tab.setTabListener(new TabsListener<MyFragment>(MainActivity.this, "thrd", MyFragment.class, args));
			getSupportActionBar().addTab(tab);
			fi = new FragmentInfo(GroupFragment.class, GROUP_FRAGMENT_TAG, args, tab);
			fi.setFragment(fm.findFragmentByTag(GROUP_FRAGMENT_TAG));
			fragments.add(fi);
			
			tab = getSupportActionBar().newTab();
			tab.setText(getString(R.string.tab_history));
			tab.setTabListener(pagerTabsListener);
			getSupportActionBar().addTab(tab);
			fi = new FragmentInfo(HistoryFragment.class, HISTORY_FRAGMENT_TAG, args, tab);
			fi.setFragment(fm.findFragmentByTag(HISTORY_FRAGMENT_TAG));
			fragments.add(fi);
			
			tab = getSupportActionBar().newTab();
			tab.setText(getString(R.string.tab_calculator));
			tab.setTabListener(pagerTabsListener);
			getSupportActionBar().addTab(tab);
			fi = new FragmentInfo(CalcFragment.class, CALCULATOR_FRAGMENT_TAG, null, tab);
			fi.setFragment(fm.findFragmentByTag(CALCULATOR_FRAGMENT_TAG));
			fragments.add(fi);
			
			adapter = new TabsAdapter(getSupportFragmentManager(), this, fragments);
		}
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(adapter);
//		pager.setPageTransformer(true, new SlidePageTransformer());
		
		
    }
    
    public Fragment getFragmentByTag(String tag) {
    	return adapter.getFragmentByTag(tag);
    }
	
	public static class TabsAdapter extends FragmentPagerAdapter implements OnPageChangeListener {

		public static class FragmentInfo {
			private Class clazz;
			private String tag;
			private Bundle args;
			private Fragment mFragment;
			private Tab tab;
			
			public FragmentInfo(Class clazz, String tag, Bundle args, Tab tab) {
				this.clazz = clazz;
				this.tag = tag;
				this.args = args;
				this.tab = tab;
			}
			
			public void setFragment(Fragment f) {
				mFragment = f;
			}
			
		}
		
		private final List<FragmentInfo> fragments;
		private final Activity mActivity;
		private final FragmentManager fm;
		
		public TabsAdapter(FragmentManager fm, Activity activity, List<FragmentInfo> fragments) {
			super(fm);
			this.fm = fm;
			this.fragments = fragments;
			mActivity = activity;
			for (FragmentInfo f : fragments) {
				f.mFragment = getFragmentByTag(f.tag);
			}
		}

		@Override
		public Fragment getItem(int position) {
			FragmentInfo info = fragments.get(position);
			if (info.mFragment == null) {
				info.mFragment = Fragment.instantiate(mActivity, info.clazz.getName(), info.args);
			}
			if (info.tab != null) {
//				info.tab.select();
			}
			return info.mFragment;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
		
		public Fragment getFragmentByTag(String tag) {
			int index = 0;
			for (FragmentInfo info : fragments) {
				if (info.tag.equals(tag)) {
					return fm.findFragmentByTag(UIUtils.makeFragmentName(R.id.tabs_pager, index));
				}
				index++;
			}
			Log.d("temp", "Fragment " + tag + " not found");
			return null;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			if (fragments.get(position).tab != null) {
				fragments.get(position).tab.select();
			}
		}
		
	}
	
	public static class PagerTabsListener implements android.support.v7.app.ActionBar.TabListener {

		private final ViewPager pager;
		public PagerTabsListener(ViewPager pager) {
			this.pager = pager;
		}
		
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			pager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}
	}

    public static class TabsListener<T extends Fragment> implements android.support.v7.app.ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle args;
        /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          */
        public TabsListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            this.args = args;
        }

        /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
        
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), args);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }
    
    @Override
    protected void onDestroy() {
    	NetworkStateProvider.getInstance().stopListening();
    	DatabaseUtils.closeDatabase();
    	super.onDestroy();
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                // app icon in action bar clicked; go home
//                Intent intent = new Intent(this, HomeActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                return false;
            case R.id.menu_export:
            	String exported = DatabaseUtils.exportDatabase(getApplicationContext());
            	if (exported == null) {
            		Toast.makeText(getApplicationContext(), getString(R.string.error_backup_db), Toast.LENGTH_SHORT).show();
            	} else {
            		Toast.makeText(getApplicationContext(), getString(R.string.success_backup_db, exported), Toast.LENGTH_SHORT).show();
            	}
            	return true;
            case R.id.menu_import:
            	FilePickDialog dialog = new FilePickDialog();
            	dialog.show(getSupportFragmentManager(), FilePickDialog.TAG);
            	return true;
            case R.id.menu_curr_list:
            	startActivityForResult(new Intent(this, CurrSelectActivity.class), 1);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	//File picker dialog listener
	@Override
	public void onFileSelected(final String fileName) {
		//TODO now always asks for saving current
//		String date = fileName.substring(fileName.length() - 3 - 8, fileName.length() - 3);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
//		if (!date.equals(sdf.format(new Date()))) {
			
			UIUtils.showYesNoDialog(this, getString(R.string.attention),
					getString(R.string.save_before_replace),
					new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							DatabaseUtils.exportDatabase(getApplicationContext());
							importDatabase(fileName);
						}
					},
					new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							importDatabase(fileName);
						}
					});
//		}
		
	}
	
	private void importDatabase(String fileName) {
		setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
		DatabaseUtils.replaceDatabase(getApplicationContext(), fileName);
        SettingsProvider.getStored().putSetting(Setting.CURRENCIES_INIT, true);
		finish();
		startActivity(new Intent(MainActivity.this, MainActivity.class));
	}
	
	@Override
	public void onDialogCanceled() {
	}

    private static AtomicBoolean updateRunning = new AtomicBoolean(false);
	@Override
	public void onConnected() {
		if (!SettingsProvider.getStored().getSetting(Setting.CURRENCIES_INIT, Boolean.class)) {
			if (connErrorDialog.isShowing()) {
				connErrorDialog.dismiss();
//            	startActivityForResult(new Intent(this, CurrSelectActivity.class), 1);
			}
		} else {
			if (updateRunning.compareAndSet(false, true)) {
				new Thread("Currencies Update Thread") {
					@Override
					public void run() {
						Updater.forceUpdateCurrencies(getApplicationContext());
						//Updater.checkUpdate(getApplicationContext());
						updateRunning.set(false);
					}
				}.start();
			}
		}
	}

	@Override
	public void onDisconnected() {
		
	}


    
}
