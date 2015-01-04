package org.eobjects.analyzer.beans.transform
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.junit.Assert

class TextCaseTransformerTest extends AssertionsForJUnit {

  @Test
  def testCapitalizeSentences() {
    val transformer = new TextCaseTransformer();
    transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_SENTENCES;

    Assert.assertEquals("Hello world. foo bar! foo", transformer.transform("hello World. FOO baR! foo"));
    Assert.assertEquals("Hello world. foo bar:\n foo", transformer.transform("hello World. FOO baR:\n foo"));
  }

  @Test
  def testCapitalizeWords() {
    val transformer = new TextCaseTransformer();
    transformer.mode = TextCaseTransformer.TransformationMode.CAPITALIZE_WORDS;

    Assert.assertEquals("Hello World. Foo Bar! Foo", transformer.transform("hello World. FOO baR! foo"));
    Assert.assertEquals("Hello World. Foo Bar:\n Foo", transformer.transform("hello World. FOO baR:\n foo"));
  }

  @Test
  def testUpperCase() {
    val transformer = new TextCaseTransformer();
    transformer.mode = TextCaseTransformer.TransformationMode.UPPER_CASE;

    Assert.assertEquals("HELLO WORLD. FOO BAR! FOO", transformer.transform("hello World. FOO baR! foo"));
  }

  @Test
  def testLowerCase() {
    val transformer = new TextCaseTransformer();
    transformer.mode = TextCaseTransformer.TransformationMode.LOWER_CASE;

    Assert.assertEquals("hello world. foo bar! foo", transformer.transform("hello World. FOO baR! foo"));
  }
}