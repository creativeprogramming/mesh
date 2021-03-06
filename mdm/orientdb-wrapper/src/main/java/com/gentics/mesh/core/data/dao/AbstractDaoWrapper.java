package com.gentics.mesh.core.data.dao;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.HibCoreElement;
import com.gentics.mesh.core.data.HibElement;
import com.gentics.mesh.core.data.generic.PermissionProperties;
import com.gentics.mesh.core.data.perm.InternalPermission;
import com.gentics.mesh.core.data.role.HibRole;
import com.gentics.mesh.core.rest.common.GenericRestResponse;
import com.gentics.mesh.core.rest.common.PermissionInfo;
import com.gentics.mesh.madl.traversal.TraversalResult;

import dagger.Lazy;

public abstract class AbstractDaoWrapper<T extends HibElement> implements DaoWrapper<T> {

	protected final Lazy<BootstrapInitializer> boot;

	protected final Lazy<PermissionProperties> permissions;

	public AbstractDaoWrapper(Lazy<BootstrapInitializer> boot, Lazy<PermissionProperties> permissions) {
		this.boot = boot;
		this.permissions = permissions;
	}

	/**
	 * Compare both values in order to determine whether the graph value should be updated.
	 * 
	 * @param restValue
	 *            Rest model string value
	 * @param graphValue
	 *            Graph string value
	 * @return true if restValue is not null and the restValue is not equal to the graph value. Otherwise false.
	 */
	protected <E> boolean shouldUpdate(E restValue, E graphValue) {
		return restValue != null && !restValue.equals(graphValue);
	}

	@Override
	public PermissionInfo getRolePermissions(HibCoreElement element, InternalActionContext ac, String roleUuid) {
		return permissions.get().getRolePermissions(element, ac, roleUuid);
	}

	@Override
	public TraversalResult<? extends HibRole> getRolesWithPerm(T element, InternalPermission perm) {
		return permissions.get().getRolesWithPerm(element, perm);
	}

	@Override
	public void setRolePermissions(T element, InternalActionContext ac, GenericRestResponse model) {
		model.setRolePerms(permissions.get().getRolePermissions(element, ac, ac.getRolePermissionParameters().getRoleUuid()));
	}

}
