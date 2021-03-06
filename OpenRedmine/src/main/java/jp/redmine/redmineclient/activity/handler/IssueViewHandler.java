package jp.redmine.redmineclient.activity.handler;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import jp.redmine.redmineclient.R;
import jp.redmine.redmineclient.fragment.Empty;
import jp.redmine.redmineclient.fragment.IssueComment;
import jp.redmine.redmineclient.fragment.IssueEdit;
import jp.redmine.redmineclient.fragment.IssueList;
import jp.redmine.redmineclient.fragment.IssueTitle;
import jp.redmine.redmineclient.fragment.IssueView;
import jp.redmine.redmineclient.fragment.pager.ProjectHome;
import jp.redmine.redmineclient.param.FilterArgument;
import jp.redmine.redmineclient.param.IssueArgument;
import jp.redmine.redmineclient.param.ProjectArgument;

public class IssueViewHandler extends Core
	implements IssueActionInterface, WebviewActionInterface {

	public IssueViewHandler(ActivityRegistry manager) {
		super(manager);
	}

	@Override
	public void onIssueFilterList(int connectionid, int filterid) {
		final FilterArgument arg = new FilterArgument();
		arg.setArgument();
		arg.setConnectionId(connectionid);
		arg.setFilterId(filterid);

		runTransaction(new TransitFragment() {
			@Override
			public void action(FragmentTransaction tran) {
				tran.replace(R.id.fragmentOne, IssueList.newInstance(arg));
			}
		}, null);
	}

	@Override
	public void onIssueList(int connectionid, long projectid) {
		final ProjectArgument arg = new ProjectArgument();
		arg.setArgument();
		arg.setConnectionId(connectionid);
		arg.setProjectId(projectid);

		runTransaction(new TransitFragment() {
			@Override
			public void action(FragmentTransaction tran) {
				tran.replace(R.id.fragmentOne, ProjectHome.newInstance(arg));
			}
		}, null);
	}

	@Override
	public void onIssueSelected(int connectionid, int issueid) {
		final IssueArgument arg = new IssueArgument();
		arg.setArgument();
		arg.setConnectionId(connectionid);
		arg.setIssueId(issueid);

		runTransaction(new TransitFragment() {
			@Override
			public void action(FragmentTransaction tran) {
				tran.replace(R.id.fragmentOne, IssueView.newInstance(arg));
				tran.replace(R.id.fragmentOneHeader, IssueTitle.newInstance(arg));
				tran.replace(R.id.fragmentOneFooter, IssueComment.newInstance(arg));
			}
		}, null);
	}

	@Override
	public void onIssueEdit(int connectionid, int issueid) {
		final IssueArgument arg = new IssueArgument();
		arg.setArgument();
		arg.setConnectionId(connectionid);
		arg.setIssueId(issueid);

		runTransaction(new TransitFragment() {
			@Override
			public void action(FragmentTransaction tran) {
				tran.replace(R.id.fragmentOne, IssueEdit.newInstance(arg));
				tran.replace(R.id.fragmentOneFooter, Empty.newInstance());
			}
		}, null);
	}

	@Override
	public void onIssueAdd(int connectionid, long projectId) {
		final IssueArgument arg = new IssueArgument();
		arg.setArgument();
		arg.setConnectionId(connectionid);
		arg.setProjectId(projectId);
		arg.setIssueId(-1);

		runTransaction(new TransitFragment() {
			@Override
			public void action(FragmentTransaction tran) {
				tran.replace(R.id.fragmentOne, IssueEdit.newInstance(arg));
			}
		}, null);
	}

	@Override
	public void onIssueRefreshed(int connectionid, int issueid) {
		final IssueArgument arg = new IssueArgument();
		arg.setArgument();
		arg.setConnectionId(connectionid);
		arg.setIssueId(issueid);

		runTransaction(new TransitFragment() {
			@Override
			public void action(FragmentTransaction tran) {
				tran.replace(R.id.fragmentOneHeader, IssueTitle.newInstance(arg));
			}
		}, null);

		manager.getFragment().popBackStack();
	}

	@Override
	public void issue(int connection, int issueid) {
		onIssueSelected(connection, issueid);
	}

	@Override
	public boolean url(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		manager.kickActivity(intent);
		return true;
	}

}
