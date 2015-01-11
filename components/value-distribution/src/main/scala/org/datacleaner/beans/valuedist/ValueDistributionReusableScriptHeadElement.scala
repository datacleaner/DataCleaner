package org.datacleaner.beans.valuedist
import scala.collection.JavaConversions.collectionAsScalaIterable
import org.datacleaner.result.html.HeadElement
import org.datacleaner.result.html.HtmlRenderingContext
import org.datacleaner.result.ValueCountingAnalyzerResult
import org.datacleaner.util.LabelUtils
import org.datacleaner.result.html.FlotChartLocator

/**
 * Defines reusable script parts for value distribution results
 */
object ValueDistributionReusableScriptHeadElement extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val flotBaseLocation = FlotChartLocator.getFlotBaseUrl

    return """<script type="text/javascript">
//<![CDATA[
function draw_value_distribution_bar(chartElement, chartData, retries) {
   
    wait_for_script_load('jQuery', function() {
        importJS('""" + flotBaseLocation + """', 'jQuery.plot', function() {
            var elem = document.getElementById(chartElement);
            
            try {
                jQuery.plot(elem, chartData, {
                    series: {
                        bars: {
                            align: "center",
                            horizontal: true,
                            show: 0.5,
                            fill: 1,
                            lineWidth: 1,
                            barWidth: 0.9
                        }
                    },
                    grid: { 
                        borderWidth: 1
                    },
                    xaxis: {
                        ticks: null
                    },
                    yaxis: {
                        show: false
                    },
                    legend: {
                        sorted: null,
                        position: "se"
                    }
                });
            } catch (err) {
                // error can sometimes occur due to load time issues
                if (retries > 0) {
                    retries = retries-1;
                    draw_value_distribution_bar(chartElement, chartData, retries);
                }
            }
        });
    });
}
//]]>
</script>
<style type="text/css">
.valueDistributionChart {
    width: 400px;
}
</style>"""
  }
}
