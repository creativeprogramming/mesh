package com.gentics.mesh.core.actions.impl;

import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.context.BulkActionContext;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.action.DAOActionContext;
import com.gentics.mesh.core.action.ProjectDAOActions;
import com.gentics.mesh.core.data.dao.ProjectDaoWrapper;
import com.gentics.mesh.core.data.page.Page;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.perm.InternalPermission;
import com.gentics.mesh.core.data.project.HibProject;
import com.gentics.mesh.core.db.Tx;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.event.EventQueueBatch;
import com.gentics.mesh.parameter.PagingParameters;

@Singleton
public class ProjectDAOActionsImpl implements ProjectDAOActions {

	@Inject
	public ProjectDAOActionsImpl() {
	}

	@Override
	public HibProject loadByUuid(DAOActionContext ctx, String uuid, InternalPermission perm, boolean errorIfNotFound) {
		ProjectDaoWrapper projectDao = ctx.tx().data().projectDao();
		if (perm == null) {
			return projectDao.findByUuid(uuid);
		} else {
			return projectDao.loadObjectByUuid(ctx.ac(), uuid, perm, errorIfNotFound);
		}
	}

	@Override
	public HibProject loadByName(DAOActionContext ctx, String name, InternalPermission perm, boolean errorIfNotFound) {
		if (perm == null) {
			return ctx.tx().data().projectDao().findByName(name);
		} else {
			throw new RuntimeException("Not supported");
		}
	}

	@Override
	public TransformablePage<? extends HibProject> loadAll(DAOActionContext ctx, PagingParameters pagingInfo) {
		return ctx.tx().data().projectDao().findAll(ctx.ac(), pagingInfo);
	}

	@Override
	public Page<? extends HibProject> loadAll(DAOActionContext ctx, PagingParameters pagingInfo,
		Predicate<HibProject> extraFilter) {
		return ctx.tx().data().projectDao().findAll(ctx.ac(), pagingInfo, extraFilter);
	}

	@Override
	public HibProject create(Tx tx, InternalActionContext ac, EventQueueBatch batch, String uuid) {
		return tx.data().projectDao().create(ac, batch, uuid);
	}

	@Override
	public boolean update(Tx tx, HibProject element, InternalActionContext ac, EventQueueBatch batch) {
		return tx.data().projectDao().update(element, ac, batch);
	}

	public void delete(Tx tx, HibProject project, BulkActionContext bac) {
		tx.data().projectDao().delete(project, bac);
	}

	@Override
	public ProjectResponse transformToRestSync(Tx tx, HibProject project, InternalActionContext ac, int level, String... languageTags) {
		return tx.data().projectDao().transformToRestSync(project, ac, level, languageTags);
	}

	@Override
	public String getAPIPath(Tx tx, InternalActionContext ac, HibProject project) {
		return project.toProject().getAPIPath(ac);
	}

	@Override
	public String getETag(Tx tx, InternalActionContext ac, HibProject project) {
		return project.toProject().getETag(ac);
	}

}
