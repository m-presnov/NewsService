package org.example.news.service.controller;

import org.example.news.service.model.Article;
import org.example.news.service.repository.ArticleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ArticleController {

    private ArticleRepository repository;

    public ArticleController(ArticleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/news")
    public List<Article> all() {
        return repository.findAll();
    }

    @GetMapping("/news/{id}")
    public Article one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    @GetMapping("/news/site/{site}")
    public List<Article> site(@PathVariable String site) {
        return repository.findByNewsSite(site);
    }
}
