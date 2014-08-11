package lt.marius.converter.user;

import java.util.Collection;

import lt.marius.converter.curview.Currency;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
	
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String password;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
			columnName = "defaultCurrency_id", canBeNull = true)
	private Currency defaultCurrency;
	
	@ForeignCollectionField(eager = false, foreignFieldName = "user", orderColumnName = "order")
	private Collection<UserCurrencies> currencies;
	
	public int getId() {
		return id;
	}
	
	void setId(int id) {
		this.id = id;
	}
	
	public Currency getDefaultCurrency() {
		return defaultCurrency;
	}
	
	public void setDefaultCurrency(Currency defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Collection<UserCurrencies> getCurrencies() {
		return currencies;
	}
	public void setCurrencies(Collection<UserCurrencies> currencies) {
		this.currencies = currencies;
	}
	
	
	
}
