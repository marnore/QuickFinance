package lt.marius.converter.settings;

import lt.marius.converter.settings.SettingsProvider.Setting;
import android.content.Context;
import android.content.SharedPreferences;

class SharedPrefsSettings implements Settings {
	
	private static final String PREFS_NAME = "GatewaySettings";
	
	private SharedPreferences prefs;
	
	SharedPrefsSettings(Context c) {
		prefs = c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public <T> T getSetting(Setting setting, Class<T> varType) {
		try {
			if (varType.equals(String.class)) {
				return varType.cast(prefs.getString(setting.toString(), setting.getDefault(String.class)));
			} else if (varType.equals(Integer.class)) {
				return varType.cast(prefs.getInt(setting.toString(), setting.getDefault(Integer.class)));
			} else if (varType.equals(Float.class)) {
				return varType.cast(prefs.getFloat(setting.toString(), setting.getDefault(Float.class)));
			} else if (varType.equals(Long.class)) {
				return varType.cast(prefs.getLong(setting.toString(), setting.getDefault(Long.class)));
			} else if (varType.equals(Boolean.class)) {
				return varType.cast(prefs.getBoolean(setting.toString(), setting.getDefault(Boolean.class)));
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println(e.getCause());
			return null;
		}
	}

	public void putSetting(Setting setting, Object value) {
		if (!setting.getClazz().isInstance(value)) return;
		if (setting.getClazz().equals(String.class)) {
			prefs.edit().putString(setting.toString(), (String)value).commit();
		} else if (setting.getClazz().equals(Integer.class)) {
			prefs.edit().putInt(setting.toString(), (Integer)value).commit();
		} else if (setting.getClazz().equals(Long.class)) {
			prefs.edit().putLong(setting.toString(), (Long)value).commit();
		} else if (setting.getClazz().equals(Float.class)) {
			prefs.edit().putFloat(setting.toString(), (Float)value).commit();
		} else if (setting.getClazz().equals(Boolean.class)) {
			prefs.edit().putBoolean(setting.toString(), (Boolean)value).commit();
		} else {
			
		}
	}
	
}
