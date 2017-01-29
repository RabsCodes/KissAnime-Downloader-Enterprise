package com.netply.web.kissanime.model;

public class Anime {
    private long id;
    private String animeURLSuffix;
    private String customName;
    private String season;


    public Anime(long id, String animeURLSuffix, String customName, String season) {
        this.id = id;
        this.animeURLSuffix = animeURLSuffix;
        this.customName = customName;
        this.season = season;
    }

    public Anime(String animeURLSuffix, String customName, String season) {
        this.animeURLSuffix = animeURLSuffix;
        this.customName = customName;
        this.season = season;
    }

    public long getId() {
        return id;
    }

    public String getAnimeURLSuffix() {
        return animeURLSuffix;
    }

    public String getCustomName() {
        return customName;
    }

    public String getSeason() {
        return season;
    }
}
