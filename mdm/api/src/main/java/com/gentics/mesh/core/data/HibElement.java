package com.gentics.mesh.core.data;

import java.util.Set;

import com.gentics.mesh.core.data.perm.InternalPermission;
import com.gentics.mesh.core.data.role.HibRole;
import com.gentics.mesh.event.EventQueueBatch;

public interface HibElement {

	/**
	 * UUID of the element.
	 * 
	 * @return
	 */
	String getUuid();

	/**
	 * Id of the element. This is legacy support method which is used to handle perm checks.
	 * 
	 * @return
	 */
	Object getId();

	/**
	 * Return set of role uuids for the given permission that were granted on the element.
	 * 
	 * @param permission
	 * @return
	 */
	Set<String> getRoleUuidsForPerm(InternalPermission permission);

	/**
	 * Set the role uuid for the given permission.
	 * 
	 * @param permission
	 * @param allowedRoles
	 */
	void setRoleUuidForPerm(InternalPermission permission, Set<String> allowedRoles);

	/**
	 * Tests if the {@link GraphPermission}s READ_PUBLISHED_PERM and READ_PUBLISHED can be set for this element.
	 * 
	 * @return
	 */
	default boolean hasPublishPermissions() {
		return false;
	}

}
