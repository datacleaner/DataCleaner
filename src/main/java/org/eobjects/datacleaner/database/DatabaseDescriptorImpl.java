package org.eobjects.datacleaner.database;

final class DatabaseDescriptorImpl implements DatabaseDriverDescriptor {

	private static final long serialVersionUID = 1L;
	private final String _displayName;
	private final String _iconImagePath;
	private final String _driverClassName;
	private final String[] _downloadUrls;
	private final String[] _connectionUrlTemplates;

	public DatabaseDescriptorImpl(String displayName, String iconImagePath, String driverClassName, String[] downloadUrls,
			String[] connectionUrlTemplates) {
		_displayName = displayName;
		_iconImagePath = iconImagePath;
		_driverClassName = driverClassName;
		_downloadUrls = downloadUrls;
		_connectionUrlTemplates = connectionUrlTemplates;
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	@Override
	public String getIconImagePath() {
		return _iconImagePath;
	}

	@Override
	public String getDriverClassName() {
		return _driverClassName;
	}

	@Override
	public String[] getConnectionUrlTemplates() {
		return _connectionUrlTemplates;
	}

	@Override
	public String[] getDownloadUrls() {
		return _downloadUrls;
	}

	@Override
	public int compareTo(DatabaseDriverDescriptor o) {
		if (this.equals(o)) {
			return 0;
		}
		int result = getDisplayName().compareTo(o.getDisplayName());
		if (result == 0) {
			result = getDriverClassName().compareTo(o.getDriverClassName());
			if (result == 0) {
				result = -1;
			}
		}
		return result;
	}
}
