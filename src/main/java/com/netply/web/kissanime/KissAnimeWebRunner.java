package com.netply.web.kissanime;

import com.netply.web.kissanime.data.Credentials;
import com.netply.web.kissanime.db.AnimeQueueManager;
import com.netply.web.kissanime.error.InvalidURLException;
import com.netply.web.kissanime.model.Anime;
import com.netply.web.kissanime.model.DownloadQueueItem;
import com.netply.web.kissanime.model.Episode;
import io.ddavison.conductor.Browser;
import io.ddavison.conductor.Config;
import io.ddavison.conductor.Locomotive;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.scheduling.annotation.Async;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Config(
        browser = Browser.CHROME,
        url = KissAnimeWebRunner.KISSANIME_HOST
)
public class KissAnimeWebRunner extends Locomotive implements KissAnimeSearchClient, AnimeThreadManager {
    public static final String KISSANIME_HOST = "http://kissanime.ru";
    public static final int MAX_CONCURRENT_DOWNLOADING_ITEMS = 10;

    private final By btnSubmit = By.id("btnSubmit");
    private final By username = By.name("username");
    private final By password = By.name("password");
    private final By listingTable = By.className("listing");
    private final By episodeLinks = By.xpath("//table[@class='listing']//a[contains(@href, 'Episode')]");
    private final By downloadLinks = By.xpath("//div[@id='divDownload']/a");
    private final By statusText = By.xpath("//p/span[contains(text(), 'Status:')]/..");

    private static final String outputDir = "/media/3 TB/Anime_KA";
    private AnimeQueueManager animeQueueManager;


    public KissAnimeWebRunner(AnimeQueueManager animeQueueManager) {
        this.animeQueueManager = animeQueueManager;
        Config configuration = this.getClass().getAnnotation(Config.class);
        String baseUrl = configuration.url();
        setupDriver(baseUrl);
    }

    private void setupDriver(String baseUrl) {
        driver.close();

        ChromeOptions options = new ChromeOptions();
        options.addExtensions(new File("uBlock0.chromium.crx"));
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        this.driver = new ChromeDriver(capabilities);

        this.actions = new Actions(this.driver);
        this.driver.navigate().to(baseUrl);
    }

    private void login() {
        navigateTo(KissAnimeWebRunner.KISSANIME_HOST + "/Login");
        waitForLoginPage();
        submitLoginCredentials();
    }

    private void waitForLoginPage() {
        waitForElement(btnSubmit);
        validatePresent(btnSubmit);
        validatePresent(username);
        validatePresent(password);
    }

    private void submitLoginCredentials() {
        setText(username, Credentials.USERNAME);
        setText(password, Credentials.PASSWORD);
        click(btnSubmit);

        waitForElement(By.id("liHome"));
    }

    @Async
    @Override
    public void startQueueThread() {
        while (true) {
            try {
                List<Anime> unprocessedAnime = animeQueueManager.getUnprocessedAnime();
                for (Anime anime : unprocessedAnime) {
                    findEpisodeList(anime);
                }
                List<DownloadQueueItem> unprocessedEpisodes = animeQueueManager.getUnprocessedEpisodes();
                while (unprocessedEpisodes.size() > 0 && currentDownloadQueue() >= MAX_CONCURRENT_DOWNLOADING_ITEMS) {
                    sleep(15000);
                }
                if (unprocessedEpisodes.isEmpty()) {
                    sleep(1000);
                }
                for (DownloadQueueItem unprocessedEpisode : unprocessedEpisodes) {
                    animeQueueManager.increaseAttempts(unprocessedEpisode.getId());
                    downloadItem(unprocessedEpisode);
                }
            } catch (Exception | Error e) {
                e.printStackTrace();
                sleep(5000);
            }
        }
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception ignored) {
        }
    }

    private int currentDownloadQueue() {
        try {
            Process p = Runtime.getRuntime().exec("/programming/kissanime-downloader/status.py");

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s;
            while ((s = stdInput.readLine()) != null) {
                if (s.trim().isEmpty()) {
                    continue;
                }
                System.out.println(s);
                try {
                    String[] split = s.split("#");
                    int activeDownloads = Integer.parseInt(split[1]);
                    int waitingDownloads = Integer.parseInt(split[2]);

                    return activeDownloads + waitingDownloads;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.err.println(s);
            }

//            new EpisodeDownloader().download(downloadURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 999;
    }

    @Override
    public List<Episode> getEpisodes(String anime) {
        String targetURL = KISSANIME_HOST + "/Anime/" + anime;
        navigateTo(targetURL);
        if (!driver.getCurrentUrl().equals(targetURL)) {
            System.err.println("Invalid URL: " + targetURL);
            throw new InvalidURLException();
        }

        waitForEpisodeTable();
        List<WebElement> linkList = driver.findElements(episodeLinks);
        Collections.reverse(linkList);

        List<Episode> episodes = linkList.stream().map(webElement -> {
            String title = webElement.getText();
            String episodeURL = webElement.getAttribute("href");

            System.out.println(title + " - " + episodeURL);
            String episodeName = title.substring(title.indexOf("Episode"));

            return new Episode(episodeName, episodeURL);
        }).collect(Collectors.toList());

        navigateTo(KISSANIME_HOST);
        return episodes;
    }

    private void waitForEpisodeTable() {
        int attempts = 0;
        while (true) {
            try {
                waitForElement(listingTable);
                waitForElement(episodeLinks);
                break;
            } catch (Exception | Error e) {
                e.printStackTrace();
                if (attempts++ >= 4) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void downloadAnime(String animeURLSuffix, String season, String customName) throws IOException {
        System.out.println("Adding anime to queue: " + animeURLSuffix + " / " + customName + " / Season " + season);
        animeQueueManager.addAnimeToQueue(new Anime(animeURLSuffix, customName, season));
    }

    private void findEpisodeList(Anime anime) throws IOException {
        String animeURLSuffix = anime.getAnimeURLSuffix();
        String targetURL = KISSANIME_HOST + "/Anime/" + animeURLSuffix;
        navigateTo(targetURL);

        if (!driver.getCurrentUrl().equals(targetURL)) {
            System.err.println("Invalid URL: " + targetURL);
            animeQueueManager.markAnimeAsInvalid(anime.getId());
            return;
        }

        waitForEpisodeTable();
        WebElement element = driver.findElement(statusText);
        boolean completed = element.getText().trim().contains("Completed");
        System.out.println(anime.getCustomName() + " - Completed?: " + completed);

        List<WebElement> linkList = driver.findElements(episodeLinks);
        Collections.reverse(linkList);

        List<Pair<String, String>> episodeDownloadLinkList = linkList.stream().map(webElement -> {
            String title = webElement.getText();
            String episodeURL = webElement.getAttribute("href");

            System.out.println(title + " - " + episodeURL);
            String episodeMatcher = "Episode";
            String episodeName = title.substring(title.indexOf(episodeMatcher));

            if (anime.getSeason() != null && !anime.getSeason().isEmpty()) {
                String seasonFormattedEpisode = String.format("- S0%sE", anime.getSeason());
                episodeName = title.replace(episodeMatcher + " 0", seasonFormattedEpisode);
            }
            return new ImmutablePair<>(episodeURL, episodeName);
        }).collect(Collectors.toList());

        String animeOutputDir = getAnimeOutputDir(anime, animeURLSuffix);

        episodeDownloadLinkList.forEach(episodeDownloadLinkItem -> {
            try {
                downloadEpisode(animeOutputDir, episodeDownloadLinkItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (completed) {
            animeQueueManager.markAnimeAsProcessed(anime.getId());
        } else {
            animeQueueManager.updateAnimeIndexTime(anime.getId());
        }
    }

    private String getAnimeOutputDir(Anime anime, String animeURLSuffix) {
        String animeOutputName = anime.getCustomName() != null ? anime.getCustomName() : animeURLSuffix.replaceAll("-", " ");
        String animeOutputDir = outputDir + File.separator + animeOutputName;
        if (anime.getSeason() != null && !anime.getSeason().isEmpty()) {
            animeOutputDir += File.separator + "Season " + anime.getSeason();
        }
        return animeOutputDir;
    }

    private void downloadEpisode(String outputDir, Pair<String, String> episodeDownloadItemPair) throws IOException {
        if (!fileExists(outputDir, episodeDownloadItemPair.getRight())) {
            queueEpisodeDownload(outputDir, episodeDownloadItemPair.getLeft(), episodeDownloadItemPair.getRight());
        }
    }

    private boolean fileExists(String outputDir, String fileName) {
        System.out.println("Checking if file exists: " + fileName + " in " + outputDir);
        File file = new File(outputDir);
        File[] files = file.listFiles(file1 -> file1.getName().startsWith(fileName));
        return files != null && files.length >= 1;
    }

    private void queueEpisodeDownload(String outputDir, String episodeURL, String episodeName) throws IOException {
        DownloadQueueItem downloadQueueItem = new DownloadQueueItem(episodeURL, episodeName, outputDir);
        animeQueueManager.addEpisodeToQueue(downloadQueueItem);
    }

    private void downloadItem(DownloadQueueItem downloadQueueItem) throws IOException {
        String episodeURL = downloadQueueItem.getEpisodeURL();
        System.out.println("Navigating to: " + episodeURL);
        navigateTo(episodeURL, true);

        waitForElement(downloadLinks);
        List<WebElement> elements = driver.findElements(downloadLinks);
        WebElement webElement = elements.get(0);
        String downloadURL = webElement.getAttribute("href");
        String fullQuality = webElement.getText();
        String quality = fullQuality.substring(fullQuality.indexOf("x") + 1, fullQuality.indexOf(".")) + "p";
        String episodeName = downloadQueueItem.getEpisodeName();
        episodeName += " - " + quality;

        System.out.println("Downloading: " + episodeName + " - " + downloadURL);
        addEpisodeToDownloadManagerQueue(downloadURL, downloadQueueItem.getOutputDir(), episodeName + ".mp4");
        animeQueueManager.markEpisodeAsProcessed(downloadQueueItem.getId());

        navigateTo(KISSANIME_HOST);
    }

    private void addEpisodeToDownloadManagerQueue(String downloadURL, String outputDir, String downloadFileName) {
        try {
//            String safeOutputDir = outputDir.replaceAll("\\s", "\\ ");
//            String safeDownloadFileName = downloadFileName.replaceAll("\\s", "\\ ");
            String script = "/programming/kissanime-downloader/add.py";
            String command = String.format(script + " %s %s %s", downloadURL, outputDir, downloadFileName);
            System.out.println(command);
            ProcessBuilder pb = new ProcessBuilder(script, downloadURL, outputDir, downloadFileName);
            Process p = pb.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.err.println(s);
            }

//            new EpisodeDownloader().download(downloadURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void navigateTo(String url, boolean requiresLogin) {
        navigateTo(url);
        if (requiresLogin && checkLoginStatus()) {
            super.navigateTo(url);
        }
    }

    @Override
    public Locomotive navigateTo(String url) {
        Locomotive locomotive = super.navigateTo(url);
        if (driver.getCurrentUrl().contains("AreYouHuman")) {
            System.out.println("Human check detected! Waiting for human bypass...");
            while (driver.getCurrentUrl().contains("AreYouHuman")) {
                sleep(1000);
            }
        }

        return locomotive;
    }

    private By getWelcomeSpanXPath() {
        return By.xpath(String.format("//span[contains(text(), '%s')]", Credentials.USERNAME));
    }

    private boolean checkLoginStatus() {
        if (!driver.getCurrentUrl().contains("Login")) {
            try {
                waitForElement(getWelcomeSpanXPath());
            } catch (Error e) {
                e.printStackTrace();
                login();
                return true;
            }
        }
        return false;
    }
}
