package com.netply.web.kissanime;

import java.io.IOException;

public interface DownloadQueueManager {
    void addToQueue(String downloadURL, String outputDir, String episodeName) throws IOException;
}
