package jp.redmine.redmineclient.adapter;

import java.sql.SQLException;

import jp.redmine.redmineclient.db.cache.DatabaseCacheHelper;
import jp.redmine.redmineclient.db.cache.RedmineProjectModel;
import jp.redmine.redmineclient.entity.RedmineProject;
import jp.redmine.redmineclient.form.IMasterRecordListItemForm;

import android.view.View;

public class RedmineProjectListAdapter extends RedmineBaseAdapter<RedmineProject> {
	private RedmineProjectModel model;
	protected Integer connection_id;
	public RedmineProjectListAdapter(DatabaseCacheHelper helper) {
		super();
		model = new RedmineProjectModel(helper);
	}

	public void setupParameter(int connection){
		connection_id = connection;
	}

    @Override
	public boolean isValidParameter(){
		if(connection_id == null)
			return false;
		else
			return true;
	}

	@Override
	protected int getItemViewId() {
		return android.R.layout.simple_list_item_1;
	}

	@Override
	protected void setupView(View view, RedmineProject data) {
		IMasterRecordListItemForm form = new IMasterRecordListItemForm(view);
		form.setValue(data);
	}

	@Override
	protected int getDbCount() throws SQLException {
		return (int) model.countByProject(connection_id,0);
	}

	@Override
	protected RedmineProject getDbItem(int position) throws SQLException {
		return model.fetchItemByProject(connection_id,0,(long) position, 1L);
	}

	@Override
	protected long getDbItemId(RedmineProject item) {
		if(item == null){
			return -1;
		} else {
			return item.getId();
		}
	}

}
