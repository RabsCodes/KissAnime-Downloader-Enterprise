package com.netply.web.kissanime.model;

import com.netply.web.kissanime.DownloadQueueManager;

public class DownloadQueueItem {
    private final DownloadQueueManager downloadQueueManager;
    private final String outputDir;
    private final String episodeURL;
    private final String episodeName;


    public DownloadQueueItem(DownloadQueueManager downloadQueueManager, String outputDir, String episodeURL, String episodeName) {
        this.downloadQueueManager = downloadQueueManager;
        this.outputDir = outputDir;
        this.episodeURL = episodeURL;
        this.episodeName = episodeName;
    }

    public DownloadQueueManager getDownloadQueueManager() {
        return downloadQueueManager;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getEpisodeURL() {
        return episodeURL;
    }

    public String getEpisodeName() {
        return episodeName;
    }
}
