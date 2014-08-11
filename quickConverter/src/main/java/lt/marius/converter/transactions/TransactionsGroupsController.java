package lt.marius.converter.transactions;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lt.marius.converter.R;
import lt.marius.converter.utils.DatabaseUtils;

public class TransactionsGroupsController {

	private static final Object LOCK = new Object();
	private static TransactionsGroupsController INSTANCE;


    public interface GroupChangeListener {
		public void onGroupAdded(TransactionsGroup group);
		public void onGroupRemoved(TransactionsGroup group);
	}
	
	private List<GroupChangeListener> listeners = new ArrayList<TransactionsGroupsController.GroupChangeListener>();
	
	public void addGroupChangeListener(GroupChangeListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}
	
	public void removeGroupChangeListener(GroupChangeListener l) {
		listeners.remove(l);
	}
	
	public static void init(Context c) {
		synchronized (LOCK) {
			if (INSTANCE == null) {
				INSTANCE = new TransactionsGroupsController(c);
			}
		}
	}
	
	public static TransactionsGroupsController getInstance() {
		
		return INSTANCE;
	}
	
	public static final Integer DEFAULT_INCOME_ID = 1;
	public static final Integer DEFAULT_EXPENSES_ID = 2;

	private RuntimeExceptionDao<TransactionsGroup, Integer> groupDao;
	private final TransactionsGroup expensesGroup;
	private final TransactionsGroup incomeGroup;
	
	private TransactionsGroupsController(Context c) {
		groupDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
		
		TransactionsGroup group = groupDao.queryForId(DEFAULT_INCOME_ID);
		if (group == null) {
			group = new TransactionsGroup(c.getString(R.string.income), 
					TransactionsGroup.Type.INCOME.getName(), "", null);
			group.setId(DEFAULT_INCOME_ID);
			groupDao.create(group);
		}
		incomeGroup = group;
		group = groupDao.queryForId(DEFAULT_EXPENSES_ID);
		if (group == null) {
			group = new TransactionsGroup(c.getString(R.string.expenses),
					TransactionsGroup.Type.EXPENSES.getName(), "", null);
			group.setId(DEFAULT_EXPENSES_ID);
			groupDao.create(group);
		}
		expensesGroup = group;
	}
	
	public TransactionsGroup getDefaultExpensesGroup() {
		return expensesGroup;
	}
	
	public TransactionsGroup getDefaultIncomeGroup() {
		return incomeGroup;
	}
	
	public void saveChanges(TransactionsGroup group) {
		groupDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
		groupDao.createOrUpdate(group);
	}
	
	/**
	 * 
	 * @param group
	 * @return true if new group created, false if existing group resurrected
	 */
	public boolean addGroup(TransactionsGroup group) {
		groupDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
		List<TransactionsGroup> groups 
			= groupDao.queryForEq(TransactionsGroup.GROUP_NAME, group.getName());
		boolean s;
		if (groups.isEmpty()) {
			saveChanges(group);
			s = true;
		} else {
			TransactionsGroup gr = groups.get(0);	//Resurrect the group
			gr.setRemoved(false);
			saveChanges(gr);
			s = false;
		}
		for (GroupChangeListener l : listeners) {
			l.onGroupAdded(group);
		}
		return s;
	}
	
	
	public void removeGroup(TransactionsGroup group) {
		RuntimeExceptionDao<CurrencyStored, Integer> currDao
			= DatabaseUtils.getHelper().getCachedDao(CurrencyStored.class);
		List<CurrencyStored> transactions = null;
		try {
			transactions = currDao.queryBuilder().where().eq(CurrencyStored.COLUMN_GROUP_ID, group).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (transactions == null || transactions.isEmpty()) {
			groupDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
			File f = new File(group.getImagePath());
			f.delete();	//remove generated image
			groupDao.delete(group);
		} else {
			group.setRemoved(true);
			saveChanges(group);
		}
		for (GroupChangeListener l : listeners) {
			l.onGroupRemoved(group);
		}
	}

	/**
	 * Get all groups for a specified parent. If parent is null all
	 * groups are returned
	 * @param parent
	 * @return
	 */
	public List<TransactionsGroup> getGroups(TransactionsGroup parent) {
		List<TransactionsGroup> ret = null;
		groupDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
		QueryBuilder<TransactionsGroup, Integer> query = groupDao.queryBuilder();
		if (parent != null) {
			try {
				
				ret = query.where()
					.eq(TransactionsGroup.GROUP_PARENT_ID, parent).and()
					.eq(TransactionsGroup.GROUP_REMOVED, false)
					.query();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				ret = query.where()
					.eq(TransactionsGroup.GROUP_REMOVED, false)
					.query();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}


    public List<TransactionsGroup> getExpensesGroups() {
        return getGroups(getDefaultExpensesGroup());
    }

    public List<TransactionsGroup> getIncomeGroups() {
        return getGroups(getDefaultIncomeGroup());
    }

	public TransactionsGroup getGroup(String name) {
		groupDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
		List<TransactionsGroup> list = groupDao.queryForEq(TransactionsGroup.GROUP_NAME, name);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public boolean groupExists(String text) {
		groupDao = DatabaseUtils.getHelper().getCachedDao(TransactionsGroup.class);
		return getGroup(text) != null;
	}
	
	
	
}
