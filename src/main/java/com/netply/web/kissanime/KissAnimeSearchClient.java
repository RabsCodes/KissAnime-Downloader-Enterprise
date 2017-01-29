package com.netply.web.kissanime;

import com.netply.web.kissanime.model.Episode;

import java.io.IOException;
import java.util.List;

public interface KissAnimeSearchClient {
    void downloadAnime(String animeURLSuffix, String season, String customName) throws IOException;

    List<Episode> getEpisodes(String anime);
}
