package quotestamp.factory;

import com.google.api.services.youtube.YouTube;
import org.springframework.stereotype.Component;
import quotestamp.config.OAuth2;

@Component
public class YouTubeFactory {
    public YouTube create() {
        return OAuth2.getYouTubeInstance();
    }
}
