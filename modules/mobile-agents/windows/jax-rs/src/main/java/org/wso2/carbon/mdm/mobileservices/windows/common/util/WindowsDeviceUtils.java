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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.mdm.mobileservices.windows.common.PluginConstants;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.BadRequestException;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for get windows device utilities.
 */
public class WindowsDeviceUtils {

    private static final String COMMA_SEPARATION_PATTERN = ", ";

    public DeviceIDHolder validateDeviceIdentifiers(List<String> deviceIDs,
                                                    Message message, MediaType responseMediaType) {
        if (deviceIDs == null) {
            message.setResponseMessage("Device identifier list is empty");
            throw new BadRequestException(message, responseMediaType);
        }
        List<String> errorDeviceIdList = new ArrayList<String>();
        List<DeviceIdentifier> validDeviceIDList = new ArrayList<DeviceIdentifier>();
        int deviceIDCounter = 0;

        for (String deviceID : deviceIDs) {
            deviceIDCounter++;
            if (deviceID == null || deviceID.isEmpty()) {
                errorDeviceIdList.add(String.format(PluginConstants.DeviceConstants.DEVICE_ID_NOT_FOUND,
                        deviceIDCounter));
                continue;
            }
            try {
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                deviceIdentifier.setId(deviceID);
                deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.
                        MOBILE_DEVICE_TYPE_WINDOWS);
                Device device = WindowsAPIUtils.getDeviceManagementService().
                        getDevice(deviceIdentifier);
                if (device == null || device.getDeviceIdentifier() == null ||
                        device.getDeviceIdentifier().isEmpty()) {
                    errorDeviceIdList.add(String.format(PluginConstants.DeviceConstants.DEVICE_ID_NOT_FOUND,
                            deviceIDCounter));
                    continue;
                }
                validDeviceIDList.add(deviceIdentifier);
            } catch (DeviceManagementException e) {
                errorDeviceIdList.add(String.format(PluginConstants.DeviceConstants.DEVICE_ID_SERVICE_NOT_FOUND,
                        deviceIDCounter));
            }
        }
        DeviceIDHolder deviceIDHolder = new DeviceIDHolder();
        deviceIDHolder.setValidDeviceIDList(validDeviceIDList);
        deviceIDHolder.setErrorDeviceIdList(errorDeviceIdList);
        return deviceIDHolder;
    }

    public String convertErrorMapIntoErrorMessage(List<String> errorDeviceIdList) {
        return StringUtils.join(errorDeviceIdList.iterator(), COMMA_SEPARATION_PATTERN);
    }
}
