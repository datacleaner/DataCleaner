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
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.HighlightPredicate.ColumnHighlightPredicate;

import com.jgoodies.looks.LookUtils;

/**
 * Highlights special columns in a JXTable
 */
public class ColumnHighlighter implements Highlighter {

	private static final Color BACKGROUND_COLOR = LookUtils
			.getSlightlyBrighter(HighlighterFactory.QUICKSILVER.darker());
	private ColorHighlighter _colorHighlighter;
	private ColumnHighlightPredicate _evaluatedColumnsPredicate;

	public ColumnHighlighter(int[] columnIndexes) {
		_evaluatedColumnsPredicate = new HighlightPredicate.ColumnHighlightPredicate(
				columnIndexes);
		_colorHighlighter = new ColorHighlighter(_evaluatedColumnsPredicate,
				BACKGROUND_COLOR, Color.BLACK);
	}

	public ColumnHighlighter(int index) {
		this(new int[] { index });
	}

	public void addChangeListener(ChangeListener arg0) {
		_colorHighlighter.addChangeListener(arg0);
	}

	public ChangeListener[] getChangeListeners() {
		return _colorHighlighter.getChangeListeners();
	}

	public Component highlight(Component component, ComponentAdapter adapter) {
		if (_evaluatedColumnsPredicate.isHighlighted(component, adapter)) {
			Component highlight = _colorHighlighter.highlight(component,
					adapter);
			highlight.setFont(highlight.getFont().deriveFont(Font.BOLD));
			return highlight;
		}
		return component;
	}

	public void removeChangeListener(ChangeListener listener) {
		_colorHighlighter.removeChangeListener(listener);
	}
}