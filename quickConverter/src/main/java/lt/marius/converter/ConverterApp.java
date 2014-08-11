package lt.marius.converter;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.AbsListView;

import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.utils.UIUtils;

import static android.os.Build.*;

public class ConverterApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SettingsProvider.init(getApplicationContext());
		UIUtils.initToThisThread();


	}

}
