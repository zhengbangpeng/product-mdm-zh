var updateStats = function (serviceURL, id) {
    invokerUtil.get(
        serviceURL,
        function (data) {
            if (!data) {
                updateStats(serviceURL, id);
            } else {
                data = JSON.parse(data);
                $(id).html(data);
            }
        }, function (message) {
            console.log(message.content);
        }
    );
};
//TODO : Refactor the users/count API to remove tenant-domain parameter
$(document).ready(function () {
    updateStats("/mdm-admin/devices/count", "#device-count");
    updateStats("/mdm-admin/policies/count", "#policy-count");
    updateStats("/mdm-admin/users/count", "#user-count");
});

function toggleEnrollment() {
    $(".modalpopup-content").html($("#qr-code-modal").html());
    generateQRCode(".modalpopup-content .qr-code");
    showPopup();
}