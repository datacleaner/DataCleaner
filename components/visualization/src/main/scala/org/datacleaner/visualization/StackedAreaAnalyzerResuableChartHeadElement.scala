package org.datacleaner.visualization

import org.datacleaner.result.html.{FlotChartLocator, HeadElement, HtmlRenderingContext}

object StackedAreaAnalyzerResuableChartHeadElement extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val flotBaseLocation = FlotChartLocator.getFlotBaseUrl
    val flotSelectionUrl = FlotChartLocator.getFlotSelectionUrl

    return """<script type="text/javascript">
//<![CDATA[
function draw_stacked_area_analyzer_chart(chartElement, chartData, retries) {
    require(['jquery', 'jquery.flot', 'jquery.flot.selection'], function ($) {
                var elem = jQuery("#" + chartElement);
                var options = {
                        series: {
                            lines: {
                                active: true,
                                show: true,
                                lineWidth: 0,
                                fill: 1
                            }
                        },
                        points: {show:false},
                        legend: {show: true,position:"se"},
                        selection: {
                            mode: "xy"
                        }
                    };

                try {
                    var plot = jQuery.plot(elem, chartData, options);
                    draw_stacked_area_analyzer_chart_buttons(elem, plot, chartData, options, false);
                } catch (err) {
                    // error can sometimes occur due to load time issues
                    if (retries > 0) {
                        retries = retries-1;
                        draw_stacked_area_analyzer_chart_buttons(chartElement, chartData, retries);
                    }
                }
            });
}

function draw_stacked_area_analyzer_chart_buttons(elem, plot, chartData, options, zoomed) {
    var zoomOutButton = jQuery("<div class='stackedAreaChartButton' style='top:10px;'>Zoom out</div>")
    if (!zoomed) {
        zoomOutButton.hide();
    }

    elem.bind("plotselected", function (event, ranges) {
        plot = $.plot(elem, chartData, jQuery.extend(true, {}, options, {
            xaxis: {
                min: ranges.xaxis.from,
                max: ranges.xaxis.to
            },
            yaxis: {
                min: ranges.yaxis.from,
                max: ranges.yaxis.to
            }
        }));
        draw_stacked_area_analyzer_chart_buttons(elem, plot, chartData, options, true);
    });

    zoomOutButton.appendTo(elem)
        .click(function (event) {
            event.preventDefault();
            plot = $.plot(elem, chartData, jQuery.extend(true, {}, options, {
                xaxis: {
                    min: null,
                    max: null
                },
                yaxis: {
                    min: null,
                    max: null
                }
            }));
            draw_stacked_area_analyzer_chart_buttons(elem, plot, chartData, options, false);
        });
}
//]]>
</script>
<style type="text/css">
.stackedAreaAnalyzerChart {
    height: 550px;
    width: 700px;
}        
.stackedAreaChartButton {
    cursor: pointer;
    background-color:#333;
    color:white;
    padding:3px;
    font-size:11px;
    position:absolute;
    right:10px;
    border-radius: 3px;
}
</style>"""
  }
}
