/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.lucene.ui;

import java.awt.Image;

import org.eobjects.datacleaner.util.ImageManager;

final class Images {

    private static final ImageManager _imageManager = ImageManager.getInstance();

    public static final Image WATERMARK_IMAGE = _imageManager.getImage("images/lucene_logo.png",
            Images.class.getClassLoader());

    public static final Image BANNER_IMAGE = _imageManager.getImage("images/banner-search-indices.png",
            Images.class.getClassLoader());

    private Images() {
        // prevent instantiation
    }
}
