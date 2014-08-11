package lt.marius.converter.user;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.utils.DatabaseUtils;

public class UsersController {
	
	private static final int DEFAULT_USER_ID = 1;
	
	private User defaultUser;
	
	private static UsersController INSTANCE;
	
	public synchronized static UsersController getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new UsersController();
		}
		return INSTANCE;
	}
	
	private UsersController() {
		RuntimeExceptionDao<User, Integer> userDao = DatabaseUtils.getHelper().getCachedDao(User.class);
		User user = userDao.queryForId(DEFAULT_USER_ID);
		if (user == null) {
			user = new User();
			user.setId(DEFAULT_USER_ID);
			user.setName("default");
			user.setPassword("default");
			userDao.create(user);
		}
		defaultUser = user;
	}
	
	public void addCurrency(Currency c, User user, int order) {
		RuntimeExceptionDao<UserCurrencies, Integer> userCurrDao = DatabaseUtils.getHelper().getCachedDao(UserCurrencies.class);

		if (user == null) user = defaultUser;
		UserCurrencies uc = new UserCurrencies(user, c, order);
		userCurrDao.create(uc);
	}
	
	public void removeCurrency(Currency c, User user) {
		RuntimeExceptionDao<UserCurrencies, Integer> userCurrDao = DatabaseUtils.getHelper().getCachedDao(UserCurrencies.class);

		if (user == null) user = defaultUser;
		try {
			DeleteBuilder<UserCurrencies, Integer> b = userCurrDao.deleteBuilder();
			b.where().eq(UserCurrencies.COLUMN_USER_ID, user.getId())
				.and().eq(UserCurrencies.COLUMN_CURRENCY, c.getId());
			userCurrDao.delete(b.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void reorderCurrencies(final List<Currency> userCurrencies, User user) {
		final RuntimeExceptionDao<UserCurrencies, Integer> userCurrDao
			= DatabaseUtils.getHelper().getCachedDao(UserCurrencies.class);

		final User u;
		if (user == null) {
			u = defaultUser;
		} else {
			u = user;
		}
		userCurrDao.callBatchTasks(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				List<UserCurrencies> list;
				try {
					Where<UserCurrencies, Integer>	where = userCurrDao.queryBuilder().where();
					where.eq(UserCurrencies.COLUMN_USER_ID, u);
					PreparedQuery<UserCurrencies> query = userCurrDao
							.queryBuilder().orderBy(UserCurrencies.COLUMN_ORDER, true).prepare();
					list = userCurrDao.query(query);
					for (UserCurrencies uc : list) {
						uc.setOrder(userCurrencies.indexOf(uc.getCurrency()));
						userCurrDao.update(uc);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		
	}
	
	/**
	 * Gets a list of currencies a specified user has
	 * @param user
	 * @return
	 */
	public List<Currency> getUserCurrencies(User user) {

		RuntimeExceptionDao<UserCurrencies, Integer> userCurrDao
			= DatabaseUtils.getHelper().getCachedDao(UserCurrencies.class);
		if (user == null) {
			user = defaultUser;
		}
		List<Currency> ret = new ArrayList<Currency>();
		List<UserCurrencies> list;
		try {
			Where<UserCurrencies, Integer>	where = userCurrDao.queryBuilder().where();
			where.eq(UserCurrencies.COLUMN_USER_ID, user);
			PreparedQuery<UserCurrencies> query = userCurrDao
					.queryBuilder().orderBy(UserCurrencies.COLUMN_ORDER, true).prepare();
			list = userCurrDao.query(query);
			for (UserCurrencies uc : list) {
				ret.add(uc.getCurrency());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public String getUserSelectedCurrencyCode() {
		return defaultUser.getDefaultCurrency().getShortCode();
	}
			
	public Currency getUserSelectedCurrency() {
		return defaultUser.getDefaultCurrency();
	}

	public void updateUserSelectedCurrency(Currency currency) {
		defaultUser.setDefaultCurrency(currency);
		DatabaseUtils.getHelper().getCachedDao(User.class).update(defaultUser);
	}

	public void updateUserSelectedCurrency(String shortCode) {
		if (shortCode == null || shortCode.equals("")) {
			shortCode = IsoCode.EUR.getName();
		}
		RuntimeExceptionDao<Currency, Integer> currDao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
		List<Currency> list = currDao.queryForEq(Currency.CURRENCY_CODE_SHORT, shortCode);
		if (!list.isEmpty()) {
			updateUserSelectedCurrency(list.get(0));
		}
	}
	
}
