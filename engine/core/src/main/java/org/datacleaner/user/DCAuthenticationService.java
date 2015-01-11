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
package org.datacleaner.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.datacleaner.util.HttpXmlUtils;

/**
 * The default authentication service implementation, that uses the RESTful web
 * services on datacleaner.org for authentication.
 */
public class DCAuthenticationService implements AuthenticationService {

	private final HttpXmlUtils _httpXmlUtils;

	@Inject
	public DCAuthenticationService(HttpXmlUtils httpXmlUtils) {
		_httpXmlUtils = httpXmlUtils;
	}

	@Override
	public boolean auth(String username, char[] password) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		try {
			String salt = _httpXmlUtils.getUrlContent("http://datacleaner.org/ws/get_salt", params);

			if (salt != null && !"not found".equals(salt)) {
				String hashedPassword = Jcrypt.crypt(salt, new String(password));

				params.put("hashed_password", hashedPassword);
				String accepted = _httpXmlUtils.getUrlContent("http://datacleaner.org/ws/login", params);

				if ("true".equals(accepted)) {
					return true;
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return false;
	}
}
