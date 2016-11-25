package org.datacleaner.beans.transform
import org.junit.{Assert, Test}
import org.scalatest.junit.AssertionsForJUnit

class RemoveUnwantedCharsTransformerTest extends AssertionsForJUnit {

  @Test
  def testTransform() = {
    val trans = new RemoveUnwantedCharsTransformer()

    Assert.assertEquals("123", trans.transform("hello-world 123.")(0));
    
    trans.removeDigits = true
    
    Assert.assertEquals("", trans.transform("hello-world 123.")(0));
    
    trans.removeLetters = false
    
    Assert.assertEquals("helloworld", trans.transform("hello-world 123.")(0));
    
    trans.removeSigns = false
    
    Assert.assertEquals("hello-world.", trans.transform("hello-world 123.")(0));
    
    trans.removeWhitespaces = false
    trans.removeSigns = true
    trans.removeDigits = false
    
    Assert.assertEquals("helloworld 123", trans.transform("hello-world 123.")(0));
  }
}
