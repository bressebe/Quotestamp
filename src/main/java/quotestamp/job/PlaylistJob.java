package quotestamp.job;

import com.google.api.services.youtube.YouTube;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import quotestamp.config.ApplicationProperties;
import quotestamp.entity.Playlist;
import quotestamp.entity.Video;
import quotestamp.factory.YouTubeFactory;
import quotestamp.service.DataService;
import quotestamp.service.JsonService;
import quotestamp.service.SrtService;
import quotestamp.service.YouTubeService;

import java.io.InputStream;
import java.util.List;

@Component
public class PlaylistJob {
    private static final Logger LOG = Logger.getLogger(PlaylistJob.class);
    @Autowired private JsonService jsonService;
    @Autowired private SrtService srtService;
    @Autowired private DataService dataService;
    @Autowired private YouTubeService youTubeService;
    @Autowired private YouTubeFactory youTubeFactory;

    /**
     * A cron job scheduled daily at 3am which pulls playlist ids from the playlist.json file, and
     * downloads subtitles for any new videos found for that playlist that are currently not cached in the database
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void update() {

        LOG.info("Updating the cached data for every configured playlist");
        YouTube youtube = youTubeFactory.create();

        // for each playlist id listed in playlists.json
        for (String playlistId : jsonService.getPlaylistIdsFromStream(ApplicationProperties.FILE.PLAYLIST.get())) {

            // find or create new playlist entity and get the youtube api to add any new videos to it
            Playlist playlist = dataService.findOrCreatePlaylist(playlistId);
            addVideosForPlaylist(youtube, playlist);

            dataService.savePlaylist(playlist);
            LOG.info(String.format("Playlist saved [id = %s]", playlistId));
        }
    }

    /**
     * Adds new video entities to a playlist that are found
     * via the API request that are currently not cached in the database
     *
     * @param youtube authenticated youtube instance
     * @param playlist playlist entity
     */
    private void addVideosForPlaylist(YouTube youtube, Playlist playlist) {

        // get youtube to list all video ids associated with the playlist
        List<String> videoIds = youTubeService.listPlaylistVideoIds(youtube, playlist.getId());

        // for each video listed by youtube that is currently not cached in the playlist entity
        for (Video video : youTubeService.populateNewVideoList(videoIds, playlist)) {

            // acquire the most suitable caption id for the video
            String captionId = youTubeService.findSuitableCaptionId(youTubeService.listCaptionsForVideo(youtube, video.getId()));

            // add captions to the video
            downloadAndParseIfValid(youtube, video, captionId);
            playlist.addVideo(video);
        }
    }

    /**
     * Attempts to download and parse the SRT data using the given
     * caption id into quote entities and subsequently add them to the video entity
     *
     * @param youtube authenticated youtube instance
     * @param video video entity
     * @param captionId caption track listing id
     */
    private void downloadAndParseIfValid(YouTube youtube, Video video, String captionId) {

        // if a caption data set was found
        if (!captionId.isEmpty()) {

            // download the srt caption data
            InputStream stream = youTubeService.downloadSRT(youtube, captionId);

            // parse srt data into quote entity and add to the video entity
            if (stream != null) {
                srtService.parseSRTForVideo(stream, video);
            }
        }
    }
}
