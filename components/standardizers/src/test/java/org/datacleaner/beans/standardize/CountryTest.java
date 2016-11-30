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
package org.datacleaner.beans.standardize;

import junit.framework.TestCase;

public class CountryTest extends TestCase {

    public void testFindNonResolveableNames() throws Exception {
        assertNull(Country.find(""));
        assertNull(Country.find("United"));
        assertNull(Country.find("States"));
        assertNull(Country.find("Foo bar"));
        assertNull(Country.find(null));
        assertNull(Country.find("    "));
        assertNull(Country.find("n/a"));
    }

    public void testFindDenmark() throws Exception {
        assertEquals(Country.DENMARK, Country.find("DK"));
        assertEquals(Country.DENMARK, Country.find("DNK"));
        assertEquals(Country.DENMARK, Country.find("Dk"));
        assertEquals(Country.DENMARK, Country.find("dk"));
        assertEquals(Country.DENMARK, Country.find("dNK"));
        assertEquals(Country.DENMARK, Country.find("Danmark"));
        assertEquals(Country.DENMARK, Country.find("Denmark"));
        assertEquals(Country.DENMARK, Country.find("Dänemark"));
        assertEquals(Country.DENMARK, Country.find(".dk"));
    }

    public void testFindIreland() throws Exception {
        assertEquals(Country.IRELAND, Country.find("IE"));
        assertEquals(Country.IRELAND, Country.find("Ireland"));
        assertEquals(Country.IRELAND, Country.find("IRL"));
        assertEquals(Country.IRELAND, Country.find("Republic of Ireland"));
        assertEquals(Country.IRELAND, Country.find("Ire land"));
        assertEquals(Country.IRELAND, Country.find("Eir"));
        assertEquals(Country.IRELAND, Country.find("Ire"));
        assertEquals(Country.IRELAND, Country.find("Eire"));
        assertEquals(Country.IRELAND, Country.find("Éire"));
        assertEquals(Country.IRELAND, Country.find("Airlann"));
        assertEquals(Country.IRELAND, Country.find(".ie"));
    }

    public void testFindBelgium() throws Exception {
        assertEquals(Country.BELGIUM, Country.find("Belgium"));
        assertEquals(Country.BELGIUM, Country.find("BE"));
        assertEquals(Country.BELGIUM, Country.find("Belgie"));
        assertEquals(Country.BELGIUM, Country.find("BELGIË"));
    }

    public void testFindGreatBritain() throws Exception {
        assertEquals(Country.UNITED_KINGDOM, Country.find("GB"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("UK"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("United Kingdom"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("GB"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("GBR"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("Scotland"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("Wales"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("England"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("Northern Ireland"));
        assertEquals(Country.UNITED_KINGDOM, Country.find(".uk"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("The United Kingdom"));
        assertEquals(Country.UNITED_KINGDOM, Country.find("Northern Ireland"));
    }

    public void testFindKongoKinshasa() throws Exception {
        assertEquals(Country.CONGO_KINSHASA, Country.find("Congo (the Democratic Republic of the)"));
        assertEquals(Country.CONGO_KINSHASA, Country.find("Congo, Democratic Republic of the"));

    }

    public void testFindCongoBrazzaville() throws Exception {
        assertEquals(Country.CONGO_BRAZZAVILLE, Country.find("Congo, Republic of the"));
    }

    public void testFindCountriesWithTheInNames() throws Exception {
        assertEquals(Country.GAMBIA, Country.find("Gambia, The"));
        assertEquals(Country.CAYMAN_ISLANDS, Country.find("Cayman Islands, The"));
        assertEquals(Country.BAHAMAS, Country.find("Bahamas, The"));
        assertEquals(Country.NETHERLANDS, Country.find("Netherlands"));
    }

    public void testRandomContries() {
        assertEquals(Country.SOUTH_SUDAN, Country.find("South Sudan"));
        assertEquals(Country.MACEDONIA, Country.find("Macedonia, The Former Yugoslav Republic of"));
        assertEquals(Country.MYANMAR, Country.find("Burma"));
        assertEquals(Country.MYANMAR, Country.find("Myanmar"));
        assertEquals(Country.TIMOR_LESTE, Country.find("East Timor"));
        assertEquals(Country.TIMOR_LESTE, Country.find("Timor-Leste"));
        assertEquals(Country.VIRGIN_ISLANDS_BRITISH, Country.find("British Virgin Islands"));
        assertEquals(Country.MICRONESIA, Country.find("Micronesia, Federated States of"));
        assertEquals(Country.SINT_MAARTEN, Country.find("St. Maarten"));
        assertEquals(Country.WALLIS_AND_FUTUNA_ISLANDS, Country.find("Wallis and Futuna"));
        assertEquals(Country.VATICAN_CITY, Country.find("Holy See"));
        assertEquals(Country.FRANCE, Country.find("Corsica"));
    }
}
