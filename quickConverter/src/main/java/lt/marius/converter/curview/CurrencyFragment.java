package lt.marius.converter.curview;

import lt.marius.converter.R;
import lt.marius.converter.currselect.CurrSelectActivity;
import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.settings.SettingsProvider.Setting;
import lt.marius.converter.utils.UIUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CurrencyFragment extends Fragment{

	private LayoutInflater mInflater; 
	private ViewGroup mParent;
	private View hint;
	private CurrencyController controller;
	private ShowCurrenciesTask showTask;
//	private DragSortListView listView;
	private CurrenciesAdapter adapter;	//unused
	
	public CurrencyFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
//		View v = inflater.inflate(R.layout.fragment_curr_list, container, false);
		View v = inflater.inflate(R.layout.fragment_currency, container, false);
		mParent = (ViewGroup) v.findViewById(R.id.ll_currencies_holder);
		hint = mParent.findViewById(R.id.text_currencies_holder_hint);
		mInflater = inflater;
		
		hint.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().startActivityForResult(new Intent(getActivity(), CurrSelectActivity.class), 1);
			}
		});
		
//		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//        co.hideOnClickOutside = true;
//		ShowcaseView sv;
//		ViewTarget target = new ViewTarget(hint);
//        sv = ShowcaseView.insertShowcaseView(target, getActivity(), R.string.showcase_currencies, R.string.showcase_currencies, co);
//        sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//			
//			@Override
//			public void onShowcaseViewShow(ShowcaseView showcaseView) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onShowcaseViewHide(ShowcaseView showcaseView) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
        
//        listView = (DragSortListView) v.findViewById(R.id.drag_sort_list);
//        listView.setDropListener(onDrop);
//        listView.setRemoveListener(onRemove);
		
		//launch async task
		//if refresh is requested it will be carried in onResume()
		if (!SettingsProvider.getStored().getSetting(Setting.REFRESH_CURR_VIEW, Boolean.class)) {
			updateViews();
		}
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (SettingsProvider.getStored().getSetting(Setting.REFRESH_CURR_VIEW, Boolean.class)) {
			SettingsProvider.getStored().putSetting(Setting.REFRESH_CURR_VIEW, false);
			updateViews();
		}
	}
	
	public void updateViews() {
		showTask = new ShowCurrenciesTask();
		showTask.execute();
	}

	
	private class CurrenciesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return controller.getModelCount();
		}

		@Override
		public Currency getItem(int position) {
			return controller.getModel(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getId() + 0xff000000;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return controller.requestView(position, convertView);
		}
		
		public void remove(int position) {
			controller.remove(position);
		}
		
		public void insert(Currency obj, int to) {
			controller.insert(obj, to);
		}
    	
    }
	
	class ShowCurrenciesTask extends AsyncTask<Void, Integer, Integer> {

    	
    	public ShowCurrenciesTask() {
    		
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		if (controller != null) {
    			controller.destroy();
    		}
     		controller = new CurrencyController(getActivity().getApplicationContext(), mParent, CurrencyFragment.this);
    	}
    	
		@Override
		protected Integer doInBackground(Void... params) {
			return controller.initCurrencies();
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Integer count) {
			if (count > 0) {
				controller.initViews();
				hint.setVisibility(View.GONE);
			} else {
				hint.setVisibility(View.VISIBLE);
			}
			showTask = null;
		}
		
    }

	public void showHistory() {
		
		UIUtils.showOkDialog(getActivity(), "Total euro spent", String.format("%.2f", controller.getStoredAmount()));
	}



}
