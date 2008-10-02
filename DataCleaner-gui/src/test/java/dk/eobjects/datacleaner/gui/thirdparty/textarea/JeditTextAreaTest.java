/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.thirdparty.textarea;

import java.awt.event.KeyEvent;

import junit.framework.TestCase;
import dk.eobjects.thirdparty.textarea.JEditTextArea;

public class JeditTextAreaTest extends TestCase {

	/**
	 * Tests that two textareas don't share the same text (bug caused by initial
	 * // conversion from jedit syntax package).
	 */
	public void testTwoTextAreas() throws Exception {
		JEditTextArea textArea1 = new JEditTextArea();
		JEditTextArea textArea2 = new JEditTextArea();

		textArea1.setText("foo");
		textArea2.setText("bar");

		assertEquals("foo", textArea1.getText());
		assertEquals("bar", textArea2.getText());
	}

	public void testInsertTab() throws Exception {
		JEditTextArea textArea = new JEditTextArea();
		textArea.setSelectedText("foobar").setSelectedText("\n")
				.setSelectedText("foo").setSelectedText("\n").setSelectedText(
						"bar").setSelectedText("\n").setSelectedText("yay")
				.setSelectedText("\n").setSelectedText("data").setSelectedText(
						"\n").setSelectedText("cleaner").setSelectedText("\n")
				.setSelectedText("gui").setSelectedText("\n").setSelectedText(
						"text");
		textArea.setSelectionStart(16);
		textArea.setSelectionEnd(23);
		int startLine = textArea.getSelectionStartLine();
		assertEquals(3, startLine);
		int endLine = textArea.getSelectionEndLine();
		assertEquals(4, endLine);
		assertEquals("ay\ndata", textArea.getSelectedText());

		KeyEvent event = new KeyEvent(textArea, KeyEvent.KEY_PRESSED, System
				.currentTimeMillis(), 0, KeyEvent.VK_TAB, '\t');
		textArea.processKeyEvent(event);

		assertEquals("foobar\nfoo\nbar\n\tyay\n\tdata\ncleaner\ngui\ntext",
				textArea.getText());
	}
}