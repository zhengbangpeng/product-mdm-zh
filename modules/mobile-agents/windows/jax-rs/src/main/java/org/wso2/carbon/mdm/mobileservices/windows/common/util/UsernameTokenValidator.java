/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.mobileservices.windows.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.validate.Credential;
import org.apache.ws.security.validate.Validator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.AuthenticationException;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Validator class for user authentication checking the default carbon user store.
 */
public class UsernameTokenValidator implements Validator {

    private static final int USER_SEGMENT = 0;
    private static final int DOMAIN_SEGMENT = 1;
    private static final String DELIMITER = "@";
    private static Log log = LogFactory.getLog(UsernameTokenValidator.class);

    /**
     * This method validates the username token in SOAP message coming from the device.
     *
     * @param credential  - Username token credentials coming from device
     * @param requestData - Request data associated with the request
     * @return - Credential object if authentication is success, or null if not success
     * @throws WSSecurityException
     */
    @Override
    public Credential validate(Credential credential, RequestData requestData) throws
            WSSecurityException {

        String domainUser = credential.getUsernametoken().getName();
        String[] domainUserArray = domainUser.split(DELIMITER);
        Credential returnCredentials;
        String user = domainUserArray[USER_SEGMENT];
        String domain = domainUserArray[DOMAIN_SEGMENT];
        String password = credential.getUsernametoken().getPassword();

        try {
            if (authenticate(user, password, domain)) {
                returnCredentials = credential;
            } else {
                String msg = "Authentication failure due to incorrect credentials.";
                log.error(msg);
                throw new WindowsDeviceEnrolmentException(msg);
            }
            //Generic exception is caught here as there is no need of taking different actions for
            //different exceptions.
        } catch (AuthenticationException e) {
            String msg = "Failure occurred in the BST validator.";
            log.error(msg, e);
            throw new WSSecurityException(msg, e);
        } catch (WindowsDeviceEnrolmentException e) {
            String msg = "Authentication Failure occurred due to binary security token.";
            log.error(msg, e);
            throw new WSSecurityException(msg, e);
        }
        return returnCredentials;
    }

    /**
     * This method authenticate the user checking the carbon default user store.
     *
     * @param username     - Username in username token
     * @param password     - Password in username token
     * @param tenantDomain - Tenant domain is extracted from the username
     * @return - Returns boolean representing authentication result
     * @throws AuthenticationException
     */
    public boolean authenticate(String username, String password, String tenantDomain) throws
            AuthenticationException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            ctx.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ctx.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);

            if (realmService == null) {
                String msg = "RealmService not initialized.";
                log.error(msg);
                throw new AuthenticationException(msg);
            }

            int tenantId;
            if (tenantDomain == null || tenantDomain.trim().isEmpty()) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            }

            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                String msg = "Invalid tenant domain " + tenantDomain;
                log.error(msg);
                throw new AuthenticationException(msg);
            }
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);

            return userRealm.getUserStoreManager().authenticate(username, password);
        } catch (UserStoreException e) {
            String msg = "User store is not initialized.";
            log.error(msg, e);
            throw new AuthenticationException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
