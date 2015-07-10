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

import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.util.HasName;
import org.datacleaner.util.HasAliases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum that contains all ISO code countries.
 * 
 * The enum values where generated based on this list:
 * 
 * <ul>
 * <li><a href="http://www.iso.org/iso/english_country_names_and_code_elements">
 * English country names and code elements</a></li>
 * </ul>
 */
public enum Country implements HasName, HasAliases {

    AFGHANISTAN("AF", "AFG", "Afghanistan"),

    ÅLAND("AX", "ALA", "Åland", "Åland Islands"),

    ALBANIA("AL", "ALB", "Albania"),

    ALGERIA("DZ", "DZA", "Algeria"),

    AMERICAN_SAMOA("AS", "ASM", "American Samoa"),

    ANDORRA("AD", "AND", "Andorra"),

    ANGOLA("AO", "AGO", "Angola"),

    ANGUILLA("AI", "AIA", "Anguilla"),

    ANTARCTICA("AQ", "ATA", "Antarctica"),

    ANTIGUA_AND_BARBUDA("AG", "ATG", "Antigua and Barbuda"),

    ARGENTINA("AR", "ARG", "Argentina"),

    ARMENIA("AM", "ARM", "Armenia"),

    ARUBA("AW", "ABW", "Aruba"),

    AUSTRALIA("AU", "AUS", "Australia"),

    AUSTRIA("AT", "AUT", "Austria"),

    AZERBAIJAN("AZ", "AZE", "Azerbaijan"),

    BAHAMAS("BS", "BHS", "Bahamas"),

    BAHRAIN("BH", "BHR", "Bahrain"),

    BANGLADESH("BD", "BGD", "Bangladesh"),

    BARBADOS("BB", "BRB", "Barbados"),

    BELARUS("BY", "BLR", "Belarus"),

    BELGIUM("BE", "BEL", "Belgium", "Kingdom of Belgium", "Belgie", "België"),

    BELIZE("BZ", "BLZ", "Belize"),

    BENIN("BJ", "BEN", "Benin"),

    BERMUDA("BM", "BMU", "Bermuda"),

    BHUTAN("BT", "BTN", "Bhutan"),

    BOLIVIA("BO", "BOL", "Bolivia", "Plurinational State of Bolivia", "Bolivia (Plurinational State of)"),

    BONAIRE_SAINT_EUSTATIUS_AND_SABA("BQ", "BES", "Bonaire, Saint Eustatius and Saba"),

    BOSNIA_AND_HERZEGOVINA("BA", "BIH", "Bosnia and Herzegovina"),

    BOTSWANA("BW", "BWA", "Botswana"),

    BOUVET_ISLAND("BV", "BVT", "Bouvet Island"),

    BRAZIL("BR", "BRA", "Brazil", "Brasil"),

    BRITISH_INDIAN_OCEAN_TERRITORY("IO", "IOT", "British Indian Ocean Territory"),

    BRUNEI_DARUSSALAM("BN", "BRN", "Brunei Darussalam"),

    BULGARIA("BG", "BGR", "Bulgaria"),

    BURKINA_FASO("BF", "BFA", "Burkina Faso"),

    BURUNDI("BI", "BDI", "Burundi"),

    CAMBODIA("KH", "KHM", "Cambodia"),

    CAMEROON("CM", "CMR", "Cameroon"),

    CANADA("CA", "CAN", "Canada"),

    CAPE_VERDE("CV", "CPV", "Cape Verde", "Cabo Verde"),

    CAYMAN_ISLANDS("KY", "CYM", "Cayman Islands"),

    CENTRAL_AFRICAN_REPUBLIC("CF", "CAF", "Central African Republic"),

    CHAD("TD", "TCD", "Chad"),

    CHILE("CL", "CHL", "Chile"),

    CHINA("CN", "CHN", "China", "Chine"),

    CHRISTMAS_ISLAND("CX", "CXR", "Christmas Island"),

    COCOS__ISLANDS("CC", "CCK", "Cocos (Keeling) Islands"),

    COLOMBIA("CO", "COL", "Colombia"),

    COMOROS("KM", "COM", "Comoros"),

    CONGO_BRAZZAVILLE("CG", "COG", "Congo (Brazzaville)"),

    CONGO_KINSHASA("CD", "COD", "Congo (Kinshasa)"),

    COOK_ISLANDS("CK", "COK", "Cook Islands"),

    COSTA_RICA("CR", "CRI", "Costa Rica"),

    COTE_D_IVOIRE("CI", "CIV", "Côte d'Ivoire", "Cote d'Ivoire", "Ivory Coast"),

    CROATIA("HR", "HRV", "Croatia"),

    CUBA("CU", "CUB", "Cuba"),

    CURAÇAO("CW", "CUW", "Curaçao"),

    CYPRUS("CY", "CYP", "Cyprus"),

    CZECH_REPUBLIC("CZ", "CZE", "Czech Republic", "Česká Republika", "Česko", "Czech"),

    DENMARK("DK", "DNK", "Denmark", "Danmark", "Dinamarca", "Dänemark"),

    DJIBOUTI("DJ", "DJI", "Djibouti"),

    DOMINICA("DM", "DMA", "Dominica"),

    DOMINICAN_REPUBLIC("DO", "DOM", "Dominican Republic"),

    ECUADOR("EC", "ECU", "Ecuador"),

    EGYPT("EG", "EGY", "Egypt"),

    EL_SALVADOR("SV", "SLV", "El Salvador"),

    EQUATORIAL_GUINEA("GQ", "GNQ", "Equatorial Guinea"),

    ERITREA("ER", "ERI", "Eritrea"),

    ESTONIA("EE", "EST", "Estonia"),

    ETHIOPIA("ET", "ETH", "Ethiopia"),

    FALKLAND_ISLANDS("FK", "FLK", "Falkland Islands", "Falkland Islands (Malvinas)", "Malvinas", "Falklands",
            "Islas Malvinas"),

    FAROE_ISLANDS("FO", "FRO", "Faroe Islands", "Faeroe Islands"),

    FIJI("FJ", "FJI", "Fiji"),

    FINLAND("FI", "FIN", "Finland"),

    FRANCE("FR", "FRA", "France", "French Republic", "République Française", "Republique Francaise"),

    FRENCH_GUIANA("GF", "GUF", "French Guiana"),

    FRENCH_POLYNESIA("PF", "PYF", "French Polynesia"),

    FRENCH_SOUTHERN_LANDS("TF", "ATF", "French Southern Lands"),

    GABON("GA", "GAB", "Gabon"),

    GAMBIA("GM", "GMB", "Gambia"),

    GEORGIA("GE", "GEO", "Georgia"),

    GERMANY("DE", "DEU", "Germany", "Deutschland", "GER"),

    GHANA("GH", "GHA", "Ghana"),

    GIBRALTAR("GI", "GIB", "Gibraltar"),

    GREECE("GR", "GRC", "Greece"),

    GREENLAND("GL", "GRL", "Greenland"),

    GRENADA("GD", "GRD", "Grenada"),

    GUADELOUPE("GP", "GLP", "Guadeloupe"),

    GUAM("GU", "GUM", "Guam"),

    GUATEMALA("GT", "GTM", "Guatemala"),

    GUERNSEY("GG", "GGY", "Guernsey"),

    GUINEA("GN", "GIN", "Guinea"),

    GUINEA_BISSAU("GW", "GNB", "Guinea-Bissau"),

    GUYANA("GY", "GUY", "Guyana"),

    HAITI("HT", "HTI", "Haiti"),

    HEARD_AND_MCDONALD_ISLANDS("HM", "HMD", "Heard and McDonald Islands"),

    HONDURAS("HN", "HND", "Honduras"),

    HONG_KONG("HK", "HKG", "Hong Kong", "China, Hong Kong SAR", "Hong Kong SAR"),

    HUNGARY("HU", "HUN", "Hungary"),

    ICELAND("IS", "ISL", "Iceland"),

    INDIA("IN", "IND", "India"),

    INDONESIA("ID", "IDN", "Indonesia"),

    IRAN("IR", "IRN", "Iran", "Islamic Republic of Iran", "Iran (Islamic Republic of)"),

    IRAQ("IQ", "IRQ", "Iraq", "Irak"),

    IRELAND("IE", "IRL", "Ireland", "Eire", "Eir", "Éire", "Airlann", "Ire"),

    ISLE_OF_MAN("IM", "IMN", "Isle of Man"),

    ISRAEL("IL", "ISR", "Israel"),

    ITALY("IT", "ITA", "Italy", "Italia"),

    JAMAICA("JM", "JAM", "Jamaica"),

    JAPAN("JP", "JPN", "Japan"),

    JERSEY("JE", "JEY", "Jersey"),

    JORDAN("JO", "JOR", "Jordan"),

    KAZAKHSTAN("KZ", "KAZ", "Kazakhstan"),

    KENYA("KE", "KEN", "Kenya"),

    KIRIBATI("KI", "KIR", "Kiribati"),

    KOREA_NORTH("KP", "PRK", "Korea, North", "Democratic People's Republic of Korea"),

    KOREA_SOUTH("KR", "KOR", "Korea, South", "Korea", "Republic of Korea"),

    KUWAIT("KW", "KWT", "Kuwait"),

    KYRGYZSTAN("KG", "KGZ", "Kyrgyzstan"),

    LAOS("LA", "LAO", "Laos", "Lao People's Democratic Republic"),

    LATVIA("LV", "LVA", "Latvia"),

    LEBANON("LB", "LBN", "Lebanon"),

    LESOTHO("LS", "LSO", "Lesotho"),

    LIBERIA("LR", "LBR", "Liberia"),

    LIBYA("LY", "LBY", "Libya"),

    LIECHTENSTEIN("LI", "LIE", "Liechtenstein"),

    LITHUANIA("LT", "LTU", "Lithuania"),

    LUXEMBOURG("LU", "LUX", "Luxembourg", "Luxemborg"),

    MACAU("MO", "MAC", "Macau", "China, Macao SAR", "Macao SAR"),

    MACEDONIA("MK", "MKD", "Macedonia", ""),

    MADAGASCAR("MG", "MDG", "Madagascar"),

    MALAWI("MW", "MWI", "Malawi"),

    MALAYSIA("MY", "MYS", "Malaysia"),

    MALDIVES("MV", "MDV", "Maldives"),

    MALI("ML", "MLI", "Mali"),

    MALTA("MT", "MLT", "Malta"),

    MARSHALL_ISLANDS("MH", "MHL", "Marshall Islands"),

    MARTINIQUE("MQ", "MTQ", "Martinique"),

    MAURITANIA("MR", "MRT", "Mauritania"),

    MAURITIUS("MU", "MUS", "Mauritius"),

    MAYOTTE("YT", "MYT", "Mayotte"),

    MEXICO("MX", "MEX", "Mexico", "México", "United Mexican States"),

    MICRONESIA("FM", "FSM", "Micronesia", "Micronesia (Federated States of)", "Federated States of Micronesia"),

    MOLDOVA("MD", "MDA", "Moldova"),

    MONACO("MC", "MCO", "Monaco"),

    MONGOLIA("MN", "MNG", "Mongolia"),

    MONTENEGRO("ME", "MNE", "Montenegro"),

    MONTSERRAT("MS", "MSR", "Montserrat"),

    MOROCCO("MA", "MAR", "Morocco", "Marocco", "Maroc"),

    MOZAMBIQUE("MZ", "MOZ", "Mozambique"),

    MYANMAR("MM", "MMR", "Myanmar"),

    NAMIBIA("NA", "NAM", "Namibia"),

    NAURU("NR", "NRU", "Nauru"),

    NEPAL("NP", "NPL", "Nepal"),

    NETHERLANDS("NL", "NLD", "Netherlands", "Holland", "Netherlands, The", "The Netherlands", "Nederland"),

    NEW_CALEDONIA("NC", "NCL", "New Caledonia"),

    NEW_ZEALAND("NZ", "NZL", "New Zealand"),

    NICARAGUA("NI", "NIC", "Nicaragua"),

    NIGER("NE", "NER", "Niger"),

    NIGERIA("NG", "NGA", "Nigeria"),

    NIUE("NU", "NIU", "Niue"),

    NORFOLK_ISLAND("NF", "NFK", "Norfolk Island"),

    NORTHERN_MARIANA_ISLANDS("MP", "MNP", "Northern Mariana Islands"),

    NORWAY("NO", "NOR", "Norway", "Norge"),

    OMAN("OM", "OMN", "Oman"),

    PAKISTAN("PK", "PAK", "Pakistan"),

    PALAU("PW", "PLW", "Palau"),

    PALESTINE("PS", "PSE", "Palestine"),

    PANAMA("PA", "PAN", "Panama"),

    PAPUA_NEW_GUINEA("PG", "PNG", "Papua New Guinea"),

    PARAGUAY("PY", "PRY", "Paraguay"),

    PERU("PE", "PER", "Peru", "Perú"),

    PHILIPPINES("PH", "PHL", "Philippines"),

    PITCAIRN("PN", "PCN", "Pitcairn"),

    POLAND("PL", "POL", "Poland"),

    PORTUGAL("PT", "PRT", "Portugal"),

    PUERTO_RICO("PR", "PRI", "Puerto Rico"),

    QATAR("QA", "QAT", "Qatar"),

    REUNION("RE", "REU", "Reunion"),

    ROMANIA("RO", "ROU", "Romania"),

    RUSSIAN_FEDERATION("RU", "RUS", "Russian Federation", "Russia", "USSR"),

    RWANDA("RW", "RWA", "Rwanda"),

    SAINT_BARTHÉLEMY("BL", "BLM", "Saint Barthélemy"),

    SAINT_HELENA("SH", "SHN", "Saint Helena"),

    SAINT_KITTS_AND_NEVIS("KN", "KNA", "Saint Kitts and Nevis"),

    SAINT_LUCIA("LC", "LCA", "Saint Lucia"),

    SAINT_MARTIN_FRENCH_PART("MF", "MAF", "Saint Martin (French part)"),

    SAINT_PIERRE_AND_MIQUELON("PM", "SPM", "Saint Pierre and Miquelon"),

    SAINT_VINCENT_AND_THE_GRENADINES("VC", "VCT", "Saint Vincent and the Grenadines"),

    SAMOA("WS", "WSM", "Samoa"),

    SAN_MARINO("SM", "SMR", "San Marino"),

    SAO_TOME_AND_PRINCIPE("ST", "STP", "Sao Tome and Principe"),

    SAUDI_ARABIA("SA", "SAU", "Saudi Arabia"),

    SENEGAL("SN", "SEN", "Senegal"),

    SERBIA("RS", "SRB", "Serbia"),

    SEYCHELLES("SC", "SYC", "Seychelles"),

    SIERRA_LEONE("SL", "SLE", "Sierra Leone"),

    SINGAPORE("SG", "SGP", "Singapore"),

    SINT_MAARTEN("SX", "SXM", "Sint Maarten"),

    SLOVAKIA("SK", "SVK", "Slovakia"),

    SLOVENIA("SI", "SVN", "Slovenia"),

    SOLOMON_ISLANDS("SB", "SLB", "Solomon Islands"),

    SOMALIA("SO", "SOM", "Somalia"),

    SOUTH_AFRICA("ZA", "ZAF", "South Africa"),

    SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS("GS", "SGS", "South Georgia and South Sandwich Islands"),

    SPAIN("ES", "ESP", "Spain", "Espana", "España"),

    SRI_LANKA("LK", "LKA", "Sri Lanka"),

    SUDAN("SD", "SDN", "Sudan"),

    SURINAME("SR", "SUR", "Suriname"),

    SVALBARD_AND_JAN_MAYEN("SJ", "SJM", "Svalbard and Jan Mayen"),

    SWAZILAND("SZ", "SWZ", "Swaziland"),

    SWEDEN("SE", "SWE", "Sweden", "Sverige"),

    SWITZERLAND("CH", "CHE", "Switzerland", "Swiss", "Schweiz", "Suisse", "Swiss Confederation"),

    SYRIA("SY", "SYR", "Syria", "Syrian Arab Republic"),

    TAIWAN("TW", "TWN", "Taiwan"),

    TAJIKISTAN("TJ", "TJK", "Tajikistan"),

    TANZANIA("TZ", "TZA", "Tanzania"),

    THAILAND("TH", "THA", "Thailand"),

    TIMOR_LESTE("TL", "TLS", "Timor-Leste"),

    TOGO("TG", "TGO", "Togo"),

    TOKELAU("TK", "TKL", "Tokelau"),

    TONGA("TO", "TON", "Tonga"),

    TRINIDAD_AND_TOBAGO("TT", "TTO", "Trinidad and Tobago"),

    TUNISIA("TN", "TUN", "Tunisia", "Tunisie", "Tūnis", "Tunis"),

    TURKEY("TR", "TUR", "Turkey"),

    TURKMENISTAN("TM", "TKM", "Turkmenistan"),

    TURKS_AND_CAICOS_ISLANDS("TC", "TCA", "Turks and Caicos Islands"),

    TUVALU("TV", "TUV", "Tuvalu"),

    UGANDA("UG", "UGA", "Uganda"),

    UKRAINE("UA", "UKR", "Ukraine"),

    UNITED_ARAB_EMIRATES("AE", "ARE", "United Arab Emirates"),

    UNITED_KINGDOM("GB", "GBR", "United Kingdom", "Great Britain", "United Kingdom of Great Britain",
            "United Kingdom of Great Britain and Northern Ireland", "England", "Scotland", "Wales", "Northern Ireland",
            "UK"),

    UNITED_STATES_MINOR_OUTLYING_ISLANDS("UM", "UMI", "United States Minor Outlying Islands"),

    UNITED_STATES_OF_AMERICA("US", "USA", "United States of America", "United States"),

    URUGUAY("UY", "URY", "Uruguay"),

    UZBEKISTAN("UZ", "UZB", "Uzbekistan"),

    VANUATU("VU", "VUT", "Vanuatu"),

    VATICAN_CITY("VA", "VAT", "Vatican City"),

    VENEZUELA("VE", "VEN", "Venezuela", "Bolivarian Republic of Venezuela", "Venezuela (Bolivarian Republic of)"),

    VIETNAM("VN", "VNM", "Vietnam", "Viet nam"),

    VIRGIN_ISLANDS_BRITISH("VG", "VGB", "Virgin Islands, British"),

    VIRGIN_ISLANDS_US("VI", "VIR", "Virgin Islands, U.S.", "United States Virgin Islands"),

    WALLIS_AND_FUTUNA_ISLANDS("WF", "WLF", "Wallis and Futuna Islands"),

    WESTERN_SAHARA("EH", "ESH", "Western Sahara"),

    YEMEN("YE", "YEM", "Yemen"),

    ZAMBIA("ZM", "ZMB", "Zambia"),

    ZIMBABWE("ZW", "ZWE", "Zimbabwe");

    private static final Logger LOGGER = LoggerFactory.getLogger(Country.class);
    private static Map<String, Country> matchingMap;

    // build reusable matching map for find(...) method
    static {
        matchingMap = new HashMap<String, Country>();
        final Country[] values = values();

        // first add aliases (separately, they take second priority)
        for (final Country c : values) {
            final String[] countryAliases = c.getAliases();
            for (String alias : countryAliases) {
                alias = standardizeForMatching(alias);
                matchingMap.put(alias, c);
            }
        }

        // add unique names/identifiers/codes
        for (final Country c : values) {
            matchingMap.put(standardizeForMatching(c.getTwoLetterISOCode()), c);
            matchingMap.put(standardizeForMatching(c.getThreeLetterISOCode()), c);
            matchingMap.put(standardizeForMatching(c.name()), c);
            matchingMap.put(standardizeForMatching(c.getCountryName()), c);
        }
    }

    private final String _twoLetterIsoCode;
    private final String _threeLetterIsoCode;
    private final String _name;
    private final String[] _aliases;

    /**
     * Constructor for country enum
     * 
     * @param isoCode2
     * @param isoCode3
     * @param name
     * @param aliases
     */
    private Country(String isoCode2, String isoCode3, String name, String... aliases) {
        _twoLetterIsoCode = isoCode2;
        _threeLetterIsoCode = isoCode3;
        _name = name;
        _aliases = aliases;
    }

    public String getTwoLetterISOCode() {
        return _twoLetterIsoCode;
    }

    public String getThreeLetterISOCode() {
        return _threeLetterIsoCode;
    }

    public String getCountryName() {
        return _name;
    }

    @Override
    public String[] getAliases() {
        return _aliases;
    }

    /**
     * Finds a particular country based on a countrycode. The search will look
     * first for country codes but in case no matches are found, then country
     * name search is also applied.
     * 
     * @param country
     *            the country code or name to search for
     * @return
     */
    public static Country find(String country) {
        return find(country, null);
    }

    /**
     * Finds a particular country based on a countrycode. The search will look
     * first for country codes but in case no matches are found, then country
     * name search is also applied.
     * 
     * @param countryInput
     *            the country code or name to search for
     * @param defaultCountry
     *            a country to return in case of no matches.
     * @return
     */
    public static Country find(final String countryInput, final Country defaultCountry) {
        LOGGER.trace("Resolving country with country code '{}'", countryInput);
        if (countryInput == null) {
            LOGGER.debug("No country code specified, returning default: {}", defaultCountry);
            return defaultCountry;
        }

        final String countryCompare = standardizeForMatching(countryInput);

        if (countryCompare.length() == 0) {
            LOGGER.debug("Country input (after standardization) was empty, returning default: {}", defaultCountry);
            return defaultCountry;
        }

        Country country = matchingMap.get(countryCompare);
        if (country != null) {
            LOGGER.debug("Found matching country (by compare value '{}'): {}", countryCompare, country);
            return country;
        }

        LOGGER.info("No matching country found for '{}', returning default: {}", countryInput, defaultCountry);
        return defaultCountry;
    }

    private static String standardizeForMatching(String country) {
        country = country.trim();

        // remove dots
        country = replaceAll(country, ".", "");

        // remove commas
        country = replaceAll(country, ",", "");

        // remove single quotes
        country = replaceAll(country, "'", "");

        // to upper case (uppercase is prefered over lowercase, for direct
        // compare with ISO codes)
        country = country.toUpperCase();

        // replace '&' with 'and'
        country = replaceAll(country, " & ", " AND ");

        // replace "islands" with "island"
        country = replaceAll(country, " ISLANDS", " ISLAND");

        // remove common filler-words like "Republic of" etc.
        country = replaceAll(country, "REPUBLIC ", " ");
        country = replaceAll(country, "STATES ", " ");
        country = replaceAll(country, "STATE ", " ");
        country = replaceAll(country, "OF ", " ");

        // remove spaces
        country = replaceAll(country, " ", "");

        return country;
    }

    /**
     * Non-regex based replace-all method
     * 
     * @param str
     * @param searchFor
     * @param replaceWith
     * @return
     */
    private static String replaceAll(String str, String searchFor, String replaceWith) {
        while (str.indexOf(searchFor) != -1) {
            str = str.replace(searchFor, replaceWith);
        }
        return str;
    }

    @Override
    public String getName() {
        return getCountryName();
    }
}
