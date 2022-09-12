var GeoScoreboardLogin = class  {
    constructor() {
    }

    init() {
        var form = document.getElementById("login-form");
        form.style.display = "none";

        var message = document.getElementById("login-message");

        switch (geoScoreboardStatus) {
            case "true":
                message.innerText = "Logged in. Redirecting.";
                window.location = "/";
                break;
            case "false":
                message.innerText = "No account with the supplied email and password exists.";
                form.style.display = "block";
                break;
            case "":
                form.style.display = "block";
                break;
        }
    }
};

var geoScoreboardLogin = new GeoScoreboardLogin();
window.addEventListener("load", geoScoreboardLogin.init);