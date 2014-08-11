package lt.marius.converter.transactions;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lt.marius.converter.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

public class HistoryItemEditFragment extends DialogFragment {

	private DatePicker datePicker;
	private EditText editValue;
	private int id;
	
	public interface HistoryItemEditCallback {
		void onUpdated(int year, int month, int day, double value, int id);
		void onDeleted(int id);
	}
	
	private HistoryItemEditCallback listener;
	
	@SuppressLint("NewApi")
	public View buildView(Bundle savedInstanceState) {
		
		Bundle args = getArguments();
		if (args == null && savedInstanceState != null) {
			args = savedInstanceState;
		}
		
		int year = args.getInt("year");
		int month = args.getInt("month");
		int day = args.getInt("day");
		double value = args.getDouble("value");
		id = args.getInt("id");
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.layout_curr_stored_edit, null, false);
		datePicker = (DatePicker) vg.findViewById(R.id.date_picker);
		editValue = (EditText) vg.findViewById(R.id.edit_value);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
			datePicker.setMaxDate(new Date().getTime());
		}
		datePicker.updateDate(year, month, day);
		editValue.setText("" + value);
		return vg;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setView(buildView(savedInstanceState));
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					double val;
					if (editValue.getText().length() > 0) {
						val = Double.parseDouble(editValue.getText().toString());
					} else {
						val = 0;
					}
					listener.onUpdated(datePicker.getYear(), datePicker.getMonth(),
							datePicker.getDayOfMonth(), val, id);
				}
			}
		});
		
		builder.setNeutralButton(R.string.dialog_delete, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onDeleted(id);
				}
			}
		});
		builder.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener = null;
				dismiss();
			}
		});
		builder.setTitle(R.string.dialog_edit_history_title);
		builder.setCancelable(true);
		return builder.create();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getParentFragment() instanceof HistoryItemEditCallback) {
			listener = (HistoryItemEditCallback)getParentFragment();
		}
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		
	}
	
}
