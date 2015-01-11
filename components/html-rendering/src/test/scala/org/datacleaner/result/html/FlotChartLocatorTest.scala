package org.datacleaner.result.html

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.junit.Assert

class FlotChartLocatorTest extends AssertionsForJUnit {

  @Test
  def testBasicStuff = {
    Assert.assertEquals("http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.min.js", FlotChartLocator.getFlotBaseUrl);
    
    FlotChartLocator.setFlotHome("../bar/");
    
    Assert.assertEquals("../bar/jquery.flot.min.js", FlotChartLocator.getFlotBaseUrl);
    
    FlotChartLocator.setFlotHome(null)
    
    Assert.assertEquals("http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.min.js", FlotChartLocator.getFlotBaseUrl);
  }
}
