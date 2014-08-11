package lt.marius.converter.chartview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lt.marius.charts.Series;
import lt.marius.charts.Series2D;
import lt.marius.charts.SeriesPie;
import lt.marius.converter.R;
import lt.marius.converter.curview.Currency;
import lt.marius.converter.transactions.CurrencyStored;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.utils.DatabaseUtils;
import lt.marius.converter.utils.GeneralUtils;
import lt.marius.converter.utils.UIUtils;

public class ChartController {

	private RuntimeExceptionDao<CurrencyStored, Integer> currencyStoreDao;
	private RuntimeExceptionDao<Currency, Integer> currencyDao;
	private List<Series> series;
	private float[] limitY;
	private float[] avgY;
	private float[] monthValues = null;
    private Context context;
	
	private double periodExpenses, periodIncome;
	
	private static final String[] MONTH_NAMES;
	private static final Paint SERIES_PAINT;
	
	static {
		MONTH_NAMES = UIUtils.getLocalizedMonths(Locale.getDefault(), false);
		SERIES_PAINT = new Paint();
		SERIES_PAINT.setStyle(Paint.Style.STROKE);
		SERIES_PAINT.setAntiAlias(true);
		SERIES_PAINT.setStrokeCap(Paint.Cap.ROUND);
		SERIES_PAINT.setStrokeWidth(5.f);
		SERIES_PAINT.setColor(Color.parseColor("#F0FF6080"));
	}
	
	public ChartController(Context applicationContext) {
        context = applicationContext;
		currencyStoreDao = DatabaseUtils.getHelper().getCachedDao(CurrencyStored.class);
		currencyDao = DatabaseUtils.getHelper().getCachedDao(Currency.class);
	}

	/**
	 * 
	 * @param start
	 * @param days
	 * @param currency
	 * @param group
	 * @return transactions sum for each day for all groups
	 */
	public List<Series2D> getSeriesByDays(Calendar start, int days, Currency currency, TransactionsGroup group) {
		List<Series2D> series = new ArrayList<Series2D>();
//		cal.set(Calendar.DATE, 1);
		
//		int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		float []xPoints = new float[days];
		for (int i = 1; i <= days; i++) {
			xPoints[i - 1] = i;
		}
        float[] values = new float[days];
		TransactionsGroupsController tgc = TransactionsGroupsController.getInstance();
		int k = 0;
		List<TransactionsGroup> groups = tgc.getGroups(group);
		groups.add(0, group);
		for (TransactionsGroup g : groups) {
			values = getGroupValuesByDay(start, days, g, currency);
			double sum = 0;
			for (int i = 0; i < values.length; i++) {
//				values[i] = -values[i];	//show as positive stuff
				sum += values[i];
			}
			if (sum == 0) {
//				if (g.equals(group)) {
					k++;	//skip default color
//				}
				continue;
			}
			Series2D s = new Series2D();
			s.setPointsX(xPoints);
	        s.setPointsY(values);
	        s.setTitle(g.getName());
            s.setGroupId(g.getId());
	        SERIES_PAINT.setColor(UIUtils.getIndexedColor(g.getId()));
	        s.setPaint(SERIES_PAINT);
	        series.add(s);
		}
		return series;
	}

    public Series2D getCombinedSeriesByDays(Calendar start, int days, Currency currency, List<TransactionsGroup> groups) {

        float []xPoints = new float[days];
        for (int i = 1; i <= days; i++) {
            xPoints[i - 1] = i;
        }
        float[] values = new float[days];
        TransactionsGroupsController tgc = TransactionsGroupsController.getInstance();
        for (TransactionsGroup g : groups) {
            float vals[] = getGroupValuesByDay(start, days, g, currency);
            for (int i = 0; i < vals.length; i++) {
                values[i] += vals[i];
            }

        }
        Series2D s = new Series2D();
        s.setPointsX(xPoints);
        s.setPointsY(values);
        s.setPaint(SERIES_PAINT);
        return s;
    }
	
	private int getDaysInMonth(int year, int month) {
		Calendar cal2 = Calendar.getInstance();
		if (month == cal2.get(Calendar.MONTH)) {
			return cal2.get(Calendar.DATE);
		} else {
			cal2.set(Calendar.DAY_OF_MONTH, 1);
			cal2.set(Calendar.YEAR, year);
			cal2.set(Calendar.MONTH, month);
			return cal2.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
	}
	
	/**
	 *
	 * @param days
	 * @param group
	 * @param currency
	 * @return values for each day for one group
	 */
	private float[] getGroupValuesByDay(Calendar calendar, int days, TransactionsGroup group, Currency currency) {
		QueryBuilder<CurrencyStored, Integer> builder = currencyStoreDao.queryBuilder();
		float[] result = new float[days];
		Calendar cal = (Calendar) calendar.clone();
//		double sumMonth = 0.d;
		for (int i = 1; i <= days; i++) {
			cal.set(Calendar.DATE, i);
			Date dateStart = cal.getTime();
			
			cal.set(Calendar.DATE, i + 1);
			Date dateEnd = cal.getTime();
			
			List<CurrencyStored> all = null;
			try {
				Where<CurrencyStored, Integer> where = builder.where();
				where.ge(CurrencyStored.COLUMN_DATE, dateStart).and().lt(CurrencyStored.COLUMN_DATE, dateEnd);
				if (group != null) {
					where.and().eq(CurrencyStored.COLUMN_GROUP_ID, group);
				}
				all = currencyStoreDao.query(builder.prepare());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			double sum = 0.d;
			for (CurrencyStored c : all) {
				sum += GeneralUtils.getValue(c, currency);
			}
			result[i-1] = (float)sum;
//			sumMonth += sum;
		}
		return result;
	}
	
	/**
	 * 
	 * @param month
	 * @param year
	 * @param currency
	 * @return expenses for each day for a specified period
	 */
	public List<Series2D> getDailyExpenses(final int month, int year, Currency currency, List<TransactionsGroup> groups) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, month);
		int days = getDaysInMonth(year, month);
		TransactionsGroupsController tgc = TransactionsGroupsController.getInstance();
		
		periodExpenses = 0;
//		Series2D s = getCombinedSeriesByDays(cal, days, currency, groups);
        List<Series2D> series = getSeriesByDays(cal, days, currency, tgc.getDefaultExpensesGroup());
        for (Series2D s : series) {
            s.setColor(context.getResources().getColor(R.color.expenses));
            for (int i = 0; i < s.getPointsY().length; i++) {
                s.getPointsY()[i] *= -1;    //make 'em positive
                periodExpenses += s.getPointsY()[i];
            }
        }
        periodIncome = getGroupMonthSum(year, month, tgc.getDefaultIncomeGroup(), currency);
        return series;
	}
	
	public List<Series2D> getDailyIncome(final int month, int year, Currency currency, List<TransactionsGroup> groups) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, month);
		
		int days = getDaysInMonth(year, month);
//		int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		TransactionsGroupsController tgc = TransactionsGroupsController.getInstance();
		
//		Series2D s = getCombinedSeriesByDays(cal, days, currency, groups);

        List<Series2D> series = getSeriesByDays(cal, days, currency, tgc.getDefaultIncomeGroup());
		periodIncome = 0;
        for (Series2D s : series) {
            s.setColor(context.getResources().getColor(R.color.income));
            for (int i = 0; i < s.getPointsY().length; i++) {
                periodIncome += s.getPointsY()[i];
            }
        }
        periodExpenses = -getGroupMonthSum(year, month, tgc.getDefaultExpensesGroup(), currency);
        return series;
	}
	
	
	public List<SeriesPie> getMonthlyTransactions(final int month, int year,
			Currency currency, TransactionsGroup group) {
		List<SeriesPie> series = new ArrayList<SeriesPie>();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);	//should reset all fields to 0
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, month);
		
		
		int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		float []xPoints = new float[days];
		for (int i = 1; i <= days; i++) {
			xPoints[i - 1] = i;
		}

//		periodExpenses = 0;
		TransactionsGroupsController tgc = TransactionsGroupsController.getInstance();
		List<TransactionsGroup> groups = tgc.getGroups(group);
		groups.add(0, group);
		
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL_AND_STROKE);
		p.setAntiAlias(true);
		p.setStrokeCap(Paint.Cap.ROUND);
		p.setStrokeWidth(0);
		int k = 0;
		for (TransactionsGroup g : groups) {
			double sum = getGroupMonthSum(year, month, g, currency, false);
			
//			periodExpenses += sum;
			if (Math.abs(sum) > 0) {
				SeriesPie s = new SeriesPie(sum);
                s.setGroupId(g.getId());
		        s.setTitle(g.getName());
		        p.setColor(UIUtils.getIndexedColor(g.getId()));
		        s.setPaint(p);
		        s.setVisible(true);
		        series.add(s);
			} /*else if (g.equals(group)) {
				k++;
			}*/
			k++;
		}
	
//        periodIncome = getGroupMonthSum(month, tgc.getDefaultIncomeGroup(), currency);
        return series;
	}
	
	public List<SeriesPie> getMonthlyExpenses(int month, int year, Currency currency) {
		TransactionsGroup group = TransactionsGroupsController.getInstance().getDefaultExpensesGroup();
		List<SeriesPie> ret = getMonthlyTransactions(month, year, currency,
				group);
		periodExpenses = 0;
		for (SeriesPie series : ret) {
			series.setValue(series.getValue() * -1);
			periodExpenses += series.getValue();
			
		}
		periodIncome = getGroupMonthSum(year, month, TransactionsGroupsController.getInstance().getDefaultIncomeGroup(), currency);
		return ret;
	}
	

	public List<SeriesPie> getMonthlyIncome(int month, int year, Currency currency) {
		TransactionsGroup group = TransactionsGroupsController.getInstance().getDefaultIncomeGroup();
		List<SeriesPie> ret = getMonthlyTransactions(month, year, currency, group);
		periodIncome = 0;
		for (SeriesPie series : ret) {
			periodIncome += series.getValue();
		}
		periodExpenses = -getGroupMonthSum(year, month, TransactionsGroupsController.getInstance().getDefaultExpensesGroup(), currency);
		return ret;
	}
	
	private double getGroupMonthSum(int year, int month, TransactionsGroup group, Currency currency) {
		return getGroupMonthSum(year, month, group, currency, true);
	}
	
	private double getGroupMonthSum(int year, int month, TransactionsGroup group, Currency currency, boolean children) {
		double sum = 0;
		QueryBuilder<CurrencyStored, Integer> builder = currencyStoreDao.queryBuilder();
		Calendar cal;
		cal = Calendar.getInstance();
		cal.set(year, month, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date dateStart = cal.getTime();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.MONTH, month + 1);
		Date dateEnd = cal.getTime();
		Where<CurrencyStored, Integer> where = builder.where();
		try {
			where.ge(CurrencyStored.COLUMN_DATE, dateStart).and().lt(CurrencyStored.COLUMN_DATE, dateEnd);
		
			if (group != null) {
				QueryBuilder<TransactionsGroup, Integer> grBuilder 
					= DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class).queryBuilder();
				grBuilder.selectColumns(TransactionsGroup.GROUP_ID);
				Where<TransactionsGroup, Integer> grWhere = grBuilder.where();
				if (children) {
					grWhere.eq(TransactionsGroup.GROUP_ID, group.getId())
						.or().eq(TransactionsGroup.GROUP_PARENT_ID, group.getId());
				} else {
					grWhere.eq(TransactionsGroup.GROUP_ID, group.getId());
				}
				where.and().in(CurrencyStored.COLUMN_GROUP_ID, grBuilder);
			}
			for (CurrencyStored c : builder.query()) {
				sum += GeneralUtils.getValue(c, currency);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sum;
	}

	
	
	
	public List<Series> getSeries() {
		return series;
	}
	
	public float getMaximumY(List<Series2D> series) {
		float max = Float.MIN_VALUE;
		for (Series2D s2 : series) {
			float m = getMaximumY(s2.getPointsY());
			if (max < m) max = m;
		}
		return max;
	}

    public float getMaximumY(Series2D series) {
        return getMaximumY(series.getPointsY());
    }

	private float getMaximumY(float []monthValues) {
		
		if (monthValues == null) {
			return 0;
		} else {
			float max = Float.MIN_VALUE;
			for (int i = 0; i < monthValues.length; i++) {
				if (max < monthValues[i]) max = monthValues[i];
			}
			return max;
		}
	}

	public double getExpensesSum() {
		
		return periodExpenses;
	}
	
	public double getIncomeSum() {
		return periodIncome;
	}

	
}
