package org.datacleaner.panels;

import javax.swing.border.EmptyBorder;

import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.jdesktop.swingx.VerticalLayout;

public class ExistingDatastorePanel extends DCPanel{
    private static final long serialVersionUID = 1L;

    public ExistingDatastorePanel() {
        setLayout(new VerticalLayout(4));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        ImageManager imageManager = ImageManager.get();
        
        final WelcomeQuestionPanel file1 = new WelcomeQuestionPanel(imageManager.getImageIcon(IconUtils.CSV_IMAGEPATH, IconUtils.ICON_SIZE_LARGE),
                "<html><b>Contact data</b></html>",
                "Example text-file with data to be cleansed");
        add(file1);
        final WelcomeQuestionPanel file2 = new WelcomeQuestionPanel(imageManager.getImageIcon(IconUtils.SALESFORCE_IMAGEPATH, IconUtils.ICON_SIZE_LARGE),
                "<html><b>Salesforce.com</b></html>",
                "Example connection to SFDC - credentials not provided");
        add(file2);
        final WelcomeQuestionPanel file3 = new WelcomeQuestionPanel(imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH, IconUtils.ICON_SIZE_LARGE),
                "<html><b>orderdb</b></html>",
                "Example database for use with DataCleaner");
        add(file3);
    }
}
