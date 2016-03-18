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

$(document).ready(function(){
    if (document.getElementById('device-location')){
        loadMap();
    }
});

function loadMap() {
    var map;
    function initialize() {
        var mapOptions = {
            zoom: 18
        };
        var lat = $("#device-location").data("lat");
        var long = $("#device-location").data("long");

        if(lat != null && lat != undefined && lat != "" && long != null && long != undefined && long != "") {
            $("#map-error").hide();
            $("#device-location").show();
            map = new google.maps.Map(document.getElementById('device-location'),
                mapOptions);

            var pos = new google.maps.LatLng(lat,
                long);
            var marker = new google.maps.Marker({
                position: pos,
                map: map
            });

            map.setCenter(pos);
        }else{
            $("#device-location").hide();
            $("#map-error").show();
        }

    }
    google.maps.event.addDomListener(window, 'load', initialize);
}