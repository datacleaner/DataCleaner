package org.eobjects.datacleaner.visualization

import org.eobjects.analyzer.result.html.HeadElement
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.html.FlotChartLocator

/**
 * Head element which defines the necesary script and style elements for scatter charts
 */
object ScatterAnalyzerResuableChartHeadElement extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val flotBaseLocation = FlotChartLocator.getFlotBaseUrl
    val flotSelectionUrl = FlotChartLocator.getFlotSelectionUrl
    
    return """<script type="text/javascript">
//<![CDATA[
function draw_scatter_chart(chartElement, chartData, retries) {
   
    wait_for_script_load('jQuery', function() {
        importJS('""" + flotBaseLocation + """', 'jQuery.plot', function() {
            importJS('""" + flotSelectionUrl + """', 'jQuery.plot.plugins[0]', function() {
                var elem = jQuery("#" + chartElement);
                var showLegend = chartData.length > 1
                
                try {
                    var options = {
                        series: {
                            points: {
                                radius: 2,
                                show: true,
                                fill: true,
                                fillColor: "#000000"
                            }
                        },
                        legend: { show: showLegend },
                        selection: {
                            mode: "xy"
                        }
                    }
            
                    var plot = jQuery.plot(elem, chartData, options);
                    draw_scatter_chart_buttons(elem, plot, chartData, options, false);
                    
                } catch (err) {
                    // error can sometimes occur due to load time issues
                    if (retries > 0) {
                        retries = retries-1;
                        draw_scatter_analyzer_chart(chartElement, chartData, retries);
                    }
                }
            });
        });
    });
}
                
function draw_scatter_chart_buttons(elem, plot, chartData, options, zoomed) {
    var zoomOutButton = jQuery("<div class='scatterChartButton' style='top:10px;'>Zoom out</div>")
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
        draw_scatter_chart_buttons(elem, plot, chartData, options, true);
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
            draw_scatter_chart_buttons(elem, plot, chartData, options, false);
        });
}
//]]>
</script>
<style type="text/css">
.scatterChart {
    width: 700px;
    height: 550px;
}
.scatterChartButton {
    cursor: pointer;
    background-color:#333;
    color:white;
    padding:3px;
    font-size:11px;
    position:absolute;
    right: 10px;
    border-radius: 3px;
}
</style>"""
  }
}