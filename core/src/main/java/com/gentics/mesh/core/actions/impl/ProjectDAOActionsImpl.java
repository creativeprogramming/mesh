package com.gentics.mesh.core.actions.impl;

import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.context.BulkActionContext;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.action.DAOActionContext;
import com.gentics.mesh.core.action.ProjectDAOActions;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.dao.ProjectDaoWrapper;
import com.gentics.mesh.core.data.page.Page;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.relationship.GraphPermission;
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
	public Project loadByUuid(DAOActionContext ctx, String uuid, GraphPermission perm, boolean errorIfNotFound) {
		ProjectDaoWrapper projectDao = ctx.tx().data().projectDao();
		if (perm == null) {
			return projectDao.findByUuid(uuid);
		} else {
			return projectDao.loadObjectByUuid(ctx.ac(), uuid, perm, errorIfNotFound);
		}
	}

	@Override
	public Project loadByName(DAOActionContext ctx, String name, GraphPermission perm, boolean errorIfNotFound) {
		if (perm == null) {
			return ctx.tx().data().projectDao().findByName(name);
		} else {
			throw new RuntimeException("Not supported");
		}
	}

	@Override
	public TransformablePage<? extends Project> loadAll(DAOActionContext ctx, PagingParameters pagingInfo) {
		return ctx.tx().data().projectDao().findAll(ctx.ac(), pagingInfo);
	}

	@Override
	public Page<? extends Project> loadAll(DAOActionContext ctx, PagingParameters pagingInfo,
		Predicate<Project> extraFilter) {
		return ctx.tx().data().projectDao().findAll(ctx.ac(), pagingInfo, extraFilter);
	}

	@Override
	public Project create(Tx tx, InternalActionContext ac, EventQueueBatch batch, String uuid) {
		return tx.data().projectDao().create(ac, batch, uuid);
	}

	@Override
	public boolean update(Tx tx, Project element, InternalActionContext ac, EventQueueBatch batch) {
		return tx.data().projectDao().update(element, ac, batch);
	}

	public void delete(Tx tx, Project project, BulkActionContext bac) {
		tx.data().projectDao().delete(project, bac);
	}

	@Override
	public ProjectResponse transformToRestSync(Tx tx, Project project, InternalActionContext ac, int level, String... languageTags) {
		return tx.data().projectDao().transformToRestSync(project, ac, level, languageTags);
	}

	@Override
	public String getAPIPath(Tx tx, InternalActionContext ac, Project project) {
		return project.getAPIPath(ac);
	}

	@Override
	public String getETag(Tx tx, InternalActionContext ac, Project project) {
		return project.getETag(ac);
	}

}
