/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a useful abstraction over regular expressions where the groups are
 * named and not nescesarily ordered in the way specified by the enum that holds
 * the group names.
 * 
 * 
 */
public class NamedPattern<E extends Enum<E>> {

    private static final Logger logger = LoggerFactory.getLogger(NamedPattern.class);

    /**
     * Defines a default group literal which resolves to a single word with any
     * kind of letter (including diacritics)
     */
    public static final String DEFAULT_GROUP_LITERAL = "([\\p{Lu}\\p{Ll}]+)";

    private EnumMap<E, Integer> groupIndexes;
    private Pattern pattern;
    private Class<E> groupEnum;

    public NamedPattern(String pattern, Class<E> groupEnum) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern cannot be null");
        }
        if (groupEnum == null) {
            throw new IllegalArgumentException("groupEnum cannot be null");
        }

        pattern = pattern.replaceAll("\\(", "\\\\(");
        pattern = pattern.replaceAll("\\)", "\\\\)");
        pattern = pattern.replaceAll("\\[", "\\\\[");
        pattern = pattern.replaceAll("\\]", "\\\\]");

        this.groupEnum = groupEnum;

        groupIndexes = new EnumMap<E, Integer>(groupEnum);

        E[] availableGroupNames = groupEnum.getEnumConstants();

        List<E> usedGroupNames = new ArrayList<E>();
        List<Integer> groupNameStringIndexOfs = new ArrayList<Integer>();

        for (int i = 0; i < availableGroupNames.length; i++) {
            E group = availableGroupNames[i];
            String groupToken = getGroupToken(group);

            int indexOf = pattern.indexOf(groupToken);
            if (indexOf != -1) {
                usedGroupNames.add(group);
                groupNameStringIndexOfs.add(indexOf);
            }
        }

        if (usedGroupNames.isEmpty()) {
            throw new IllegalArgumentException("None of the groups defined in " + groupEnum.getSimpleName()
                    + " where found in the pattern: " + pattern);
        }

        Integer groupIndex = getIndexOfHighest(groupNameStringIndexOfs);
        while (groupIndex != null) {

            E group = usedGroupNames.remove(groupIndex.intValue());
            groupNameStringIndexOfs.remove(groupIndex.intValue());

            groupIndexes.put(group, usedGroupNames.size() + 1);

            pattern = pattern.replace(getGroupToken(group), getGroupLiteral(group));

            groupIndex = getIndexOfHighest(groupNameStringIndexOfs);
        }

        logger.info("compiling pattern: {}", pattern);
        this.pattern = Pattern.compile(pattern);
    }

    protected String getGroupToken(E group) {
        return group.name();
    }

    protected String getGroupLiteral(E group) {
        if (group instanceof HasGroupLiteral) {
            String groupLiteral = ((HasGroupLiteral) group).getGroupLiteral();
            if (groupLiteral == null) {
                return DEFAULT_GROUP_LITERAL;
            }
            return groupLiteral;
        }
        return DEFAULT_GROUP_LITERAL;
    }

    private Integer getIndexOfHighest(List<Integer> integerList) {
        Integer result = null;
        int highestValue = -1;
        for (int i = 0; i < integerList.size(); i++) {
            Integer integer = integerList.get(i);
            if (integer.intValue() > highestValue) {
                result = i;
                highestValue = integer;
            }
        }
        return result;
    }

    /**
     * Matches a string against this named pattern.
     * 
     * @param string
     *            the string to match
     * @return a match object, or null if there was no match
     */
    public NamedPatternMatch<E> match(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();

            if (start == 0 && end == string.length()) {
                Map<E, String> resultMap = new EnumMap<E, String>(groupEnum);
                Set<Entry<E, Integer>> entries = groupIndexes.entrySet();
                for (Entry<E, Integer> entry : entries) {
                    E group = entry.getKey();
                    Integer groupIndex = entry.getValue();
                    String result = matcher.group(groupIndex);
                    resultMap.put(group, result);
                }
                return new NamedPatternMatch<E>(resultMap);
            }
        }

        return null;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Set<E> getUsedGroups() {
        return groupIndexes.keySet();
    }
}
