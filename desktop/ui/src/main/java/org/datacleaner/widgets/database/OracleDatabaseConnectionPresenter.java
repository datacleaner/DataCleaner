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
package org.datacleaner.widgets.database;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.NamedPattern;
import org.datacleaner.util.NamedPatternMatch;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextField;

/**
 * {@link DatabaseConnectionPresenter} for Oracle database connections.
 * 
 * Some special formatting conventions used in the URL:
 * 
 * DATABASE = Service name
 * 
 * PARAM1 = SID
 * 
 * PARAM2 = Server name
 * 
 * PARAM3 = Instance name
 */
public class OracleDatabaseConnectionPresenter extends UrlTemplateDatabaseConnectionPresenter {

    private static final String URL_FALLBACK = "jdbc:oracle:thin:@HOSTNAME:PORT";
    private static final String URL_SERVICE_NAME_BASED = "jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE";
    private static final String URL_SID_BASED = "jdbc:oracle:thin:@HOSTNAME:PORT:PARAM1";

    private final JRadioButton _radioSid;
    private final JRadioButton _radioServiceName;

    public OracleDatabaseConnectionPresenter() {
        super(URL_FALLBACK, URL_SERVICE_NAME_BASED, URL_SID_BASED);

        _radioServiceName = new JRadioButton("Connect with Service name:", true);
        _radioSid = new JRadioButton("Connect with SID:");

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(_radioServiceName);
        buttonGroup.add(_radioSid);

        final ActionListener radioActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTextFieldsAfterRadioButtonChange();
            }
        };
        
        updateTextFieldsAfterRadioButtonChange();
        
        _radioServiceName.addActionListener(radioActionListener);
        _radioSid.addActionListener(radioActionListener);
    }

    protected void updateTextFieldsAfterRadioButtonChange() {
        final JXTextField sidTextField = getParam1TextField();
        final JXTextField databaseTextField = getDatabaseTextField();

        final boolean sidMode = _radioSid.isSelected();
        sidTextField.setEnabled(sidMode);
        databaseTextField.setEnabled(!sidMode);

        sidTextField.setPrompt(sidMode ? getLabelForParam1() : "");
        databaseTextField.setPrompt(sidMode ? "" : getLabelForDatabase());
    }

    @Override
    protected String getJdbcUrl(String hostname, int port, String database, String param1, String param2,
            String param3, String param4) {
        if (_radioSid.isSelected()) {
            return replaceParameters(URL_SID_BASED, hostname, port, database, param1, param2, param3);
        } else if (_radioServiceName.isSelected()) {
            if (!StringUtils.isNullOrEmpty(database)) {
                return replaceParameters(URL_SERVICE_NAME_BASED, hostname, port, database, param1, param2, param3);
            }
        }
        return replaceParameters(URL_FALLBACK, hostname, port, database, param1, param2, param3);
    }

    private String replaceParameters(String url, String hostname, int port, String database, String param1,
            String param2, String param3) {
        url = url.replace("HOSTNAME", hostname);
        url = url.replace("PORT", Integer.toString(port));
        url = url.replace("DATABASE", database);
        url = url.replace("PARAM1", param1);
        return url;
    }

    @Override
    public boolean initialize(JdbcDatastore datastore) {
        final boolean initialize = super.initialize(datastore);
        updateTextFieldsAfterRadioButtonChange();
        return initialize;
    }

    @Override
    protected boolean initializeFromMatch(JdbcDatastore datastore, NamedPattern<UrlPart> namedPattern,
            NamedPatternMatch<UrlPart> match) {
        final String pattern = namedPattern.toString();
        switch (pattern) {
        case URL_SID_BASED:
            _radioSid.doClick();
            break;
        case URL_FALLBACK:
        case URL_SERVICE_NAME_BASED:
        default:
            _radioServiceName.doClick();
            break;
        }

        return super.initializeFromMatch(datastore, namedPattern, match);
    }

    @Override
    protected void layoutGridBagBelowCredentials(DCPanel panel, int row) {

        row++;
        WidgetUtils.addToGridBag(_radioServiceName, panel, 0, row);

        super.layoutGridBagBelowCredentials(panel, row);
    }

    @Override
    protected int layoutGridBagParams(DCPanel panel, int row) {
        row++;
        WidgetUtils.addToGridBag(_radioSid, panel, 0, row);
        return super.layoutGridBagParams(panel, row);
    }

    @Override
    protected int getDefaultPort() {
        return 1521;
    }

    @Override
    protected boolean showDatabaseAboveCredentials() {
        return false;
    }

    @Override
    protected String getLabelForDatabase() {
        return "Service name";
    }

    @Override
    protected String getLabelForParam1() {
        return "System ID (SID)";
    }
}
