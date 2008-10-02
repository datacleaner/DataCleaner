/*
 * TextAreaDefaults.java - Encapsulates default values for various settings
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package dk.eobjects.thirdparty.textarea;

import java.awt.Color;

import javax.swing.JPopupMenu;

import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 * Encapsulates default settings for a text area. This can be passed to the
 * constructor once the necessary fields have been filled out. The advantage of
 * doing this over calling lots of set() methods after creating the text area is
 * that this method is faster.
 */
public class TextAreaDefaults {
	public InputHandler inputHandler;
	public SyntaxDocument document;
	public boolean editable;

	public boolean caretVisible;
	public boolean caretBlinks;
	public boolean blockCaret;
	public int electricScroll;

	public int cols;
	public int rows;
	public SyntaxStyle[] styles;
	public Color caretColor;
	public Color selectionColor;
	public Color lineHighlightColor;
	public boolean lineHighlight;
	public Color bracketHighlightColor;
	public boolean bracketHighlight;
	public Color eolMarkerColor;
	public boolean eolMarkers;
	public boolean paintInvalid;

	public JPopupMenu popup;

	/**
	 * Returns a new TextAreaDefaults object with the default values filled in.
	 */
	public static TextAreaDefaults getDefaults() {
		TextAreaDefaults defaults = new TextAreaDefaults();

		defaults.inputHandler = new DefaultInputHandler();
		defaults.inputHandler.addDefaultKeyBindings();
		defaults.document = new SyntaxDocument();
		defaults.editable = true;

		defaults.blockCaret = false;
		defaults.caretVisible = true;
		defaults.caretBlinks = true;
		defaults.electricScroll = 3;

		defaults.cols = 40;
		defaults.rows = 20;
		defaults.styles = SyntaxUtilities.getDefaultSyntaxStyles();
		defaults.caretColor = Color.BLACK;
		defaults.selectionColor = new Color(222, 222, 200);
		defaults.lineHighlightColor = HighlighterFactory.BEIGE;
		defaults.lineHighlight = true;
		defaults.bracketHighlightColor = Color.black;
		defaults.bracketHighlight = true;
		defaults.eolMarkerColor = new Color(0x009999);
		defaults.eolMarkers = false; // true;
		defaults.paintInvalid = true; // true;

		return defaults;
	}
}
