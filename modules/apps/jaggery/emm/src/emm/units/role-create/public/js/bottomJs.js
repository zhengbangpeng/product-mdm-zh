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
function formatRepo (user) {
    if (user.loading) {
        return user.text
    }
    if (!user.username){
        return;
    }
    var markup = '<div class="clearfix">' +
        '<div clas="col-sm-8">' +
        '<div class="clearfix">' +
        '<div class="col-sm-3">' + user.username + '</div>';
    if (user.firstname) {
        markup +=  '<div class="col-sm-3"><i class="fa fa-code-fork"></i> ' + user.firstname + '</div>';
    }
    if (user.emailAddress) {
        markup += '<div class="col-sm-2"><i class="fa fa-star"></i> ' + user.emailAddress + '</div></div>';
    }
    markup += '</div></div>';
    return markup;
}

function formatRepoSelection (user) {
    return user.username || user.text;
}

$(document).ready(function () {

    $("#users").select2({
        multiple:true,
        tags: false,
        ajax: {
            url: window.location.origin + "/emm/api/invoker/execute/",
            method: "POST",
            dataType: 'json',
            delay: 250,
            id: function (user) {
                return user.username;
            },
            data: function (params) {
                var postData = {};
                postData.actionMethod = "GET";
                postData.actionUrl = "/mdm-admin/users/view-users?username=" + params.term;
                postData.actionPayload = null;
                return JSON.stringify(postData);
            },
            processResults: function (data, page) {
                var newData = [];
                $.each(data.responseContent, function (index, value) {
                    value.id = value.username;
                    newData.push(value);
                });
                return {
                    results: newData
                };
            },
            cache: true
        },
        escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
        minimumInputLength: 1,
        templateResult: formatRepo, // omitted for brevity, see the source of this page
        templateSelection: formatRepoSelection // omitted for brevity, see the source of this page
    });

    /**
     * Following click function would execute
     * when a user clicks on "Add Role" button
     * on Add Role page in WSO2 MDM Console.
     */
    $("button#add-role-btn").click(function() {
        var roleName = $("input#rolename").val();
        var domain = $("#domain").val();
        var users = $("#users").val();

        var errorMsgWrapper = "#role-create-error-msg";
        var errorMsg = "#role-create-error-msg span";
        if (!roleName) {
            $(errorMsg).text("Role name is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(/^[^~?!#$:;%^*`+={}\[\]\\()|<>,'"" "]{3,30}$/, roleName)) {
            $(errorMsg).text("Provided role name is invalid.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!domain) {
            $(errorMsg).text("Domain is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!inputIsValid(/^[^~?!#$:;%^*`+={}\[\]\\()|<>,'"0-9]{1,30}$/, domain)) {
            $(errorMsg).text("Provided domain is invalid.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addRoleFormData = {};

            addRoleFormData.roleName = roleName;

            if (domain != "PRIMARY"){
                addRoleFormData.roleName = domain + "/" + roleName;
            }
            if (users == null){
                users = [];
            }
            addRoleFormData.users = users;

            var addRoleAPI = "/mdm-admin/roles";

            invokerUtil.post(
                addRoleAPI,
                addRoleFormData,
                function (data) {
                    data = JSON.parse(data);
                    if (data.errorMessage) {
                        $(errorMsg).text("Selected user store prompted an error : " + data.errorMessage);
                        $(errorMsgWrapper).removeClass("hidden");
                    } else {
                        // Clearing user input fields.
                        $("input#rolename").val("");
                        $("#domain").val("");
                        // Refreshing with success message
                        $("#role-create-form").addClass("hidden");
                        $("#role-created-msg").removeClass("hidden");
                    }
                }, function (data) {
                    if (JSON.parse(data.responseText).errorMessage.indexOf("RoleExisting") > -1) {
                        $(errorMsg).text("Role name : " + roleName + " already exists. Pick another role name.");
                    } else {
                        $(errorMsg).text(JSON.parse(data.responseText).errorMessage);
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });
});