var analysisResult = {};

cdnBaseUrl = "https://cdnjs.cloudflare.com/ajax/libs/";

require.config({
    shim: {
        'jquery-ui': {
            deps: ['jquery']
        },
        'jquery.flot': {
            deps: ['jquery'],
            exports: '$.plot'
        },
        'jquery.flot.selection': {
            deps: ['jquery.flot']
        }
    },
    paths: {
        'jquery': cdnBaseUrl + 'jquery/2.2.4/jquery.min',
        'jquery-ui': cdnBaseUrl + 'jqueryui/1.12.0/jquery-ui.min',
        'jquery.flot': cdnBaseUrl + 'flot/0.8.3/jquery.flot.min',
        'jquery.flot.selection': cdnBaseUrl + 'flot/0.8.3/jquery.flot.selection.min'
    }
});

require(['jquery', 'jquery-ui'], function ($) {
    $(function () {
        loadCSS(cdnBaseUrl + 'jqueryui/1.12.0/themes/base/jquery-ui.css');
        $(".analysisResultContainer").tabs({
            tabTemplate: "<li><a href=\"#{href}\">#{label}</a></li>"
        });
        loadCSS(dcRelativeDir + '/analysis-result.css?load=script');
    });
});

function drillToDetails(elementId) {
    var elem = $('#' + elementId);

    var wWidth = $(window).width();
    var dWidth = wWidth * 0.85;
    var wHeight = $(window).height();
    var dHeight = wHeight * 0.8;
    elem.dialog({
        modal : true,
        width : dWidth,
        height : dHeight
    });
}

function loadCSS(url) {
    var link = document.createElement("link");
    link.type = "text/css";
    link.rel = "stylesheet";
    link.href = url;
    document.getElementsByTagName("head")[0].appendChild(link);
}
