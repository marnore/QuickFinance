package lt.marius.converter.data;

import java.math.BigDecimal;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.network.NetworkUtils;

public class GoogleCurrencyAPI implements CurrencyAPI {

//	private static final String currencyApiUrl = "http://www.google.com/ig/calculator?hl=en&q=1%s%%3D%%3F%s";
	private static final String currencyApiUrl = "http://www.google.com/ig/calculator?hl=en&q=1%s=?%s";

	
	@Override
	public Currency getCurrencyValue(String baseCurrency, String currency) {
		String response = NetworkUtils.downloadString(
				String.format(currencyApiUrl, currency, baseCurrency));
		Currency ret = null;
		if (response != null && response.length() > 0) {
			BigDecimal curValue = null;
			String title = "";
			int rhs = response.indexOf("rhs:");
			if (rhs != -1) {
				int start = response.indexOf("\"", rhs);
				int end = response.indexOf(" ", start);
				String value = response.substring(start + 1, end);
				try {
					curValue = new BigDecimal(value.trim());
				} catch (NumberFormatException ex) {
					
				}
			}
			//Get currency long name
			int lhs = response.indexOf("lhs: ");
			if (lhs != -1) {
				int start = response.indexOf(" ", lhs + 5);
				int end = response.indexOf("\"", start);
				title = response.substring(start + 1, end);
			}
			ret = new Currency(curValue, currency, title);
		}
		return ret;
	}


	@Override
	public IsoCode[] getSupportedIsoCodes() {
		return IsoCode.values();
	}
	
}
