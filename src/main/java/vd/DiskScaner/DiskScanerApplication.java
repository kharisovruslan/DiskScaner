package vd.DiskScaner;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 *
 * @author Kharisov Ruslan
 */
@SpringBootApplication
@EnableAsync
public class DiskScanerApplication {

    @Bean
    public SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public static void main(String[] args) {
        SpringApplication.run(DiskScanerApplication.class, args);
        openHomePage("http://localhost:8080");
    }

    private static void openHomePage(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
