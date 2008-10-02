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
package dk.eobjects.datacleaner.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

/**
 * Helper class to build components with fitting UIs easier
 */
public class GuiBuilder<E extends JComponent> {

	private E _component;

	public GuiBuilder(E component) {
		_component = component;
	}

	public E toComponent() {
		return _component;
	}

	public GuiBuilder<E> applyBackground(Color color) {
		_component.setBackground(color);
		return this;
	}

	public GuiBuilder<E> applyLightBackground() {
		return applyBackground(GuiHelper.BG_COLOR_LIGHT);
	}

	public GuiBuilder<E> applyDarkBackground() {
		return applyBackground(GuiHelper.BG_COLOR_DARK);
	}

	public GuiBuilder<E> applyBorder() {
		_component.setBorder(GuiHelper.BORDER_THIN);
		return this;
	}

	public GuiBuilder<E> applyTitledBorder(String title) {
		_component.setBorder(new TitledBorder(GuiHelper.BORDER_THIN, title));
		return this;
	}

	public GuiBuilder<E> applyName(String name) {
		_component.setName(name);
		return this;
	}

	public GuiBuilder<E> applySize(Dimension d) {
		_component.setSize(d);
		_component.setPreferredSize(d);
		return this;
	}

	public GuiBuilder<E> applySize(Integer width, Integer height) {
		Dimension d = new Dimension();
		if (width != null) {
			d.width = width;
		}
		if (height != null) {
			d.height = height;
		}
		return applySize(d);
	}

	public GuiBuilder<E> applyNormalFont() {
		return applyFont(GuiHelper.FONT_NORMAL);
	}

	public GuiBuilder<E> applyHeaderFont() {
		return applyFont(GuiHelper.FONT_HEADER);
	}

	public GuiBuilder<E> applyMonospaceFont() {
		return applyFont(GuiHelper.FONT_MONOSPACE);
	}

	public GuiBuilder<E> applyFont(Font font) {
		_component.setFont(font);
		return this;
	}

	public GuiBuilder<E> applyBorderLayout() {
		_component.setLayout(new BorderLayout());
		return this;
	}

	public GuiBuilder<E> applyVerticalLayout() {
		_component.setLayout(new BoxLayout(_component, BoxLayout.Y_AXIS));
		return this;
	}

	public GuiBuilder<E> applyHorisontalLayout() {
		_component.setLayout(new BoxLayout(_component, BoxLayout.X_AXIS));
		return this;
	}

	public GuiBuilder<E> applyDarkBlueBackground() {
		return applyBackground(GuiHelper.BG_COLOR_DARKBLUE);
	}

	public GuiBuilder<E> applyLayout(LayoutManager layout) {
		_component.setLayout(layout);
		return this;
	}
}