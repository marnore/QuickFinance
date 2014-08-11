package lt.marius.converter.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import lt.marius.converter.R;

public class UIUtils {
	
	private static final Handler uiHandler = new Handler();
	
	/**
	 * Does nothing. Just inits handlers to specified thread
	 */
	public static void initToThisThread() {
		
	}
	
	public static void runOnUiThread(Runnable r) {
		uiHandler.post(r);
	}
	
	public static AlertDialog showOkDialog(Activity context, String title, String message) {
		return showOkDialog(context, title, message, null);
	}

    public static int[] colors = {
            0xffe51c23,
            0xff9c27b0,
            0xff3f51b5,
            0xff03a9f4,
            0xff009688,
            0xff8bc34a,
            0xffffeb3b,
            0xffff9800,
            0xff795548,
            0xff607d8b,
            0xffe91e63,
            0xff673ab7,
            0xff5677fc,
            0xff00bcd4,
            0xff259b24,
            0xffcddc39,
            0xffffc107,
            0xffff5722,
            0xff9e9e9e,};

    public static int getIndexedColor(int index) {
        return colors[index % colors.length];
    }

	public static AlertDialog showOkDialog(Activity context, String title, String message, final DialogInterface.OnDismissListener afterDismiss) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(message).setCancelable(false);
			if (title != null) {
				builder.setTitle(title);
			}
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					if (afterDismiss != null) {
						afterDismiss.onDismiss(dialog);
					}
				}
			});
			if (!context.isFinishing()) {
				AlertDialog alert = builder.create();
				if (title == null) {
					alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
				}
				alert.show();
				try {
					((TextView)alert.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
				} catch (Exception ex) {}
				return alert;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//ignore
		}
		return null;
	}
	
//	private float getDip(float pix) {
//
//		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pix, getResources().getDisplayMetrics());
//		return px;
//	}
	
	/**
	 * Returns an array of localized weekday names starting from Monday. 
	 * @param locale - Represents needed language and area codes. E.g. new Locale('fr')
	 * 				   passing null uses default system locale
	 * @return	an array containing translated week days' names
	 */
	public static String[] getLocalizedWeekdays(Locale locale, boolean shortStr) {
		DateFormatSymbols dfSymbols;
		if (locale != null) {
			dfSymbols = new DateFormatSymbols(locale);
		} else {
			dfSymbols = new DateFormatSymbols();
		}
		String[] wDays = shortStr ? dfSymbols.getShortWeekdays() : dfSymbols.getWeekdays();
		int[] days = {Calendar.MONDAY, Calendar.TUESDAY,	//order of days to appear in final array
				      Calendar.WEDNESDAY, Calendar.THURSDAY,
				      Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
		String[] weekDays = new String[days.length];
		for (int i = 0; i < days.length; i++) {	//map results
			weekDays[i] = wDays[days[i]];
		}
		return weekDays;
	}
	
	/**
	 * Returns an array of localized months' names starting from January.
	 * @param languageCode - Represents needed language and area codes. E.g. new Locale('fr')
	 * 						 passing null uses default system locale
	 * @return	an array containing translated months' names
	 */
	public static String[] getLocalizedMonths(Locale locale, boolean shortStr) {
		DateFormatSymbols dfSymbols;
		if (locale != null) {
			dfSymbols = new DateFormatSymbols(locale);
		} else {
			dfSymbols = new DateFormatSymbols();
		}
		String[] allMonths = shortStr ? dfSymbols.getShortMonths() : dfSymbols.getMonths();
		int[] months = {Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH,
						Calendar.APRIL, Calendar.MAY, Calendar.JUNE,
						Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER,
						Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER};
		String[] retMonths = new String[months.length];
		for (int i = 0; i < months.length; i++) {
			retMonths[i] = allMonths[months[i]];
		}
		return retMonths;
	}
	
	public static Bitmap makeLetterBitmap(Bitmap background, String letter) {
		int h = background.getHeight();
		int w = background.getWidth();
		Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setStyle(Paint.Style.FILL_AND_STROKE);
		p.setTypeface(Typeface.DEFAULT_BOLD);
		float heightRatio = 0.8f;
		float textHeight = (float)(h * heightRatio);
		p.setTextSize(textHeight);
		float textWidth = p.measureText(letter);
		while (textWidth > w - 10) {
			heightRatio -= 0.1;
			textHeight = (float)(h * heightRatio);
			p.setTextSize(textHeight);
			
			textWidth = p.measureText(letter);
		}
		Canvas c = new Canvas();
		c.setBitmap(bmp);
		c.drawBitmap(background, 0, 0, null);
//		p.setColor(Color.DKGRAY);
//		c.drawText(letter, (w - textWidth) / 2 + 2, h - ((h - textHeight + p.descent()) / 2) - 2, p);
		p.setColor(Color.WHITE);
		c.drawText(letter, (w - textWidth) / 2, h - ((h - textHeight + p.descent()) / 2), p);
		return bmp;
	}

	public static String saveBitmap(Bitmap bmp, String path, String suggestedName) throws IOException {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		//maybe random enough
		if (suggestedName == null) {
			suggestedName = GeneralUtils.encodeToMD5(new Date().toString());
		}
		File image = new File(dir, suggestedName);
		FileOutputStream out = new FileOutputStream(image);
		bmp.compress(CompressFormat.PNG, 90, out);
		out.flush();
		out.close();
		return image.getAbsolutePath();
	}
	
	public static int dpToPx(float dp, Context c) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
	}

	public static int getRecursiveTop(View anchor) {
		int top = anchor.getTop();
		ViewGroup parent = (ViewGroup)anchor.getParent();
		while (parent.getParent() != null && parent.getParent() instanceof ViewGroup) {
			parent = (ViewGroup)parent.getParent();
			top += parent.getTop();
		}
		return top;
	}
	
	public static int getRecursiveLeft(View anchor) {
		int top = anchor.getLeft();
		ViewGroup parent = (ViewGroup)anchor.getParent();
		while (parent.getParent() != null && parent.getParent() instanceof ViewGroup) {
			parent = (ViewGroup)parent.getParent();
			top += parent.getLeft();
		}
		return top;
	}

	public static void hideSoftKeyboard(Context context, View keyboardTrigger) {
		InputMethodManager imm = (InputMethodManager)context.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(keyboardTrigger.getWindowToken(),
				0);
	}

	public static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

	public static Bitmap getFlagBitmap(Context c, String shortCode) {
		try {
			return BitmapFactory.decodeStream(c.getAssets().open("flags/" + shortCode.toLowerCase() + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void removeFromParent(View view) {
		if (view != null && view.getParent() != null) {
			((ViewGroup)view.getParent()).removeView(view);
		}
	}

	
	public static void setLocale(String langCode, Context context) {
		Resources res = context.getResources();
	    // Change locale settings in the app.
	    DisplayMetrics dm = res.getDisplayMetrics();
	    android.content.res.Configuration conf = res.getConfiguration();
	    conf.locale = localeFromStr(langCode);
	    res.updateConfiguration(conf, dm);

	}
	
	public static Locale localeFromStr(String str) {
		Locale locale = new Locale("en");
		if (str.length() == 2) {	//e.g. "en"
	    	locale = new Locale(str.toLowerCase());
	    } else if(str.length() == 6) { //e.g. "nl-rBE"
	    	locale = new Locale(str.substring(0, 2), str.substring(4));
	    }
		return locale;
	}

	public static void showYesNoDialog(Activity context, String title, String message,
			OnClickListener positiveClickListener,
			OnClickListener negativeClickListener) {
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setTitle(title);
		b.setMessage(message);
		if (positiveClickListener != null) {
			b.setPositiveButton(context.getString(R.string.yes), positiveClickListener);
		}
		if (negativeClickListener != null) {
			b.setNegativeButton(context.getString(R.string.no), negativeClickListener);
		}
		b.create().show();
	}

	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void setBackground(View v, Drawable drawable) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			v.setBackground(drawable);
		} else {
			v.setBackgroundDrawable(drawable);
		}
	}

    public static Bitmap createColorBitmap(int color, int width, int height) {
        int[] colors = new int[width * height];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = color;
        }
        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    }

    public enum Rotation { PORTRAIT, LANDSCAPE };
	
	public static Rotation getCurrRotation(Context c) {
		DisplayMetrics mm = c.getResources().getDisplayMetrics();
		return mm.heightPixels > mm.widthPixels ? Rotation.PORTRAIT : Rotation.LANDSCAPE;
	}

    public static void fastScrollHack(ListView listView) {
        // special hack for violet fast scroll
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            try {
                java.lang.reflect.Field f = AbsListView.class.getDeclaredField("mFastScroller");
                f.setAccessible(true);
                Object o = f.get(listView);
                f = f.getType().getDeclaredField("mThumbDrawable");
                f.setAccessible(true);
                Drawable drawable = (Drawable) f.get(o);
                drawable = listView.getContext().getResources().getDrawable(R.drawable.fastscroll_thumb_holo);
                f.set(o, drawable);
            } catch (Exception e) {
                //ignore
            }
        }
    }
	
//	public static ViewGroup getRootView(View child) {
//		ViewGroup root;
//		if (child instanceof ViewGroup) {
//			root = (ViewGroup) child;
//		} else {
//			root = (ViewGroup) child.getParent();
//		}
//		while (root.getParent() != null && root.getParent() instanceof ViewGroup) {
//			root = (ViewGroup)root.getParent();
//		}
//		return root;
//	}
}
