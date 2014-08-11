package lt.marius.converter.curview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lt.marius.converter.Option;
import lt.marius.converter.OptionsButton.OnOptionSelectListener;
import lt.marius.converter.R;
import lt.marius.converter.groupview.GroupsController;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.utils.BackKeyHandler;
import lt.marius.converter.utils.BackKeyHandler.BackKeyListener;
import lt.marius.converter.utils.UIUtils;

class CurrencyView implements OnClickListener, OnLongClickListener {
	
	public static interface OnValueChangeListener {
		public void onValueChanged(CurrencyView view, String newValue);
	}
	
	/** 
	 * date and group might be null
	 * @author marius
	 *
	 */
	public static interface OnValueStoredListener {
		public void onValueAdded(CurrencyView view, String newValue, Date date, TransactionsGroup group);
		public void onValueSubstracted(CurrencyView view, String newValue, Date date, TransactionsGroup group);
		
	}

	private View mLayout;
	private ImageView flag;
	private EditText value;
	private Context mContext;
	private OnValueChangeListener mListener;
	private OnValueStoredListener mStoredListener;
	private ImageButton addButton, subButton;
	private boolean suspendCallbacks;
	private List<Option> expensesOptions;
	private List<Option> incomeOptions;
	
	public CurrencyView(Context c, Currency cur, ViewGroup parent) {
		mContext = c;
		suspendCallbacks = false;
		mLayout = inflateView(cur, parent);
	}
	
	public View getView() {
		return mLayout;
	}
	
	private TextWatcher textChangeListener = new TextWatcher() {
		
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (mListener != null && !suspendCallbacks) {
				mListener.onValueChanged(CurrencyView.this, s.toString());
			}
		}
		
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		
		public void afterTextChanged(Editable s) {
		}
	};
	

	public void setIncomeOptions(List<Option> incOptions) {
		incomeOptions = incOptions;
//		addButton.setOptions(incomeOptions);
//		addButton.setOnOptionSelectedListener(optionAddListener);
	}
	

	public void setExpensesOptions(List<Option> expOptions) {
		expensesOptions = expOptions;
//		subButton.setOptions(expensesOptions);
//		subButton.setOnOptionSelectedListener(optionSubListener);
	}
	
	
	private View inflateView(Currency cur, ViewGroup parent) {
		ViewGroup v = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.fragment_currency_enter, null);
		value = ((EditText)v.findViewById(R.id.edit_currency_value));
		value.setHint(cur.getCurrencyTitle());
		value.setImeOptions(EditorInfo.IME_ACTION_DONE);
		flag = ((ImageView)v.findViewById(R.id.iv_country_flag));
		flag.setImageBitmap(UIUtils.getFlagBitmap(mContext, cur.getShortCode()));
		value.addTextChangedListener(textChangeListener);
		addButton = (ImageButton) v.findViewById(R.id.button_store_value);
		
		addButton.setOnClickListener(this);
//		addButton.setOnLongClickListener(this);
		subButton = (ImageButton) v.findViewById(R.id.button_spend_value);
		
		subButton.setOnClickListener(this);
//		subButton.setOnLongClickListener(this);
		v.setId(cur.getId());
		if (parent != null) parent.addView(v);
		return v;
	}
	
	private OnOptionSelectListener optionAddListener = new OnOptionSelectListener() {
		
		@Override
		public void onOptionSelected(Option selected) {
			if (mStoredListener != null) {
				mStoredListener.onValueAdded(CurrencyView.this, 
						value.getText().toString(), null, (TransactionsGroup)selected.getTag());
			}
		}
	};
	
	private OnOptionSelectListener optionSubListener = new OnOptionSelectListener() {
		
		@Override
		public void onOptionSelected(Option selected) {
			if (mStoredListener != null) {
				mStoredListener.onValueSubstracted(CurrencyView.this, 
						value.getText().toString(), null, (TransactionsGroup)selected.getTag());
			}
		}
	};
	
	public void setOnValueChangeListener(OnValueChangeListener l) {
		mListener = l;
	}
	
	public void updateValue(Double newValue) {
		updateValue(newValue, false);
	}
	
	public void updateValue(Double newValue, boolean notify) {
		if (value != null) {
			if (!notify) {
				value.removeTextChangedListener(textChangeListener);
			}
			value.setText(String.format("%.2f", newValue));
			if (!notify) {
				value.addTextChangedListener(textChangeListener);
			}
		}
	}
	
	public String getValue() {
		String ret = "";
		if (value != null) {
			ret = value.getText().toString();
		}
		return ret;
	}

	public void setOnValueStoredListener(OnValueStoredListener l) {
		mStoredListener = l;		
	}

	@Override
	public void onClick(View v) {
		if (v == addButton) {
			showGroupOptions(TransactionsGroupsController.getInstance().getDefaultIncomeGroup());
//			if (mStoredListener != null) {
//				mStoredListener.onValueAdded(this, value.getText().toString(), null, null);
//			}
		} else if (v == subButton) {
			showGroupOptions(TransactionsGroupsController.getInstance().getDefaultExpensesGroup());
//			if (mStoredListener != null) {
//				mStoredListener.onValueSubstracted(this, value.getText().toString(), null, null);
//			}
		}
		
	}
	
	private ViewGroup overlay;
	private View shade;
	
	private void showGroupOptions(TransactionsGroup parentGroup) {
		ViewGroup root = (ViewGroup) mLayout.getRootView();
		int top = UIUtils.getRecursiveTop((View) mLayout.getParent());
		LayoutInflater inflater = LayoutInflater.from(mContext);
		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.TOP | Gravity.LEFT);
		
		int pad = UIUtils.dpToPx(5, mContext);
		
		TextView tv;
		tv = (TextView) inflater.inflate(R.layout.item_group_choice, null);
		tv.setText(parentGroup.getName());
//		tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		Bitmap bmp = GroupsController.getGroupImage(parentGroup, mContext);
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		Drawable d = new BitmapDrawable(mContext.getResources(), bmp);
		d.setBounds(0, 0, w, h);
		tv.setCompoundDrawables(d, null, null, null);
		tv.setTag(parentGroup);
		if (parentGroup.getTypeEnum().equals(TransactionsGroup.Type.INCOME)) {
			tv.setOnClickListener(incomeGroupButtonClickListener);
		} else {
			tv.setOnClickListener(expensesGroupButtonClickListener);
		}
		tv.setPadding(0, pad, 0, pad);
		ll.addView(tv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		for (TransactionsGroup group : TransactionsGroupsController.getInstance().getGroups(parentGroup)) {
			tv = (TextView) inflater.inflate(R.layout.item_group_choice, null);
			d = new BitmapDrawable(mContext.getResources(), group.getImagePath());
			d.setBounds(0, 0, w, h);
			tv.setCompoundDrawables(d, null, null, null);
			tv.setText(group.getName());
			tv.setTag(group);
			if (parentGroup.getTypeEnum().equals(TransactionsGroup.Type.INCOME)) {
				tv.setOnClickListener(incomeGroupButtonClickListener);
			} else {
				tv.setOnClickListener(expensesGroupButtonClickListener);
			}
			tv.setPadding(0, pad, 0, pad);
			ll.addView(tv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
		overlay = new ScrollView(mContext);

		shade = new View(mContext);
		shade.setBackgroundColor(Color.parseColor("#A0000000"));
		shade.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideGroupOptions();
			}
			
		});
		root.addView(shade);
		overlay.addView(ll);

//		overlay.setPadding(0, top, 0, 0);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = top;
		params.gravity = Gravity.TOP;
		root.addView(overlay, params);
		
		Interpolator interpolator = new DecelerateInterpolator();
		for (int i = 0, n = ll.getChildCount(); i < n; i++) {
			View child = ll.getChildAt(i);
			Animation animation = new TranslateAnimation(0, 0, -(i + 1) * 100, 0);
			animation.setInterpolator(interpolator);
			animation.setDuration(400);
			child.startAnimation(animation);
		}
		UIUtils.hideSoftKeyboard(mContext, value);
		BackKeyHandler.get().subscribe(backKeyListener);
	}
	
	private BackKeyListener backKeyListener = new BackKeyListener() {
		
		@Override
		public boolean onBackPressed() {
			hideGroupOptions();
			return true;
		}
	};
	
	private void hideGroupOptions() {
		BackKeyHandler.get().unsubscribe(backKeyListener);
		UIUtils.removeFromParent(shade);
		UIUtils.removeFromParent(overlay);
	}
	
	private View detailsView;
	private DatePicker datePicker;

	private void closeDetailsView() {
		if (detailsView != null) {
			((ViewGroup)detailsView.getParent()).removeView(detailsView);
			LayoutParams p = mLayout.getLayoutParams();
			p.height = LayoutParams.WRAP_CONTENT;
			mLayout.setLayoutParams(p);
			detailsView = null;
			datePicker = null;
		}
	}
	
	private Date getPickerDate(){
		if (datePicker == null) return new Date();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, datePicker.getDayOfMonth());
		cal.set(Calendar.MONTH, datePicker.getMonth());
		cal.set(Calendar.YEAR, datePicker.getYear());
		cal.set(Calendar.HOUR_OF_DAY, 12);
		return cal.getTime();
	}
	
	private OnClickListener incomeGroupButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TransactionsGroup group = (TransactionsGroup)v.getTag();
			if (mStoredListener != null) {
				mStoredListener.onValueAdded(CurrencyView.this, 
						value.getText().toString(), getPickerDate(), group);
//				closeDetailsView();
				hideGroupOptions();
			}
		}
	};
	
	private OnClickListener expensesGroupButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TransactionsGroup group = (TransactionsGroup)v.getTag();
			if (mStoredListener != null) {
				mStoredListener.onValueSubstracted(CurrencyView.this, 
						value.getText().toString(), getPickerDate(), group);
//				closeDetailsView();
				hideGroupOptions();
			}
		}
	};
	
	// **** Not currently used ****
	private void createOverlayLayout(View anchor, TransactionsGroup parentGroup) {
		int top = UIUtils.getRecursiveTop(anchor);
//		mLayout.findViewById(R.id.rl_transaction_details).setVisibility(View.VISIBLE);
		ViewGroup root = (ViewGroup) mLayout.getRootView();
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View details = inflater.inflate(R.layout.layout_transaction_details, null);
		details.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeDetailsView();
			}
		});
		DatePicker datePicker = (DatePicker)details.findViewById(R.id.transaction_date);
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DATE);
		datePicker.init(year, month, day, null);
		this.datePicker = datePicker;
		
		LinearLayout groups = (LinearLayout)details.findViewById(R.id.ll_group_picker);
		int padding = UIUtils.dpToPx(16, mContext);
		LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ivParams.rightMargin = padding;
		for (TransactionsGroup group : TransactionsGroupsController.getInstance().getGroups(parentGroup)) {
			ImageView iv = new ImageView(anchor.getContext());
			iv.setImageBitmap(BitmapFactory.decodeFile(group.getImagePath()));
			iv.setTag(group);
			if (parentGroup.getTypeEnum().equals(TransactionsGroup.Type.INCOME)) {
				iv.setOnClickListener(incomeGroupButtonClickListener);
			} else {
				iv.setOnClickListener(expensesGroupButtonClickListener);
			}
			groups.addView(iv, ivParams);
		}
		
		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)mLayout.getLayoutParams();
		int popupHeight = UIUtils.dpToPx(250, mContext);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				popupHeight);
		params.topMargin = top + mLayout.getHeight() + p.topMargin + p.bottomMargin;
		params.leftMargin = mContext.getResources().getDimensionPixelSize(R.dimen.padding_medium);
		params.rightMargin = mContext.getResources().getDimensionPixelSize(R.dimen.padding_medium);
		details.findViewById(R.id.rl_transaction_details).setLayoutParams(params);	//position the overlay view
		//add the whole view
		params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		root.addView(details, params);
		
		p.height = popupHeight + mLayout.getHeight();
		mLayout.setLayoutParams(p);
		detailsView = details;
		UIUtils.hideSoftKeyboard(mContext, value);
	}
	
	@Override
	public boolean onLongClick(View v) {
		if (v == subButton) {
			createOverlayLayout(v, TransactionsGroupsController.getInstance().getDefaultExpensesGroup());
			return true;
		} else if (v == addButton) {
			createOverlayLayout(v, TransactionsGroupsController.getInstance().getDefaultIncomeGroup());
			return true;
		}
		return false;
	}

	public void clearValue() {
		value.setText("");
	}



}
