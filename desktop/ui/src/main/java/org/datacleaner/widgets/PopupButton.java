package org.datacleaner.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;

public class PopupButton extends JToggleButton {  
    private static final long serialVersionUID = 1L;
    private final JPopupMenu popupMenu = new JPopupMenu();
    
    public PopupButton(String text){
        this(text, null);
    }
    
    public PopupButton(String text, Icon icon){
        super(text, icon);

        
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSelected())
                    popupMenu.show(PopupButton.this, 0, getHeight());
            }
        });

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent pme) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
                if (UIManager.getBoolean("PopupMenu.consumeEventOnClose")) {
                    setSelected(false);
                } else {
                    Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
                    Point componentLoc = getLocationOnScreen();
                    mouseLoc.x -= componentLoc.x;
                    mouseLoc.y -= componentLoc.y;
                    if (!contains(mouseLoc)) {
                        setSelected(false);
                    }
                }
            }
        });        
    }
    
    public JPopupMenu getMenu(){
        return popupMenu;
    }
    
    public static void main(String[] args) {
        LookAndFeelManager.get().init();
        PopupButton popupButton = new PopupButton("More",  ImageManager.get().getImageIcon("images/menu/more.png"));
        JPanel toolBarPanel = new JPanel();
        toolBarPanel.add(popupButton);
        JToolBar toolBar = new JToolBar();
        toolBar.add(toolBarPanel);

        JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BorderLayout());
        popupPanel.add(new JLabel("This popup has components"), BorderLayout.NORTH);
        popupPanel.add(new JTextArea("Some text", 15, 20), BorderLayout.CENTER);
        popupPanel.add(new JSlider(), BorderLayout.SOUTH);
        popupButton.getMenu().add(popupPanel);
        
        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(640, 480));
        frame.add(toolBar, BorderLayout.PAGE_START);

        frame.pack();
        frame.setVisible(true);
    }
}
