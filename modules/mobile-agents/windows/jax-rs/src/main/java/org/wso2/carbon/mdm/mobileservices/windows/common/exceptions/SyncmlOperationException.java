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

package org.wso2.carbon.mdm.mobileservices.windows.common.exceptions;

/**
 * Exception class for syncml Operation and policy related errors.
 */
public class SyncmlOperationException extends Exception {

    private String errorMessage;

    public SyncmlOperationException(String message) {
        super(message);
        setErrorMessage(message);
    }

    public SyncmlOperationException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public SyncmlOperationException(String message, Exception nestedEx) {
        super(message, nestedEx);
        setErrorMessage(message);
    }

    public SyncmlOperationException(Throwable cause) {
        super(cause);
    }

    public SyncmlOperationException() {
        super();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
