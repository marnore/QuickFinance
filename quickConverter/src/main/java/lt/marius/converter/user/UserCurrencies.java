package lt.marius.converter.user;

import lt.marius.converter.curview.Currency;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users_currencies")
public class UserCurrencies {
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_ORDER = "order";
	public static final String COLUMN_CURRENCY = "currency";
	
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(columnName = COLUMN_USER_ID, foreign = true)
	private User user;
	
	@DatabaseField(foreign = true, foreignAutoCreate = false, 
			foreignColumnName = Currency.COLUMN_CURRENCY_ID, columnName = COLUMN_CURRENCY)
	private Currency currency;
	@DatabaseField(columnName = COLUMN_ORDER)
	private int order;
	
	public UserCurrencies() {}
	
	public UserCurrencies(User user, Currency c, int order) {
		this.user = user;
		currency = c;
		this.order = order;
	}

	public int getUserId() {
		return user.getId();
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
	
	
}
