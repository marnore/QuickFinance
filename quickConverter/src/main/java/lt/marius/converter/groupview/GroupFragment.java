package lt.marius.converter.groupview;

import java.util.List;

import lt.marius.converter.R;
import lt.marius.converter.transactions.TransactionsGroup;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Since all the functionality is moved to either controller of views,
 * This class acts only as a dim placeholder
 */
public class GroupFragment extends Fragment {

	private GroupsController controller;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_group, null);
		//add groups here
		if (controller == null) {
			controller = new GroupsController(container.getContext());
			controller.createView(layout);
			controller.setFragmentManager(getActivity().getSupportFragmentManager());
		} else {
			controller.populateView(layout);
		}
		
		return layout;
	}
	
	
	
}
