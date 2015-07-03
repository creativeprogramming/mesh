package com.gentics.mesh.core.data.root.impl;

import static com.gentics.mesh.core.data.relationship.MeshRelationships.ASSIGNED_TO_PROJECT;
import static com.gentics.mesh.core.data.relationship.MeshRelationships.HAS_SCHEMA_CONTAINER;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gentics.mesh.core.data.SchemaContainer;
import com.gentics.mesh.core.data.impl.SchemaContainerImpl;
import com.gentics.mesh.core.data.root.SchemaContainerRoot;

public class SchemaContainerRootImpl extends AbstractRootVertex<SchemaContainer> implements SchemaContainerRoot {

	@Override
	protected Class<? extends SchemaContainer> getPersistanceClass() {
		return SchemaContainerImpl.class;
	}

	@Override
	protected String getRootLabel() {
		return HAS_SCHEMA_CONTAINER;
	}

	@Override
	public void addSchemaContainer(SchemaContainer schema) {
		linkOut(schema.getImpl(), HAS_SCHEMA_CONTAINER);
	}

	@Override
	public SchemaContainer create(String name) {
		SchemaContainerImpl schema = getGraph().addFramedVertex(SchemaContainerImpl.class);
		schema.setSchemaName(name);
		addSchemaContainer(schema);
		return schema;
	}

	// TODO unique index

	@Override
	public SchemaContainerRootImpl getImpl() {
		return this;
	}

	@Override
	public SchemaContainer findByName(String projectName, String name) {
		if (StringUtils.isEmpty(projectName) || StringUtils.isEmpty(name)) {
			throw new NullPointerException("name or project name null");
		}
		return out(HAS_SCHEMA_CONTAINER).has("name", name).has(SchemaContainerImpl.class).mark().out(ASSIGNED_TO_PROJECT).has("name", projectName)
				.back().nextOrDefault(SchemaContainerImpl.class, null);
	}

}
