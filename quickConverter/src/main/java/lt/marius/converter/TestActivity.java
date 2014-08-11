package lt.marius.converter;

import java.util.ArrayList;

import lt.marius.converter.OptionsButton.OnOptionSelectListener;
import lt.marius.converter.utils.UIUtils;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity implements OnOptionSelectListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
//		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.income_group_bg);
//		for (char c = 'A'; c <= 'Z'; c++) {
//			ImageView iv = new ImageView(this);
//			Bitmap letterBmp = UIUtils.makeLetterBitmap(bmp, c + "");
//			iv.setImageBitmap(letterBmp);
//			iv.setPadding(5, 5, 5, 5);
//			l.addView(iv);
//		}
		setContentView(ll);
//		bmp.recycle();
//		bmp = null;
		
		TextView tv = new TextView(this);
		tv.setText("Hello world");
		ImageView iv = new ImageView(this);
		iv.setImageResource(R.drawable.ic_launcher);
		ll.addView(tv);
		ll.addView(iv);
		ll.addView(new EditText(this));
		
		LinearLayout l = new LinearLayout(this);
		l.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		parameters.gravity = Gravity.RIGHT;
		ll.addView(l, parameters);
		
		ArrayList<Option> opts = new ArrayList<Option>();
		String[] names = {"First Menu Item", "Second", "Third", "Forth", "And so on"};
		Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.expenses_group_bg);
		int j = 0;
		for (char i = 'c'; i <= 'g'; i++) {
			opts.add(new Option(
					names[j], 
					UIUtils.makeLetterBitmap(bg, "E" + i),
					j,
					null
					));
			j++;
		}
		
		OptionsButton btn = new OptionsButton(this);
		btn.setOnOptionSelectedListener(this);
		btn.setOptions(opts);
//		btn.setText("Options");
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT;
		params.topMargin = 10;
		ll.addView(btn, params);
		
		j = 0;
		opts.clear();
		for (char i = 'g'; i <= 'i'; i++) {
			opts.add(new Option(
					names[j % names.length], 
					UIUtils.makeLetterBitmap(bg, "M" + i),
					j,
					null
					));
			j++;
		}
		
		
		btn = new OptionsButton(this);
		btn.setOptions(opts);
//		btn.setText("Options2");
		params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT;
		params.topMargin = 10;
		ll.addView(btn, params);
		
		j = 0;
		opts.clear();
		for (char i = 'k'; i <= 'k'; i++) {
			opts.add(new Option(
					names[j % names.length], 
					UIUtils.makeLetterBitmap(bg, "O" + i),
					j,
					null
					));
			j++;
		}
		btn = new OptionsButton(this);
		btn.setOptions(opts);
//		btn.setText("Options3");
		params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT;
		params.topMargin = 10;
		ll.addView(btn, params);
		
		j = 0;
		opts.clear();
		for (char i = 'a'; i <= 'h'; i++) {
			opts.add(new Option(
					names[j % names.length], 
					UIUtils.makeLetterBitmap(bg, "J" + i),
					j,
					null
					));
			j++;
		}
		btn = new OptionsButton(this);
		btn.setOptions(opts);
//		btn.setText("Options4");
		params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT;
		params.topMargin = 10;
		ll.addView(btn, params);
		
		j = 0;
		opts.clear();
		for (char i = 'y'; i <= 'z'; i++) {
			opts.add(new Option(
					names[j % names.length], 
					UIUtils.makeLetterBitmap(bg, "I" + i),
					j,
					null
					));
			j++;
		}
		btn = new OptionsButton(this);
		btn.setOptions(opts);
//		btn.setText("Options5");
		params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT;
		params.topMargin = 10;
		ll.addView(btn, params);
		
	}

	@Override
	public void onOptionSelected(Option selected) {
		Toast.makeText(getApplicationContext(), selected.getTitle(), Toast.LENGTH_SHORT).show();
	}
	
}
