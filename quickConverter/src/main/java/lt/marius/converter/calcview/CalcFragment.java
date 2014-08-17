package lt.marius.converter.calcview;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.groupview.GroupsController;
import lt.marius.converter.transactions.CurrencyStored;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroup.Type;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.user.UsersController;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.GeneralUtils;
import lt.marius.converter.utils.TouchClickListener;
import lt.marius.converter.utils.UIUtils;
import lt.marius.converter.views.EnhancedLinearLayout;
import lt.marius.converter.views.EnhancedLinearLayout.EnhancedLayoutListener;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class CalcFragment extends Fragment {

	private ListView expList;
	private ListView incList;
	private EditText editDateFrom, editDateTo;
	private TextView textCalc, textAnswer;
	private HorizontalScrollView scrollText;
	
	private GroupsAdapter incAdapter, expAdapter;
	private Currency userCurrency;
	private EnhancedLinearLayout main;
	
	private Date dateFrom = null, dateTo = null;
	private DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG);
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("dateFrom", dateFrom);
		outState.putSerializable("dateTo", dateTo);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		main = (EnhancedLinearLayout)inflater.inflate(R.layout.fragment_calculator, container, false);

		expList = (ListView) main.findViewById(R.id.list_calc_expenses);
		incList = (ListView) main.findViewById(R.id.list_calc_income);
		editDateFrom = (EditText) main.findViewById(R.id.edit_date_from);
		editDateTo = (EditText) main.findViewById(R.id.edit_date_to);
		scrollText = (HorizontalScrollView) main.findViewById(R.id.scroll_text_calc);
		textCalc = (TextView) main.findViewById(R.id.text_calculation);
		textCalc.setText("=");
		textAnswer = (TextView) main.findViewById(R.id.text_answer);
		if (textAnswer != null) textAnswer.setVisibility(View.GONE);
		TransactionsGroupsController tgc = TransactionsGroupsController.getInstance();
		
		expAdapter = new GroupsAdapter(container.getContext(), 0);
		List<TransactionsGroup> list = tgc.getGroups(tgc.getDefaultExpensesGroup());
		list.add(tgc.getDefaultExpensesGroup());
		expAdapter.addAll(list);
		expList.setAdapter(expAdapter);
		expList.setSelector(new ColorDrawable(android.R.color.transparent));
		
		incAdapter = new GroupsAdapter(container.getContext(), 0);
		list = tgc.getGroups(tgc.getDefaultIncomeGroup());
		list.add(tgc.getDefaultIncomeGroup());
		incAdapter.addAll(list);
		incList.setAdapter(incAdapter);
		incList.setSelector(new ColorDrawable(android.R.color.transparent));

		if (savedInstanceState != null && savedInstanceState.containsKey("dateFrom")) {
			dateFrom = (Date) savedInstanceState.get("dateFrom");
			dateTo = (Date) savedInstanceState.get("dateTo");
			
		} 
		if (dateFrom == null || dateTo == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			dateFrom = cal.getTime();
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.SECOND, -1);
			dateTo = cal.getTime();
		}
		editDateFrom.setText(dateFormat.format(dateFrom));
		editDateTo.setText(dateFormat.format(dateTo));
		
		editDateFrom.setOnTouchListener(new TouchClickListener(new PickDateListener(){
			@Override
			public Date getDate() {
				return dateFrom;
			}

			@Override
			public void setDate(Date date) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				dateFrom = cal.getTime();
				if (dateFrom.compareTo(dateTo) > 0) {
					cal.set(Calendar.HOUR_OF_DAY, 23);
					cal.set(Calendar.MINUTE, 59);
					cal.set(Calendar.SECOND, 59);
					cal.set(Calendar.MILLISECOND, 999);
					dateTo = cal.getTime();
					editDateTo.setText(dateFormat.format(dateTo));
				}
			}
			
		}));
		
		editDateTo.setOnTouchListener(new TouchClickListener(new PickDateListener(){
			@Override
			public Date getDate() {
				return dateTo;
			}

			@Override
			public void setDate(Date date) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				dateTo = date;
				if (dateTo.compareTo(dateFrom) < 0) {
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					dateFrom = cal.getTime();
					editDateFrom.setText(dateFormat.format(dateFrom));
				}
			}
			
		}));

		
		userCurrency = UsersController.getInstance().getUserSelectedCurrency();
		if (userCurrency != null) {
			expList.setOnItemClickListener(itemClick);
			incList.setOnItemClickListener(itemClick);
		}
			
			
		main.setListener(new EnhancedLayoutListener() {
			
			@Override
			public void onSizeChanged(final int width, int height) {
				textCalc.setMinimumWidth(width);
				textCalc.setGravity(Gravity.CENTER);
				UIUtils.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						updateValues();
					}
				});
			}
		});
		
		return main;

	}
	
	@Override
	public void onResume() {
		Currency c = UsersController.getInstance().getUserSelectedCurrency();
		if (c != null && !c.equals(userCurrency)) {
			userCurrency = c;
			recalculateAndUpdateValues();
		}
		super.onResume();
	}
	
	
	private abstract class PickDateListener implements OnClickListener {
		
		public abstract Date getDate();
		public abstract void setDate(Date date);
		
		@Override
		public void onClick(final View v) {
			v.requestFocus();
			Calendar cal = Calendar.getInstance();
			cal.setTime(getDate());
			DatePickerDialog dialog = new DatePickerDialog(getActivity(), new OnDateSetListener() {
				
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar cal = Calendar.getInstance();
					cal.set(year, monthOfYear, dayOfMonth);
					setDate(cal.getTime());
					((TextView)v).setText(dateFormat.format(getDate()));
					
					recalculateAndUpdateValues();
				}
			}, cal.get(YEAR), cal.get(MONTH), cal.get(DAY_OF_MONTH));
			dialog.show();
		}

	}
	
	private double getSum(TransactionsGroup group) {
		RuntimeExceptionDao<CurrencyStored, Integer> dao = DatabaseUtils.getHelper().getCachedDao(CurrencyStored.class);
		QueryBuilder<CurrencyStored, Integer> qbuilder = dao.queryBuilder();
		double sum = 0;
		try {
			qbuilder.where().eq(CurrencyStored.COLUMN_GROUP_ID, group.getId())
				.and().ge(CurrencyStored.COLUMN_DATE, dateFrom).and().lt(CurrencyStored.COLUMN_DATE, dateTo)
				.query();
			for (CurrencyStored c : qbuilder.query()) {
				sum += GeneralUtils.getValue(c, userCurrency);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sum;
	}
	
	private void recalculateAndUpdateValues() {
		for (Entry<TransactionsGroup, Double>  e : expenses.entrySet()) {
			e.setValue(getSum(e.getKey()));
		}
		for (Entry<TransactionsGroup, Double>  e : income.entrySet()) {
			e.setValue(getSum(e.getKey()));
		}
		
		updateValues();
	}
	
	private LinkedHashMap<TransactionsGroup, Double> expenses = new LinkedHashMap<TransactionsGroup, Double>();
	private LinkedHashMap<TransactionsGroup, Double> income = new LinkedHashMap<TransactionsGroup, Double>();
	
	private void updateValues() {
		double total = 0;
		StringBuilder sb = new StringBuilder();
		for (Double c : income.values()) {
			if (sb.length() > 0) {
				sb.append(" + ");
			}
			if (Math.abs(c) <= 0.001) {
				sb.append("0");
			} else {
				sb.append(String.format("%.2f", c));
			}
			total += c;
		}
		for (Double c : expenses.values()) {
			if (sb.length() > 0) {
				sb.append(" - ");
			}
			if (Math.abs(c) <= 0.001) {
				sb.append("0");
			} else {
				if (sb.length() == 0) {
					sb.append("-");
				}
				sb.append(String.format("%.2f", -c));
			}
			total += c;
		}
		
		if (expenses.isEmpty() && income.isEmpty()) {
			textCalc.setText(getString(R.string.select_groups));
			textCalc.setGravity(Gravity.CENTER);
		} else {
			float textWidth = -1;
			textCalc.setGravity(Gravity.RIGHT);
			String totalStr = String.format(" = %.2f%s", total, 
					IsoCode.fromStr(userCurrency.getCurrencyCodeShort()).getSign());
			if (textAnswer == null) {
				sb.append(totalStr);
				textCalc.setText(sb.toString());
			} else {
				textAnswer.setVisibility(View.GONE);
				textCalc.setTextSize(30);
				String text = sb.toString() + totalStr;
				textWidth = textCalc.getPaint().measureText(text);
				if (textWidth > main.getWidth()) {
					textCalc.setTextSize(24);
					textWidth = textCalc.getPaint().measureText(text);
					if (textWidth > main.getWidth()) {
						textCalc.setText(sb.toString());
						textAnswer.setText(totalStr);
						textAnswer.setVisibility(View.VISIBLE);
					} else {
						textCalc.setText(text);
					}
				} else {
					textCalc.setText(text);
				}
	//			textAnswer.setText(String.format(" = %.2f", total));
			}
		}
		UIUtils.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				scrollText.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		});
		
	}
	
	
	private OnItemClickListener itemClick = new OnItemClickListener() {
		public void onItemClick(final AdapterView<?> adapter, final View v, int position, long arg3) {
			UIUtils.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					TransactionsGroup group = (TransactionsGroup) v.getTag();

					if (expenses.containsKey(group)) {
						expenses.remove(group);
						setTextViewSelected((TextView) v, false);
					} else if (income.containsKey(group)) {
						income.remove(group);
						setTextViewSelected((TextView) v, false);
					} else {
						double sum = getSum(group);

						// Component comp = new Component();
						// comp.group = group;
						// comp.value = sum;
						if (sum < 0) {
							expenses.put(group, sum);
						} else {
							income.put(group, sum);
						}

						setTextViewSelected((TextView) v, true);
					}
					updateValues();
				}
			});
		}

	};
	
	private class GroupsAdapter extends ArrayAdapter<TransactionsGroup> {

		private int pad;
		private int imagePad;
		
		public GroupsAdapter(Context context, int resource) {
			super(context, resource);
			pad = UIUtils.dpToPx(5, context);
			imagePad = UIUtils.dpToPx(5, context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new CheckedTextView(parent.getContext());
				((TextView)convertView).setGravity(Gravity.CENTER_VERTICAL);
				((TextView)convertView).setPadding(pad, pad, pad, pad);
			}
			((TextView)convertView).setText(getItem(position).getName());
			
			convertView.setTag(getItem(position));
			TransactionsGroup group = getItem(position);
			Bitmap bmp = GroupsController.getGroupImage(getItem(position), parent.getContext());
			
			BitmapDrawable left;
			setTextViewSelected(((TextView)convertView),
					income.containsKey(group) || expenses.containsKey(group));
			
			Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, bmp.getWidth() - 1 * imagePad, bmp.getHeight() - 1 * imagePad, true);
			left = new BitmapDrawable(getResources(), bmp2);
			left.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
			bmp.recycle();
			bmp = null;
			((TextView)convertView).setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
			((TextView)convertView).setCompoundDrawablePadding(imagePad);
			
			return convertView;
		}
	}
	
	private void setTextViewSelected(TextView tv, boolean selected) {
		TransactionsGroup g = (TransactionsGroup)tv.getTag();
		if ( selected ) {
			tv.setTextAppearance(getActivity(), R.style.TextSelected);
			tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(UIUtils.getIndexedColor(g.getId()));

//			if (g.getTypeEnum().equals(Type.EXPENSES)) {
//				tv.setBackgroundResource(R.drawable.expenses_selected_bg);
//			} else {
//				tv.setBackgroundResource(R.drawable.income_selected_bg);
//			}
		} else {
			tv.setTextColor(Color.parseColor("#E0E0E0"));
			tv.setTextAppearance(getActivity(), R.style.TextNormal);
			if (g.getTypeEnum().equals(Type.EXPENSES)) {
				UIUtils.setBackground(tv, null);
			} else {
//				tv.setBackgroundResource(R.drawable.income_list_selector);
				UIUtils.setBackground(tv, null);
			}
		}
	}
	
}
