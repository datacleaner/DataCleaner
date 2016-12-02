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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;

public abstract class AbstractDialog extends JDialog implements DCWindow, WindowListener {

    private static final long serialVersionUID = 1L;
    private final WindowContext _windowContext;
    private volatile boolean initialized = false;
    private Image _bannerImage;

    private volatile Color _backgroundColor = WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND;
    private DCBannerPanel _banner;

    public AbstractDialog(final WindowContext windowContext) {
        this(windowContext, null);
    }

    public AbstractDialog(final WindowContext windowContext, final Image bannerImage) {
        this(windowContext, bannerImage, null);
    }

    public AbstractDialog(final WindowContext windowContext, final Image bannerImage, final AbstractWindow owner) {
        super(owner);
        // modal dialogs are turned off because they prevent use of default
        // uncaught exception handlers(!)
        setModal(false);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        setResizable(isWindowResizable());
        _windowContext = windowContext;
        _bannerImage = bannerImage;

        // make dialog itself focusable and add a listener for closing it when
        // ESC is typed.
        setFocusable(true);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EscapeAction");
        getRootPane().getActionMap().put("EscapeAction", createEscapeAction());
    }

    protected Action createEscapeAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                close();
            }
        };
    }

    public Image getBannerImage() {
        return _bannerImage;
    }

    public void setBannerImage(final Image bannerImage) {
        _bannerImage = bannerImage;
        _banner = null;
    }

    protected DCBannerPanel getBanner() {
        if (_banner == null && _bannerImage != null) {
            _banner = createBanner(_bannerImage);
        }
        return _banner;
    }

    protected void setBackgroundColor(final Color backgroundColor) {
        _backgroundColor = backgroundColor;
    }

    @Override
    public void open() {
        setVisible(true);
    }

    @Override
    public void close() {
        if (isVisible()) {
            dispose();
        }
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

    protected void initialize() {
        updateWindowTitle();
        setIconImage(getWindowIcon());
        setResizable(isWindowResizable());

        final JComponent content = getWindowContent();
        getContentPane().removeAll();
        getContentPane().add(content);

        getContentPane().setPreferredSize(content.getPreferredSize());

        pack();

        if (!initialized) {
            WidgetUtils.centerOnScreen(this);
        }

        if (_windowContext != null) {
            _windowContext.onShow(this);
        }
    }

    @Override
    public final void setVisible(final boolean b) {
        if (!b) {
            throw new UnsupportedOperationException("Window does not support hiding, consider using dispose()");
        }
        if (!initialized) {
            initialize();
            initialized = true;
        }
        super.setVisible(true);
    }

    protected boolean isWindowResizable() {
        return false;
    }

    @Override
    public Image getWindowIcon() {
        return ImageManager.get().getImage(IconUtils.APPLICATION_ICON);
    }

    protected final JComponent getWindowContent() {
        final DCPanel panel = new DCPanel(_backgroundColor);
        panel.setLayout(new BorderLayout());

        final int bannerHeight;
        final DCBannerPanel banner = getBanner();
        if (banner == null) {
            bannerHeight = 0;
        } else {
            panel.add(banner, BorderLayout.NORTH);
            bannerHeight = banner.getPreferredSize().height;
        }
        final JComponent dialogContent = getDialogContent();
        panel.add(dialogContent, BorderLayout.CENTER);

        panel.setPreferredSize(getDialogWidth(),
                bannerHeight + dialogContent.getPreferredSize().height + getDialogHeightBuffer());

        setMinimumSize(new Dimension(getDialogWidth(), 300));

        return panel;
    }

    /**
     * Method that can be overridden by subclasses to add "buffer space" to the
     * height of the dialog. This is usually used if the contents of the dialog
     * is expected to grow as the user uses it.
     *
     * @return
     */
    protected int getDialogHeightBuffer() {
        return 0;
    }

    protected DCBannerPanel createBanner(final Image bannerImage) {
        if (bannerImage == null) {
            return null;
        } else {
            final DCBannerPanel bannerPanel = new DCBannerPanel(bannerImage, getBannerTitle());
            // we also apply the key listener of the dialog to the banner since
            // it is the only visible item on the dialog to gain focus after the
            // initial focus has been lost.
            bannerPanel.setFocusable(true);
            return bannerPanel;
        }
    }

    protected abstract String getBannerTitle();

    protected abstract int getDialogWidth();

    protected abstract JComponent getDialogContent();

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
        if (_windowContext != null) {
            _windowContext.onDispose(this);
        }
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
