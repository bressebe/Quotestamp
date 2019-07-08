package quotestamp.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class OAuth2 {
    private static final Logger LOG = Logger.getLogger(OAuth2.class);
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Collection<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl");
    private static final String DATA_STORE_DIR = ".creds";

    /**
     * Authorizes the YouTube OAuth2 credentials and returns an
     * instance of Google's YouTube class for making API requests
     *
     * @return authenticated YouTube instance
     */
    public static YouTube getYouTubeInstance() {

        try { // load google oauth2 secrets
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, ApplicationProperties.FILE.CLIENT_SECRET.get());

            // set up authorization code flow
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(transport, JSON_FACTORY, secrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(DATA_STORE_DIR))).setAccessType("offline").build();

            // set up receiver
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setHost(ApplicationProperties.ENV.RECEIVER_HOST.get())
                    .setPort(Integer.parseInt(ApplicationProperties.ENV.RECEIVER_PORT.get())).build();

            // authorize
            Credential creds = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            YouTube youtube = new YouTube.Builder(transport, JSON_FACTORY, creds)
                    .setApplicationName(ApplicationProperties.ENV.APPNAME.get()).build();

            LOG.info("Youtube OAuth2 credentials authorized");
            return youtube;

        } catch (Exception e) {
            LOG.error("YouTube OAuth2 authorization error", e);
            throw new RuntimeException(e);
        }
    }
}
