package com.netply.web.kissanime.db;

import com.netply.web.kissanime.model.Anime;
import com.netply.web.kissanime.model.DownloadQueueItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AnimeQueueManagerImpl implements AnimeQueueManager {
    private final static Logger LOGGER = Logger.getLogger(AnimeQueueManagerImpl.class.getName());
    private Connection connection;
    private String mysqlIp;
    private int mysqlPort;
    private String mysqlDb;
    private final String mysqlUser;
    private final String mysqlPassword;


    public AnimeQueueManagerImpl(String mysqlIp, int mysqlPort, String mysqlDb, String mysqlUser, String mysqlPassword) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        this.mysqlIp = mysqlIp;
        this.mysqlPort = mysqlPort;
        this.mysqlDb = mysqlDb;
        this.mysqlUser = mysqlUser;
        this.mysqlPassword = mysqlPassword;
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        connection = createConnection();
    }

    private Connection createConnection() throws SQLException {
        return createConnectionForCredentials(mysqlIp, mysqlPort, mysqlDb, mysqlUser, mysqlPassword);
    }

    private Connection createConnectionForCredentials(String mysqlIp, int mysqlPort, String mysqlDb, String mysqlUser, String mysqlPassword) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + mysqlIp + ":" + mysqlPort + "/" + mysqlDb, mysqlUser, mysqlPassword);
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    public List<Anime> getUnprocessedAnime() {
        List<Anime> animeList = new ArrayList<>();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM anime_to_download WHERE `processed` = 0 AND `lastIndexed` < NOW() - INTERVAL 8 HOUR")) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                animeList.add(new Anime(resultSet.getInt("id"), resultSet.getString("urlSuffix"), resultSet.getString("customName"), resultSet.getString("season")));
            }
            return animeList;
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return animeList;
    }

    @Override
    public void addAnimeToQueue(Anime anime) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO anime_to_download (urlSuffix, customName, season, `lastIndexed`) VALUES (?, ?, ?, NOW() - INTERVAL 1 DAY)")) {
            preparedStatement.setString(1, anime.getAnimeURLSuffix());
            preparedStatement.setString(2, anime.getCustomName());
            preparedStatement.setString(3, anime.getSeason());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void markAnimeAsInvalid(long id) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE anime_to_download SET `processed` = 1 AND `invalid` AND `lastIndexed` = NOW() = 1 WHERE `id` = ?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void markAnimeAsProcessed(long id) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE anime_to_download SET `processed` = 1, `lastIndexed` = NOW() WHERE `id` = ?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void updateAnimeIndexTime(long id) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE anime_to_download SET `lastIndexed` = NOW() WHERE `id` = ?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void addEpisodeToQueue(DownloadQueueItem downloadQueueItem) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO episodes_to_download (episodeURL, episodeName, outputDir) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, downloadQueueItem.getEpisodeURL());
            preparedStatement.setString(2, downloadQueueItem.getEpisodeName());
            preparedStatement.setString(3, downloadQueueItem.getOutputDir());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<DownloadQueueItem> getUnprocessedEpisodes() {
        List<DownloadQueueItem> episodeList = new ArrayList<>();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM episodes_to_download WHERE `processed` = 0 AND `attempts` < 15")) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                episodeList.add(new DownloadQueueItem(resultSet.getInt("id"), resultSet.getString("episodeURL"), resultSet.getString("episodeName"), resultSet.getString("outputDir")));
            }
            return episodeList;
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return episodeList;
    }

    @Override
    public void markEpisodeAsProcessed(long id) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE episodes_to_download SET `processed` = 1 WHERE `id` = ?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void increaseAttempts(long id) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE episodes_to_download SET `attempts` = `attempts` + 1 WHERE `id` = ?")) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            AnimeQueueManagerImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
