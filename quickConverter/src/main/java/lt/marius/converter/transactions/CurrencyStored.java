package lt.marius.converter.transactions;

import java.math.BigDecimal;
import java.util.Date;

import lt.marius.converter.curview.Currency;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "transactions")
public class CurrencyStored {
	
	public static final String COLUMN_GROUP_ID = "group_id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_BASE_AMOUNT = "baseAmount";
	public static final String COLUMN_BASE_AMOUNT_DOUBLE = "baseDouble";
	
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(foreign = true, columnName = Currency.COLUMN_CURRENCY_ID)
	private Currency currency;
	@DatabaseField(columnName = COLUMN_DATE)
	private Date date;
	@DatabaseField
	private BigDecimal amount;	//amount in original currency
	@DatabaseField(columnName = COLUMN_BASE_AMOUNT)
	private BigDecimal baseAmount;	//amount in base currency
	@DatabaseField(columnName = COLUMN_BASE_AMOUNT_DOUBLE)
	private double baseDouble;	//for quick calculations
	@DatabaseField(foreign = true, columnName = COLUMN_GROUP_ID)
	private TransactionsGroup group;
	
	public CurrencyStored() {}
	
	public CurrencyStored(Currency currency, BigDecimal amount, TransactionsGroup group) {
		this.currency = currency;
		this.amount = amount;
		baseAmount = currency.getValue(amount);
		baseDouble = baseAmount.doubleValue();
		date = new Date();
		this.group = group;
	}
	
	public CurrencyStored(Currency currency, BigDecimal amount, Date date, TransactionsGroup group) {
		this.currency = currency;
		this.amount = amount;
		baseAmount = currency.getValue(amount);
		baseDouble = baseAmount.doubleValue();
		this.date = date;
		this.group = group;
	}
	

	public void update(Date date, double value) {
		this.amount = new BigDecimal(value);
		baseAmount = currency.getValue(amount);
		baseDouble = baseAmount.doubleValue();
		this.date = date;
	}

	public Currency getCurrency() {
		return currency;
	}

	public Date getDate() {
		return date;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getBaseAmount() {
		return baseAmount;
	}

	public double getBaseDouble() {
		return baseDouble;
	}
	
	public TransactionsGroup getGroup() {
		return group;
	}

	public int getId() {
		return id;
	}


}
