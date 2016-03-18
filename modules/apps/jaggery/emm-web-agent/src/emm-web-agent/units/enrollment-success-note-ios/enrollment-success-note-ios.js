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

function onRequest (context) {
    var log = new Log("enrollment-success-note-ios-unit");
    log.debug("calling enrollment-success-note-ios-unit backend js");

    var mdmProps = require('/config/mdm-props.js').config();
    context["deviceOwner"] = session.get("enrolledUser");
    context["serverURL"] = mdmProps["httpsURL"] + mdmProps["appContext"];
    var companyProps = session.get("COMPANY_DETAILS");
    if (!companyProps) {
        context.companyName = mdmProps.generalConfig.companyName;
    } else {
        context.companyName = companyProps.companyName;
    }
    return context;
}