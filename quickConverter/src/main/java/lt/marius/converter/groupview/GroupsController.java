package lt.marius.converter.groupview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lt.marius.converter.R;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.utils.UIUtils;
import lt.marius.converter.views.EditTextDialog;
import lt.marius.converter.views.EditTextDialog.NoticeDialogListener;

public class GroupsController {
	
	
	private List<GroupView> views;
	private Context context;
	
	public GroupsController(Context context) {
		views = new ArrayList<GroupView>();
		this.context = context;
	}
	
	public static Bitmap getGroupImage(TransactionsGroup gr, Context c) {
		if (gr.getImagePath() != null) {
			File f = new File(gr.getImagePath());
			if (f.exists()) {
				return BitmapFactory.decodeFile(gr.getImagePath());
			}
		}
//		int bgResource = R.drawable.expenses_group_bg4;
//		if (gr.getTypeEnum().equals(TransactionsGroup.Type.INCOME)) {
//			bgResource = R.drawable.income_group_bg;
//		}
		gr.setImagePath(null);
		return createGroupImage(gr, c, gr.getTypeEnum().getBgResource());
		
	}
	
	private static Bitmap createGroupImage(TransactionsGroup gr, Context c, int bgResource) {
		if (gr.getImagePath() == null || gr.getImagePath().length() == 0) {
			String path = c.getFilesDir().getAbsolutePath() + "/images";
			String imagePath = gr.getName() + gr.getType();
			String name = gr.getName();
			if (name.length() > 2) {
				StringTokenizer st = new StringTokenizer(name);
				if (st.countTokens() >= 2) {
					name = st.nextToken().charAt(0) + "" + st.nextToken().charAt(0);
				} else {
					name = name.substring(0, 2);
				}
			}

			Bitmap bmp = UIUtils.makeLetterBitmap(
                    UIUtils.createColorBitmap(UIUtils.getIndexedColor(gr.getId()), UIUtils.dpToPx(48, c), UIUtils.dpToPx(48, c)),
                    //BitmapFactory.decodeResource(c.getResources(), bgResource),
                    name
					/*gr.getName().toUpperCase(Locale.getDefault()).charAt(0) + ""*/);
			try {
				imagePath = UIUtils.saveBitmap(bmp, path, imagePath);
			} catch (IOException e) {
				imagePath = "";
				e.printStackTrace();
			}
			gr.setImagePath(imagePath);
			TransactionsGroupsController.getInstance().saveChanges(gr);
			return bmp;
		}
		return null;
	}
	
	
	public void createView(ViewGroup container) {
		//Add expenses groups first
		TransactionsGroup gr = TransactionsGroupsController.getInstance().getDefaultExpensesGroup();
		//create group image if it does not exist
		createGroupImage(gr, container.getContext(), TransactionsGroup.Type.EXPENSES.getBgResource());
		addGroup(gr, context);
		List<TransactionsGroup> groups = TransactionsGroupsController.getInstance().getGroups(gr);
		for (TransactionsGroup group : groups) {
			addGroup(group, context);
		}
		//Add income groups later
		gr = TransactionsGroupsController.getInstance().getDefaultIncomeGroup();
		createGroupImage(gr, container.getContext(), TransactionsGroup.Type.INCOME.getBgResource());
		addGroup(gr, context);
		groups = TransactionsGroupsController.getInstance().getGroups(gr);
		for (TransactionsGroup group : groups) {
			addGroup(group, context);
		}
		populateView(container);
	}
	
	public void populateView(ViewGroup container) {
		ListView list = (ListView) container.findViewById(R.id.list_groups);
		list.setAdapter(listAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (manager != null) {
                    pendingGroup = views.get(position).getModel();
                    dialog = new EditTextDialog();
                    dialog.setParameters(context.getString(R.string.pick_group_title),
                            context.getString(R.string.group_title), pendingGroup.getName());
                    dialog.setListener(editDialogListener);
                    dialog.show(manager, "EditTextDialog");
                }

            }
        });
//		list.setOnItemLongClickListener(listLongClickListener);
	}
	
	private BaseAdapter listAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = views.get(position).getLayout();
			v.setTag(position);
			return v;
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public Object getItem(int position) {
			return views.get(position);
		}
		
		@Override
		public int getCount() {
			return views.size();
		}
	};
	
	private OnItemLongClickListener listLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view,
				int position, long id) {
			//make sure not to remove default groups
			if (views.get(position).getModel() != TransactionsGroupsController.getInstance().getDefaultExpensesGroup() &&
					views.get(position).getModel() != TransactionsGroupsController.getInstance().getDefaultIncomeGroup()) {
				TransactionsGroupsController.getInstance().removeGroup(views.get(position).getModel());
				views.remove(position);
				listAdapter.notifyDataSetChanged();
			}
			return true;
		}
		
	};
	
	private void addGroup(TransactionsGroup group, Context context) {
		GroupView gv = new GroupView(group, context, this);
		views.add(gv);
	}

	public void removeGroup(TransactionsGroup group) {
		int position = findViewPosition(group);
		TransactionsGroupsController.getInstance().removeGroup(views.get(position).getModel());
		views.remove(position);
		listAdapter.notifyDataSetChanged();
	}
	
	private FragmentManager manager;
	public void setFragmentManager(FragmentManager fragmentManager) {
		manager = fragmentManager;
	}
	
	private int findViewPosition(TransactionsGroup group) {
		int i = 0;
		for (GroupView v : views) {
			if (v.getModel() == group) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/* UI Callback methods */
	private TransactionsGroup pendingGroup;
	private NoticeDialogListener dialogListener = new NoticeDialogListener() {
		
		@Override
		public void onDialogPositiveClick(String text) {
			if (text.length() == 0) {
				pendingGroup = null;
				return;
			}
            TransactionsGroup gr = TransactionsGroupsController.getInstance().getGroup(text);
			if (gr != null && !gr.isRemoved()) {
				UIUtils.showOkDialog(dialog.getActivity(), "", context.getString(R.string.error_group_exists));
				return;
			}
			//Add subgroup for a group
			TransactionsGroup group = new TransactionsGroup(text, pendingGroup.getType(),
					"", pendingGroup);
			if (TransactionsGroupsController.getInstance().addGroup(group)) {
				createGroupImage(group, context, pendingGroup.getTypeEnum().getBgResource());
			} else {
				//get the resurrected group
				group = TransactionsGroupsController.getInstance().getGroup(group.getName());
			}
//			int pos = findViewPosition(pendingGroup);
//			if (pos + 1 < views.size()) {
//				views.add(pos + 1, new GroupView(group, context, GroupsController.this));
//			} else {
//				views.add(new GroupView(group, context, GroupsController.this));
//			}
			if (pendingGroup.getTypeEnum().equals(TransactionsGroup.Type.EXPENSES)) {
				int pos = findViewPosition(TransactionsGroupsController.getInstance().getDefaultIncomeGroup());
				views.add(pos, new GroupView(group, context, GroupsController.this));
			} else {
				views.add(new GroupView(group, context, GroupsController.this));
			}
			listAdapter.notifyDataSetChanged();
			pendingGroup = null;
		}
		
		@Override
		public void onDialogNegativeClick() {
			pendingGroup = null;
		}
	};

    private NoticeDialogListener editDialogListener = new NoticeDialogListener() {

        @Override
        public void onDialogPositiveClick(String text) {
            if (pendingGroup != null) {
                pendingGroup.setName(text);
                TransactionsGroupsController.getInstance().saveChanges(pendingGroup);
                if (listAdapter != null) listAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onDialogNegativeClick() {

        }
    };
	
	private EditTextDialog dialog;
	public void onSubGroupAdd(TransactionsGroup group) {
		if (manager != null) {
			pendingGroup = group;
			dialog = new EditTextDialog();
			dialog.setParameters(context.getString(R.string.pick_group_title),
					context.getString(R.string.group_title));
			dialog.setListener(dialogListener);
			dialog.show(manager, "EditTextDialog");
		}
	}
	
	public void onSubGroupRemove(TransactionsGroup group) {
		removeGroup(group);
	}
	
	
}
