package org.eobjects.datacleaner.user;

public interface AuthenticationService {

	public boolean auth(String username, char[] password);
}
