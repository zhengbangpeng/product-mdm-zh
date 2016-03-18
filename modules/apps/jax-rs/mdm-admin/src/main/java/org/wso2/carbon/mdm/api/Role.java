/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.mdm.api.common.MDMAPIException;
import org.wso2.carbon.mdm.api.util.MDMAPIUtils;
import org.wso2.carbon.mdm.api.util.ResponsePayload;
import org.wso2.carbon.mdm.beans.RoleWrapper;
import org.wso2.carbon.mdm.util.SetReferenceTransformer;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Role {

    private static Log log = LogFactory.getLog(Role.class);

    /**
     * Get user roles (except all internal roles) from system.
     *
     * @return A list of users
     * @throws org.wso2.carbon.mdm.api.common.MDMAPIException
     */
    @GET
    @Produces ({MediaType.APPLICATION_JSON})
    public Response getRoles() throws MDMAPIException {
        UserStoreManager userStoreManager = MDMAPIUtils.getUserStoreManager();
        String[] roles;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles");
            }
            roles = userStoreManager.getRoleNames();

        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of user roles.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        // removing all internal roles and roles created for Service-providers
        List<String> filteredRoles = new ArrayList<String>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Application/"))) {
                filteredRoles.add(role);
            }
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All user roles were successfully retrieved.");
        responsePayload.setResponseContent(filteredRoles);
        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    /**
     * Get user roles by user store(except all internal roles) from system.
     *
     * @return A list of users
     * @throws org.wso2.carbon.mdm.api.common.MDMAPIException
     */
    @GET
    @Path ("{userStore}")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response getRoles(@PathParam ("userStore") String userStore) throws MDMAPIException {
        AbstractUserStoreManager abstractUserStoreManager = (AbstractUserStoreManager) MDMAPIUtils.getUserStoreManager();
        String[] roles;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles");
            }
            roles = abstractUserStoreManager.getRoleNames(userStore+"/*", -1, false, true, true);

        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of user roles.";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        // removing all internal roles and roles created for Service-providers
        List<String> filteredRoles = new ArrayList<String>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Application/"))) {
                filteredRoles.add(role);
            }
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All user roles were successfully retrieved.");
        responsePayload.setResponseContent(filteredRoles);
        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    /**
     * Get user roles by providing a filtering criteria(except all internal roles & system roles) from system.
     *
     * @return A list of users
     * @throws org.wso2.carbon.mdm.api.common.MDMAPIException
     */
    @GET
    @Path ("search")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response getMatchingRoles(@QueryParam ("filter") String filter) throws MDMAPIException {
        AbstractUserStoreManager abstractUserStoreManager = (AbstractUserStoreManager) MDMAPIUtils.getUserStoreManager();
        String[] roles;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles using filter : " + filter);
            }
            roles = abstractUserStoreManager.getRoleNames("*" + filter + "*", -1, true, true, true);

        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the list of user roles using the filter : " + filter;
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        // removing all internal roles and roles created for Service-providers
        List<String> filteredRoles = new ArrayList<String>();
        for (String role : roles) {
            if (!(role.startsWith("Internal/") || role.startsWith("Application/"))) {
                filteredRoles.add(role);
            }
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All matching user roles were successfully retrieved.");
        responsePayload.setResponseContent(filteredRoles);
        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    /**
     * Get role permissions.
     *
     * @return list of permissions
     * @throws MDMAPIException
     */
    @GET
    @Path ("permissions")
    @Produces ({MediaType.APPLICATION_JSON})
    public ResponsePayload getPermissions(@QueryParam ("rolename") String roleName) throws MDMAPIException {
        final UserRealm userRealm = MDMAPIUtils.getUserRealm();
        org.wso2.carbon.user.core.UserRealm userRealmCore = null;
        final UIPermissionNode rolePermissions;
        if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
            userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
        }

        try {
            final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
	        rolePermissions = userRealmProxy.getRolePermissions(roleName, MultitenantConstants.SUPER_TENANT_ID);
            UIPermissionNode[] deviceMgtPermissions = new UIPermissionNode[2];

            for (UIPermissionNode permissionNode : rolePermissions.getNodeList()) {
                if (permissionNode.getResourcePath().equals("/permission/admin")) {
                    for (UIPermissionNode node : permissionNode.getNodeList()) {
                        if (node.getResourcePath().equals("/permission/admin/device-mgt")) {
                            deviceMgtPermissions[0] = node;
                        } else if (node.getResourcePath().equals("/permission/admin/login")) {
                            deviceMgtPermissions[1] = node;
                        }
                    }
                }
            }
            rolePermissions.setNodeList(deviceMgtPermissions);
        } catch (UserAdminException e) {
            String msg = "Error occurred while retrieving the user role";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All permissions retrieved");
        responsePayload.setResponseContent(rolePermissions);
        return responsePayload;
    }

    /**
     * Get user role of the system
     *
     * @return user role
     * @throws org.wso2.carbon.mdm.api.common.MDMAPIException
     */
    @GET
    @Path("role")
    @Produces ({MediaType.APPLICATION_JSON})
    public ResponsePayload getRole(@QueryParam ("rolename") String roleName) throws MDMAPIException {
        final UserStoreManager userStoreManager = MDMAPIUtils.getUserStoreManager();
        final UserRealm userRealm = MDMAPIUtils.getUserRealm();
        org.wso2.carbon.user.core.UserRealm userRealmCore = null;
        if (userRealm instanceof org.wso2.carbon.user.core.UserRealm) {
            userRealmCore = (org.wso2.carbon.user.core.UserRealm) userRealm;
        }

        RoleWrapper roleWrapper = new RoleWrapper();
        try {
            final UserRealmProxy userRealmProxy = new UserRealmProxy(userRealmCore);
            if (log.isDebugEnabled()) {
                log.debug("Getting the list of user roles");
            }
            if (userStoreManager.isExistingRole(roleName)) {
                roleWrapper.setRoleName(roleName);
                roleWrapper.setUsers(userStoreManager.getUserListOfRole(roleName));
                // Get the permission nodes and hand picking only device management and login perms
                final UIPermissionNode rolePermissions =
                        userRealmProxy.getRolePermissions(roleName, MultitenantConstants.SUPER_TENANT_ID);
                UIPermissionNode[] deviceMgtPermissions = new UIPermissionNode[2];

                for (UIPermissionNode permissionNode : rolePermissions.getNodeList()) {
                    if (permissionNode.getResourcePath().equals("/permission/admin")) {
                        for (UIPermissionNode node : permissionNode.getNodeList()) {
                            if (node.getResourcePath().equals("/permission/admin/device-mgt")) {
                                deviceMgtPermissions[0] = node;
                            } else if (node.getResourcePath().equals("/permission/admin/login")) {
                                deviceMgtPermissions[1] = node;
                            }
                        }
                    }
                }
                rolePermissions.setNodeList(deviceMgtPermissions);
                ArrayList<String> permList = new ArrayList<String>();
                iteratePermissions(rolePermissions, permList);
                roleWrapper.setPermissionList(rolePermissions);
                String[] permListAr = new String[permList.size()];
                roleWrapper.setPermissions(permList.toArray(permListAr));
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving the user role";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        } catch (UserAdminException e) {
            String msg = "Error occurred while retrieving the user role";
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setStatusCode(HttpStatus.SC_OK);
        responsePayload.setMessageFromServer("All user roles were successfully retrieved.");
        responsePayload.setResponseContent(roleWrapper);
        return responsePayload;
    }

    /**
     * API is used to persist a new Role
     *
     * @param roleWrapper
     * @return
     * @throws MDMAPIException
     */
    @POST
    @Produces ({MediaType.APPLICATION_JSON})
    public Response addRole(RoleWrapper roleWrapper) throws MDMAPIException {
        UserStoreManager userStoreManager = MDMAPIUtils.getUserStoreManager();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Persisting the role to user store");
            }
            Permission[] permissions = null;
            if (roleWrapper.getPermissions() != null && roleWrapper.getPermissions().length > 0) {
                permissions = new Permission[roleWrapper.getPermissions().length];

                for (int i = 0; i < permissions.length; i++) {
                    String permission = roleWrapper.getPermissions()[i];
                    permissions[i] = new Permission(permission, CarbonConstants.UI_PERMISSION_ACTION);
                }
            }
            userStoreManager.addRole(roleWrapper.getRoleName(), roleWrapper.getUsers(), permissions);
        } catch (UserStoreException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return Response.status(HttpStatus.SC_CREATED).build();
    }

    /**
     * API is used to update a role Role
     *
     * @param roleWrapper
     * @return
     * @throws MDMAPIException
     */
    @PUT
    @Produces ({MediaType.APPLICATION_JSON})
    public Response updateRole(@QueryParam ("rolename") String roleName, RoleWrapper roleWrapper) throws
            MDMAPIException {
        final UserStoreManager userStoreManager = MDMAPIUtils.getUserStoreManager();
        final AuthorizationManager authorizationManager = MDMAPIUtils.getAuthorizationManager();
        String newRoleName = roleWrapper.getRoleName();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating the role to user store");
            }
            if (newRoleName != null && !roleName.equals(newRoleName)) {
                userStoreManager.updateRoleName(roleName, newRoleName);
            }
            if (roleWrapper.getUsers() != null) {
                SetReferenceTransformer transformer = new SetReferenceTransformer();
                transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(newRoleName)),
                                      Arrays.asList(roleWrapper.getUsers()));
                final String[] usersToAdd = (String[])
                        transformer.getObjectsToAdd().toArray(new String[transformer.getObjectsToAdd().size()]);
                final String[] usersToDelete = (String[])
                        transformer.getObjectsToRemove().toArray(new String[transformer.getObjectsToRemove().size()]);
                userStoreManager.updateUserListOfRole(newRoleName, usersToDelete, usersToAdd);
            }
            if (roleWrapper.getPermissions() != null) {
                // Delete all authorizations for the current role before authorizing the permission tree
                authorizationManager.clearRoleAuthorization(roleName);
                if (roleWrapper.getPermissions().length > 0) {
                    for (int i = 0; i < roleWrapper.getPermissions().length; i++) {
                        String permission = roleWrapper.getPermissions()[i];
                        authorizationManager.authorizeRole(roleName, permission, CarbonConstants.UI_PERMISSION_ACTION);
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return Response.status(HttpStatus.SC_OK).build();
    }

    /**
     * API is used to delete a role and authorizations
     *
     * @param roleName
     * @return
     * @throws MDMAPIException
     */
    @DELETE
    @Produces ({MediaType.APPLICATION_JSON})
    public Response deleteRole(@QueryParam ("rolename") String roleName) throws MDMAPIException {
        final UserStoreManager userStoreManager = MDMAPIUtils.getUserStoreManager();
        final AuthorizationManager authorizationManager = MDMAPIUtils.getAuthorizationManager();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting the role in user store");
            }
            userStoreManager.deleteRole(roleName);
            // Delete all authorizations for the current role before deleting
            authorizationManager.clearRoleAuthorization(roleName);
        } catch (UserStoreException e) {
            String msg = "Error occurred while deleting the role: " + roleName;
            log.error(msg, e);
            throw new MDMAPIException(msg, e);
        }
        return Response.status(HttpStatus.SC_OK).build();
    }

    /**
     * API is used to update users of a role
     *
     * @param roleName
     * @param userList
     * @return
     * @throws MDMAPIException
     */
    @PUT
    @Path ("users")
    @Produces ({MediaType.APPLICATION_JSON})
    public Response updateUsers(@QueryParam ("rolename") String roleName, List<String> userList)
            throws MDMAPIException {
        final UserStoreManager userStoreManager = MDMAPIUtils.getUserStoreManager();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating the users of a role");
            }
            SetReferenceTransformer transformer = new SetReferenceTransformer();
            transformer.transform(Arrays.asList(userStoreManager.getUserListOfRole(roleName)),
                    userList);
            final String[] usersToAdd = (String[])
                    transformer.getObjectsToAdd().toArray(new String[transformer.getObjectsToAdd().size()]);
            final String[] usersToDelete = (String[])
                    transformer.getObjectsToRemove().toArray(new String[transformer.getObjectsToRemove().size()]);

            userStoreManager.updateUserListOfRole(roleName, usersToDelete, usersToAdd);
        } catch (UserStoreException e) {
            String msg = "Error occurred while saving the users of the role: " + roleName;
            log.error(msg, e);
            throw new MDMAPIException(e.getMessage(), e);
        }
        return Response.status(HttpStatus.SC_OK).build();
    }

    public ArrayList<String> iteratePermissions(UIPermissionNode uiPermissionNode, ArrayList<String> list) {
        for (UIPermissionNode permissionNode : uiPermissionNode.getNodeList()) {
            list.add(permissionNode.getResourcePath());
            if (permissionNode.getNodeList() != null && permissionNode.getNodeList().length > 0) {
                iteratePermissions(permissionNode, list);
            }
        }
        return list;
    }
}
