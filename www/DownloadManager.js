var exec = require('cordova/exec');

exports.download = function(arg0, arg1, arg2, arg3, success, error) {
    exec(success, error, "DownloadManager", "download", [arg0, arg1, arg2, arg3]);
};

