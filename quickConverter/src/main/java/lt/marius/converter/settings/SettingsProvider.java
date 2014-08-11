package lt.marius.converter.settings;

import java.io.Serializable;
import android.content.Context;

/**
 * Acts as factory for creating setting provider objects. 
 * Clients obtain Settings type objects which may provide settings from local cache,
 * shared preferences, memory etc. 
 * 		Currently, only settings provider form shared preferences is implemented
 * 
 * All the used settings must be stored in Setting enum. When adding a new enum
 * value, default setting value must be specified in constructor.
 * 
 * See Settings interface for settings storage and retrieval
 * 
 * @author Marius
 *
 */
public class SettingsProvider {
	
	public enum Setting {
		CURRENCIES_INIT(false),
		REFRESH_CURR_VIEW(false),
		CURRENCIES_UPDATE(false),
		STATISTICS_CURR_CODE(""),	//deprecated. Use a field in Users table
		CHART_TYPE(0);
		
		private boolean serializable;
		private Object defValue;
		private Class<? extends Object> clazz;
		
		<T> Setting(T defValue) {
			this.defValue = defValue;
			serializable = false;
			if (Serializable.class.isInstance(defValue)) {
				serializable = true;
			}
			this.clazz = defValue.getClass();
		}
		
		public <T> T getDefault(Class<T> type) throws Exception {
			return type.cast(defValue);
		}
		
		public Class<? extends Object> getClazz() {
			return clazz;
		}
		
		public boolean isSerializable() {
			return serializable;
		}
	}
	
	
	private static Settings STORED;
	
	private SettingsProvider() {
	}
	
	/**
	 * Needs to be called before retrieving Settings objects
	 * @param c
	 */
	public static void init(Context c) {
		STORED = new SharedPrefsSettings(c);
	}
	
	/**
	 * 
	 * @return Settings instance where all values are backed in shared preferences
	 */
	public static Settings getStored() {
		return STORED;
	}
	
}
