package quotestamp.factory;

import org.springframework.stereotype.Component;
import quotestamp.model.PlaylistItemListResponse;

@Component
public class PlaylistItemListResponseFactory {
    public PlaylistItemListResponse create(com.google.api.services.youtube.model.PlaylistItemListResponse response) {
        return new PlaylistItemListResponse(response);
    }
}
