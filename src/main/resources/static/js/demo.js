var GeoScoreboardDemo = class  {
    constructor() {}

    init() {
        this.addEvents();
    }

    addEvents() {
        var button = document.getElementById("demo-submit");
        var that = this;
        button.addEventListener("click", function () {
            var text = document.getElementById("demo-text");
            geoScoreboard.publish("DEMO", text.value);
            geoScoreboard.show("DEMO");
        });
    }
};

var geoScoreboardDemo = new GeoScoreboardDemo();
window.addEventListener("load", function () {
    geoScoreboardDemo.init();
});