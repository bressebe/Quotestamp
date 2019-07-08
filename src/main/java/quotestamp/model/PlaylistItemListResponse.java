package quotestamp.model;

import com.google.api.services.youtube.model.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistItemListResponse {
    private com.google.api.services.youtube.model.PlaylistItemListResponse response;

    public PlaylistItemListResponse(com.google.api.services.youtube.model.PlaylistItemListResponse response) {
        this.response = response;
    }
    
    public List<String> getVideoIds() {
        List<String> ids = new ArrayList<>();
        for (PlaylistItem item : response.getItems()) {
            ids.add(item.getContentDetails().getVideoId());
        }
        return ids;
    }

    public String getNextPageToken() {
        return response.getNextPageToken();
    }
}
