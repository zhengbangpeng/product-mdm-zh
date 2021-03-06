function onRequest(context) {
    // var log = new Log("policy-listing.js");
    var policyModule = require("/modules/policy.js")["policyModule"];
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
            context["policyListingStatusMsg"] = "请添加更多策略以排序其优先级";
            context["saveNewPrioritiesButtonEnabled"] = false;
            context["noPolicy"] = false;
        } else {
            context["policyListingStatusMsg"] = "可移动策略重新排序其优先级";
            context["saveNewPrioritiesButtonEnabled"] = true;
            context["noPolicy"] = false;
        }
    } else {
        // here, response["status"] == "error"
        context["policyListToView"] = [];
        context["policyListingStatusMsg"] = response["content"];
        context["saveNewPrioritiesButtonEnabled"] = false;
    }
    return context;
}
