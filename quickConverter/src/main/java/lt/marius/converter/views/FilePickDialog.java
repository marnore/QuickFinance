package lt.marius.converter.views;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lt.marius.converter.R;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.UIUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FilePickDialog extends DialogFragment {

	public interface FilePickDialogListener {
		public void onFileSelected(String fileName);

		public void onDialogCanceled();
	}

	public static final String TAG = "FilePickDialog";
	

	private String title;
	private String hint;
	private ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			title = args.getString("title");
			hint = args.getString("hint");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		ListView list = new ListView(getActivity());
		
		final FilesAdapter adapter = new FilesAdapter();
		final Activity caller = getActivity();
		if (caller instanceof FilePickDialogListener ) {
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					File f = adapter.getItem(position);
					if (caller != null) {
						((FilePickDialogListener) caller).onFileSelected(f.getAbsolutePath());
					}
					dismiss();
				}
				
			});
		}
		list.setAdapter(adapter);
//		int p = UIUtils.dpToPx(10.f, getActivity());
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		params.setMargins(p, p, p, p);
		if (adapter.getCount() > 0) {
			title = getActivity().getString(R.string.select_db_to_import);
		} else {
			title = getActivity().getString(R.string.no_items_found);
		}
		
		builder.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(getString(R.string.cancel), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setView(list);
		// Create the AlertDialog object and return it
		AlertDialog d = builder.create();
		d.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (caller != null) {
					((FilePickDialogListener) caller).onDialogCanceled();
				}
			}
		});
		return d;
	}
	
	private class FilesAdapter extends BaseAdapter {

		private File[] files;
		private List<File> filesList;
		private List<String> names;
		private int dp;
		
		public FilesAdapter() {
			dp = UIUtils.dpToPx(5, getActivity());
			files = Environment.getExternalStorageDirectory().listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					if ( (filename.startsWith("quick_converter") || filename.startsWith(DatabaseUtils.EXPORTED_DB_PREFIX))
							&& filename.endsWith(".db")) return true;
					return false;
				}
			});
			filesList = Arrays.asList(files);
			Collections.sort(filesList, new Comparator<File>() {
				@Override
				public int compare(File lhs, File rhs) {
					return lhs.getName().compareTo(rhs.getName());
				}
			});
			names = new ArrayList<String>(filesList.size());
			for (File f : filesList) {
				String name = f.getName();
				try {
					Date d = sdf.parse(name.substring(name.length() - 3 - 8, name.length() - 3));
					names.add(sdf2.format(d));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public int getCount() {
			return files == null ? 0 : files.length;
		}

		@Override
		public File getItem(int position) {
			return files == null ? null : files[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy MMMM dd", Locale.getDefault());
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new TextView(parent.getContext());
				convertView.setPadding(0, dp, 0, dp);
				((TextView)convertView).setGravity(Gravity.CENTER_HORIZONTAL);
				((TextView)convertView).setTextSize(20);
			}
			TextView tv = (TextView)convertView;
			tv.setText(names.get(position));
			return tv;
		}
		
	}

}
