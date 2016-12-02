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
package org.datacleaner.windows;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.util.WidgetUtils;

public abstract class AbstractWindow extends JFrame implements DCWindow, WindowListener {

    /**
     * System property (see {@link System#setProperty(String, String)} which can
     * be set to "true" in case windows should not be opened when
     * {@link #open()} is called. This can be useful for eg. testing.
     */
    public static final String SYSTEM_PROPERTY_HIDE_WINDOWS = "DataCleaner.Windows.Hide";

    private static final long serialVersionUID = 1L;
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 300;
    private final WindowContext _windowContext;
    private volatile boolean initialized = false;

    public AbstractWindow(final WindowContext windowContext) {
        _windowContext = windowContext;
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        getContentPane().setBackground(WidgetUtils.BG_COLOR_BRIGHT);
    }

    protected boolean maximizeWindow() {
        return false;
    }

    protected void initialize() {
        updateWindowTitle();
        setIconImage(getWindowIcon());
        setResizable(isWindowResizable());
        final JComponent content = getWindowContent();
        getContentPane().add(content);
        final boolean maximizeWindow = maximizeWindow();
        if (maximizeWindow) {
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
        autoSetSize(content);
        _windowContext.onShow(this);
    }

    public Dimension autoSetSize() {
        return autoSetSize(getContentPane().getComponent(0));
    }

    @Override
    public void open() {
        WidgetUtils.invokeSwingAction(() -> {
            if ("true".equals(System.getProperty(SYSTEM_PROPERTY_HIDE_WINDOWS))) {
                // simulate that the window has opened.
                initialize();
                return;
            }
            setVisible(true);
        });
    }

    @Override
    public void close() {
        if (isVisible()) {
            dispose();
        }
    }

    public Dimension autoSetSize(final Component content) {

        final Dimension preferredSize = content.getPreferredSize();

        final Toolkit toolkit = Toolkit.getDefaultToolkit();

        final int maxWidth = toolkit.getScreenSize().width;
        final int maxHeight = toolkit.getScreenSize().height;
        preferredSize.width = Math.min(preferredSize.width, maxWidth);
        preferredSize.height = Math.min(preferredSize.height, maxHeight);

        if (isVisible()) {
            final Dimension currentSize = getContentPane().getSize();
            preferredSize.width = Math.max(preferredSize.width, currentSize.width);
            preferredSize.width = Math.max(preferredSize.height, currentSize.height);
        }

        getContentPane().setPreferredSize(preferredSize);
        pack();

        if (isCentered()) {
            centerOnScreen();
        }
        return preferredSize;
    }

    protected abstract boolean isWindowResizable();

    protected abstract boolean isCentered();

    @Override
    public final void setVisible(final boolean b) {
        if (b == false) {
            throw new UnsupportedOperationException("Window does not support hiding, consider using dispose()");
        }
        if (!initialized) {
            initialized = true;
            initialize();
        }
        super.setVisible(true);
        onWindowVisible();
    }

    protected void onWindowVisible() {
    }

    protected void updateWindowTitle() {
        String windowTitle = getWindowTitle();
        if (windowTitle == null) {
            windowTitle = "DataCleaner";
        } else {
            if (windowTitle.indexOf("DataCleaner") == -1) {
                windowTitle = windowTitle + " | DataCleaner";
            }
        }
        setTitle(windowTitle);
    }

    protected abstract JComponent getWindowContent();

    public void centerOnScreen() {
        WidgetUtils.centerOnScreen(this);
    }

    @Override
    public void windowOpened(final WindowEvent e) {
    }

    @Override
    public final void windowClosing(final WindowEvent e) {
        final boolean dispose = onWindowClosing();
        if (dispose) {
            dispose();
        }
    }

    @Override
    public void dispose() {
        _windowContext.onDispose(this);
        super.dispose();
    }

    @Override
    public WindowContext getWindowContext() {
        return _windowContext;
    }

    protected boolean onWindowClosing() {
        return true;
    }

    @Override
    public void windowClosed(final WindowEvent e) {
    }

    @Override
    public void windowIconified(final WindowEvent e) {
    }

    @Override
    public void windowDeiconified(final WindowEvent e) {
    }

    @Override
    public void windowActivated(final WindowEvent e) {
    }

    @Override
    public void windowDeactivated(final WindowEvent e) {
    }

    @Override
    public Component toComponent() {
        return this;
    }
}
