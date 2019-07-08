package quotestamp.factory;

import org.springframework.stereotype.Component;
import quotestamp.entity.Playlist;
import quotestamp.entity.Video;

@Component
public class VideoFactory {
    public Video create(String id, Playlist fk) {
        return new Video(id, fk);
    }
}