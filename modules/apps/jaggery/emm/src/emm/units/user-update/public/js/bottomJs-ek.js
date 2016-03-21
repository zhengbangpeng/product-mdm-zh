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

/**
 * Checks if an email address has the valid format or not.
 *
 * @param email Email address
 * @returns {boolean} true if email has the valid format, otherwise false.
 */
function emailIsValid(email) {
    var regExp = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
    return regExp.test(email);
}

$(document).ready(function () {
    $("select.select2[multiple=multiple]").select2({
        tags : false
    });
    var roleList = $("#roles").attr("selectedVals").trim().replace(/ /g,"");
    roleList = roleList.replace(/(\r\n|\n|\r)/gm,"");
    var roleArr = roleList.split(",");
    $("#roles").val(roleArr).trigger("change");

    /**
     * Following click function would execute
     * when a user clicks on "Add User" button
     * on Add User page in WSO2 MDM Console.
     */
    $("button#add-user-btn").click(function() {
        var username = $("input#username").val();
        var firstname = $("input#firstname").val();
        var lastname = $("input#lastname").val();
        var emailAddress = $("input#emailAddress").val();
        var roles = $("select#roles").val();
        var errorMsgWrapper = "#user-create-error-msg";
        var errorMsg = "#user-create-error-msg span";
        if (!username) {
            $(errorMsg).text("用户名不能为空");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!firstname) {
            $(errorMsg).text("名不能为空");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(/^[^~?!#$:;%^*`+={}\[\]\\()|<>,'"]{1,30}$/, firstname)) {
            $(errorMsg).text("名无效");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!lastname) {
            $(errorMsg).text("姓不能为空");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(/^[^~?!#$:;%^*`+={}\[\]\\()|<>.,'"]{1,30}$/, lastname)) {
            $(errorMsg).text("姓无效");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!emailAddress) {
            $(errorMsg).text("邮件不能为空");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!emailIsValid(emailAddress)) {
            $(errorMsg).text("邮件无效");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addUserFormData = {};

            addUserFormData.username = username;
            addUserFormData.firstname = firstname;
            addUserFormData.lastname = lastname;
            addUserFormData.emailAddress = emailAddress;

            if (roles == null){
                roles = [];
            }
            addUserFormData.roles = roles;

            var addUserAPI = "/mdm-admin/users?username=" + username;

            invokerUtil.put(
                addUserAPI,
                addUserFormData,
                function (data) {
                    data = JSON.parse(data);
                    if (data["statusCode"] == 201) {
                        // Clearing user input fields.
                        $("input#username").val("");
                        $("input#firstname").val("");
                        $("input#lastname").val("");
                        $("input#email").val("");
                        $("select#roles").select2("val", "");
                        // Refreshing with success message
                        $("#user-create-form").addClass("hidden");
                        $("#user-created-msg").removeClass("hidden");
                    }
                }, function (data) {
                    if (data["status"] == 409) {
                        $(errorMsg).text("用户 : " + username + " 不存在");
                    } else if (data["status"] == 500) {
                        $(errorMsg).text("后台发生错误，请稍后重试。");
                    } else {
                        $(errorMsg).text(data.errorMessage);
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });
});