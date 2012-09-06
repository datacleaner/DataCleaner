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
package org.eobjects.datacleaner.monitor.shared.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

public class ColorBox extends Composite
        implements HasValue<String>, HasName {

    
    
    public interface Images extends ClientBundle {
        ImageResource lightness();
        ImageResource hueSaturation();
    }

    private static final Images IMAGES = GWT.create(Images.class);

    private class ColorPopup extends PopupPanel {

        private FlowPanel panel;
        private Image hueSaturation;
        private Image lightness;
        private Label preview;
        private boolean down = false;
        private boolean clicked = false;
        float h = 200;
        float s = 2 / 3f;
        float l = 1 / 3f;

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

            DOM.setStyleAttribute(hueSaturation.getElement(), "cursor", "crosshair");
            DOM.setStyleAttribute(lightness.getElement(), "cursor", "ns-resize");
            DOM.setStyleAttribute(preview.getElement(), "float", "right");
            DOM.setStyleAttribute(preview.getElement(), "cssFloat", "right");
            DOM.setStyleAttribute(preview.getElement(), "styleFloat", "right");

            setColor();

            hueSaturation.addMouseDownHandler(new MouseDownHandler() {

                @Override
                public void onMouseDown(MouseDownEvent event) {
                    event.preventDefault();
                    setHueSaturation(event.getNativeEvent());
                    down = true;
                }
            });

            hueSaturation.addMouseUpHandler(new MouseUpHandler() {

                @Override
                public void onMouseUp(MouseUpEvent event) {
                    setHueSaturation(event.getNativeEvent());
                    down = false;
                }
            });

            hueSaturation.addMouseMoveHandler(new MouseMoveHandler() {

                @Override
                public void onMouseMove(MouseMoveEvent event) {
                    if (down)
                        setHueSaturation(event.getNativeEvent());
                }
            });

            hueSaturation.addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    down = false;
                }
            });

            /* --- */

            lightness.addMouseDownHandler(new MouseDownHandler() {

                @Override
                public void onMouseDown(MouseDownEvent event) {
                    event.preventDefault();
                    setLightness(event.getNativeEvent());
                    down = true;
                }
            });

            lightness.addMouseUpHandler(new MouseUpHandler() {

                @Override
                public void onMouseUp(MouseUpEvent event) {
                    setLightness(event.getNativeEvent());
                    down = false;
                }
            });

            lightness.addMouseMoveHandler(new MouseMoveHandler() {

                @Override
                public void onMouseMove(MouseMoveEvent event) {
                    if (down)
                        setLightness(event.getNativeEvent());
                }
            });

            lightness.addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    down = false;
                }
            });

            /* --- */

            preview.addMouseDownHandler(new MouseDownHandler() {

                @Override
                public void onMouseDown(MouseDownEvent event) {
                    clicked = false;
                    hide();
                }
            });
        }

        public String getHex() {
            return new Color(h, s, l).toString();
        }

        public void setHex(String colorString) {
            if (colorString.startsWith("#") && colorString.length() == 7 && clicked) {
                Color rgb = new Color(colorString);
                h = rgb.getHue();
                s = rgb.getSaturation();
                l = rgb.getLightness();
                setColor();
            }
        }

        private void setColor() {
            Color p = new Color(h, s, l);
            DOM.setStyleAttribute(preview.getElement(), "backgroundColor", p.toString());
            Color l = new Color(h, s, 0.5f);
            DOM.setStyleAttribute(lightness.getElement(), "backgroundColor", l.toString());

            DOM.setStyleAttribute(blotch.getElement(), "backgroundColor", getHex());
        }

        private void setHueSaturation(NativeEvent event) {
            clicked = true;
            int x = event.getClientX() - hueSaturation.getAbsoluteLeft();
            int y = event.getClientY() - hueSaturation.getAbsoluteTop();

            if (x > -1 && x < 181 && y > -1 && y < 101) {
                h = x * 2;
                s = (float) (100 - y) / 100f;

                setColor();
            } else {
                down = false;
            }
        }

        private void setLightness(NativeEvent event) {
            clicked = true;
            int y = event.getClientY() - lightness.getAbsoluteTop();

            if (y > -1 && y < 101) {
                l = (float) (100 - y) / 100f;
                setColor();
            } else {
                down = false;
            }
        }
    }

    private ColorPopup popup;
    private TextBox textbox;
    private Anchor blotch;
    private FlowPanel panel;
    private boolean keyPressed = false;
    private int rx = 10,
                ry = 20;

    public ColorBox(String colorString) {
        this.panel = new FlowPanel();
        this.textbox = new TextBox();
        this.blotch = new Anchor();
        this.popup = new ColorPopup();
        textbox.setText(colorString);
        popup.addAutoHidePartner(blotch.getElement());

        textbox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                enterEditMode();
            }
        });

        textbox.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                keyPressed = true;
                popup.setHex(getValue());
                DOM.setStyleAttribute(blotch.getElement(), "backgroundColor", getValue());
            }
        });

        blotch.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (!popup.isShowing()) {
                    enterEditMode();
                } else {
                    
                    popup.hide();
                }
            }
        });

        popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (!keyPressed) {
                    setValue(popup.getHex());
                    DOM.setStyleAttribute(blotch.getElement(), "backgroundColor", popup.getHex());
                } else {
                    popup.setHex(getValue());
                    DOM.setStyleAttribute(blotch.getElement(), "backgroundColor", getValue());
                    keyPressed = false;
                    
                }
                popup.clicked = false;
            }
        });

        panel.add(textbox);
        panel.add(blotch);
        initWidget(panel);

        blotch.addStyleName("blotch");
        addStyleName("gwt-ColorBox");
    }

    @Override
    public String getName() {
        return textbox.getName();
    }

    @Override
    public void setName(String name) {
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
    public void setValue(String value) {
        textbox.setValue(value);
        DOM.setStyleAttribute(blotch.getElement(), "backgroundColor", value);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        textbox.setValue(value, fireEvents);
        DOM.setStyleAttribute(blotch.getElement(), "backgroundColor", value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<String> handler) {

        return textbox.addValueChangeHandler(handler);
    }

    public void setRelativeX(int x) {
        this.rx = x;
    }

    public void setRelativeY(int y) {
        this.ry = y;
    }

    private void enterEditMode() {
        popup.setHex(getValue());
        popup.setPopupPosition(getAbsoluteLeft() + rx, getAbsoluteTop() + ry);
        popup.show();
    }

}
