/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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

      final String data = _result.getGroups().stream().map(group -> mapDataCoordinates(group.getCoordinates()) + " label:\"" + group.getName() + "\" \n}").collect(Collectors.joining(",\n"));
      final String text = "<script type=\"text/javascript\"> \n" 
        + "//<![CDATA[ \n"
        + " var data = ["
        + data
        + "];"
        + " \n"
        + "  wait_for_script_load('jQuery', function() {\n" + 
        "      $(function(){\n" + 
        "        draw_scatter_chart('"+ _elementId +"', data, 2);\n" + 
        "      });\n" + 
        "    });"
        + "\n" + 
        "    //]]>\n" + 
        "</script>" ; 

   
        return text; 
    }

    private String mapDataCoordinates(Set<Point> coordinates) {

        final String data = coordinates.stream().map(coor -> "[" + coor.getX() + "," + coor.getY() + "]").collect(
                Collectors.joining(","));
        return "{ \n data: \n [ \n" + data + "],\n";

    }
}
