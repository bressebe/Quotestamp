package quotestamp.config;

import java.io.InputStreamReader;

public class ApplicationProperties {

    public enum FILE {
        PLAYLIST("/playlists.json"),
        CLIENT_SECRET("/client_secret.json");

        private String path;

        FILE(String path) {
            this.path = path;
        }

        public InputStreamReader get() {
            return new InputStreamReader(this.getClass().getResourceAsStream(path));
        }
    }

    public enum ENV {
        APPNAME("QUOTESTAMP_APPNAME"),
        RECEIVER_HOST("QUOTESTAMP_RECEIVER_HOST"),
        RECEIVER_PORT("QUOTESTAMP_RECEIVER_PORT"),
        API("QUOTESTAMP_YOUTUBE_APIKEY");

        private String env;

        ENV(String env) {
            this.env = env;
        }

        public String get() {
            return System.getenv(env);
        }
    }
}
