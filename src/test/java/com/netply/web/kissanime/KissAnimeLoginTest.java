package com.netply.web.kissanime;

import com.netply.web.kissanime.data.Credentials;
import io.ddavison.conductor.Browser;
import io.ddavison.conductor.Config;
import io.ddavison.conductor.Locomotive;
import org.junit.Test;
import org.openqa.selenium.By;

@Config(
        browser = Browser.CHROME,
        url = "http://kissanime.to/Login"
)
public class KissAnimeLoginTest extends Locomotive {
    @Test
    public void testLogin() throws Exception {
        waitForElement(By.id("btnSubmit"));
        validatePresent(By.id("btnSubmit"));
        validatePresent(By.name("username"));
        validatePresent(By.name("password"));

        setText(By.name("username"), Credentials.USERNAME);
        setText(By.name("password"), Credentials.PASSWORD);
    }
}
