package com.netply.web.kissanime.model;

public class Anime {
    private String animeURLSuffix;
    private String season;
    private String customName;


    public Anime(String animeURLSuffix, String season, String customName) {
        this.animeURLSuffix = animeURLSuffix;
        this.season = season;
        this.customName = customName;
    }

    public String getAnimeURLSuffix() {
        return animeURLSuffix;
    }

    public String getSeason() {
        return season;
    }

    public String getCustomName() {
        return customName;
    }
}
