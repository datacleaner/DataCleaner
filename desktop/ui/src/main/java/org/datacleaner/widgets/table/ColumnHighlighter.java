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
package org.datacleaner.widgets.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.event.ChangeListener;

import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlightPredicate.ColumnHighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

/**
 * Highlights special columns in a JXTable
 */
public final class ColumnHighlighter implements Highlighter {

    private static final Color BACKGROUND_COLOR = WidgetUtils.BG_COLOR_LESS_DARK;
    private static final Color FOREGROUND_COLOR = WidgetUtils.BG_COLOR_BRIGHTEST;
    private ColorHighlighter _colorHighlighter;
    private ColumnHighlightPredicate _evaluatedColumnsPredicate;

    public ColumnHighlighter(final int[] columnIndexes) {
        _evaluatedColumnsPredicate = new HighlightPredicate.ColumnHighlightPredicate(columnIndexes);
        _colorHighlighter = new ColorHighlighter(_evaluatedColumnsPredicate, BACKGROUND_COLOR, FOREGROUND_COLOR);
    }

    public ColumnHighlighter(final int index) {
        this(new int[] { index });
    }

    public void addChangeListener(final ChangeListener arg0) {
        _colorHighlighter.addChangeListener(arg0);
    }

    public ChangeListener[] getChangeListeners() {
        return _colorHighlighter.getChangeListeners();
    }

    public Component highlight(final Component component, final ComponentAdapter adapter) {
        if (_evaluatedColumnsPredicate.isHighlighted(component, adapter)) {
            return _colorHighlighter.highlight(component, adapter);
        }
        return component;
    }

    public void removeChangeListener(final ChangeListener listener) {
        _colorHighlighter.removeChangeListener(listener);
    }
}
