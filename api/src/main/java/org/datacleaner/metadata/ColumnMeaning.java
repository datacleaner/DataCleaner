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
package org.datacleaner.metadata;

/**
 * A common and general enum of 'meanings of columns/fields' in a table. This
 * type is available as a convenience for mapping and registering metadata.
 */
public enum ColumnMeaning implements HasColumnMeaning {

    // generic items

    OTHER("Other", "Disregard", "Nothing"),

    KEY_PRIMARY("Primary Key", "ID", "Record ID", "Identifier", "key", "PKID", "Record key"),

    KEY_FOREIGN("Foreign Key", "FK"),

    // PERSON oriented items

    PERSON_NAME_GIVEN("First name", "Given name", "Forename", "Christian name", "1st name", "Name one",
            "Contact first name", "Contact given name"),

    PERSON_NAME_FAMILY("Last name", "Family name", "Surname", "2nd name", "Name two", "Contact last name",
            "Contact family name"),

    PERSON_NAME_FULL("Full name", "Name", "Person name", "Contact", "Contact name"),

    PERSON_INITIALS("Initials", "Name initials", "Person name initials"),

    PERSON_TITLE("Person title", "Title", "Salutation"),

    PERSON_GENDER("Gender", "Sex"),

    PERSON_AGE("Age", "Customer age", "Contact age"),

    PERSON_BIRTHDATE("Birthdate", "Date of birth", "DoB"),

    PERSON_REGISTRATION_NUMBER("Social security number", "Person number", "Person Identifier"),

    PERSON_JOB_TITLE("Job title", "Person job title", "Employee title", "Employment role"),

    // COMPANY oriented items

    COMPANY_REGISTRATION_NUMBER("Company registration number", "Reg number"),

    COMPANY_NAME("Company name", "Company", "Organization", "Business", "Organisation name", "Employer", "Firm",
            "Workplace", "Works at", "Vendor", "Vendor name", "Supplier", "Supplier name", "Customer name"),

    // PRODUCT oriented items

    PRODUCT_CODE("Product code", "Product ID"),

    PRODUCT_NAME("Product name", "Product"),

    PRODUCT_LINE("Product line", "Product type", "Product area"),

    PRODUCT_QUANTITY("Product quantity", "Quantity", "Product amount", "Quantity in stock", "Quantity ordered",
            "Quantity bought"),

    // MONEY oriented items

    MONEY_AMOUNT("Money amount", "Amount", "Price", "Cost", "Credit", "Buy price", "Payment amount", "Total price",
            "Unit price", "Price each", "Income amount"),

    MONEY_CURRENCY("Money currency", "Currency", "Valuta", "Exchange", "Income currency", "Payment currency",
            "Price currency"),

    // ADDRESS oriented items

    ADDRESS_LINE("Addressline", "Line", "Address"),

    ADDRESS_COUNTRY("Country", "Country code", "Country name", "Land", "Country ISO", "Nation", "Cty", "Nationality"),

    ADDRESS_STATE("State", "State code", "Province", "Postal state"),

    ADDRESS_COUNTY("Region/County", "County name", "County", "County code", "Region"),

    ADDRESS_CITY("City", "Town", "Postal town", "Post town", "Postal city"),

    ADDRESS_POSTAL_CODE("Postal code", "Zip", "Zip code", "Post code", "Post number"),

    ADDRESS_STREET("Street", "Street name", "Thoroughfare", "Road", "Avenue", "Str", "Way"),

    ADDRESS_HOUSE_NUMBER("House number", "House no", "Hausnummer", "huisnummer", "nummer", "Husnummer"),

    ADDRESS_APARTMENT("Apartment/Suite number", "Apartment", "Suite", "Unit", "Floor", "Etage", "Ste", "Lejlighed"),

    // PHONE oriented items

    PHONE_PHONENUMBER("Phone number", "Phone", "Phone no", "Telefon", "Tel", "Tele", "Landline"),

    PHONE_MOBILE("Mobile phone", "Mobile", "Cellphone", "Mobil", "Mob"),

    PHONE_FAX("Fax number", "Fax", "Faxnummer"),

    // EMAIL oriented items

    EMAIL_ADDRESS("Email", "Mail", "Email address", "Mail address", "@"),

    // ONLINE oriented items

    ONLINE_WEBSITE("Website", "Website URL", "url", "www", "Homepage", "Homepage URL", "Blog URL", "Blog", "Web"),

    ONLINE_TWITTER("Twitter ID", "Twitter", "Twitter account"),

    ONLINE_FACEBOOK("Facebook ID", "Facebook", "Facebook account"),

    ONLINE_LINKEDIN("LinkedIn ID", "LinkedIn", "LinkedIn account"),;

    private final String _name;
    private final String[] _aliases;

    // Used for all the fields that are also available for input
    ColumnMeaning(final String name, final String... aliases) {
        _name = name;
        if (aliases == null) {
            _aliases = new String[0];
        } else {
            _aliases = aliases;
        }
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String[] getAliases() {
        return _aliases;
    }

    @Override
    public String toString() {
        return getName();
    }
}
