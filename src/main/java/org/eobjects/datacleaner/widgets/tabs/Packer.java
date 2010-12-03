/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.tabs;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * This is a slightly rewritten/modified version of swingutil's
 * ClosableTabbedPane
 */
final class Packer extends GridBagLayout implements Cloneable {

	private static final long serialVersionUID = 5315740263318700891L;

	/**
	 * The current constraints used for subsequent uses of the PackAs interface
	 * returned by the methods below.
	 */
	protected GridBagConstraints gc = null;

	/**
	 * The component currently being layed out.
	 */
	protected Component comp;

	/**
	 * If the Packer( Container ) constructor is used, this member contains a
	 * reference to the container that this instance provides layout for.
	 */
	protected Container container;

	/**
	 * Create a new Packer instance. A Packer constructed with this constructor
	 * will have no "designated" container for the elements that it packs. Thus,
	 * each will need to be added to the container individually as well as being
	 * pack()'d or add()'d with the packer.
	 */
	public Packer() {
		super();
	}

	/**
	 * Creates a new Packer instance that is used to layout the passed
	 * container. This version of the constructor allows for more compact code
	 * by causing this instance to automatically add() the components to the
	 * container as well as do the layout.
	 */
	public Packer(Container cont) {
		super();
		cont.setLayout(this);
		container = cont;
	}

	/**
	 * Create a copy of this Packer Object.
	 * 
	 * @exception CloneNotSupportedException
	 *                if strange clone errors occur
	 */
	@Override
	public Packer clone() {
		try {
			return (Packer) super.clone();
		} catch (CloneNotSupportedException e) {
			// should never happen
			throw new RuntimeException(e);
		}
	}

	/**
	 * Set the designated container for objects packed by this instance.
	 * 
	 * @param cont
	 *            the Container to use to add the components to when .pack() or
	 *            .add() is invoked.
	 * @exception IllegalAccessException
	 *                if container already set
	 */
	public Packer setContainer(Container cont) throws IllegalAccessException {
		if (container != null) {
			Packer p = (Packer) clone();
			container.setLayout(p);
		}
		container = cont;
		cont.setLayout(this);
		return this;
	}

	/**
	 * Copy the passed component's contraints as the current constraints. This
	 * is typically used to reuse constraints set on another component. Be
	 * careful with this method as it does not protect you from cascading reuse
	 * issues. This method uses {@link GridbagLayout.getConstraints()} to get a
	 * copy of the constraints for <code>comp</code>.
	 * 
	 * @param comp
	 *            the component to get the constraints
	 * 
	 *            <pre>
	 * Packer pk = new Packer(pan);
	 * pk.pack(lab).gridx(0).inset(10, 10, 10, 10);
	 * pk.pack(text).like(lab).gridx(1).fillx(); // same insets
	 * </pre>
	 */
	public Packer like(Component comp) {
		// Get a copy of the components constraints
		gc = getConstraints(comp);
		if (gc == null)
			throw new IllegalArgumentException(comp + " has no existing constraints");
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * A non chainable version of {@link #like(Component)}
	 * 
	 * @see #like(Component)
	 */
	public void setInitialConstraintsFrom(Component comp) {
		gc = getConstraints(comp);
		if (gc == null)
			throw new IllegalArgumentException(comp + " has no existing constraints");
		setConstraints(comp, gc);
	}

	/**
	 * Get the designated container for this instance.
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * This method is used to specify the container that the next component that
	 * is added or packed will be placed in.
	 * 
	 * @param cont
	 *            The container to place future components into.
	 * @exception IllegalAccessException
	 *                if container is already set
	 */
	public Packer into(Container cont) throws IllegalAccessException {
		setContainer(cont);
		return this;
	}

	/**
	 * Establishes a new set of constraints to layout the widget passed. The
	 * created constraints are applied to the passed Component and if a
	 * Container is known to this object, the component is added to the known
	 * container.
	 * 
	 * @param cp
	 *            The component to layout.
	 */
	public Packer pack(Component cp) {
		if (container != null) {
			container.add(cp);
		}
		comp = cp;
		gc = new GridBagConstraints();
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Reuses the previous set of constraints to layout the widget passed
	 * 
	 * @param cp
	 *            The component to layout. It must be added to the Container
	 *            owning this LayoutManager by the calling code.
	 */
	public Packer add(Component cp) {
		if (container != null) {
			container.add(cp);
		}
		comp = cp;
		if (gc == null) {
			gc = new GridBagConstraints();
		}
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=NORTH to the constraints for the current component if how ==
	 * true remove it if false.
	 */
	public Packer setAnchorNorth(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.NORTH;
		else
			gc.anchor &= ~GridBagConstraints.NORTH;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorNorth() {
		return gc.anchor == GridBagConstraints.NORTH;
	}

	/**
	 * Add anchor=NORTH to the constraints for the current component.
	 */
	public Packer north() {
		gc.anchor = GridBagConstraints.NORTH;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=SOUTH to the constraints for the current component if how ==
	 * true remove it if false.
	 */
	public Packer setAnchorSouth(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.SOUTH;
		else
			gc.anchor &= ~GridBagConstraints.SOUTH;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorSouth() {
		return gc.anchor == GridBagConstraints.SOUTH;
	}

	/**
	 * Add anchor=SOUTH to the constraints for the current component.
	 */
	public Packer south() {
		gc.anchor = GridBagConstraints.SOUTH;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=EAST to the constraints for the current component if how ==
	 * true remove it if false.
	 */
	public Packer setAnchorEast(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.EAST;
		else
			gc.anchor &= ~GridBagConstraints.EAST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorEast() {
		return gc.anchor == GridBagConstraints.EAST;
	}

	/**
	 * Add anchor=EAST to the constraints for the current component.
	 */
	public Packer east() {
		gc.anchor = GridBagConstraints.EAST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=WEST to the constraints for the current component if how ==
	 * true remove it if false.
	 */
	public Packer setAnchorWest(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.WEST;
		else
			gc.anchor &= ~GridBagConstraints.WEST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Set the anchor term to the passed mask. The mask values are described in
	 * the {@link java.awt.GridbagConstraints} javadoc. This provides a simple
	 * way to support the JDK1.6 constraints without having Packer/PackAs
	 * branch. In the future, we can add appropriate methods for the baseline
	 * etc constraints.
	 */
	public Packer anchor(int mask) {
		gc.anchor = mask;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorWest() {
		return gc.anchor == GridBagConstraints.WEST;
	}

	/**
	 * Add anchor=WEST to the constraints for the current component.
	 */
	public Packer west() {
		gc.anchor = GridBagConstraints.WEST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=NORTHWEST to the constraints for the current component if how
	 * == true remove it if false.
	 */
	public Packer setAnchorNorthWest(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.NORTHWEST;
		else
			gc.anchor &= ~GridBagConstraints.NORTHWEST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorNorthWest() {
		return gc.anchor == GridBagConstraints.NORTHWEST;
	}

	/**
	 * Add anchor=NORTHWEST to the constraints for the current component.
	 */
	public Packer northwest() {
		gc.anchor = GridBagConstraints.NORTHWEST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=SOUTHWEST to the constraints for the current component if how
	 * == true remove it if false.
	 */
	public Packer setAnchorSouthWest(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.SOUTHWEST;
		else
			gc.anchor &= ~GridBagConstraints.SOUTHWEST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorSouthWest() {
		return gc.anchor == GridBagConstraints.SOUTHWEST;
	}

	/**
	 * Add anchor=SOUTHWEST to the constraints for the current component.
	 */
	public Packer southwest() {
		gc.anchor = GridBagConstraints.SOUTHWEST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=NORTHEAST to the constraints for the current component if how
	 * == true remove it if false.
	 */
	public Packer setAnchorNorthEast(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.NORTHEAST;
		else
			gc.anchor &= ~GridBagConstraints.NORTHEAST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorNorthEast() {
		return gc.anchor == GridBagConstraints.NORTHEAST;
	}

	/**
	 * Add anchor=NORTHEAST to the constraints for the current component.
	 */
	public Packer northeast() {
		gc.anchor = GridBagConstraints.NORTHEAST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add anchor=SOUTHEAST to the constraints for the current component if how
	 * == true remove it if false.
	 */
	public Packer setAnchorSouthEast(boolean how) {
		if (how == true)
			gc.anchor = GridBagConstraints.SOUTHEAST;
		else
			gc.anchor &= ~GridBagConstraints.SOUTHEAST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getAnchorSouthEast() {
		return gc.anchor == GridBagConstraints.SOUTHEAST;
	}

	/**
	 * Add anchor=SOUTHEAST to the constraints for the current component.
	 */
	public Packer southeast() {
		gc.anchor = GridBagConstraints.SOUTHEAST;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add gridx=RELATIVE to the constraints for the current component if how ==
	 * true 0 it if false.
	 */
	public Packer setXLeftRelative(boolean how) {
		if (how == true)
			gc.gridx = GridBagConstraints.RELATIVE;
		else
			gc.gridx = 0;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getXLeftRelative() {
		return gc.gridx == GridBagConstraints.RELATIVE;
	}

	/**
	 * Add gridx=RELATIVE to the constraints for the current component.
	 */
	public Packer left() {
		gc.gridx = GridBagConstraints.RELATIVE;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add gridy=RELATIVE to the constraints for the current component if how ==
	 * true 0 it if false.
	 */
	public Packer setYTopRelative(boolean how) {
		if (how == true)
			gc.gridy = GridBagConstraints.RELATIVE;
		else
			gc.gridy = 0;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getYTopRelative() {
		return gc.gridy == GridBagConstraints.RELATIVE;
	}

	/**
	 * Add gridy=RELATIVE to the constraints for the current component.
	 */
	public Packer top() {
		gc.gridy = GridBagConstraints.RELATIVE;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add gridWidth=RELATIVE to the constraints for the current component if
	 * how == true 1 it if false.
	 */
	public Packer setXRightRelative(boolean how) {
		if (how == true)
			gc.gridwidth = GridBagConstraints.RELATIVE;
		else
			gc.gridwidth = 1;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getXRightRelative() {
		return gc.gridwidth == GridBagConstraints.RELATIVE;
	}

	/**
	 * Add gridwidth=RELATIVE to the constraints for the current component.
	 */
	public Packer right() {
		gc.gridwidth = GridBagConstraints.RELATIVE;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add gridWidth=RELATIVE to the constraints for the current component if
	 * how == true 1 it if false.
	 */
	public Packer setYBottomRelative(boolean how) {
		if (how == true)
			gc.gridheight = GridBagConstraints.RELATIVE;
		else
			gc.gridheight = 1;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 */
	public boolean getYBottomRelative() {
		return gc.gridheight == GridBagConstraints.RELATIVE;
	}

	/**
	 * Add gridheight=RELATIVE to the constraints for the current component.
	 */
	public Packer bottom() {
		gc.gridheight = GridBagConstraints.RELATIVE;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add gridx=tot to the constraints for the current component.
	 * 
	 * @param tot
	 *            - the value to set gridx to.
	 */
	public Packer gridx(int tot) {
		gc.gridx = tot;
		setConstraints(comp, gc);
		return this;
	}

	public int getGridX() {
		return gc.gridx;
	}

	/**
	 * Add gridy=tot to the constraints for the current component.
	 * 
	 * @param tot
	 *            - the value to set gridy to.
	 */
	public Packer gridy(int tot) {
		gc.gridy = tot;
		setConstraints(comp, gc);
		return this;
	}

	public int getGridY() {
		return gc.gridy;
	}

	/**
	 * Add gridwidth=tot to the constraints for the current component.
	 * 
	 * @param tot
	 *            - the value to set gridwidth to.
	 */
	public Packer gridw(int tot) {
		gc.gridwidth = tot;
		setConstraints(comp, gc);
		return this;
	}

	public int getGridW() {
		return gc.gridwidth;
	}

	/**
	 * Add gridheight=tot to the constraints for the current component.
	 * 
	 * @param tot
	 *            - the value to set gridheight to.
	 */
	public Packer gridh(int tot) {
		gc.gridheight = tot;
		setConstraints(comp, gc);
		return this;
	}

	public int getGridH() {
		return gc.gridheight;
	}

	/**
	 * Add ipadx=cnt to the constraints for the current component.
	 * 
	 * @param cnt
	 *            - the value to set ipadx to.
	 */
	public Packer padx(int cnt) {
		gc.ipadx = cnt;
		setConstraints(comp, gc);
		return this;
	}

	public int getPadX() {
		return gc.ipadx;
	}

	/**
	 * Add ipady=cnt to the constraints for the current component.
	 * 
	 * @param cnt
	 *            - the value to set ipady to.
	 */
	public Packer pady(int cnt) {
		gc.ipady = cnt;
		setConstraints(comp, gc);
		return this;
	}

	public int getPadY() {
		return gc.ipady;
	}

	/**
	 * Add fill=HORIZONTAL, weightx=1 to the constraints for the current
	 * component if how == true. fill=0, weightx=0 if how is false.
	 */
	public Packer setFillX(boolean how) {
		if (how == true) {
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.weightx = 1;
		} else {
			gc.weightx = 0;
			gc.fill = 0;
		}
		setConstraints(comp, gc);
		return this;
	}

	public boolean getFillX() {
		return gc.fill == GridBagConstraints.HORIZONTAL;
	}

	/**
	 * Add fill=HORIZONTAL, weightx=wtx to the constraints for the current
	 * component.
	 */
	public Packer fillx(double wtx) {
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = wtx;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add fill=HORIZONTAL, weightx=1 to the constraints for the current
	 * component.
	 */
	public Packer fillx() {
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add fill=VERITCAL to the constraints for the current component if how ==
	 * true 1 it if false.
	 */
	public Packer setFillY(boolean how) {
		if (how == true) {
			gc.fill = GridBagConstraints.VERTICAL;
			gc.weighty = 1;
		} else {
			gc.weighty = 0;
			gc.fill = 0;
		}
		setConstraints(comp, gc);
		return this;
	}

	public boolean getFillY() {
		return gc.fill == GridBagConstraints.VERTICAL;
	}

	/**
	 * Add fill=VERTICAL, weighty=wty to the constraints for the current
	 * component.
	 */
	public Packer filly(double wty) {
		gc.fill = GridBagConstraints.VERTICAL;
		gc.weighty = wty;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add fill=VERTICAL, weighty=1 to the constraints for the current
	 * component.
	 */
	public Packer filly() {
		gc.fill = GridBagConstraints.VERTICAL;
		gc.weighty = 1;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add fill=BOTH, weightx=1, weighty=1 to the constraints for the current
	 * component if how == true, fill=0, weightx=0, weighty=0 if it is false.
	 */
	public Packer setFillBoth(boolean how) {
		if (how == true) {
			gc.fill = GridBagConstraints.BOTH;
			gc.weightx = 1;
			gc.weighty = 1;
		} else {
			gc.weightx = 0;
			gc.weighty = 0;
			gc.fill = 0;
		}
		setConstraints(comp, gc);
		return this;
	}

	public boolean getFillBoth() {
		return gc.fill == GridBagConstraints.BOTH;
	}

	/**
	 * Add fill=BOTH, weighty=1, weightx=1 to the constraints for the current
	 * component.
	 */
	public Packer fillboth(double wtx, double wty) {
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = wtx;
		gc.weighty = wty;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add fill=BOTH, weighty=1, weightx=1 to the constraints for the current
	 * component.
	 */
	public Packer fillboth() {
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 1;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Specify the insets for the component.
	 * 
	 * @param insets
	 *            the insets to apply
	 */
	public Packer inset(Insets insets) {
		gc.insets = insets;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * sets top Insets on the constraints for the current component to the value
	 * specified.
	 */
	public Packer setInsetTop(int val) {
		Insets i = gc.insets;
		if (i == null)
			i = new Insets(0, 0, 0, 0);
		gc.insets = new Insets(val, i.left, i.bottom, i.right);
		setConstraints(comp, gc);
		return this;
	}

	public int getInsetTop() {
		return gc.insets.top;
	}

	public int getInsetBottom() {
		return gc.insets.bottom;
	}

	public int getInsetLeft() {
		return gc.insets.left;
	}

	public int getInsetRight() {
		return gc.insets.right;
	}

	/**
	 * sets bottom Insets on the constraints for the current component to the
	 * value specified.
	 */
	public Packer setInsetBottom(int val) {
		Insets i = gc.insets;
		if (i == null)
			i = new Insets(0, 0, 0, 0);
		gc.insets = new Insets(i.top, i.left, val, i.right);
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * sets left Insets on the constraints for the current component to the
	 * value specified.
	 */
	public Packer setInsetLeft(int val) {
		Insets i = gc.insets;
		if (i == null)
			i = new Insets(0, 0, 0, 0);
		gc.insets = new Insets(i.top, val, i.bottom, i.right);
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * sets right Insets on the constraints for the current component to the
	 * value specified.
	 */
	public Packer setInsetRight(int val) {
		Insets i = gc.insets;
		if (i == null)
			i = new Insets(0, 0, 0, 0);
		gc.insets = new Insets(i.top, i.left, i.bottom, val);
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Specify the insets for the component.
	 * 
	 * @param left
	 *            the inset from the left.
	 * @param top
	 *            the inset from the top.
	 * @param right
	 *            the inset from the right.
	 * @param bottom
	 *            the inset from the bottom.
	 */
	public Packer inset(int top, int left, int bottom, int right) {
		gc.insets = new Insets(top, left, bottom, right);
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add weightx=wt to the constraints for the current component.
	 * 
	 * @param wt
	 *            - the value to set weightx to.
	 */
	public Packer weightx(double wt) {
		gc.weightx = wt;
		setConstraints(comp, gc);
		return this;
	}

	public double getWeightX() {
		return gc.weightx;
	}

	public double getWeightY() {
		return gc.weighty;
	}

	/**
	 * Add weighty=wt to the constraints for the current component.
	 * 
	 * @param wt
	 *            - the value to set weightx to.
	 */
	public Packer weighty(double wt) {
		gc.weighty = wt;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add gridWidth=REMAINDER to the constraints for the current component if
	 * how == true 1 it if false.
	 */
	public Packer setRemainX(boolean how) {
		if (how == true)
			gc.gridwidth = GridBagConstraints.REMAINDER;
		else
			gc.gridwidth = 1;
		setConstraints(comp, gc);
		return this;
	}

	public boolean getRemainX() {
		return gc.gridwidth == GridBagConstraints.REMAINDER;
	}

	/**
	 * Add gridwidth=REMAINDER to the constraints for the current component.
	 */
	public Packer remainx() {
		gc.gridwidth = GridBagConstraints.REMAINDER;
		setConstraints(comp, gc);
		return this;
	}

	/**
	 * Add gridWidth=REMAINDER to the constraints for the current component if
	 * how == true 1 it if false.
	 */
	public Packer setRemainY(boolean how) {
		if (how == true)
			gc.gridheight = GridBagConstraints.REMAINDER;
		else
			gc.gridheight = 1;
		setConstraints(comp, gc);
		return this;
	}

	public boolean getRemainY() {
		return gc.gridheight == GridBagConstraints.REMAINDER;
	}

	/**
	 * Add gridheight=REMAINDER to the constraints for the current component.
	 */
	public Packer remainy() {
		gc.gridheight = GridBagConstraints.REMAINDER;
		setConstraints(comp, gc);
		return this;
	}
}