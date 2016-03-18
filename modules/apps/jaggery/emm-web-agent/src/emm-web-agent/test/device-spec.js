/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

describe('Device Module', function () {
    var log = new Log();
    var mobileDB;
    var cdmDB;
    var deviceModule = require("/modules/device.js").deviceModule;

    function tearUp() {
        mobileDB = new Database("MobileDM_DS");
        cdmDB = new Database("DM_DS");
        cdmDB.query("insert into dm_device(description, name, date_of_enrollment, date_of_last_update, " +
                            "ownership,status, device_type_id, device_identification, owner, tenant_id ) " +
                                "values ('Galaxy Tab','Admin Samsung', 1425467382, 1425467382, 'BYOD', 'ACTIVE'," +
                                    " 1,'4892813d-0b18-4a02-b7b1-61775257488e', 'admin@wso2.com', '-1234');");
        cdmDB.query("insert into dm_device(description, name, date_of_enrollment, date_of_last_update, " +
                            "ownership,status, device_type_id, device_identification, owner, tenant_id ) " +
                                "values ('Galaxy Tab','Admin Samsung', 1425467382, 1425467382, 'BYOD', 'ACTIVE'," +
                                    " 1,'4892813d-0b18-4a02-b7b1-61775257488F', 'mdm@wso2.com', '-1234');");

        mobileDB.query("insert into mbl_device (mobile_device_id, push_token, imei ,imsi, os_version, " +
                        "device_model , vendor ,latitude ,longitude , challenge ,token, unlock_token ,serial ) " +
                            "values ('4892813d-0b18-4a02-b7b1-61775257488e', 'sdfsdf', 'cxv', 'vbcb', '4.1', " +
                                "'Galaxy Tab', 'Samsung',  '234234234', '4345345234234', 'dfjsdlfk', 'wuweir234', " +
                                    "'ksdfjlskfjwer', '234234');");
        mobileDB.query("insert into mbl_device (mobile_device_id, push_token, imei ,imsi, os_version, " +
                        "device_model , vendor ,latitude ,longitude , challenge ,token, unlock_token ,serial ) " +
                            "values ('4892813d-0b18-4a02-b7b1-61775257488F', 'sdfsdf', 'cxv', 'vbcb', '4.1', " +
                                "'Galaxy Tab', 'Samsung',  '234234234', '4345345234234', 'dfjsdlfk', 'wuweir234', " +
                                    "'ksdfjlskfjwer', '234234');");
    }

    function tearDown() {
        deleteData();
        mobileDB.close();
        cdmDB.close();
    }

    function deleteData(){
        cdmDB.query("delete from dm_device where device_identification='4892813d-0b18-4a02-b7b1-61775257488e'");
        cdmDB.query("delete from dm_device where device_identification='4892813d-0b18-4a02-b7b1-61775257488F'");
        mobileDB.query("delete from mbl_device where mobile_device_id='4892813d-0b18-4a02-b7b1-61775257488e'");
        mobileDB.query("delete from mbl_device where mobile_device_id='4892813d-0b18-4a02-b7b1-61775257488F'");
    }
    /*
    Not testable since the functionality moved to front end

    it('List all Devices - Device Module', function () {
        try {
            tearUp();
            var results = deviceModule.listDevices();
            expect(results.length).not.toBe(0);
        } catch (e) {
            log.error(e);
            throw e;
        } finally {
            tearDown();
        }
    });
    it('List Devices for User - Device Module', function () {
        try {
            tearUp();
            var results = deviceModule.listDevicesForUser("mdm@wso2.com");
            expect(results.length).toBe(1);
        } catch (e) {
            log.error(e);
            throw e;
        } finally {
            tearDown();
        }
    });
    it('Perform operation on the device', function(){
        try {
            tearUp();
            var devices = [{"id": "4892813d-0b18-4a02-b7b1-61775257488e", "type": "android"}];
            var operation = {"featureName": "DEVICE_LOCK", "type": "COMMAND", "properties": {"enabled": true}}
            var results = deviceModule.performOperation(devices, operation);
            expect(results.length).toBe(1);
        } catch (e) {
            log.error(e);
            throw e;
        } finally {
            tearDown();
        }
    });
    */
});