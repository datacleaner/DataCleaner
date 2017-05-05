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

import java.awt.Color;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class ColorsResourceBundle extends ResourceBundle {
    private Map<String, Color> _colorMap;

    public ColorsResourceBundle() {
        _colorMap = new HashMap<>();
        // blue base color of DC styling (#05b9f0)
        _colorMap.put("color.blue.medium", new Color(5, 185, 240));
        _colorMap.put("color.blue.bright", new Color(11, 205, 255));
        _colorMap.put("color.blue.dark", new Color(4, 166, 216));

        // green base color of DC styling (#70be44)
        _colorMap.put("color.green", new Color(122, 190, 68));
        _colorMap.put("color.green.bright", new Color(135, 211, 75));
        _colorMap.put("color.green.dark", new Color(109, 171, 61));

        // orange base color of DC styling (#f58132)
        _colorMap.put("color.orange", new Color(245, 129, 50));
        _colorMap.put("color.orange.bright", new Color(255, 143, 55));
        _colorMap.put("color.orange.dark", new Color(220, 116, 45));

        // white with 10% alpha/opacity
        _colorMap.put("color.semitransparent", new Color(0.0f, 0.0f, 0.0f, 0.05f));

        // white
        _colorMap.put("color.brightest", Color.WHITE);

        // #e1e1e1 (silver-ish)
        final Color brightColor = new Color(245, 245, 245);
        _colorMap.put("color.bright", brightColor);

        // slightly darker than BRIGHT
        _colorMap.put("color.semibright", new Color(220, 220, 220));

        // #a0a0a0
        _colorMap.put("color.medium", new Color(130, 140, 150));

        _colorMap.put("color.semidark", new Color(55, 55, 55));

        final Color darkColor = new Color(33, 33, 33);
        _colorMap.put("color.dark", darkColor);

        _colorMap.put("color.darkest", java.awt.Color.BLACK);

        _colorMap.put("color.background.default", Color.WHITE);
        _colorMap.put("color.background.well", brightColor);
        _colorMap.put("color.background.alternative", darkColor);

        // additional colors, only intended for special widget coloring such as
        // charts etc.
        // Red: #d32424
        _colorMap.put("color.red.bright", new Color(211, 36, 36));

        // Purple: #d3249c
        _colorMap.put("color.purple.bright", new Color(211, 36, 156));

        // Cyan: #24d1d3
        _colorMap.put("color.cyan.bright", new Color(36, 209, 211));
    }

    @Override
    protected Object handleGetObject(final String key) {
        return _colorMap.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }

    @Override
    protected Set<String> handleKeySet() {
        return _colorMap.keySet();
    }
}
