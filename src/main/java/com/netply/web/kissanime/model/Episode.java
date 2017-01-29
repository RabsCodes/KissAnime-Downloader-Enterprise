package com.netply.web.kissanime.model;

public class Episode {
    private String name;
    private String url;


    public Episode() {
    }

    public Episode(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode = (Episode) o;

        if (name != null ? !name.equals(episode.name) : episode.name != null) return false;
        return url != null ? url.equals(episode.url) : episode.url == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Episode{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
