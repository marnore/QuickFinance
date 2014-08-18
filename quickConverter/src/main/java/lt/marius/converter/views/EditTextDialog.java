package lt.marius.converter.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import lt.marius.converter.R;
import lt.marius.converter.utils.UIUtils;

public class EditTextDialog extends SimpleDialogFragment {

    public static final java.lang.String HINT = "hint";
    public static final java.lang.String TITLE = "title";
    public static final java.lang.String VALUE = "value";
    public static final String MODE = "mode";
    public static final String MODE_EDIT = "mode_edit";
    public static final String MODE_CREATE = "mode_create";



    public String getMode() {
        String mode = getArguments().getString(MODE);
        if (mode == null) mode = MODE_CREATE;
        return mode;
    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(EditTextDialog dialog, String text);
        public void onDialogNegativeClick(EditTextDialog dialog);
    }

	private NoticeDialogListener listener;
	private String title = "";
	private String hint = "";
    private String value = "";
	
//	public void setParameters(String title, String hint) {
//		this.hint = hint;
//		this.title = title;
//	}
//
//    public void setParameters(String title, String hint, String value) {
//        this.hint = hint;
//        this.title = title;
//        this.value = value;
//    }
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
//		bundle.putBoolean("dismiss", true);	//FIXME so far just dismiss it since it is tricky to save state listener
		bundle.putString("title", title);
		bundle.putString("hint", hint);
        bundle.putString("value", value);
        bundle.putString("mode", getMode());
		super.onSaveInstanceState(bundle);
	}


    @Override
    protected Builder build(Builder builder) {
        Fragment f = getParentFragment();
        if (f instanceof  NoticeDialogListener) {
            listener = (NoticeDialogListener) getParentFragment();
        }
        this.hint = getArguments().getString(HINT);
        this.title = getArguments().getString(TITLE);
        this.value = getArguments().getString(VALUE);
//        if (savedInstanceState != null) {
//            if (savedInstanceState.getBoolean("dismiss", false)) {
//                UIUtils.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        dismiss();
//                    }
//                });
//                return dialog.create();
//            }
//            hint = savedInstanceState.getString("hint");
//            title = savedInstanceState.getString("title");
//        }
        LinearLayout layout = new LinearLayout(getActivity());
        final EditText edit = new EditText(getActivity());
        edit.setTextAppearance(getActivity(), R.style.EditText);
        edit.setImeOptions(edit.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        int p = UIUtils.dpToPx(10.f, getActivity());
        edit.setPadding(p, p, p, p);
        edit.setHint(hint);
        edit.setText(value);
        edit.setTextAppearance(getActivity(), R.style.EditText);
        edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(p, p, p, p);
        layout.addView(edit, params);


        builder.setTitle(title)
                .setPositiveButton(R.string.add_subgroup, new View.OnClickListener() {
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onDialogPositiveClick(EditTextDialog.this, edit.getText().toString());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new View.OnClickListener() {
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onDialogNegativeClick(EditTextDialog.this);
                        }
                        EditTextDialog.this.dismiss();
                    }
                })
                .setView(layout);
        return builder;
    }





	public void setListener(NoticeDialogListener dialogListener) {
		this.listener = dialogListener;
	}

	
}
