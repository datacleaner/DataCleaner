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
package org.datacleaner.monitor.shared.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Shows colors for selection.
 */
public class ColorBox extends Composite implements HasValue<String>, HasName, HasClickHandlers {

    public interface Images extends ClientBundle {
        ImageResource lightness();

        ImageResource hueSaturation();
    }

    private class ColorPopup extends PopupPanel {

        float hue = 200;
        float saturation = 2 / 3f;
        float luminance = 1 / 3f;
        private FlowPanel panel;
        private Image hueSaturation;
        private Image lightness;
        private Label preview;
        private boolean down = false;
        private boolean clicked = false;

        ColorPopup() {
            super(true);

            this.panel = new FlowPanel();
            this.hueSaturation = new Image(IMAGES.hueSaturation());
            this.lightness = new Image(IMAGES.lightness());
            this.preview = new Label();

            panel.setSize("220px", "100px");
            preview.setSize("20px", "100px");

            panel.add(hueSaturation);
            panel.add(lightness);
            panel.add(preview);
            setWidget(panel);
            addStyleName("fp-cp");

            setStyleAttribute(hueSaturation.getElement(), "cursor", "crosshair");
            setStyleAttribute(lightness.getElement(), "cursor", "ns-resize");
            setStyleAttribute(preview.getElement(), "float", "right");
            setStyleAttribute(preview.getElement(), "cssFloat", "right");
            setStyleAttribute(preview.getElement(), "styleFloat", "right");

            setColor();

            hueSaturation.addMouseDownHandler(event -> {
                event.preventDefault();
                setHueSaturation(event.getNativeEvent());
                down = true;
            });

            hueSaturation.addMouseUpHandler(event -> {
                setHueSaturation(event.getNativeEvent());
                down = false;
            });

            hueSaturation.addMouseMoveHandler(event -> {
                if (down) {
                    setHueSaturation(event.getNativeEvent());
                }
            });

            hueSaturation.addMouseOutHandler(event -> down = false);

            /* --- */

            lightness.addMouseDownHandler(event -> {
                event.preventDefault();
                setLightness(event.getNativeEvent());
                down = true;
            });

            lightness.addMouseUpHandler(event -> {
                setLightness(event.getNativeEvent());
                down = false;
            });

            lightness.addMouseMoveHandler(event -> {
                if (down) {
                    setLightness(event.getNativeEvent());
                }
            });

            lightness.addMouseOutHandler(event -> down = false);

            /* --- */

            preview.addMouseDownHandler(event -> {
                clicked = false;
                hide();
            });
        }

        public String getHex() {
            return new Color(hue, saturation, luminance).toString();
        }

        public void setHex(final String colorString) {
            if (colorString.startsWith("#") && colorString.length() == 7 && clicked) {
                final Color rgb = new Color(colorString);
                hue = rgb.getHue();
                saturation = rgb.getSaturation();
                luminance = rgb.getLightness();
                setColor();
            }
        }

        private void setColor() {
            final Color p = new Color(hue, saturation, luminance);
            setStyleAttribute(preview.getElement(), "backgroundColor", p.toString());
            final Color l = new Color(hue, saturation, 0.5f);
            setStyleAttribute(lightness.getElement(), "backgroundColor", l.toString());

            setStyleAttribute(blotch.getElement(), "backgroundColor", getHex());
        }

        private void setHueSaturation(final NativeEvent event) {
            clicked = true;
            final int x = event.getClientX() - hueSaturation.getAbsoluteLeft();
            final int y = event.getClientY() - hueSaturation.getAbsoluteTop();

            if (x > -1 && x < 181 && y > -1 && y < 101) {
                hue = x * 2;
                saturation = (float) (100 - y) / 100f;

                setColor();
            } else {
                down = false;
            }
        }

        private void setLightness(final NativeEvent event) {
            clicked = true;
            final int y = event.getClientY() - lightness.getAbsoluteTop();

            if (y > -1 && y < 101) {
                luminance = (float) (100 - y) / 100f;
                setColor();
            } else {
                down = false;
            }
        }
    }

    private static final Images IMAGES = GWT.create(Images.class);
    private ColorPopup popup;
    private TextBox textbox;
    private Anchor blotch;
    private FlowPanel panel;
    private boolean keyPressed = false;
    private int rx = 10;
    private int ry = 20;

    public ColorBox(final String colorString) {
        this.panel = new FlowPanel();
        this.textbox = new TextBox();
        this.blotch = new Anchor();
        this.popup = new ColorPopup();
        textbox.setText(colorString);
        popup.addAutoHidePartner(blotch.getElement());

        textbox.addFocusHandler(event -> enterEditMode());

        textbox.addKeyPressHandler(event -> {
            keyPressed = true;
            popup.setHex(getValue());
            setStyleAttribute(blotch.getElement(), "backgroundColor", getValue());
        });

        blotch.addMouseDownHandler(event -> {
            if (!popup.isShowing()) {
                enterEditMode();
            } else {

                popup.hide();
            }
        });

        popup.addCloseHandler(event -> {
            if (!keyPressed) {
                setValue(popup.getHex());
                setStyleAttribute(blotch.getElement(), "backgroundColor", popup.getHex());
            } else {
                popup.setHex(getValue());
                setStyleAttribute(blotch.getElement(), "backgroundColor", getValue());
                keyPressed = false;

            }
            popup.clicked = false;
        });

        panel.add(textbox);
        panel.add(blotch);
        initWidget(panel);

        blotch.addStyleName("blotch");
        addStyleName("gwt-ColorBox");
    }

    private static void setStyleAttribute(
            @SuppressWarnings("deprecation") final com.google.gwt.user.client.Element element, final String key,
            final String value) {
        element.getStyle().setProperty(key, value);
    }

    @Override
    public String getName() {
        return textbox.getName();
    }

    @Override
    public void setName(final String name) {
        textbox.setName(name);
    }

    public TextBox getTextBox() {
        return textbox;
    }

    @Override
    public String getValue() {
        return textbox.getValue();
    }

    @Override
    public void setValue(final String value) {
        textbox.setValue(value);
        setStyleAttribute(blotch.getElement(), "backgroundColor", value);
    }

    @Override
    public void setValue(final String value, final boolean fireEvents) {
        textbox.setValue(value, fireEvents);
        setStyleAttribute(blotch.getElement(), "backgroundColor", value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {

        return textbox.addValueChangeHandler(handler);
    }

    @SuppressWarnings("checkstyle:ParameterName")
    public void setRelativeX(final int x) {
        this.rx = x;
    }

    @SuppressWarnings("checkstyle:ParameterName")
    public void setRelativeY(final int y) {
        this.ry = y;
    }

    private void enterEditMode() {
        popup.setHex(getValue());
        popup.setPopupPosition(getAbsoluteLeft() + rx, getAbsoluteTop() + ry);
        popup.show();
    }

    @Override
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        return addHandler(handler, ClickEvent.getType());
    }
}
