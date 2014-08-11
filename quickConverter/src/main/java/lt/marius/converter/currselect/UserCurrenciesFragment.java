package lt.marius.converter.currselect;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.List;

import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.user.UsersController;
import lt.marius.converter.utils.UIUtils;


public class UserCurrenciesFragment extends ListFragment implements CurrSelectChangeListener {
	
	private CurrenciesAdapter adapter;
	private List<Currency> userCurrencies;
	private UsersController usersController;
	private Context appContext;
	private Currency userCurrency;
	
	public UserCurrenciesFragment() {
		
	}

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, final int to) {
                    if (from != to) {
                        Currency item = adapter.getItem(from);
                        adapter.remove(from);
                        adapter.insert(item, to);
                        adapter.notifyDataSetChanged();
                        new Thread("Reorder task") {
                        	public void run() {
                        		usersController.reorderCurrencies(userCurrencies, null);
                        	}
                        }.start();
                    }
                }
            };

    private DragSortListView.RemoveListener onRemove = 
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
//                    adapter.remove(which);
                	if (adapter.getCount() >= 2 && which == 0) {
                		String s = adapter.getItem(1).getCurrencyCodeShort();
                		usersController.updateUserSelectedCurrency(s);
                    	userCurrency = usersController.getUserSelectedCurrency();
                    	
                	}
                    ((CurrSelectActivity)getActivity()).currencyRemoved(adapter.getItem(which));
                }
            };
            
    protected int getLayout() {
        // this DSLV xml declaration does not call for the use
        // of the default DragSortController; therefore,
        // DSLVFragment has a buildController() method.
        return R.layout.fragment_user_curr_list;
    }
    
    /**
     * Return list item layout resource passed to the ArrayAdapter.
     */
    protected int getItemLayout() {
//    	if (removeMode == DragSortController.CLICK_REMOVE) {
//            return R.layout.list_item_click_remove;
//        } else {
//            return R.layout.list_item_handle_left;
//        }
    	return R.layout.item_user_curr;
    }

    private DragSortListView mDslv;
    private DragSortController mController;

    public int dragStartMode = DragSortController.ON_DRAG;
    public boolean removeEnabled = true;
    public int removeMode = DragSortController.CLICK_REMOVE;
    public boolean sortEnabled = true;
    public boolean dragEnabled = true;

    public static UserCurrenciesFragment newInstance(int headers, int footers) {
        UserCurrenciesFragment f = new UserCurrenciesFragment();
        return f;
    }

    public DragSortController getController() {
        return mController;
    }

    /**
     * Called from DSLVFragment.onActivityCreated(). Override to
     * set a different adapter.
     */
    public void setListAdapter() {
        adapter = new CurrenciesAdapter();
        mDslv.setAdapter(adapter);
    }

    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv) {
        // defaults are
        //   dragStartMode = onDown
        //   removeMode = flingRight
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.iv_flag);
        controller.setClickRemoveId(R.id.iv_remove);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        controller.setBackgroundColor(getResources().getColor(R.color.primary_bright));
        return controller;
    }


    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		usersController = UsersController.getInstance();
		userCurrencies = usersController.getUserCurrencies(null);
		userCurrency = usersController.getUserSelectedCurrency();
    	
        mDslv = (DragSortListView) inflater.inflate(getLayout(), container, false);

        mController = buildController(mDslv);
        mDslv.setFloatViewManager(mController);
        mDslv.setOnTouchListener(mController);
        mDslv.setDragEnabled(dragEnabled);
        return mDslv;
    }
    
    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
    	final String s = adapter.getItem(position).getCurrencyCodeShort();
    	if (!s.equals(userCurrency.getCurrencyCodeShort())) {
	    	new Thread("Reorder task") {
	        	public void run() {
	    			usersController.updateUserSelectedCurrency(s);
	        		userCurrency = usersController.getUserSelectedCurrency();
	        		UIUtils.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							adapter.notifyDataSetChanged();
						}
					});
	        	}
	        }.start();
    	}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        appContext = getActivity().getApplicationContext();
        mDslv = (DragSortListView) getListView(); 

        mDslv.setDropListener(onDrop);
        mDslv.setRemoveListener(onRemove);
        ((CurrSelectActivity)getActivity()).addListener(this);
        setListAdapter();
    }
    
    @Override
    public void onDestroy() {
    	if (getActivity() != null) {
    		((CurrSelectActivity)getActivity()).removeListener(this);
    	}
    	super.onDestroy();
    }
    
	private class CurrenciesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return userCurrencies.size();
		}

		@Override
		public Currency getItem(int position) {
			return userCurrencies.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Currency cur = getItem(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_curr, null);
			}
			ImageView iv = (ImageView)convertView.findViewById(R.id.iv_flag);
			iv.setImageBitmap(UIUtils.getFlagBitmap(appContext, cur.getShortCode()));
			TextView tv = (TextView)convertView.findViewById(R.id.text_currency_name);
			tv.setTextColor(getResources().getColor(R.color.default_text));
			if (cur.equals(userCurrency)) {
				tv.setTextAppearance(parent.getContext(), R.style.TextSelected);
			} else {
				tv.setTextAppearance(parent.getContext(), R.style.TextNormal);
			}
			tv.setText(cur.getCurrencyTitle() + " (" + cur.getShortCode() + ") ");
			return convertView;
		}
		
		public void remove(int position) {
			userCurrencies.remove(position);
		}
		
		public void insert(Currency cur, int to) {
			userCurrencies.add(to, cur);
		}
    	
    }

	@Override
	public void onSearchQuery(String query) {
//		if (query.length() == 0) {
//			userCurrencies = currDao.queryForAll();
//		} else {
//			try {
//				String q = "%" + query + "%";
//				userCurrencies = currDao.queryBuilder()
//						.where().like(Currency.CURRENCY_NAME, q)
//						.or().like(Currency.CURRENCY_CODE_SHORT, q)
//						.query();
//				adapter.notifyDataSetChanged();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	@Override
	public void onCurrencyAdded(Currency currency) {
		(getActivity()).getActionBar().setTitle(R.string.save_changes);
		if (userCurrencies.isEmpty()) {
			String s = currency.getCurrencyCodeShort();
        	usersController.updateUserSelectedCurrency(s);
        	userCurrency = usersController.getUserSelectedCurrency();
		}
		userCurrencies.add(currency);
		adapter.notifyDataSetChanged();
		usersController.addCurrency(currency, null, 0);
	}
	
	@Override
	public void onCurrencyRemoved(Currency currency) {
		(getActivity()).getActionBar().setTitle(R.string.save_changes);

		userCurrencies.remove(currency);
		usersController.removeCurrency(currency, null);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

}
