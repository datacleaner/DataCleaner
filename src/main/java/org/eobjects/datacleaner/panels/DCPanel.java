package org.eobjects.datacleaner.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class DCPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Image _watermark;
	private final int _imageHeight;
	private final int _imageWidth;
	private final float _horizontalAlignment;
	private final float _verticalAlignment;
	private final Color _bottomColor;
	private final Color _topColor;

	public DCPanel() {
		this(null, 0, 0);
	}

	public DCPanel(Color topColor, Color bottomColor) {
		this(null, 0, 0, topColor, bottomColor);
	}

	/**
	 * 
	 * @param watermark
	 * @param horizontalAlignmentInPercent
	 *            horizontal alignment of the watermark in percent where 0 is
	 *            LEFT and 100 is RIGHT
	 */
	public DCPanel(Image watermark, int horizontalAlignmentInPercent, int verticalAlignmentInPercent) {
		this(watermark, horizontalAlignmentInPercent, verticalAlignmentInPercent, null, null);
	}

	public DCPanel(Image watermark, int horizontalAlignmentInPercent, int verticalAlignmentInPercent, Color topColor,
			Color bottomColor) {
		super();
		if (topColor == null || bottomColor == null) {
			setOpaque(false);
		}
		_topColor = topColor;
		_bottomColor = bottomColor;
		_watermark = watermark;
		_horizontalAlignment = horizontalAlignmentInPercent / 100f;
		_verticalAlignment = verticalAlignmentInPercent / 100f;
		if (watermark != null) {
			ImageIcon icon = new ImageIcon(watermark);
			_imageWidth = icon.getIconWidth();
			_imageHeight = icon.getIconHeight();
		} else {
			_imageWidth = -1;
			_imageHeight = -1;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			if (_topColor == null || _bottomColor == null) {
				super.paintComponent(g);
			} else {
				GradientPaint paint = new GradientPaint(getX(), getY(), _topColor, getX(), getHeight(), _bottomColor);
				if (g instanceof Graphics2D) {
					Graphics2D g2d = (Graphics2D) g;
					g2d.setPaint(paint);
				} else {
					g.setColor(_bottomColor);
				}
				g.fillRect(getX(), getY(), getWidth(), getHeight());
			}
		}

		if (_watermark != null) {
			int x = getWidth() - _imageWidth;
			x = (int) (x * _horizontalAlignment);

			int y = getHeight() - _imageHeight;
			y = (int) (y * _verticalAlignment);

			g.drawImage(_watermark, x, y, this);
		}
	}

	public void setPreferredSize(int width, int height) {
		setPreferredSize(new Dimension(width, height));
	}

}
