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

package org.wso2.carbon.mdm.mobileservices.windows.services.syncml.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.mdm.mobileservices.windows.common.PluginConstants;
import org.wso2.carbon.mdm.mobileservices.windows.common.beans.CacheEntry;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.SyncmlMessageFormatException;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.SyncmlOperationException;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.mdm.mobileservices.windows.common.util.DeviceUtil;
import org.wso2.carbon.mdm.mobileservices.windows.common.util.WindowsAPIUtils;
import org.wso2.carbon.mdm.mobileservices.windows.operations.*;
import org.wso2.carbon.mdm.mobileservices.windows.operations.util.*;
import org.wso2.carbon.mdm.mobileservices.windows.services.syncml.SyncmlService;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.mdm.mobileservices.windows.common.util.WindowsAPIUtils.convertToDeviceIdentifierObject;

/**
 * Implementing class of SyncmlImpl interface.
 */
public class SyncmlServiceImpl implements SyncmlService {

    private static Log log = LogFactory.getLog(SyncmlServiceImpl.class);

    /**
     * This method is used to generate and return Device object from the received information at
     * the Syncml step.
     *
     * @param deviceID     - Unique device ID received from the Device
     * @param osVersion    - Device OS version
     * @param imsi         - Device IMSI
     * @param imei         - Device IMEI
     * @param manufacturer - Device Manufacturer name
     * @param model        - Device Model
     * @return - Generated device object
     */
    private Device generateDevice(String type, String deviceID, String osVersion, String imsi,
                                  String imei, String manufacturer, String model, String user) {

        Device generatedDevice = new Device();

        Device.Property OSVersionProperty = new Device.Property();
        OSVersionProperty.setName(PluginConstants.SyncML.OS_VERSION);
        OSVersionProperty.setValue(osVersion);

        Device.Property IMSEIProperty = new Device.Property();
        IMSEIProperty.setName(PluginConstants.SyncML.IMSI);
        IMSEIProperty.setValue(imsi);

        Device.Property IMEIProperty = new Device.Property();
        IMEIProperty.setName(PluginConstants.SyncML.IMEI);
        IMEIProperty.setValue(imei);

        Device.Property DevManProperty = new Device.Property();
        DevManProperty.setName(PluginConstants.SyncML.VENDOR);
        DevManProperty.setValue(manufacturer);

        Device.Property DevModProperty = new Device.Property();
        DevModProperty.setName(PluginConstants.SyncML.MODEL);
        DevModProperty.setValue(model);

        List<Device.Property> propertyList = new ArrayList<>();
        propertyList.add(OSVersionProperty);
        propertyList.add(IMSEIProperty);
        propertyList.add(IMEIProperty);
        propertyList.add(DevManProperty);
        propertyList.add(DevModProperty);

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner(user);
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);

        generatedDevice.setEnrolmentInfo(enrolmentInfo);
        generatedDevice.setDeviceIdentifier(deviceID);
        generatedDevice.setProperties(propertyList);
        generatedDevice.setType(type);

        return generatedDevice;
    }

    /**
     * Method for calling SyncML engine for producing the Syncml response. For the first SyncML message comes from
     * the device, this method produces a response to retrieve device information for enrolling the device.
     *
     * @param request - SyncML request
     * @return - SyncML response
     * @throws WindowsOperationException
     * @throws WindowsDeviceEnrolmentException
     */
    @Override
    public Response getResponse(Document request)
            throws WindowsDeviceEnrolmentException, WindowsOperationException, NotificationManagementException,
            WindowsConfigurationException {
        int msgId;
        int sessionId;
        String user;
        String token;
        String response;
        SyncmlDocument syncmlDocument;
        List<Operation> deviceInfoOperations;
        List<? extends Operation> pendingOperations;
        OperationUtils operationUtils = new OperationUtils();
        DeviceInfo deviceInfo = new DeviceInfo();

        try {
            if (SyncmlParser.parseSyncmlPayload(request) != null) {
                try {
                    syncmlDocument = SyncmlParser.parseSyncmlPayload(request);
                } catch (SyncmlMessageFormatException e) {
                    String msg = "Error occurred due to bad syncml format.";
                    log.error(msg, e);
                    throw new SyncmlMessageFormatException(msg, e);
                }
                SyncmlHeader syncmlHeader = syncmlDocument.getHeader();
                sessionId = syncmlHeader.getSessionId();
                user = syncmlHeader.getSource().getLocName();
                DeviceIdentifier deviceIdentifier = convertToDeviceIdentifierObject(syncmlHeader.getSource()
                        .getLocURI());
                msgId = syncmlHeader.getMsgID();
                if ((PluginConstants.SyncML.SYNCML_FIRST_MESSAGE_ID == msgId) &&
                        (PluginConstants.SyncML.SYNCML_FIRST_SESSION_ID == sessionId)) {
                    token = syncmlHeader.getCredential().getData();
                    CacheEntry cacheToken = (CacheEntry) DeviceUtil.getCacheEntry(token);

                    if ((cacheToken.getUsername() != null) && (cacheToken.getUsername().equals(user))) {

                        if (enrollDevice(request)) {
                            deviceInfoOperations = deviceInfo.getDeviceInfo();
                            try {
                                response = generateReply(syncmlDocument, deviceInfoOperations);
                                PolicyManagerService policyManagerService = WindowsAPIUtils.getPolicyManagerService();
                                policyManagerService.getEffectivePolicy(deviceIdentifier);
                                return Response.status(Response.Status.OK).entity(response).build();
                            } catch (PolicyManagementException e) {
                                String msg = "Error occurred in while getting effective policy.";
                                log.error(msg, e);
                                throw new WindowsConfigurationException(msg, e);
                            } catch (SyncmlOperationException e) {
                                String msg = "Error occurred in while generating hash value.";
                                log.error(msg, e);
                                throw new WindowsOperationException(msg, e);
                            }

                        } else {
                            String msg = "Error occurred in device enrollment.";
                            log.error(msg);
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                        }
                    } else {
                        String msg = "Authentication failure due to incorrect credentials.";
                        log.error(msg);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(msg).build();
                    }
                } else if (PluginConstants.SyncML.SYNCML_SECOND_MESSAGE_ID == msgId &&
                        PluginConstants.SyncML.SYNCML_FIRST_SESSION_ID == sessionId) {

                    if (enrollDevice(request)) {
                        try {
                            return Response.ok().entity(generateReply(syncmlDocument, null)).build();
                        } catch (SyncmlOperationException e) {
                            String msg = "Error occurred in while getting effective feature";
                            log.error(msg, e);
                            throw new WindowsOperationException(msg, e);
                        }
                    } else {
                        String msg = "Error occurred in modify enrollment.";
                        log.error(msg);
                        return Response.status(Response.Status.NOT_MODIFIED).entity(msg).build();
                    }
                } else if (sessionId >= PluginConstants.SyncML.SYNCML_SECOND_SESSION_ID) {
                    if ((syncmlDocument.getBody().getAlert() != null)) {
                        if (!syncmlDocument.getBody().getAlert().getData().equals(Constants.DISENROLL_ALERT_DATA)) {
                            try {
                                pendingOperations = operationUtils.getPendingOperations(syncmlDocument);
                                return Response.ok().entity(generateReply(syncmlDocument, pendingOperations)).build();
                            } catch (OperationManagementException e) {
                                String msg = "Cannot access operation management service.";
                                log.error(msg, e);
                                throw new WindowsOperationException(msg, e);
                            } catch (DeviceManagementException e) {
                                String msg = "Cannot access Device management service.";
                                log.error(msg, e);
                                throw new WindowsOperationException(msg, e);
                            } catch (FeatureManagementException e) {
                                String msg = "Error occurred in getting effective features. ";
                                log.error(msg, e);
                                throw new WindowsOperationException(msg, e);
                            } catch (PolicyComplianceException e) {
                                String msg = "Error occurred in setting policy compliance.";
                                log.error(msg, e);
                                throw new WindowsConfigurationException(msg, e);
                            } catch (NotificationManagementException e) {
                                String msg = "Error occurred in while getting notification service";
                                throw new WindowsOperationException(msg, e);
                            } catch (SyncmlOperationException e) {
                                String msg = "Error occurred in while encoding hash value.";
                                log.error(msg, e);
                                throw new WindowsOperationException(msg, e);
                            }
                        } else {
                            try {
                                if (WindowsAPIUtils.getDeviceManagementService().getDevice(deviceIdentifier) != null) {
                                    WindowsAPIUtils.getDeviceManagementService().disenrollDevice(deviceIdentifier);
                                    return Response.ok().entity(generateReply(syncmlDocument, null)).build();
                                } else {
                                    String msg = "Enrolled device can not be found in the server.";
                                    log.error(msg);
                                    return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
                                }
                            } catch (DeviceManagementException e) {
                                String msg = "Failure occurred in dis-enrollment flow.";
                                log.error(msg, e);
                                throw new WindowsOperationException(msg, e);
                            } catch (SyncmlOperationException e) {
                                String msg = "Error occurred in while generating hash value.";
                                log.error(msg, e);
                                throw new WindowsOperationException(msg, e);
                            }
                        }
                    } else {
                        try {
                            pendingOperations = operationUtils.getPendingOperations(syncmlDocument);
                            return Response.ok().entity(generateReply(syncmlDocument, pendingOperations))
                                    .build();
                        } catch (OperationManagementException e) {
                            String msg = "Cannot access operation management service.";
                            log.error(msg, e);
                            throw new WindowsOperationException(msg, e);
                        } catch (DeviceManagementException e) {
                            String msg = "Cannot access Device management service.";
                            log.error(msg, e);
                            throw new WindowsOperationException(msg, e);
                        } catch (FeatureManagementException e) {
                            String msg = "Error occurred in getting effective features. ";
                            log.error(msg, e);
                            throw new WindowsConfigurationException(msg, e);
                        } catch (PolicyComplianceException e) {
                            String msg = "Error occurred in setting policy compliance.";
                            log.error(msg, e);
                            throw new WindowsConfigurationException(msg, e);
                        } catch (NotificationManagementException e) {
                            String msg = "Error occurred in while getting notification service.";
                            log.error(msg, e);
                            throw new WindowsOperationException(msg, e);
                        } catch (SyncmlOperationException e) {
                            String msg = "Error occurred in while getting effective feature.";
                            log.error(msg, e);
                            throw new WindowsConfigurationException(msg, e);
                        }
                    }
                } else {
                    String msg = "Failure occurred in Device request message.";
                    log.error(msg);
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
            }
        } catch (SyncmlMessageFormatException e) {
            String msg = "Error occurred in parsing syncml request.";
            log.error(msg, e);
            throw new WindowsOperationException(msg, e);
        }
        return null;
    }

    /**
     * Enroll phone device
     *
     * @param request Device syncml request for the server side.
     * @return enroll state
     * @throws WindowsDeviceEnrolmentException
     * @throws WindowsOperationException
     */
    private boolean enrollDevice(Document request) throws WindowsDeviceEnrolmentException,
            WindowsOperationException {

        String osVersion;
        String imsi = null;
        String imei = null;
        String devID;
        String devMan;
        String devMod;
        String devLang;
        String vender;
        String macAddress;
        String resolution;
        String modVersion;
        boolean status = false;
        String user;
        String deviceName;
        int msgID;
        SyncmlDocument syncmlDocument;

        try {
            syncmlDocument = SyncmlParser.parseSyncmlPayload(request);
            msgID = syncmlDocument.getHeader().getMsgID();
            if (msgID == PluginConstants.SyncML.SYNCML_FIRST_MESSAGE_ID) {
                Replace replace = syncmlDocument.getBody().getReplace();
                List<Item> itemList = replace.getItems();
                devID = itemList.get(PluginConstants.SyncML.DEVICE_ID_POSITION).getData();
                devMan = itemList.get(PluginConstants.SyncML.DEVICE_MAN_POSITION).getData();
                devMod = itemList.get(PluginConstants.SyncML.DEVICE_MODEL_POSITION).getData();
                modVersion = itemList.get(PluginConstants.SyncML.DEVICE_MOD_VER_POSITION).getData();
                devLang = itemList.get(PluginConstants.SyncML.DEVICE_LANG_POSITION).getData();
                user = syncmlDocument.getHeader().getSource().getLocName();

                if (log.isDebugEnabled()) {
                    log.debug(
                            "OS Version:" + modVersion + ", DevID: " + devID + ", DevMan: " + devMan +
                                    ", DevMod: " + devMod + ", DevLang: " + devLang);
                }
                Device generateDevice = generateDevice(DeviceManagementConstants.MobileDeviceTypes.
                        MOBILE_DEVICE_TYPE_WINDOWS, devID, modVersion, imsi, imei, devMan, devMod, user);
                status = WindowsAPIUtils.getDeviceManagementService().enrollDevice(generateDevice);
                WindowsAPIUtils.startTenantFlow(user);
                return status;

            } else if (msgID == PluginConstants.SyncML.SYNCML_SECOND_MESSAGE_ID) {
                List<Item> itemList = syncmlDocument.getBody().getResults().getItem();
                osVersion = itemList.get(PluginConstants.SyncML.OSVERSION_POSITION).getData();
                imsi = itemList.get(PluginConstants.SyncML.IMSI_POSITION).getData();
                imei = itemList.get(PluginConstants.SyncML.IMEI_POSITION).getData();
                vender = itemList.get(PluginConstants.SyncML.VENDER_POSITION).getData();
                devMod = itemList.get(PluginConstants.SyncML.MODEL_POSITION).getData();
                macAddress = itemList.get(PluginConstants.SyncML.MACADDRESS_POSITION).getData();
                resolution = itemList.get(PluginConstants.SyncML.RESOLUTION_POSITION).getData();
                deviceName = itemList.get(PluginConstants.SyncML.DEVICE_NAME_POSITION).getData();
                DeviceIdentifier deviceIdentifier = convertToDeviceIdentifierObject(syncmlDocument.
                        getHeader().getSource().getLocURI());
                Device existingDevice = WindowsAPIUtils.getDeviceManagementService().getDevice(deviceIdentifier);
                if (!existingDevice.getProperties().isEmpty()) {
                    List<Device.Property> existingProperties = new ArrayList<>();

                    Device.Property imeiProperty = new Device.Property();
                    imeiProperty.setName(PluginConstants.SyncML.IMEI);
                    imeiProperty.setValue(imei);
                    existingProperties.add(imeiProperty);

                    Device.Property osVersionProperty = new Device.Property();
                    osVersionProperty.setName(PluginConstants.SyncML.OS_VERSION);
                    osVersionProperty.setValue(osVersion);
                    existingProperties.add(osVersionProperty);

                    Device.Property imsiProperty = new Device.Property();
                    imsiProperty.setName(PluginConstants.SyncML.IMSI);
                    imsiProperty.setValue(imsi);
                    existingProperties.add(imsiProperty);

                    Device.Property venderProperty = new Device.Property();
                    venderProperty.setName(PluginConstants.SyncML.VENDOR);
                    venderProperty.setValue(vender);
                    existingProperties.add(venderProperty);

                    Device.Property macAddressProperty = new Device.Property();
                    macAddressProperty.setName(PluginConstants.SyncML.MAC_ADDRESS);
                    macAddressProperty.setValue(macAddress);
                    existingProperties.add(macAddressProperty);

                    Device.Property resolutionProperty = new Device.Property();
                    resolutionProperty.setName(PluginConstants.SyncML.DEVICE_INFO);
                    resolutionProperty.setValue(resolution);
                    existingProperties.add(resolutionProperty);

                    Device.Property deviceNameProperty = new Device.Property();
                    deviceNameProperty.setName(PluginConstants.SyncML.DEVICE_NAME);
                    deviceNameProperty.setValue(deviceName);
                    existingProperties.add(deviceNameProperty);

                    Device.Property deviceModelProperty = new Device.Property();
                    deviceNameProperty.setName(PluginConstants.SyncML.MODEL);
                    deviceNameProperty.setValue(devMod);
                    existingProperties.add(deviceModelProperty);

                    existingDevice.setProperties(existingProperties);
                    existingDevice.setDeviceIdentifier(syncmlDocument.getHeader().getSource().getLocURI());
                    existingDevice.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
                    status = WindowsAPIUtils.getDeviceManagementService().modifyEnrollment(existingDevice);
                    // call effective policy for the enrolling device.
                    PolicyManagerService policyManagerService = WindowsAPIUtils.getPolicyManagerService();
                    policyManagerService.getEffectivePolicy(deviceIdentifier);
                    return status;
                }
            }
        } catch (DeviceManagementException e) {
            String msg = "Failure occurred in enrolling device.";
            log.error(msg, e);
            throw new WindowsDeviceEnrolmentException(msg, e);
        } catch (SyncmlMessageFormatException e) {
            String msg = "Error occurred in bad format of the syncml payload.";
            log.error(msg, e);
            throw new WindowsOperationException(msg, e);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred in getting effective policy.";
            log.error(msg, e);
            throw new WindowsOperationException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return status;
    }

    /**
     * Generate Device payloads.
     *
     * @param syncmlDocument parsed syncml payload from the syncml engine.
     * @param operations     operations for generate payload.
     * @return String type syncml payload.
     * @throws WindowsOperationException
     * @throws JSONException
     * @throws PolicyManagementException
     * @throws org.wso2.carbon.policy.mgt.common.FeatureManagementException
     */
    public String generateReply(SyncmlDocument syncmlDocument, List<? extends Operation> operations)
            throws SyncmlMessageFormatException, SyncmlOperationException {

        OperationReply operationReply;
        SyncmlGenerator generator;
        SyncmlDocument syncmlResponse;
        if (operations == null) {
            operationReply = new OperationReply(syncmlDocument);
        } else {
            operationReply = new OperationReply(syncmlDocument, operations);
        }
        syncmlResponse = operationReply.generateReply();
        generator = new SyncmlGenerator();
        return generator.generatePayload(syncmlResponse);
    }
}
