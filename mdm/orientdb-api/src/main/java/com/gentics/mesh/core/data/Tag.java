package com.gentics.mesh.core.data;

import static com.gentics.mesh.core.rest.MeshEvent.TAG_CREATED;
import static com.gentics.mesh.core.rest.MeshEvent.TAG_DELETED;
import static com.gentics.mesh.core.rest.MeshEvent.TAG_UPDATED;

import java.util.Objects;

import com.gentics.mesh.ElementType;
import com.gentics.mesh.core.TypeInfo;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.relationship.GraphPermission;
import com.gentics.mesh.core.data.user.MeshAuthUser;
import com.gentics.mesh.core.rest.common.ContainerType;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.relationship.GraphPermission;
import com.gentics.mesh.core.rest.common.ContainerType;
import com.gentics.mesh.core.rest.tag.TagReference;
import com.gentics.mesh.core.rest.tag.TagResponse;

/**
 * Graph domain model interface for a tag.
 * 
 * Tags can currently only hold a single string value. Tags are not localizable. A tag can only be assigned to a single tag family.
 */
public interface Tag extends MeshCoreVertex<TagResponse, Tag>, ReferenceableElement<TagReference>, UserTrackingVertex, ProjectElement, HibCoreElement {

	TypeInfo TYPE_INFO = new TypeInfo(ElementType.TAG, TAG_CREATED, TAG_UPDATED, TAG_DELETED);

	/**
	 * Compose the index name for tags. Use the projectUuid in order to create a project specific index.
	 * 
	 * @param projectUuid
	 * @return
	 */
	static String composeIndexName(String projectUuid) {
		Objects.requireNonNull(projectUuid, "A projectUuid must be provided.");
		StringBuilder indexName = new StringBuilder();
		indexName.append("tag");
		indexName.append("-").append(projectUuid);
		return indexName.toString();
	}

	static String composeDocumentId(String elementUuid) {
		Objects.requireNonNull(elementUuid, "A elementUuid must be provided.");
		return elementUuid;
	}

	@Override
	default TypeInfo getTypeInfo() {
		return TYPE_INFO;
	}

	/**
	 * Return the tag family to which the tag belongs.
	 * 
	 * @return Tag family of the tag
	 */
	TagFamily getTagFamily();

	/**
	 * Set the tag family of this tag.
	 * 
	 * @param tagFamily
	 */
	void setTagFamily(TagFamily tagFamily);

	/**
	 * Return the project to which the tag was assigned to
	 * 
	 * @return Project of the tag
	 */
	Project getProject();

}
