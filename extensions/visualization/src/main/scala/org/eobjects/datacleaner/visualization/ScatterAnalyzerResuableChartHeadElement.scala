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
    
    return """<script type="text/javascript">
//<![CDATA[
function draw_scatter_chart(chartElement, chartData, retries) {
   
    wait_for_script_load('jQuery', function() {
        importJS('""" + flotBaseLocation + """', 'jQuery.plot', function() {
            var elem = document.getElementById(chartElement);
            
            try {
                jQuery.plot(elem, chartData, {
                    series: {
                        points: {
                            radius: 2,
                            show: true,
                            fill: true,
                            fillColor: "#000000"
                        }
                    }
                });
            } catch (err) {
                // error can sometimes occur due to load time issues
                if (retries > 0) {
                    retries = retries-1;
                    draw_scatter_analyzer_chart(chartElement, chartData, retries);
                }
            }
        });
    });
}
//]]>
</script>
<style type="text/css">
.scatterChart {
    width: 700px;
    height: 550px;
}
</style>"""
  }
}