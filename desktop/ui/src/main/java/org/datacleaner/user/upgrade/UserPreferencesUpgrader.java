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
package org.datacleaner.user.upgrade;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;

/**
 * Object responsible for upgrading {@link UserPreferencesImpl} if found during
 * {@link DataCleanerHomeUpgrader} routines.
 */
class UserPreferencesUpgrader {

    private final FileObject _newFolder;

    public UserPreferencesUpgrader(FileObject newFolder) {
        _newFolder = newFolder;
    }

    public void upgrade() throws FileSystemException {
        // initially the new folder might have a copy of the old
        // userpreferences.dat file - we don't want that, rather we create a new
        // one.
        final FileObject existingFile = _newFolder.getChild(UserPreferencesImpl.DEFAULT_FILENAME);

        // if there is no new userpreferences.dat file, then there's nothing to
        // upgrade
        if (existingFile == null) {
            return;
        }

        final UserPreferences oldUserPreferences = UserPreferencesImpl.load(existingFile, false);

        final UserPreferencesImpl newUserPreferences = new UserPreferencesImpl(existingFile);

        // copy the relevant parts of the user prefs
        newUserPreferences.getAdditionalProperties().putAll(oldUserPreferences.getAdditionalProperties());
        newUserPreferences.setMonitorConnection(oldUserPreferences.getMonitorConnection());
        newUserPreferences.setProxyAuthenticationEnabled(oldUserPreferences.isProxyAuthenticationEnabled());
        newUserPreferences.setProxyEnabled(oldUserPreferences.isProxyEnabled());
        newUserPreferences.setProxyHostname(oldUserPreferences.getProxyHostname());
        newUserPreferences.setProxyPort(oldUserPreferences.getProxyPort());
        newUserPreferences.setProxyUsername(oldUserPreferences.getProxyUsername());
        newUserPreferences.setProxyPassword(oldUserPreferences.getProxyPassword());
        newUserPreferences.getUserDatastores().addAll(oldUserPreferences.getUserDatastores());
        newUserPreferences.getUserDictionaries().addAll(oldUserPreferences.getUserDictionaries());
        newUserPreferences.getUserStringPatterns().addAll(oldUserPreferences.getUserStringPatterns());
        newUserPreferences.getUserSynonymCatalogs().addAll(oldUserPreferences.getUserSynonymCatalogs());

        newUserPreferences.save();
    }

}
