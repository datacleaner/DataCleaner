package org.eobjects.datacleaner.panels;

import java.awt.Graphics;
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

	public DCPanel() {
		this(null, 0, 0);
	}
	
	/**
	 * 
	 * @param watermark
	 * @param horizontalAlignmentInPercent
	 *            horizontal alignment of the watermark in percent where 0 is
	 *            LEFT and 100 is RIGHT
	 */
	public DCPanel(Image watermark, int horizontalAlignmentInPercent, int verticalAlignmentInPercent) {
		super();
		setOpaque(false);
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
			super.paintComponent(g);
		}

		if (_watermark != null) {
			int x = getWidth() - _imageWidth;
			x = (int) (x * _horizontalAlignment);

			int y = getHeight() - _imageHeight;
			y = (int) (y * _verticalAlignment);

			g.drawImage(_watermark, x, y, this);
		}
	}
	
	
}
