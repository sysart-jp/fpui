if (process.env.NODE_ENV === "production") {
    const opt = require("./examplemultipage-opt.js");
    opt.main();
    module.exports = opt;
} else {
    var exports = window;
    exports.require = require("./examplemultipage-fastopt-entrypoint.js").require;
    window.global = window;

    const fastOpt = require("./examplemultipage-fastopt.js");
    fastOpt.main()
    module.exports = fastOpt;

    if (module.hot) {
        module.hot.accept();
    }
}
