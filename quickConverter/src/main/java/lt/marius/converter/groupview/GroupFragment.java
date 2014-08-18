package lt.marius.converter.groupview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lt.marius.converter.R;
import lt.marius.converter.transactions.TransactionsGroup;
import lt.marius.converter.transactions.TransactionsGroupsController;
import lt.marius.converter.views.EditTextDialog;

/**
 * Since all the functionality is moved to either controller of views,
 * This class acts only as a dim placeholder
 */
public class GroupFragment extends Fragment implements EditTextDialog.NoticeDialogListener{

    private static final String PENDING_GROUP_ID = "pending_group_id";
    private GroupsController controller;

    @Override
    public void onDialogPositiveClick(EditTextDialog dialog, String text) {
        if (controller != null) {
            controller.onDialogPositiveClick(dialog, text);
        }

    }

    @Override
    public void onDialogNegativeClick(EditTextDialog dialog) {
        if (controller != null) {
            controller.onDialogNegativeClick(dialog);
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_group, null);
		//add groups here
        if (savedInstanceState != null) {
            String id = savedInstanceState.getString(PENDING_GROUP_ID);
            if (id != null) {
                pendingGroup = TransactionsGroupsController.getInstance().getGroup(id);
            }
        }

		if (controller == null) {
			controller = new GroupsController(container.getContext());
			controller.createView(layout);
            controller.setPendingGroup(pendingGroup);
			controller.setFragmentManager(this, getChildFragmentManager());
		} else {
			controller.populateView(layout);
		}
		
		return layout;
	}

    TransactionsGroup pendingGroup;

    public TransactionsGroup getPendingGroup() {
        return pendingGroup;
    }

    public void setPendingGroup(TransactionsGroup pendingGroup) {
        this.pendingGroup = pendingGroup;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (pendingGroup != null) {
            outState.putString(PENDING_GROUP_ID, pendingGroup.getName());
        }
        super.onSaveInstanceState(outState);
    }

}
