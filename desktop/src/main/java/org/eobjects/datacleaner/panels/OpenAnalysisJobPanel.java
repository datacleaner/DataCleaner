package org.eobjects.datacleaner.panels;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Icon;

import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.DCLabel;

public class OpenAnalysisJobPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Icon icon = ImageManager.get().getImageIcon(IconUtils.MODEL_JOB);

    private static final Dimension PREFERRED_SIZE = new Dimension(278, 70);

    public OpenAnalysisJobPanel() {
        super(WidgetUtils.BG_COLOR_LESS_BRIGHT, WidgetUtils.BG_COLOR_LESS_BRIGHT);
        setLayout(new FlowLayout(Alignment.LEFT.getFlowLayoutAlignment()));
        setBorder(WidgetUtils.BORDER_LIST_ITEM);

        String filename = "My job 1234";
        String description = "This is a dummy description. It's a bit longish maybe?";

        final DCLabel label = DCLabel.dark("<html><b>" + filename + "</b><br/>" + description + "<br/>Datastore: SFDC</html>");
        label.setIconTextGap(10);
        label.setHorizontalAlignment(Alignment.LEFT.getLabelAlignment());
        label.setIcon(icon);
        add(label);
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }
}
