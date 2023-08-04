package org.example.news.service.service;

import org.example.news.service.configuration.NewsServiceConfiguration;
import org.example.news.service.model.Article;
import org.example.news.service.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);
    private static final String BASE_URL = "https://api.spaceflightnewsapi.net/v3/articles?_limit=";
    private final ConcurrentHashMap<String, Collection<Article>> bufferedArticles = new ConcurrentHashMap<>();
    private final NewsServiceConfiguration configuration;
    private final AtomicInteger startCount = new AtomicInteger();
    private final CyclicBarrier finishBarrier;
    private final RestTemplate restTemplate;
    @Autowired
    private ArticleRepository articleRepository;

    public ArticleService(NewsServiceConfiguration configuration, RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        finishBarrier = new CyclicBarrier(configuration.getDownloadingThreadsNumber());
        restTemplate = restTemplateBuilder.build();
    }

    @Async("newsDownloadTaskExecutor")
    public void downloadNews() {
        LOGGER.debug("downloadNews: start download...");
        int cycleNewsNumber = configuration.getThreadCycleNewsNumber();
        int totalNewsNumber = configuration.getTotalNewsNumber();
        int start;
        while ((start = startCount.getAndAdd(cycleNewsNumber)) < totalNewsNumber) {
            String url = BASE_URL + Math.min(cycleNewsNumber, totalNewsNumber - start) + "&_start=" + start;
            LOGGER.debug("downloadNews: from {}", start);
            Article[] articles;
            do {
                articles = restTemplate.getForObject(url, Article[].class);
                if (articles != null) {
                    LOGGER.debug("downloadNews: downloaded {}", Arrays.toString(articles));
                    Arrays.stream(articles).filter(this::filterArticle).forEach(a1 -> {
                        Collection<Article> group = bufferedArticles.computeIfAbsent(a1.getNewsSite(),
                                k -> new ConcurrentSkipListSet<>());
                        group.add(a1);
                        if (group.size() >= configuration.getSiteNewsLimit()) {
                            for (Article a2 : group) {
                                if (group.remove(a2)) {
                                    addToDB(a2);
                                }
                            }
                        }
                    });
                } else {
                    LOGGER.error("Download error for URL {}", url);
                }
            } while (articles == null);
        }
        LOGGER.debug("downloadNews: main download finished");
        try {
            finishBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("downloadNews: add remain news");
        Collection<Collection<Article>> groups = bufferedArticles.values();
        for (Collection<Article> group : groups) {
            if (groups.remove(group)) {
                for (Article article : group) {
                    addToDB(article);
                }
            }
        }
        LOGGER.debug("downloadNews: remain download finished");
        try {
            finishBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("downloadNews: all download finished");
    }

    private void addToDB(Article article) {
        LOGGER.debug("addToDB: {}", article);
        String content;
        do {
            String url = article.getUrl();
            content = restTemplate.getForObject(url, String.class);
            if (content != null) {
                article.setArticle(content);
                articleRepository.save(article);
            } else {
                LOGGER.error("Download error for URL {}", url);
            }
        } while (content == null);
    }

    private boolean filterArticle(Article article) {
        for (String forbiddenWord : configuration.getBlackList()) {
            if (article.getTitle().contains(forbiddenWord)) {
                return false;
            }
        }
        return true;
    }
}
