package lt.marius.converter.data;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.CurrencyUpdatedListener;
import lt.marius.converter.user.UsersController;
import lt.marius.converter.utils.DatabaseUtils;

public class Updater {

	public static int USER_CURR_UPDATE_INTERVAL = 12 * 60 * 60 * 1000;			//12 hours in ms
	public static int OTHER_CURR_UPDATE_INTERVAL = 2 * 24 * 60 * 60 * 1000;		//every 2 days
	
	public static void checkUpdate(Context c) {
		if (!DatabaseUtils.isInitialized()) {
            return;
        }
		List<Currency> userCurrs = UsersController.getInstance().getUserCurrencies(null);
		final int userCurrCount = userCurrs.size();
		final CurrencyProvider provider = new CurrencyProvider(c, Currency.BASE_CURRENCY);
		if (userCurrCount > 0 &&
			(new Date().getTime() - userCurrs.get(0).getLastUpdated().getTime()) > USER_CURR_UPDATE_INTERVAL) {
			provider.updateCurrencies(userCurrs, new CurrencyUpdatedListener() {
				int count = 0;
				@Override
				public void onCurrencyUpdated(Currency cur) {
					count++;
					Log.i("Updater", "Updated user currency " + cur);
					
					if (count == userCurrCount) {
						updateOtherCurrencies(provider);
						count = 0;
					}
				}
			});
		} else {
			updateOtherCurrencies(provider);
		}
		
	}

	private static void updateOtherCurrencies(CurrencyProvider provider) {
		RuntimeExceptionDao<Currency, Integer> dao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
		long updateTime = new Date().getTime() - OTHER_CURR_UPDATE_INTERVAL;
		try {
			List<Currency> updateList = dao.queryBuilder().where().le(Currency.COLUMN_LAST_UPDATED, new Date(updateTime)).query();
			provider.updateCurrencies(updateList, new CurrencyUpdatedListener() {
				
				@Override
				public void onCurrencyUpdated(Currency cur) {
					Log.i("Updater", "Updated currency " + cur);
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
