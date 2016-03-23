function onRequest(context) {
    // var log = new Log("policy-listing.js");
    var policyModule = require("/modules/policy.js")["policyModule"];
    var userModule = require("/modules/user.js")["userModule"];
    var response = policyModule.getAllPolicies();
    if (response["status"] == "success") {
        var policyListToView = response["content"];
        context["policyListToView"] = policyListToView;
        var policyCount = policyListToView.length;
        if (policyCount == 0) {
            context["policyListingStatusMsg"] = "无策略可显示";
            context["saveNewPrioritiesButtonEnabled"] = false;
            context["noPolicy"] = true;
        } else if (policyCount == 1) {
            context["saveNewPrioritiesButtonEnabled"] = false;
            context["noPolicy"] = false;
            context["isUpdated"] = response["updated"] ;
        } else {
            context["saveNewPrioritiesButtonEnabled"] = true;
            context["noPolicy"] = false;
            context["isUpdated"] = response["updated"] ;
        }
    } else {
        // here, response["status"] == "error"
        context["policyListToView"] = [];
        context["policyListingStatusMsg"] = "后台发生错误，请稍后重试。";
        context["saveNewPrioritiesButtonEnabled"] = false;
        context["noPolicy"] = true;
    }

    if(userModule.isAuthorized("/permission/admin/device-mgt/policies/delete")){
        context["removePermitted"] = true;
    }

    return context;
}
