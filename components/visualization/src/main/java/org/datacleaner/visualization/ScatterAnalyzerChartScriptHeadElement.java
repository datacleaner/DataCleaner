package org.datacleaner.visualization;

import java.awt.Point;
import java.util.Set;
import java.util.stream.Collectors;

import org.datacleaner.result.html.HeadElement;
import org.datacleaner.result.html.HtmlRenderingContext;

public class ScatterAnalyzerChartScriptHeadElement implements HeadElement {

    private final ScatterAnalyzerResult _result;
    private final String _elementId;

    public ScatterAnalyzerChartScriptHeadElement(ScatterAnalyzerResult result, String elementId) {
        _result = result;
        _elementId = elementId;
    }

    @Override
    public String toHtml(HtmlRenderingContext context) {

      final String data = _result.getGroups().stream().map(group -> mapDataCoordinates(group.getCoordinates()) + "label:\"\"" + group.getName() + "\"\"}\"").collect(Collectors.joining(","));
      final String text = "\"<script type=\"text/javascript\"> \n" 
        + "//<![CDATA[ \n"
        + "var data = ["
        + "\""
        + data
        + "\""
        + "];"
        + "  wait_for_script_load('jQuery', function() {\n" + 
        "      $(function(){\n" + 
        "        draw_scatter_chart('\""+ _elementId +" \"', data, 2);\n" + 
        "      });\n" + 
        "    });"
        + "  });\n" + 
        "    //]]>\n" + 
        "</script>" ; 

   
        return text; 
    }

    private String mapDataCoordinates(Set<Point> coordinates) {

        final String data = coordinates.stream().map(coor -> "[" + coor.getX() + "," + coor.getY() + "]").collect(
                Collectors.joining(","));
        return "\"{ data :[\"" + data + "\"],";

    }
}
