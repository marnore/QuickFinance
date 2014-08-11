package lt.marius.converter;

import java.util.List;

import lt.marius.converter.transactions.CurrencyStored;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.utils.DatabaseUtils;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.RuntimeExceptionDao;

public class StatsFragment extends Fragment {
	
	private LinearLayout layout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.layout = new LinearLayout(container.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(Color.parseColor("#0A0A0A"));
		
		
		new AsyncTask<Void, String, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				RuntimeExceptionDao<CurrencyStored, Integer> currDao
					= DatabaseUtils.getHelper().getCachedDao(CurrencyStored.class);
				double sum = 0;
				for (CurrencyStored c : currDao.queryForAll()) {
					sum += c.getBaseDouble();
				}
				publishProgress(String.format("Overall sum %.2f", sum));
				
				RuntimeExceptionDao<TransactionsGroup, Integer> groupDao
					= DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
				TransactionsGroup incomeGroup = TransactionsGroupsController.getInstance().getDefaultIncomeGroup();
				List<TransactionsGroup> incomeGroups = groupDao.queryForEq(TransactionsGroup.GROUP_PARENT_ID, incomeGroup);
				incomeGroups.add(0, incomeGroup);
				for (TransactionsGroup group : incomeGroups) {
					sum = 0;
					for (CurrencyStored c : currDao.queryForEq(CurrencyStored.COLUMN_GROUP_ID, group)) {
						sum += c.getBaseDouble();
					}
					publishProgress(String.format("Income Group %s: %.2f", group.getName(), sum));
				}
				TransactionsGroup expensesGroup = TransactionsGroupsController.getInstance().getDefaultExpensesGroup();
				List<TransactionsGroup> expensesGroups 
					= groupDao.queryForEq(TransactionsGroup.GROUP_PARENT_ID, expensesGroup);
				expensesGroups.add(0, expensesGroup);
				for (TransactionsGroup group : expensesGroups) {
					sum = 0;
					for (CurrencyStored c : currDao.queryForEq(CurrencyStored.COLUMN_GROUP_ID, group)) {
						sum += c.getBaseDouble();
					}
					publishProgress(String.format("Expenses Group %s: %.2f", group.getName(), sum));
				}
				return null;
			}
			
			@Override
			protected void onProgressUpdate(String... values) {
				TextView text = new TextView(layout.getContext());
				text.setText(values[0]);
				layout.addView(text);
			}
			
		}.execute();
		
		return layout;
	}
	
}
