package lt.marius.converter.curview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import lt.marius.converter.data.CurrencyProvider.IsoCode;

import android.graphics.drawable.Drawable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "StoredCurrencies")
public class Currency {

	public static final String CURRENCY_CODE_SHORT = "currency_code_short";
	public static final String COLUMN_CURRENCY_ID = "currency_id";
	public static final String CURRENCY_NAME = "currencyTitle";
	public static final String COLUMN_LAST_UPDATED = "lastUpdated";
	
	public static final Currency BASE_CURRENCY = new Currency(BigDecimal.ONE, IsoCode.EUR.getName(), "Euro");
	
	@DatabaseField(generatedId = true, columnName = COLUMN_CURRENCY_ID)
	private int id;
	@DatabaseField
	private BigDecimal currency;
	@DatabaseField(columnName = CURRENCY_CODE_SHORT)
	private String currencyCodeShort;
	@DatabaseField(columnName = CURRENCY_NAME)
	private String currencyTitle;
	@DatabaseField(columnName = COLUMN_LAST_UPDATED, dataType = DataType.DATE_LONG)
	private Date lastUpdated;
	
	public Currency() {}

	public void setCurrencyCodeShort(String currencyCodeShort) {
		this.currencyCodeShort = currencyCodeShort;
	}
	
	public String getCurrencyCodeShort() {
		return currencyCodeShort;
	}

	public Currency(BigDecimal currency, String code, String title) {
		this.currency = currency;
		currencyCodeShort = code;
		currencyTitle = title;
		lastUpdated = new Date();
	}
	
//	public void setImage(Drawable image) {
//		this.image = image;
//	}
//	
//	public Drawable getFlagImage() {
//		return image;
//	}
	
	public BigDecimal getCurrency() {
		return currency;
	}
	
	public double getCurrencyValue() {
		return currency.doubleValue();
	}
	
	/**
	 * 
	 * @param amount
	 * @return amount of money in base currency
	 */
	public BigDecimal getValue(BigDecimal amount) {
		return amount.multiply(currency);
	}
	
	/**
	 * 
	 * @param amount
	 * @return amount of money in base currency
	 */
	public BigDecimal getInverseValue(BigDecimal amount) {
		return amount.divide(currency, RoundingMode.UP);
	}

	public String getShortCode() {
		return currencyCodeShort;
	}

	public String getCurrencyTitle() {
		return currencyTitle;
	}

	public void setCurrencyTitle(String currencyTitle) {
		this.currencyTitle = currencyTitle;
	}

	public void updateValue(BigDecimal newValue) {
		this.currency = newValue;
		this.lastUpdated = new Date();
	}
	
	@Override
	public String toString() {
		return currencyTitle + "(" + currencyCodeShort + ")" + "  " + currency.toPlainString();
	}
	
	@Override
	public int hashCode() {
		return 31 + id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Currency other = (Currency) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	
}