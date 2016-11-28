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
package org.datacleaner.widgets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.datacleaner.Version;
import org.datacleaner.panels.RightInformationPanel;
import org.junit.Before;
import org.junit.Test;

public class CommunityEditionStatusLabelTest {
    CommunityEditionStatusLabel _label;

    @Before
    public void setup() {
        final RightInformationPanel rightInformationPanel = mock(RightInformationPanel.class);

        _label = new CommunityEditionStatusLabel(rightInformationPanel);
    }

    @Test
    public void testLabelTitle() {
        assertEquals("Should always show edition", _label.getText(), Version.getEdition());
    }
}
