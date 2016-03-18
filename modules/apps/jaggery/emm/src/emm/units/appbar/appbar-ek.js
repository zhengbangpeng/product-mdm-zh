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

function onRequest(context) {
    var userModule = require("/modules/user.js").userModule;
    var mdmProps = require('/config/mdm-props.js').config();
    var uiPermissions = userModule.getUIPermissions();
    context["permissions"] = uiPermissions;

    var links = {
        "user-mgt": [],
        "role-mgt": [],
        "policy-mgt": [],
        "device-mgt": []
    };

    if (uiPermissions["ADD_USER"]) {
        links["user-mgt"].push({
            "title": "添加用户",
            "icon": "fw-add",
            "url": "/emm/users/add-user"
        });
    }
    if (uiPermissions["ADD_ROLE"]) {
        links["role-mgt"].push({
            "title": "添加角色",
            "icon": "fw-add",
            "url": "/emm/roles/add-role"
        });
    }

    if (uiPermissions["ADD_POLICY"]) {
        links["policy-mgt"].push({
            "title": "添加策略",
            "icon": "fw-add",
            "url": "/emm/policies/add-policy"
        });
    }

    if (uiPermissions["CHANGE_POLICY_PRIORITY"]) {
        links["policy-mgt"].push({
            "title": "策略优先级",
            "icon": "fw-throttling-policy",
            "url": "/emm/policies/priority"
        });

        links["policy-mgt"].push({
            "title": "应用修改",
            "icon": "fw-check",
            "url": "#",
            "tooltip": "Click to apply policy changes to devices (This depends on policy priority if you have multiple policies)",
            "id": "apply-changes"
        });
    }

    // following context.link value comes here based on the value passed at the point
    // where units are attached to a page zone.
    // eg: {{unit "appbar" pageLink="users" title="User Management"}}
    context["currentActions"] = links[context["pageLink"]];
    context["enrollmentURL"] = mdmProps.generalConfig.host + mdmProps.enrollmentDir;
    return context;
}