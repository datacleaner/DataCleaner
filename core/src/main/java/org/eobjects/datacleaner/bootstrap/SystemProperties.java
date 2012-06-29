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
package org.eobjects.datacleaner.bootstrap;

import org.eobjects.datacleaner.user.DataCleanerHome;
import org.eobjects.datacleaner.user.UserPreferences;

/**
 * Represents commonly referenced system properties which DataCleaner makes use
 * of.
 */
public class SystemProperties {

    /**
     * Property which in case of a "true" value makes the UI visible even with
     * command line parameters.
     */
    public static final String UI_VISIBLE = "datacleaner.ui.visible";

    /**
     * Identifies the name of a client that is embedding datacleaner.
     */
    public static final String EMBED_CLIENT = "datacleaner.embed.client";

    /**
     * Property which in case of a "true" value makes DataCleaner work in
     * "Sandbox" mode which means that it will not assume there's any
     * {@link DataCleanerHome} folder and will not attempt to write any
     * {@link UserPreferences} file etc.
     */
    public static final String SANDBOX = "datacleaner.sandbox";
}
