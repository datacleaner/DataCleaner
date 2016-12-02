package org.datacleaner.result.html

import org.datacleaner.api.{Renderable, Renderer, RendererPrecedence}

/**
 * An extension of the renderer interface, useful for most simple HTML renderers.
 */
trait HtmlRenderer[R <: Renderable] extends Renderer[R, HtmlFragment] {

  override def getPrecedence(renderable: R) = RendererPrecedence.MEDIUM;

  override def render(result: R): HtmlFragment = new HtmlFragment {

    val fragment = new SimpleHtmlFragment();
    
    override def initialize(context: HtmlRenderingContext) = {
      handleFragment(fragment, result, context);
    }
    
    override def getHeadElements() = fragment.getHeadElements();

    override def getBodyElements() = fragment.getBodyElements();
  }

  def handleFragment(frag: SimpleHtmlFragment, result: R, context: HtmlRenderingContext): Unit
}
