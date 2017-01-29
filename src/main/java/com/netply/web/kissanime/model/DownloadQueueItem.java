package com.netply.web.kissanime.model;

public class DownloadQueueItem {
    private long id;
    private final String outputDir;
    private final String episodeURL;
    private final String episodeName;


    public DownloadQueueItem(long id, String episodeURL, String episodeName, String outputDir) {
        this.id = id;
        this.outputDir = outputDir;
        this.episodeURL = episodeURL;
        this.episodeName = episodeName;
    }

    public DownloadQueueItem(String episodeURL, String episodeName, String outputDir) {
        this.outputDir = outputDir;
        this.episodeURL = episodeURL;
        this.episodeName = episodeName;
    }

    public long getId() {
        return id;
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
