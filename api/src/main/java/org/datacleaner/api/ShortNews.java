package org.datacleaner.api;

import java.util.Date;
import java.util.List;

public class ShortNews implements java.io.Serializable {

    List<Items> newsItems;

    public ShortNews() {
    }

    public ShortNews(List<Items> newsItems) {
        this.newsItems = newsItems;
    }

    public List<Items> getNewsItems() {
        return newsItems;
    }

    public void setNewsItems(List<Items> newsItems) {
        this.newsItems = newsItems;
    }

    public static class Items implements java.io.Serializable {

        private String name;
        private String title;
        private Date dateCreated;
        private String message;

        public Items() {
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

        public void setName(String name) {
            this.name = name;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDateCreated(Date dateCreated) {
            this.dateCreated = dateCreated;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Items items = (Items) o;

            return name.equals(items.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
