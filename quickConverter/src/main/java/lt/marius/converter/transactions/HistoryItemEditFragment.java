package lt.marius.converter.transactions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Date;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import lt.marius.converter.R;

public class HistoryItemEditFragment extends SimpleDialogFragment {

	private DatePicker datePicker;
	private EditText editValue;
	private int id;
	
	public interface HistoryItemEditCallback {
		void onUpdated(int year, int month, int day, double value, int id);
		void onDeleted(int id);
	}
	
	private HistoryItemEditCallback listener;
	
	@SuppressLint("NewApi")
	public View buildView() {
		
		Bundle args = getArguments();
//		if (args == null && savedInstanceState != null) {
//			args = savedInstanceState;
//		}
		
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
    protected Builder build(Builder builder) {
        builder.setView(buildView());
        builder.setPositiveButton(R.string.ok, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    double val;
                    if (editValue.getText().length() > 0) {
                        val = Double.parseDouble(editValue.getText().toString());
                    } else {
                        val = 0;
                    }
                    listener.onUpdated(datePicker.getYear(), datePicker.getMonth(),
                            datePicker.getDayOfMonth(), val, id);
                    dismiss();
                }
            }
        });

        builder.setNeutralButton(R.string.dialog_delete, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleted(id);
                }
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                listener = null;
                dismiss();
            }
        });
        builder.setTitle(R.string.dialog_edit_history_title);
        return builder;
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
