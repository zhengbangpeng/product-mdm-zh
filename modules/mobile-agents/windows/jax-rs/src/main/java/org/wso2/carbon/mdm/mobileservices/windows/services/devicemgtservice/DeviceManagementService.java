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

package org.wso2.carbon.mdm.mobileservices.windows.services.devicemgtservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.mdm.mobileservices.windows.common.util.Message;
import org.wso2.carbon.mdm.mobileservices.windows.common.util.WindowsAPIUtils;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Windows Device Management REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */
@WebService
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public class DeviceManagementService {

    private static Log log = LogFactory.getLog(DeviceManagementService.class);

    /**
     * Get all devices.Returns list of Windows devices registered in MDM.
     *
     * @return Device List
     * @throws WindowsConfigurationException
     */
    @GET
    public List<Device> getAllDevices()
            throws WindowsConfigurationException {
        String msg;
        List<org.wso2.carbon.device.mgt.common.Device> devices;

        try {
            devices = WindowsAPIUtils.getDeviceManagementService().
                    getAllDevices(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
        } catch (DeviceManagementException e) {
            msg = "Error occurred while fetching the device list.";
            log.error(msg, e);
            throw new WindowsConfigurationException(msg, e);
        }
        return devices;
    }

    /**
     * Fetch Windows device details of a given device Id.
     *
     * @param id Device Id
     * @return Device
     * @throws WindowsConfigurationException
     */
    @GET
    @Path("{id}")
    public org.wso2.carbon.device.mgt.common.Device getDevice(@PathParam("id") String id)
            throws WindowsConfigurationException {

        String msg;
        org.wso2.carbon.device.mgt.common.Device device;

        try {
            DeviceIdentifier deviceIdentifier = WindowsAPIUtils.convertToDeviceIdentifierObject(id);
            device = WindowsAPIUtils.getDeviceManagementService().getDevice(deviceIdentifier);
            if (device == null) {
                Response.status(Response.Status.NOT_FOUND);
            }
        } catch (DeviceManagementException deviceMgtEx) {
            msg = "Error occurred while fetching the device information.";
            log.error(msg, deviceMgtEx);
            throw new WindowsConfigurationException(msg, deviceMgtEx);
        }
        return device;
    }

    /**
     * Update Windows device details of given device id.
     *
     * @param id     Device Id
     * @param device Device Details
     * @return Message
     * @throws WindowsConfigurationException
     */
    @PUT
    @Path("{id}")
    public Message updateDevice(@PathParam("id") String id, Device device)
            throws WindowsConfigurationException {
        String msg;
        Message responseMessage = new Message();
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(id);
        deviceIdentifier
                .setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
        boolean result;
        try {
            device.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            result = WindowsAPIUtils.getDeviceManagementService()
                    .updateDeviceInfo(deviceIdentifier, device);
            if (result) {
                Response.status(Response.Status.ACCEPTED);
                responseMessage.setResponseMessage("Device information has modified successfully.");
            } else {
                Response.status(Response.Status.NOT_MODIFIED);
                responseMessage.setResponseMessage("Device not found for the update.");
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while modifying the device information.";
            log.error(msg, e);
            throw new WindowsConfigurationException(msg, e);
        }
        return responseMessage;
    }

    @GET
    @Path("license")
    @Produces("application/json")
    public License getLicense() throws WindowsConfigurationException {
        License license;

        try {
            license =
                    WindowsAPIUtils.getDeviceManagementService().getLicense(
                            DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS,
                            DeviceManagementConstants.LanguageCodes.LANGUAGE_CODE_ENGLISH_US);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the license configured for Windows device enrollment";
            log.error(msg, e);
            throw new WindowsConfigurationException(msg, e);
        }
        return license;
    }

}

