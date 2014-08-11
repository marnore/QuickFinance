package lt.marius.converter.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lt.marius.converter.data.DatabaseHelper;

public class DatabaseUtils {

	public static final String EXPORTED_DB_PREFIX = "quick_finance_";
	private static DatabaseHelper databaseHelper = null;
	private static int refCount = 0;
	
	public static void initDatabse(Context context) {

		if (databaseHelper == null) {
			File db = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);

            try {
                GeneralUtils.copyFile(db, new File(Environment.getExternalStorageDirectory(),
                        DatabaseHelper.DATABASE_NAME));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!db.exists()) {
				db.getParentFile().mkdirs();
				try {
					GeneralUtils.copyFile(context.getAssets().open("quick_converter.db"), db);
					Log.i("DatabaseUtils", "Db copied from assets");
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
	        databaseHelper =
	            OpenHelperManager.getHelper(context, DatabaseHelper.class);
	    }
		refCount++;
	}
	
	public static void closeDatabase() {
		refCount--;
		if (refCount > 0) return;
//		refCount = 0;
		if (databaseHelper != null) {
			databaseHelper.close();
			databaseHelper = null;
	        OpenHelperManager.releaseHelper();
	    }
	}
	
	public static DatabaseHelper getHelper() {
	    return databaseHelper;
	}
	
	public static String exportDatabase(Context c) {
    	File db = c.getDatabasePath(DatabaseHelper.DATABASE_NAME);
    	try {
    		FileInputStream fis = new FileInputStream(db);
    		
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    		File outFile = new File(Environment.getExternalStorageDirectory().getPath() + 
    				"/" + EXPORTED_DB_PREFIX + sdf.format(new Date()) + ".db");
    		if (outFile.exists()) {
    			int bakCount = 1;
    			File bakFile = null;
    			while (bakFile == null || bakFile.exists()) {
    				bakFile = new File(outFile.getAbsolutePath() + ".bak" + bakCount);
    				bakCount++;
    			}
    			outFile.renameTo(bakFile);
    		}
    		GeneralUtils.copyFile(fis, outFile);
    		return outFile.getName();
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
    	return null;
    }
	
	public static void importDatabase(Context c, String inputFileName) {
		File db = c.getDatabasePath(DatabaseHelper.DATABASE_NAME);
		
		try {
    		FileInputStream fis = new FileInputStream(inputFileName);
    		GeneralUtils.copyFile(fis, db);
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	}
	}

	public static void replaceDatabase(Context c, String fileName) {
		if (databaseHelper != null) {
			databaseHelper.close();
			databaseHelper = null;
	        OpenHelperManager.releaseHelper();
	    }
		importDatabase(c, fileName);
	}

    public static boolean isInitialized() {
        return databaseHelper != null;
    }
}
