package com.gentics.mesh.core.actions.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.core.action.DAOActionContext;
import com.gentics.mesh.core.action.LoadAllAction;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.schema.HibSchema;
import com.gentics.mesh.parameter.PagingParameters;

@Singleton
public class ProjectSchemaLoadAllActionImpl implements LoadAllAction<HibSchema> {

	@Inject
	public ProjectSchemaLoadAllActionImpl() {
	}

	@Override
	public TransformablePage<? extends HibSchema> loadAll(DAOActionContext ctx, PagingParameters pagingInfo) {
		return ctx.tx().data().schemaDao().findAll(ctx.ac(), ctx.project(), pagingInfo);
	}
}
