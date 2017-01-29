package com.netply.web.kissanime;

import com.netply.web.kissanime.model.Episode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AnimeSearchController {
    private final KissAnimeSearchClient searchClient;


    @Autowired
    public AnimeSearchController(KissAnimeSearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> searchAnime(@RequestParam(value = "anime") String anime,
                                  @RequestParam(value = "q", defaultValue = "World") String cat) {
        ArrayList<String> strings = new ArrayList<>();
        List<Episode> episodes = searchClient.getEpisodes(anime);
        strings.add(anime + "[" + String.valueOf(episodes) + "]");
        return strings;
    }

    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public void downloadAnime(@RequestParam(value = "anime") String anime,
                              @RequestParam(value = "season", required = false, defaultValue = "") String season,
                              @RequestParam(value = "customName", required = false, defaultValue = "") String customName) throws IOException {
        searchClient.downloadAnime(anime, season, customName);
    }
}
