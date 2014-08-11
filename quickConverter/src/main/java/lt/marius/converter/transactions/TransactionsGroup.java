package lt.marius.converter.transactions;

import lt.marius.converter.R;
import android.graphics.Bitmap;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "transactions_groups")
public class TransactionsGroup {
	
	public enum Type {
		INCOME("income", R.drawable.income_group_bg),
		EXPENSES("expenses", R.drawable.expenses_group_bg4);
		
		private String name;
		private int resource;
		Type(String s, int r) {
			name = s;
			resource = r;
		}
		
		public String getName() {
			return name;
		}

		public int getBgResource() {
			return resource;
		}
	}
	
	public static final String GROUP_PARENT_ID = "parent_id";
	public static final String GROUP_REMOVED = "removed";
	public static final String GROUP_NAME = "name";
	public static final String GROUP_ID = "id";
	
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(columnName = GROUP_NAME)
	private String name;
	@DatabaseField
	private String image;
	@DatabaseField(useGetSet = true)
	private String type;
	@DatabaseField(foreign = true, columnName = GROUP_PARENT_ID)
	private TransactionsGroup parent;
	@DatabaseField(columnName = GROUP_REMOVED)
	private boolean removed = false;


	public TransactionsGroup() {}
	
	public TransactionsGroup(String name, String type, String imagePath, TransactionsGroup parent) {
		this.name = name;
		setType(type);
		this.image = imagePath;
		this.parent = parent;
	}
	
	public String getName() {
		return name;
	}
	
	public String getImagePath() {
		return image;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TransactionsGroup)) return super.equals(o);
		return id == ((TransactionsGroup)o).id;
	}
	
	@Override
	public int hashCode() {
		return id;	// (y)
	}
	
	public String getType() {
		return type;
	}
	
	public Type getTypeEnum() {
		for (Type t : Type.values()) {	//not too efficient but works
			if (t.getName().equals(type)) {
				return t;
			}
		}
		return null;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setImagePath(String imagePath) {
		this.image = imagePath;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setParent(TransactionsGroup parent) {
		this.parent = parent;
	}

	public TransactionsGroup getParent() {
		return parent;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
	}

	public int getId() {
		return id;
	}
}
