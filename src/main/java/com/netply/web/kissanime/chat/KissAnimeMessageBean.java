package com.netply.web.kissanime.chat;

import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.web.kissanime.KissAnimeSearchClient;
import com.netply.zero.service.base.credentials.SessionManager;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class KissAnimeMessageBean {
    public static final String MUSIC_DIR = "/Music/";
    private String botChanURL;
    private KissAnimeSearchClient animeSearchClient;
    private MessageListener messageListener;


    @Autowired
    public KissAnimeMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, KissAnimeSearchClient animeSearchClient) {
        this.botChanURL = botChanURL;
        this.animeSearchClient = animeSearchClient;
        messageListener = new MessageListener(this.botChanURL);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMusicMessages() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add(ChatMatchers.DOWNLOAD_ANIME_MATCHER);
        messageMatchers.add(ChatMatchers.DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME);
        messageMatchers.add(ChatMatchers.DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME_AND_SEASON);
        messageListener.checkMessages("/messages", new MatcherList(SessionManager.getClientID(), messageMatchers), this::parseMessage);
    }

    private void parseMessage(Message message) {
        String messageText = message.getMessage();
        if (messageText.matches(ChatMatchers.DOWNLOAD_ANIME_MATCHER)
                || messageText.matches(ChatMatchers.DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME)
                || messageText.matches(ChatMatchers.DOWNLOAD_ANIME_MATCHER_WITH_CUSTOM_NAME_AND_SEASON)) {
            downloadAnime(message, messageText);
        }
    }

    private void downloadAnime(Message message, String messageText) {
        if (messageText.contains("\n")) {
            String[] lines = messageText.split("\n");

            if (lines.length >= 2) {
                String animeURL = lines[0];
                String customName = lines[1];
                String season = "";
                if (lines.length >= 3) {
                    season = lines[2];
                }

                System.out.println("Adding anime: " + animeURL + " / " + customName + " / " + season);
                try {
                    String animeURLSuffix = animeURL.substring("http://kissanime.ru/Anime/".length());
                    animeSearchClient.downloadAnime(animeURLSuffix, season, customName);
                    MessageUtil.reply(botChanURL, message, "Anime successfully added to download list");
                } catch (IOException e) {
                    e.printStackTrace();
                    MessageUtil.reply(botChanURL, message, "Error adding Anime");
                }
            } else {
                MessageUtil.reply(botChanURL, message, "Something did not go as expected. I have failed.");
            }
        } else {
            String animeURLSuffix = messageText.substring("http://kissanime.ru/Anime/".length());
            String customName = animeURLSuffix.replaceAll("-", " ");
            MessageUtil.reply(botChanURL, message, messageText + "\n" + customName + "\n" + "1");
        }
    }
}
