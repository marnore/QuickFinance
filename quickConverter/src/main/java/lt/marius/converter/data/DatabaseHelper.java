package lt.marius.converter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.settings.SettingsProvider;
import lt.marius.converter.settings.SettingsProvider.Setting;
import lt.marius.converter.transactions.CurrencyStored;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.user.User;
import lt.marius.converter.user.UserCurrencies;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	public static final String DATABASE_NAME = "mydatabase.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 7;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION/*, R.raw.ormlite_config*/);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			//Create associated tables in database
			TableUtils.createTable(connectionSource, Currency.class);
			TableUtils.createTable(connectionSource, CurrencyStored.class);
			TableUtils.createTable(connectionSource, TransactionsGroup.class);
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, UserCurrencies.class);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		if (oldVersion < 4) {	//DB changes introduced in version 4
			try {
				Dao<User, Integer> dao = getDao(User.class);
				dao.executeRaw("ALTER TABLE `users` ADD COLUMN defaultCurrency_id INTEGER;");
				String currName = SettingsProvider.getStored().getSetting(Setting.STATISTICS_CURR_CODE, String.class);
				if (currName == null || currName.equals("")) {
					currName = IsoCode.EUR.getName();
				}
				Currency curr = null;
				Dao<Currency, Integer> currDao = getDao(Currency.class);
				List<Currency> list = currDao.queryForEq(Currency.CURRENCY_CODE_SHORT, currName);
				if (!list.isEmpty()) {
					curr = list.get(0);
				}
				for (User u : dao.queryForAll()) {
					u.setDefaultCurrency(curr);
					dao.update(u);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//also update group images
//			RuntimeExceptionDao<TransactionsGroup, Integer> groupsDao
//				= getCachedDao(TransactionsGroup.class);
//			for (TransactionsGroup g : groupsDao.queryForAll()) {
//				File f = new File(g.getImagePath());
//				if (f.exists()) f.delete();
//			}
		} 
		if (oldVersion < 7) {
			RuntimeExceptionDao<TransactionsGroup, Integer> groupsDao
				= getCachedDao(TransactionsGroup.class);
			for (TransactionsGroup g : groupsDao.queryForAll()) {
				File f = new File(g.getImagePath());
				if (f.exists()) f.delete();
			}
		}
	}

	Map<Class<?>, Object> cache = new HashMap<Class<?>, Object>();
	
	
	public <T> RuntimeExceptionDao<T, Integer> getCachedDao(Class<T> objClass) {
		RuntimeExceptionDao<T, Integer> dao = (RuntimeExceptionDao<T, Integer>) cache.get(objClass);
		if (dao != null) return dao;
		dao = getRuntimeExceptionDao(objClass);
		cache.put(objClass, dao);
		return dao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		cache.clear();
	}
}
