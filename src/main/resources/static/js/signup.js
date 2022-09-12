var GeoScoreboardSignup = class  {
    constructor() {
    }

    init() {
        var form = document.getElementById("signup-form");
        form.style.display = "none";

        var message = document.getElementById("signup-message");

        switch (geoScoreboardStatus) {
            case "invalid_data":
                message.innerText = "The form was passed with invalid data.";
                form.style.display = "block";
                break;
            case "email_exists":
                message.innerText = "An account with this email already exists.";
                form.style.display = "block";
                break;
            case "success":
                message.innerText = "Account created. Redirecting.";
                window.location = "/";
                break;
            case "":
                form.style.display = "block";
                break;
        }
    }
};

var geoScoreboardSignup = new GeoScoreboardSignup();
window.addEventListener("load", geoScoreboardSignup.init);