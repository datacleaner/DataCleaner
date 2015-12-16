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
package org.datacleaner.documentation.swagger;

/**
 * @since 23. 09. 2015
 */
public class SwaggerInfo {
    private String title = "DataCleaner REST API";
    private String description = title;
    private String version = "1.0";
    private String termsOfService = "http://www.gnu.org/licenses/lgpl-3.0.html";
    private SwaggerContact contact = new SwaggerContact();
    private SwaggerLicense license = new SwaggerLicense();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTermsOfService() {
        return termsOfService;
    }

    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    public SwaggerContact getContact() {
        return contact;
    }

    public void setContact(SwaggerContact contact) {
        this.contact = contact;
    }

    public SwaggerLicense getLicense() {
        return license;
    }

    public void setLicense(SwaggerLicense license) {
        this.license = license;
    }
}
