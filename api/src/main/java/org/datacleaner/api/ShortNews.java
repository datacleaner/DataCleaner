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
package org.datacleaner.api;

import java.util.Date;
import java.util.List;

public class ShortNews implements java.io.Serializable {

    List<Item> newsItems;

    public ShortNews() {
    }

    public ShortNews(List<Item> newsItems) {
        this.newsItems = newsItems;
    }

    public List<Item> getNewsItems() {
        return newsItems;
    }

    public void setNewsItems(List<Item> newsItems) {
        this.newsItems = newsItems;
    }

    public static class Item implements java.io.Serializable {

        private String name;
        private String title;
        private Date dateCreated;
        private String message;

        public Item() {
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public Date getDateCreated() {
            return dateCreated;
        }

        public String getMessage() {
            return message;
        }

        private void setName(String name) {
            this.name = name;
        }

        private void setTitle(String title) {
            this.title = title;
        }

        private void setDateCreated(Date dateCreated) {
            this.dateCreated = dateCreated;
        }

        private void setMessage(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            return name.equals(item.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
