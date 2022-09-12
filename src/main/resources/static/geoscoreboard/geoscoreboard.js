var GeoScoreboardOptions = {
    title: "GeoScoreboard",
    apiKey: "",
    _server: 'http://localhost:5000/'
};

var GeoScoreboard = class  {
    constructor() {}

    init() {
        this._addStyleSheet();
    }

    _addStyleSheet() {
        var head = document.getElementsByTagName("head")[0];
        var link = document.createElement("link");
        link.rel = "stylesheet";
        link.href = GeoScoreboardOptions._server + "geoscoreboard/geoscoreboard.css";
        head.appendChild(link);
    }

    publish(scoreboard, score, longitude, latitude) {
        if (typeof longitude !== "undefined" && typeof latitude !== "undefined") {
            this._handlePosition(scoreboard, longitude, latitude, score);
            return;
        }
        var that = this;
        navigator.geolocation.getCurrentPosition(function (event) {
                    that._handlePosition(scoreboard, event.coords.longitude,
                        event.coords.latitude, score);
                },
                function (event) {
                    that._handlePosition(scoreboard, -1, -1, score);
                });
    }

    _handlePosition(scoreboard, longitude, latitude, score) {
        var xhr = new XMLHttpRequest();
        xhr.open("POST", GeoScoreboardOptions._server + "publish");
        var apiKey = GeoScoreboardOptions.apiKey;
        xhr.send(`scoreboard=${scoreboard}&longitude=${longitude}&latitude=${latitude}` +
            `&score=${score}&api_key=${apiKey}`);
    }

    show(scoreboard, longitude, latitude) {
        if (typeof longitude !== "undefined" && typeof latitude !== "undefined") {
            this._show(scoreboard, longitude, latitude);
            return;
        }

        var that = this;
        navigator.geolocation.getCurrentPosition(function (event) {
                that._show(scoreboard, event.coords.longitude,
                    event.coords.latitude);
            },
            function (event) {
                that._show(scoreboard, -1, -1);
            });
    }
    _show(scoreboardName, longitude, latitude) {
        this.hide();

        var body = document.getElementsByTagName("body")[0];
        var scoreboard = document.createElement("div");
        scoreboard.id = "geo_scoreboard_main";

        var exit = document.createElement("img");
        exit.id = "geo_scoreboard_exit";
        exit.src = GeoScoreboardOptions._server + "geoscoreboard/image/exit.png";
        var that = this;
        exit.addEventListener("click", function () {
            that.hide();
        });
        scoreboard.appendChild(exit);

        var title = document.createElement("div");
        title.id = "geo_scoreboard_title";
        title.innerText = GeoScoreboardOptions.title;
        scoreboard.appendChild(title);

        var inner = document.createElement("div");
        inner.id = "geo_scoreboard_inner"
        scoreboard.appendChild(inner);

        var xhr = new XMLHttpRequest();
        xhr.addEventListener("readystatechange", function (event) {
            if (this.readyState === 4 && this.status === 200) {
                var json = JSON.parse(this.responseText);
                for (var index = 0; index < json.length; index++) {
                    var score = document.createElement("div");
                    score.className = "geo_scoreboard_score";
                    score.innerText = (index + 1) + ". " + json[index]["score"];
                    inner.appendChild(score);
                }
            }
        });
        var apiKey = GeoScoreboardOptions.apiKey;
        var queryParama = `scoreboard=${scoreboardName}&longitude=${longitude}&latitude=${latitude}` +
            `&api_key=${apiKey}`;
        xhr.open("GET", GeoScoreboardOptions._server + "get?" + queryParama);
        xhr.send();

        body.appendChild(scoreboard);
    }

    hide() {
        var body = document.getElementsByTagName("body")[0];
        var scoreboard = document.getElementById("geo_scoreboard_main");
        if (typeof scoreboard !== "undefined" && scoreboard !== null)
            body.removeChild(scoreboard);
    }
};

var geoScoreboard = new GeoScoreboard()
window.addEventListener("load", function () {
    geoScoreboard.init();
});