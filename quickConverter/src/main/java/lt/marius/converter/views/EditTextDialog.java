package lt.marius.converter.views;

import lt.marius.converter.R;
import lt.marius.converter.utils.UIUtils;
import lt.marius.converter.views.EditTextDialog.NoticeDialogListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

public class EditTextDialog extends DialogFragment {
	
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(String text);
        public void onDialogNegativeClick();
    }

	private NoticeDialogListener listener;
	private String title = "";
	private String hint = "";
    private String value = "";
	
	public void setParameters(String title, String hint) {
		this.hint = hint;
		this.title = title;
	}

    public void setParameters(String title, String hint, String value) {
        this.hint = hint;
        this.title = title;
        this.value = value;
    }
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putBoolean("dismiss", true);	//FIXME so far just dismiss it since it is tricky to save state listener
		bundle.putString("title", title);
		bundle.putString("hint", hint);
        bundle.putString("value", value);
		super.onSaveInstanceState(bundle);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("dismiss", false)) {
				UIUtils.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						dismiss();
					}
				});
				return builder.create();
			}
			hint = savedInstanceState.getString("hint");
			title = savedInstanceState.getString("title");
		}
        LinearLayout layout = new LinearLayout(getActivity());
        final EditText edit = new EditText(getActivity());
        int p = UIUtils.dpToPx(10.f, getActivity());
        edit.setPadding(p, p, p, p);
        edit.setHint(hint);
        edit.setText(value);
        edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(p, p, p, p);
        layout.addView(edit, params);
        
        builder.setTitle(title)
               .setPositiveButton(R.string.add_subgroup, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   if (listener != null) {
                		   listener.onDialogPositiveClick(edit.getText().toString());
                	   }
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   if (listener != null) {
                		   listener.onDialogNegativeClick();
                	   }
                       dialog.cancel();
                   }
               })
               .setView(layout);
        // Create the AlertDialog object and return it
        return builder.create();
    }



	public void setListener(NoticeDialogListener dialogListener) {
		this.listener = dialogListener;
	}

	
}
