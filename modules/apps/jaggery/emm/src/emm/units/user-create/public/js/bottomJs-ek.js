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
$( "#userStore" )
    .change(function () {
        var str = "";
        $( "select option:selected" ).each(function() {
            str += $( this ).text() + " ";
        });
        var addUserAPI = "/mdm-admin/roles/"+ str;

        invokerUtil.get(
            addUserAPI,
            function (data) {
                data = JSON.parse(data);
                if (data.errorMessage) {
                    $(errorMsg).text("选择的用户库发生错误 : " + data.errorMessage);
                    $(errorMsgWrapper).removeClass("hidden");
                } else if (data["statusCode"] == 200) {
                    $("#roles").empty();
                    for(i=0;i<data.responseContent.length;i++){
                        var newOption = $('<option value="'+data.responseContent[i]+'">'+data.responseContent[i]+'</option>');
                        $('#roles').append(newOption);
                    }
                }
            }
        );
    }).change();

$(document).ready(function () {
    $("select.select2[multiple=multiple]").select2({
        tags: false
    });

    /**
     * Following click function would execute
     * when a user clicks on "Add User" button
     * on Add User page in WSO2 MDM Console.
     */
    $("button#add-user-btn").click(function () {
        var charLimit = parseInt($("input#username").attr("limit"));
        var domain = $("#userStore").val();
        var username = $("input#username").val().trim();
        var firstname = $("input#firstname").val();
        var lastname = $("input#lastname").val();
        var emailAddress = $("input#emailAddress").val();
        var roles = $("select#roles").val();

        var errorMsgWrapper = "#user-create-error-msg";
        var errorMsg = "#user-create-error-msg span";
        if (!username) {
            $(errorMsg).text("用户名不能为空");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (username.length > charLimit || username.length < 3) {
            $(errorMsg).text("用户名必须为 3 - " + charLimit + "");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(/^[a-zA-Z0-9\-\\\/\@\.\_]+$/, username)) {
            $(errorMsg).text("用户名无效");
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
            $(errorMsg).text("邮件地址不能为空");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!emailIsValid(emailAddress)) {
            $(errorMsg).text("邮件地址无效");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addUserFormData = {};

            addUserFormData.username = domain + "/" + username;
            addUserFormData.firstname = firstname;
            addUserFormData.lastname = lastname;
            addUserFormData.emailAddress = emailAddress;
            addUserFormData.roles = roles;

            var addUserAPI = "/mdm-admin/users";

            invokerUtil.post(
                addUserAPI,
                addUserFormData,
                function (data) {
                    data = JSON.parse(data);
                    if (data.errorMessage) {
                        $(errorMsg).text("选择的用户库发生错误 : " + data.errorMessage);
                        $(errorMsgWrapper).removeClass("hidden");
                    } else if (data["statusCode"] == 201) {
                        // Clearing user input fields.
                        $("input#username").val("");
                        $("input#firstname").val("");
                        $("input#lastname").val("");
                        $("input#email").val("");
                        $("select#roles").select2("val", "");
                        // Refreshing with success message
                        $("#user-create-form").addClass("hidden");
                        $("#user-created-msg").removeClass("hidden");
                        generateQRCode("#user-created-msg .qr-code");
                    } else if (data["status"] == 409) {
                        $(errorMsg).text(data["messageFromServer"]);
                        $(errorMsgWrapper).removeClass("hidden");
                    } else if (data["status"] == 500) {
                        $(errorMsg).text("后台发生错误，请稍后再试。");
                        $(errorMsgWrapper).removeClass("hidden");
                    }
                }, function (data) {
                    if (data["status"] == 409) {
                        $(errorMsg).text("User : " + username + " already exists. Pick another username.");
                    } else if (data["status"] == 500) {
                        $(errorMsg).text("后台发生错误，请稍后再试。");
                    } else {
                        $(errorMsg).text(data.errorMessage);
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });
});