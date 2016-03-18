/**
 * Returns a context with the user object to be populated by the edit-user page.
 *
 * @param context Object that gets updated with the dynamic state of this page to be presented
 * @returns {*} A context object that returns the dynamic state of this page to be presented
 */
function onRequest(context) {
    var userModule = require("/modules/user.js")["userModule"];

    var userName = request.getParameter("username");

    if (userName) {
        var userStore = "PRIMARY";
        if(userName.indexOf("/") > -1) {
            userStore = userName.substr(0, userName.indexOf('/'));
        }
        var response = userModule.getUser(userName);

        if (response["status"] == "success") {
            context["editUser"] = response["content"];
        }

        response = userModule.getRolesByUsername(userName);
        if (response["status"] == "success") {
            context["usersRoles"] = response["content"];
        }
        response = userModule.getRolesByUserStore(userStore);
        if (response["status"] == "success") {
            context["userRoles"] = response["content"];
        }
    }
    return context;
}