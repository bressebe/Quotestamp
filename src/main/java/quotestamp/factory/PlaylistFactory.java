package quotestamp.factory;

import org.springframework.stereotype.Component;
import quotestamp.entity.Playlist;

@Component
public class PlaylistFactory {
    public Playlist create(String id) {
        return new Playlist(id);
    }
}
