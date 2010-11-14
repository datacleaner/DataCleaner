package org.eobjects.datacleaner.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.datacleaner.util.HttpUtils;

/**
 * The default authentication service implementation, that uses the RESTful web
 * services on datacleaner.eobjects.org for authentication.
 * 
 * @author Kasper Sørensen
 */
public class DCAuthenticationService implements AuthenticationService {

	@Override
	public boolean auth(String username, char[] password) {

		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		try {
			String salt = HttpUtils.getUrlContent("http://datacleaner.eobjects.org/ws/get_salt", params);

			if (salt != null && !"not found".equals(salt)) {
				String hashedPassword = Jcrypt.crypt(salt, new String(password));

				params.put("hashed_password", hashedPassword);
				String accepted = HttpUtils.getUrlContent("http://datacleaner.eobjects.org/ws/login", params);

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
