/**
 * Returns the dynamic state to be populated by add-user page.
 * 
 * @param context Object that gets updated with the dynamic state of this page to be presented
 * @returns {*} A context object that returns the dynamic state of this page to be presented
 */
function onRequest(context) {
    var userModule = require("/modules/user.js")["userModule"];
    var roleName = request.getParameter("rolename");

    if (roleName) {
        var response = userModule.getRole(roleName);
        if (response["status"] == "success") {
            context["role"] = response["content"];
        }
        var userStores = userModule.getSecondaryUserStores();
        context["userStores"] = userStores;
    }
    //TODO: error scenario
    return context;
}