package org.example.news.service.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "ARTICLES")
public class Article implements Comparable<Article> {

    @Id
    private long id;
    @Column(nullable = false)
    private String title;
    @Transient
    private String url;
    @Column(name = "news_site", nullable = false)
    private String newsSite;
    @Column(name = "published_date", nullable = false)
    private Date publishedAt;
    @Lob
    @Column(nullable = false)
    private String article;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNewsSite() {
        return newsSite;
    }

    public void setNewsSite(String newsSite) {
        this.newsSite = newsSite;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    @Override
    public int compareTo(Article a) {
        return publishedAt.compareTo(a.publishedAt);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Article && id == ((Article) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Article{id=" + id + ", title=" + title +  ", newsSite=" + newsSite +
                ", publishedDate=" + publishedAt + '}';
    }
}
