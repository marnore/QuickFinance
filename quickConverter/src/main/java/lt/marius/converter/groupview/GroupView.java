package lt.marius.converter.groupview;

import lt.marius.converter.R;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.views.EditTextDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GroupView {

	private Context context;
	private TransactionsGroup group;
	private GroupsController controller;
	
	public GroupView(TransactionsGroup group, Context c, GroupsController controller) {
		context = c;
		this.group = group;
		this.controller = controller;
	}
	
	public ViewGroup getLayout() {
		RelativeLayout rl = (RelativeLayout)LayoutInflater
				.from(context).inflate(R.layout.item_group, null);
		ImageView iv = (ImageView)rl.findViewById(R.id.iv_group_icon);
		
		iv.setImageBitmap(GroupsController.getGroupImage(group, context));
		TextView tv = (TextView)rl.findViewById(R.id.text_group_title);
		tv.setText(group.getName());
		Button btn = (Button)rl.findViewById(R.id.button_subgroup_add);
		View remove = rl.findViewById(R.id.iv_subgroup_remove);
		if (this.group.getParent() == null) {
			btn.setOnClickListener(subGroupAddClick);
			remove.setVisibility(View.GONE);
		} else {
			btn.setVisibility(View.GONE);
			remove.setOnClickListener(subGroupRemoveClick);
		}
		
		return rl;
	}

	private OnClickListener subGroupAddClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			controller.onSubGroupAdd(group);
		}
	};
	
	private OnClickListener subGroupRemoveClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			controller.onSubGroupRemove(group);
		}
	};

	public TransactionsGroup getModel() {
		return group;
	}
	
}
