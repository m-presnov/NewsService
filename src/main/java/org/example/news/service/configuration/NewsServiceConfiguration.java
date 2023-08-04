package org.example.news.service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

@Configuration
@EnableAsync
public class NewsServiceConfiguration {

//    private static final Logger LOGGER = LoggerFactory.getLogger(NewsServiceConfiguration.class);

    @Value("${news-service-application.total-news-number}")
    private int totalNewsNumber;

    @Value("${news-service-application.downloading-threads-number}")
    private int downloadingThreadsNumber;

    @Value("${news-service-application.thread-cycle-news-number}")
    private int threadCycleNewsNumber;

    @Value("${news-service-application.site-news-limit}")
    private int siteNewsLimit;

    @Value("${news-service-application.black-list}")
    private List<String> blackList;

    public int getTotalNewsNumber() {
        return totalNewsNumber;
    }

    public int getDownloadingThreadsNumber() {
        return downloadingThreadsNumber;
    }

    public int getThreadCycleNewsNumber() {
        return threadCycleNewsNumber;
    }

    public int getSiteNewsLimit() {
        return siteNewsLimit;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    @Bean("newsDownloadTaskExecutor")
    public TaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(downloadingThreadsNumber);
        executor.setMaxPoolSize(downloadingThreadsNumber);
        executor.setQueueCapacity(downloadingThreadsNumber);
        executor.setThreadNamePrefix("NewsDownloadTaskExecutor-");
        executor.initialize();
        return executor;
    }
}
