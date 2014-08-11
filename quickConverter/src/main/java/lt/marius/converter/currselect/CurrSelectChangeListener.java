package lt.marius.converter.currselect;

import lt.marius.converter.curview.Currency;

public interface CurrSelectChangeListener {
	void onSearchQuery(String query);
	void onCurrencyAdded(Currency currency);
	void onCurrencyRemoved(Currency currency);
}
