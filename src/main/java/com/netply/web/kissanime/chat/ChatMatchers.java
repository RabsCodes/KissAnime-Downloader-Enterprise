package com.netply.web.kissanime.chat;

public class ChatMatchers {
    public static final String DOWNLOAD_ANIME_MATCHER = "^(?i)(.*)http://kissanime.ru/Anime/(.*)";
    public static final String DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME = "^(?i)(.*)http://kissanime.ru/Anime/(.*)\n(.*)";
    public static final String DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME_AND_SEASON = "^(?i)(.*)http://kissanime.ru/Anime/(.*)\n(.*)\n(.*)";

    static {
        "".matches(DOWNLOAD_ANIME_MATCHER);
        "".matches(DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME);
        "".matches(DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME_AND_SEASON);
    }
}
