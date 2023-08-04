package org.example.news.service;

import org.example.news.service.configuration.NewsServiceConfiguration;
import org.example.news.service.service.ArticleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NewsServiceRunner implements CommandLineRunner {

    private final ArticleService service;
    private final int downloadingThreadsNumber;

    public NewsServiceRunner(ArticleService service, NewsServiceConfiguration configuration) {
        this.service = service;
        downloadingThreadsNumber = configuration.getDownloadingThreadsNumber();
    }

    @Override
    public void run(String[] args) {
        for (int i = 0; i < downloadingThreadsNumber; ++i) {
            service.downloadNews();
        }
    }
}
