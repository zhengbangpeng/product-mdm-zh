/**
 * Checks if provided input is valid against RegEx input.
 *
 * @param regExp Regular expression
 * @param inputString Input string to check
 * @returns {boolean} Returns true if input matches RegEx
 */
function inputIsValid(regExp, inputString) {
    return regExp.test(inputString);
}

$(document).ready(function () {
    var modalPopup = ".wr-modalpopup";
    var modalPopupContainer = modalPopup + " .modalpopup-container";
    var modalPopupContent = modalPopup + " .modalpopup-content";

    $("#change-password").click(function () {

        $(modalPopupContent).html($('#change-password-window').html());
        showPopup();

        $("a#change-password-yes-link").click(function () {
            var oldPassword = $("#old-password").val();
            var newPassword = $("#new-password").val();
            var confirmedPassword = $("#confirmed-password").val();
            var user = $("#user").val();

            var errorMsgWrapper = "#notification-error-msg";
            var errorMsg = "#notification-error-msg span";
            if (!oldPassword) {
                $(errorMsg).text("旧密码为必填选项，不能为空。");
                $(errorMsgWrapper).removeClass("hidden");
            } else if (!newPassword) {
                $(errorMsg).text("新密码为必填选项，不能为空。");
                $(errorMsgWrapper).removeClass("hidden");
            } else if (!confirmedPassword) {
                $(errorMsg).text("确认新密码为必填选项。");
                $(errorMsgWrapper).removeClass("hidden");
            } else if (confirmedPassword != newPassword) {
                $(errorMsg).text("两次新密码不一致。");
                $(errorMsgWrapper).removeClass("hidden");
            } else if (!inputIsValid(/^[\S]{5,30}$/, confirmedPassword)) {
                $(errorMsg).text("密码长度至少为5，且不能包含空格。");
                $(errorMsgWrapper).removeClass("hidden");
            } else {
                var changePasswordFormData = {};
                changePasswordFormData.username = user;
                changePasswordFormData.oldPassword = window.btoa(unescape(encodeURIComponent(oldPassword)));
                changePasswordFormData.newPassword = window.btoa(unescape(encodeURIComponent(confirmedPassword)));

                var changePasswordAPI = "/mdm-admin/users/reset-password";

                invokerUtil.post(
                    changePasswordAPI,
                    changePasswordFormData,
                    function (data) {
                        data = JSON.parse(data);
                        if (data.statusCode == 201) {
                            $(modalPopupContent).html($('#change-password-success-content').html());
                            $("a#change-password-success-link").click(function () {
                                hidePopup();
                            });
                        } else if (data.statusCode == 400) {
                            $(errorMsg).text("旧密码输入错误。");
                            $(errorMsgWrapper).removeClass("hidden");
                        }
                    }, function (data) {
                        if (data.status == 400) {
                            $(errorMsg).text("旧密码输入错误。");
                            $(errorMsgWrapper).removeClass("hidden");
                        } else {
                            $(errorMsg).text("密码修改失败，请稍后再试。");
                            $(errorMsgWrapper).removeClass("hidden");
                        }
                    }
                );
            }

        });

        $("a#change-password-cancel-link").click(function () {
            hidePopup();
        });
    });
});