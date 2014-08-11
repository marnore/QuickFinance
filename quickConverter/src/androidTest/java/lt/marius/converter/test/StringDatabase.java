package lt.marius.converter.test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class StringDatabase {

	private String language;
	private Class clazz;
	private Map<Integer, String> strings = new HashMap<Integer, String>();
	
	public StringDatabase(String language, Class Rclass) {
		this.language = language;
		
			clazz = Rclass;
		
	}
	
	public void init(Context c) throws Exception {
		Object obj = clazz.newInstance();
		Field[] fields = clazz.getFields();
		for (Field f : fields) {
			int a = f.getInt(obj);
			strings.put(a, c.getString(a));
		}
		print();
	}
	
	private void print() {
		for (String s : strings.values()) {
			System.out.println(s);
		}
	}
	
}
