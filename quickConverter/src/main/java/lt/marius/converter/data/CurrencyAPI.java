package lt.marius.converter.data;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;

public interface CurrencyAPI {

	public Currency getCurrencyValue(String baseCurrency, String currency);
	public IsoCode[] getSupportedIsoCodes();

}
