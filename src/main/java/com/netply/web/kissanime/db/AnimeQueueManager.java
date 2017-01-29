package com.netply.web.kissanime.db;

import com.netply.web.kissanime.model.Anime;
import com.netply.web.kissanime.model.DownloadQueueItem;

import java.util.List;

public interface AnimeQueueManager {
    List<Anime> getUnprocessedAnime();

    void addAnimeToQueue(Anime anime);

    void markAnimeAsInvalid(long id);

    void markAnimeAsProcessed(long id);

    void updateAnimeIndexTime(long id);

    void addEpisodeToQueue(DownloadQueueItem downloadQueueItem);

    List<DownloadQueueItem> getUnprocessedEpisodes();

    void markEpisodeAsProcessed(long id);

    void increaseAttempts(long id);
}
