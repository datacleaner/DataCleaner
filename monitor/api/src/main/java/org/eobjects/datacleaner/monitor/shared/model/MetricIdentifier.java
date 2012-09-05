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
package org.eobjects.datacleaner.monitor.shared.model;

import java.io.Serializable;

/**
 * Identifies a metric of interest.
 */
public class MetricIdentifier implements Serializable, Comparable<MetricIdentifier> {

    private static final long serialVersionUID = 1L;

    private String _analyzerDescriptorName;
    private String _analyzerName;
    private String _analyzerInputName;
    private String _metricDescriptorName;
    private String _metricDisplayName;
    private String _paramQueryString;
    private String _paramColumnName;
    private boolean _parameterizedByQueryString;
    private boolean _parameterizedByColumnName;

    // full constructor that specifies every field in one go.
    public MetricIdentifier(String metricDisplayName, String analyzerDescriptorName, String analyzerName,
            String analyzerInputName, String metricDescriptorName, String paramQueryString, String paramColumnName,
            boolean parameterizedByQueryString, boolean parameterizedByColumnName) {
        _metricDisplayName = metricDisplayName;
        _analyzerDescriptorName = analyzerDescriptorName;
        _analyzerName = analyzerName;
        _analyzerInputName = analyzerInputName;
        _metricDescriptorName = metricDescriptorName;
        _paramQueryString = paramQueryString;
        _paramColumnName = paramColumnName;
        _parameterizedByQueryString = parameterizedByQueryString;
        _parameterizedByColumnName = parameterizedByColumnName;
    }

    // no-arg constructor used by GWT
    public MetricIdentifier() {
    }

    public String getAnalyzerDescriptorName() {
        return _analyzerDescriptorName;
    }

    public void setAnalyzerDescriptorName(String analyzerDescriptorName) {
        _analyzerDescriptorName = analyzerDescriptorName;
    }

    public String getAnalyzerName() {
        return _analyzerName;
    }

    public void setAnalyzerName(String analyzerName) {
        _analyzerName = analyzerName;
    }

    public String getMetricDescriptorName() {
        return _metricDescriptorName;
    }

    public void setMetricDescriptorName(String metricDescriptorName) {
        _metricDescriptorName = metricDescriptorName;
    }

    public String getAnalyzerInputName() {
        return _analyzerInputName;
    }

    public void setAnalyzerInputName(String analyzerInputName) {
        _analyzerInputName = analyzerInputName;
    }

    public void setParamColumnName(String paramColumnName) {
        _paramColumnName = paramColumnName;
    }

    public String getParamColumnName() {
        return _paramColumnName;
    }

    public void setParamQueryString(String paramQueryString) {
        _paramQueryString = paramQueryString;
    }

    public String getParamQueryString() {
        return _paramQueryString;
    }

    public String getId() {
        String ID = _analyzerDescriptorName + _analyzerInputName + _analyzerName + _metricDescriptorName
                + _paramColumnName + _paramQueryString;
        return ID.replaceAll("'", "");
    }

    public boolean isParameterizedByColumnName() {
        return _parameterizedByColumnName;
    }

    public void setParameterizedByColumnName(boolean parameterizedByColumnName) {
        _parameterizedByColumnName = parameterizedByColumnName;
    }

    public boolean isParameterizedByQueryString() {
        return _parameterizedByQueryString;
    }

    public void setParameterizedByQueryString(boolean parameterizedByQueryString) {
        _parameterizedByQueryString = parameterizedByQueryString;
    }

    /**
     * Creates a copy of this {@link MetricIdentifier}.
     * 
     * @return
     */
    public MetricIdentifier copy() {
        final MetricIdentifier metricIdentifier = new MetricIdentifier();
        metricIdentifier.setAnalyzerDescriptorName(_analyzerDescriptorName);
        metricIdentifier.setAnalyzerInputName(_analyzerInputName);
        metricIdentifier.setAnalyzerName(_analyzerName);
        metricIdentifier.setMetricDescriptorName(_metricDescriptorName);
        metricIdentifier.setMetricDisplayName(_metricDisplayName);
        metricIdentifier.setParamColumnName(_paramColumnName);
        metricIdentifier.setParameterizedByColumnName(_parameterizedByColumnName);
        metricIdentifier.setParameterizedByQueryString(_parameterizedByQueryString);
        metricIdentifier.setParamQueryString(_paramQueryString);
        return metricIdentifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_analyzerDescriptorName == null) ? 0 : _analyzerDescriptorName.hashCode());
        result = prime * result + ((_analyzerInputName == null) ? 0 : _analyzerInputName.hashCode());
        result = prime * result + ((_analyzerName == null) ? 0 : _analyzerName.hashCode());
        result = prime * result + ((_metricDescriptorName == null) ? 0 : _metricDescriptorName.hashCode());
        result = prime * result + ((_paramColumnName == null) ? 0 : _paramColumnName.hashCode());
        result = prime * result + ((_paramQueryString == null) ? 0 : _paramQueryString.hashCode());
        result = prime * result + (_parameterizedByColumnName ? 1231 : 1237);
        result = prime * result + (_parameterizedByQueryString ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetricIdentifier other = (MetricIdentifier) obj;
        if (_analyzerDescriptorName == null) {
            if (other._analyzerDescriptorName != null)
                return false;
        } else if (!_analyzerDescriptorName.equals(other._analyzerDescriptorName))
            return false;
        if (_analyzerInputName == null) {
            if (other._analyzerInputName != null)
                return false;
        } else if (!_analyzerInputName.equals(other._analyzerInputName))
            return false;
        if (_analyzerName == null) {
            if (other._analyzerName != null)
                return false;
        } else if (!_analyzerName.equals(other._analyzerName))
            return false;
        if (_metricDescriptorName == null) {
            if (other._metricDescriptorName != null)
                return false;
        } else if (!_metricDescriptorName.equals(other._metricDescriptorName))
            return false;
        if (_paramColumnName == null) {
            if (other._paramColumnName != null)
                return false;
        } else if (!_paramColumnName.equals(other._paramColumnName))
            return false;
        if (_paramQueryString == null) {
            if (other._paramQueryString != null)
                return false;
        } else if (!_paramQueryString.equals(other._paramQueryString))
            return false;
        if (_parameterizedByColumnName != other._parameterizedByColumnName)
            return false;
        if (_parameterizedByQueryString != other._parameterizedByQueryString)
            return false;
        return true;
    }

    public boolean equalsIgnoreParameterValues(MetricIdentifier other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (_analyzerDescriptorName == null) {
            if (other._analyzerDescriptorName != null)
                return false;
        } else if (!_analyzerDescriptorName.equals(other._analyzerDescriptorName))
            return false;
        if (_analyzerInputName == null) {
            if (other._analyzerInputName != null)
                return false;
        } else if (!_analyzerInputName.equals(other._analyzerInputName))
            return false;
        if (_analyzerName == null) {
            if (other._analyzerName != null)
                return false;
        } else if (!_analyzerName.equals(other._analyzerName))
            return false;
        if (_metricDescriptorName == null) {
            if (other._metricDescriptorName != null)
                return false;
        } else if (!_metricDescriptorName.equals(other._metricDescriptorName))
            return false;
        if (_parameterizedByColumnName != other._parameterizedByColumnName)
            return false;
        if (_parameterizedByQueryString != other._parameterizedByQueryString)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MetricIdentifier[analyzerInputName=" + _analyzerInputName + ",metricDescriptorName="
                + _metricDescriptorName + (_paramColumnName != null ? ",paramColumnName=" + _paramColumnName : "")
                + (_paramQueryString != null ? ",paramQueryString=" + _paramQueryString : "") + "]";
    }

    public void setMetricDisplayName(String _metricDisplayName) {
        this._metricDisplayName = _metricDisplayName;
    }

    public String getDisplayName() {
        if (_metricDisplayName == null || "".equals(_metricDisplayName)) {
            if (_paramColumnName != null) {
                return _metricDescriptorName + " (" + _paramColumnName + ")";
            }
            if (_paramQueryString != null) {
                return _metricDescriptorName + ": " + _paramQueryString;
            }
            if (_analyzerInputName != null) {
                return _metricDescriptorName + " (" + _analyzerInputName + ")";
            }
            return _metricDescriptorName;
        }
        return _metricDisplayName;
    }

    @Override
    public int compareTo(MetricIdentifier other) {
        if (this == other) {
            return 0;
        }
        return getId().compareTo(other.getId());
    }

}
