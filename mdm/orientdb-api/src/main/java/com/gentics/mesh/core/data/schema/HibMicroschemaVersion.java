package com.gentics.mesh.core.data.schema;

import com.gentics.mesh.core.rest.microschema.MicroschemaVersionModel;
import com.gentics.mesh.core.rest.schema.MicroschemaReference;

public interface HibMicroschemaVersion extends HibFieldSchemaVersionElement {

	MicroschemaVersionModel getSchema();

	MicroschemaReference transformToReference();

	HibMicroschema getSchemaContainer();

	HibMicroschemaVersion getPreviousVersion();

	HibMicroschemaVersion getNextVersion();

	SchemaChange<?> getNextChange();

	void deleteElement();

}
