package org.datacleaner.visualization

import java.lang.Number

import org.datacleaner.beans.api.AnalyzerBean
import org.datacleaner.beans.api.Categorized
import org.datacleaner.beans.api.Description
import org.datacleaner.beans.api.Initialize
import org.datacleaner.beans.api.RendererBean
import org.datacleaner.beans.api.Validate
import org.datacleaner.data.InputColumn
import org.datacleaner.data.MockInputColumn
import org.datacleaner.data.MockInputRow
import org.datacleaner.result.renderer.SwingRenderingFormat
import org.datacleaner.util.LookAndFeelManager

import javax.swing.JFrame

object StackedAreaAnalyzerResultSwingRendererTestApp {

  def main(args: Array[String]) {
    val analyzer = new StackedAreaAnalyzer
    analyzer.categoryColumn = new MockInputColumn("category", classOf[Number])

    val col1 = new MockInputColumn("foo", classOf[Number])
    val col2 = new MockInputColumn("bar", classOf[Number])
    analyzer.measureColumns = Array[InputColumn[Number]](col1, col2)
    analyzer.validate
    analyzer.initialize

    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 1).put(col1, 10).put(col2, 10), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 2).put(col1, 12).put(col2, 1), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 3).put(col1, 21).put(col2, 2), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 4).put(col1, 10).put(col2, 6), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 5).put(col1, 1).put(col2, 11), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 6).put(col1, 18).put(col2, 10), 1);
    analyzer.run(new MockInputRow().put(analyzer.categoryColumn, 7).put(col1, 25).put(col2, 14), 1);
    analyzer.run(new MockInputRow().put(col1, 5), 1);

    val result = analyzer.getResult

    val jpanel = new StackedAreaAnalyzerResultSwingRenderer().render(result);

    LookAndFeelManager.get().init();

    val window = new JFrame("Example window")
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    window.getContentPane().add(jpanel)
    window.pack();
    window.setVisible(true)
  }
}
