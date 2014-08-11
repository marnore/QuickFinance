package lt.marius.converter.data;

import android.content.Context;
import android.content.res.Resources;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.utils.DatabaseUtils;

public class CurrencyProvider {
	
	public static final int THREAD_POOL_SIZE = 1;
	
	public static enum IsoCode {
		AED("AED", "د.إ"),
		ANG("ANG", "ƒ"),
		ARS("ARS", "$"),
		AUD("AUD", "$"),
		AWG("AWG", "ƒ"),
		BAM("BAM", "KM"),
		BBD("BBD", "$"),
		BDT("BDT", "৳"),
		BGN("BGN", "лв"),
		BHD("BHD", ".د.ب"),
		BMD("BMD", "$"),
		BND("BND", "$"),
		BOB("BOB", "Bs."),
		BRL("BRL", "R$"),
		BSD("BSD", "$"),
		BWP("BWP", "P"),
		CAD("CAD", "$"),
		CHF("CHF", "Fr"),
		CLP("CLP", "$"),
		CNY("CNY", "¥"),
		COP("COP", "$"),
		CRC("CRC", "₡"),
		CZK("CZK", "Kč"),
		DKK("DKK", "kr"),
		DOP("DOP", "$"),
		DZD("DZD", "د.ج"),
		EGP("EGP", "£"),
		EUR("EUR", "€"),
		FJD("FJD", "$"),
		GBP("GBP", "£"),
		HKD("HKD", "$"),
		HNL("HNL", "L"),
		HRK("HRK", "kn"),
		HUF("HUF", "Ft"),
		IDR("IDR", "Rp"),
		ILS("ILS", "₪"),
		INR("INR", "inr"),
		JMD("JMD", "$"),
		JOD("JOD", "د.ا"),
		JPY("JPY", "¥"),
		KES("KES", "Sh"),
		KRW("KRW", "₩"),
		KWD("KWD", "د.ك"),
		KYD("KYD", "$"),
		KZT("KZT", "₸"),
		LBP("LBP", "ل.ل"),
		LKR("LKR", "Rs"),
		LTL("LTL", "Lt"),
		LVL("LVL", "Ls"),
		MAD("MAD", "د.م."),
		MDL("MDL", "L"),
		MKD("MKD", "ден"),
		MUR("MUR", "₨"),
		MXN("MXN", "$"),
		MYR("MYR", "RM"),
		NAD("NAD", "$"),
		NGN("NGN", "₦"),
		NIO("NIO", "C$"),
		NOK("NOK", "kr"),
		NPR("NPR", "₨"),
		NZD("NZD", "$"),
		OMR("OMR", "ر.ع."),
		PEN("PEN", "S/."),
		PGK("PGK", "K"),
		PHP("PHP", "₱"),
		PKR("PKR", "₨"),
		PLN("PLN", "zł"),
		PYG("PYG", "₲"),
		QAR("QAR", "ر.ق"),
		RON("RON", "L"),
		RSD("RSD", "дин."),
		RUB("RUB", "р."),
		SAR("SAR", "ر.س"),
		SCR("SCR", "₨"),
		SEK("SEK", "kr"),
		SGD("SGD", "$"),
		SLL("SLL", "Le"),
		SVC("SVC", "₡"),
		THB("THB", "฿"),
		TND("TND", "د.ت"),
		TRY("TRY", "try"),
		TTD("TTD", "$"),
		TWD("TWD", "$"),
		TZS("TZS", "Sh"),
		UAH("UAH", "₴"),
		UGX("UGX", "Sh"),
		USD("USD", "$"),
		UYU("UYU", "$"),
		UZS("UZS", "лв"),
		VEF("VEF", "Bs F"),
		VND("VND", "₫"),
		XCD("XCD", "$"),
		XOF("XOF", "Fr"),
		YER("YER", "﷼"),
		ZAR("ZAR", "R");
		
		private String shortCode;
		private String sign;
		IsoCode(String shortName, String sign) {
			shortCode = shortName;
			this.sign = sign;
		}
		
		public String getName() {
			return shortCode;
		}
		
		public String getSign() {
			return sign;
		}
		
		public static IsoCode fromStr(String str) {
			return IsoCode.valueOf(str);
		}
	};
	
	public interface CurrencyUpdatedListener {
		void onCurrencyUpdated(Currency cur);
	}
	
	private Resources mResources;
	private Currency mBaseCurrency;
	private CurrencyAPI currApi;
	private RuntimeExceptionDao<Currency, Integer> currencyDao;
	
	public CurrencyProvider(Context c, Currency baseCurrency) {
		mResources = c.getResources();
		mBaseCurrency = baseCurrency;
		currApi = new TheMoneyConverterCurrencyAPI();
		currencyDao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
	}
	
	public Currency getCurrency(IsoCode code) {
		Currency c = getValueForCurrency(mBaseCurrency.getShortCode(), code.getName());
		//TODO make some modifications for image
		return c;
	}
	
	private Currency getValueForCurrency(String base, String code) {
		Currency ret; 
		ret = getValueForCurrencyFromStorage(code);
		if (ret == null) {
			ret = currApi.getCurrencyValue(base, code);
			if (ret != null) {
				currencyDao.create(ret);
			}
		}
		return ret;
	}
	
	private Currency getValueForCurrencyFromStorage(String currencyCode) {
		List<Currency> list = currencyDao.queryForEq(Currency.CURRENCY_CODE_SHORT, currencyCode);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	
	/**
	 * Download all currencies and store them in db
	 * Callback UI on each insertion
	 */
	public void updateCurrencies(List<Currency> currencies, CurrencyUpdatedListener l) {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		List<Future<Currency>> results = new ArrayList<Future<Currency>>(currencies.size());
		for (final Currency curr : currencies) {
			Callable<Currency> task = new Callable<Currency>() {
				@Override
				public Currency call() {
					Currency c = currApi.getCurrencyValue(mBaseCurrency.getShortCode(), curr.getShortCode());
					curr.updateValue(c.getCurrency());
					currencyDao.update(curr);
					return curr;
				}
			};
			results.add(executor.submit(task));
		}
		if (l != null) {
			for (Future<Currency> f : results) {
				try {
					l.onCurrencyUpdated(f.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
//					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Download all currencies and store them in db
	 * Callback UI on each insertion
	 * @param codes
	 * @param l
	 * @return true if all currencies were downloaded correctly
	 */
	public boolean downloadAllCurrencies(IsoCode[] codes, CurrencyUpdatedListener l) {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		List<Future<Currency>> results = new ArrayList<Future<Currency>>(codes.length);
		try {
			currencyDao.deleteBuilder().delete();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		for (final IsoCode code : codes) {
			Callable<Currency> task = new Callable<Currency>() {
				@Override
				public Currency call() {
					Currency c = currApi.getCurrencyValue(mBaseCurrency.getShortCode(), code.getName());
					if (c != null) {
						currencyDao.create(c);
					}
					return c;
				}
			};
			results.add(executor.submit(task));
		}
		boolean success = true;
		if (l != null) {
			for (Future<Currency> f : results) {
				try {
					Currency c = f.get();
					if (c != null) {
						l.onCurrencyUpdated(c);
					} else {
						success = false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					success = false;
				} catch (ExecutionException e) {
					e.printStackTrace();
					success = false;
				}
			}
			
		}
		return success;
	}

	public IsoCode[] getSupportedIsoCodes() {
		return currApi.getSupportedIsoCodes();
	}
}
