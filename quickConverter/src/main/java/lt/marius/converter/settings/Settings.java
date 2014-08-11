package lt.marius.converter.settings;

import lt.marius.converter.settings.SettingsProvider.Setting;

/**
 * public interface for settings retrieval
 * Returned form SettingsProvider factory methods such as getStored()
 * 
 * usage example:
 * 		s.putSetting(Setting.MY_SETTING, 42);
 * 		int a = s.getSetting(Setting.MY_SETTING, Integer.class);
 * @author Marius
 *
 */
public interface Settings {
	/**
	 * 
	 * @param setting	- Setting from Setting enum to retrieve. If setting
	 * 					  is not storred, default value will be returned
	 * @param varType	- variable class of stored settings e.g. String.class
	 * @return
	 */
	public <T> T getSetting(Setting setting, Class<T> varType);
	/**
	 * 
	 * @param setting	- Setting from Setting enum
	 * @param value		- value to set for the setting. Must be of a setting type
	 * 					  .i.e if MY_SETTING stores default value of String,
	 * 					  the supplied value param must be of a type String.
	 */
	public void putSetting(Setting setting, Object value);
}
