package quotestamp.service;

import com.google.api.services.youtube.YouTube;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quotestamp.config.ApplicationProperties;
import quotestamp.entity.Playlist;
import quotestamp.entity.Video;
import quotestamp.factory.*;
import quotestamp.model.CaptionSnippet;
import quotestamp.model.PlaylistItemListResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class YouTubeService {
    private static final Logger LOG = Logger.getLogger(YouTubeService.class);
    @Autowired private VideoFactory videoFactory;
    @Autowired private PlaylistItemListResponseFactory playlistItemListResponseFactory;
    @Autowired private CaptionSnippetFactory captionSnippetFactory;

    /**
     * API request(s) for returning a list of
     * video ids associated with the given playlist id
     *
     * @param youtube authenticated YouTube instance
     * @param playlistId playlist id
     * @return resulting list of video ids from the API request
     */
    public List<String> listPlaylistVideoIds(YouTube youtube, String playlistId) {
        List<String> videoIds = new ArrayList<>();
        String pageToken = "";

        try { // keep requesting for the next 50 playlist items until there are no more left
            while (pageToken != null) {

                // get youtube to list 50 playlist items for the given playlist id
                YouTube.PlaylistItems.List list = youtube.playlistItems().list("contentDetails");
                PlaylistItemListResponse response = playlistItemListResponseFactory.create(list.setMaxResults((long)50)
                        .setPlaylistId(playlistId).setKey(ApplicationProperties.ENV.API.get()).setPageToken(pageToken).execute());

                // append items to the list and get the token for the next request
                videoIds.addAll(response.getVideoIds());
                pageToken = response.getNextPageToken();
            }
            LOG.info(String.format("%d total items(s) listed for playlist [id = %s]", videoIds.size(), playlistId));

        } catch (IOException e) {
            LOG.warn(String.format("Error during video id listing for playlist [id = %s]", playlistId), e);
            videoIds = new ArrayList<>();
        }
        return videoIds;
    }

    /**
     * Sorts through a given list of video ids and returns a list of
     * new video entities with unique ids not currently cached in the database
     *
     * @param videoIds list of video ids
     * @param playlist the playlist associated with the video ids
     * @return list of video entities with unique ids
     */
    public List<Video> populateNewVideoList(List<String> videoIds, Playlist playlist) {
        List<Video> videos = new ArrayList<>();

        for (String videoId : videoIds) {
            if (!playlist.containsVideo(videoId)) {
                videos.add(videoFactory.create(videoId, playlist));
            }
        }
        // order the video entities in reverse to download the older captions first
        Collections.reverse(videos);
        LOG.info(String.format("%d new video(s) found for playlist [id = %s]", videos.size(), playlist.getId()));
        return videos;
    }

    /**
     * API request for listing all
     * caption snippets associated with a given video id
     *
     * @param youtube authenticated YouTube instance
     * @param videoId video id
     * @return resulting caption snippet list from the API request
     */
    public List<CaptionSnippet> listCaptionsForVideo(YouTube youtube, String videoId) {
        List<CaptionSnippet> snippets;
        try {
            YouTube.Captions.List list = youtube.captions().list("snippet", videoId);
            snippets = captionSnippetFactory.createList(list.execute());
            LOG.info(String.format("%d caption data set(s) listed for video [id = %s]", snippets.size(), videoId));
        } catch (IOException e) {
            LOG.warn(String.format("Caption list error for video [id = %s]", videoId), e);
            snippets = new ArrayList<>();
        }
        return snippets;
    }

    /**
     * Determines the most suitable caption data from a caption snippet list,
     * must be english and either a standard track (preferably) or an auto-generated track
     *
     * @param snippets resulting caption snippet list from an API request
     * @return the caption id with the most suitable data
     */
    public String findSuitableCaptionId(List<CaptionSnippet> snippets) {
        String captionId = "";
        for (CaptionSnippet snippet : snippets) {

            if (snippet.getLanguage().toLowerCase().equals("en")) {

                if (snippet.getTrackKind().toLowerCase().equals("standard")) {
                    captionId = snippet.getId();
                    break;
                } else if (snippet.getTrackKind().toLowerCase().equals("asr")) {
                    captionId = snippet.getId();
                }
            }
        }
        return captionId;
    }

    /**
     * API request for downloading SRT
     * caption data for a given caption id
     *
     * @param youtube authenticated YouTube instance
     * @param captionId caption id
     * @return SRT caption data stream
     */
    public InputStream downloadSRT(YouTube youtube, String captionId) {
        InputStream stream;
        try {
            YouTube.Captions.Download captions = youtube.captions().download(captionId);
            stream = captions.setTfmt("srt").executeMediaAsInputStream();
            LOG.info(String.format("SRT downloaded for caption [id = %s]", captionId));
        } catch (IOException e) {
            LOG.warn(String.format("SRT download error for caption [id = %s]", captionId), e);
            stream = null;
        }
        return stream;
    }
}
