package org.eobjects.analyzer.beans.writers
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import scala.xml.PrettyPrinter
import javax.xml.transform.Transformer
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration
import javax.inject.Inject

@RendererBean(classOf[HtmlRenderingFormat])
class WriteDataResultHtmlRenderer extends Renderer[WriteDataResult, HtmlFragment] {
  
  @Inject
  var configuration: AnalyzerBeansConfiguration = null
  
  override def getPrecedence(renderable: WriteDataResult) = RendererPrecedence.MEDIUM;

  override def render(r: WriteDataResult): HtmlFragment = {
    val inserts = r.getWrittenRowCount()
    val updates = r.getUpdatesCount()
    val errors = r.getErrorRowCount()
    val total = inserts + updates + errors
    val datastoreName = if (configuration == null) null else {
      val ds = r.getDatastore(configuration.getDatastoreCatalog())
      if (ds == null) null else ds.getName()
    }
    
    val html = <div>
                 { if (datastoreName != null) { <p>{if (total == 0) "No data" else "Data"} written to <span class="datastoreName">{datastoreName}</span></p> } }
                 { if (inserts > 0) { <p>Executed { inserts } inserts</p> } }
                 { if (updates > 0) { <p>Executed { updates } updates</p> } }
                 { if (errors > 0) { <p>{ errors } Erroneous records</p> } }
               </div>;

               val frag = new SimpleHtmlFragment();
    frag.addBodyElement(html.toString());
    return frag;
  }
  
  def setConfiguration(configuration: AnalyzerBeansConfiguration) {
    this.configuration = configuration;
  }
}