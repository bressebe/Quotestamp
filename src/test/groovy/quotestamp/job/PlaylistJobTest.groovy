package quotestamp.job

import com.google.api.services.youtube.YouTube
import quotestamp.entity.Playlist
import quotestamp.entity.Video
import quotestamp.factory.YouTubeFactory
import quotestamp.model.CaptionSnippet
import quotestamp.service.DataService
import quotestamp.service.JsonService
import quotestamp.service.SrtService
import quotestamp.service.YouTubeService
import spock.lang.Specification
import spock.lang.Unroll

class PlaylistJobTest extends Specification {
    def playlistJob, youtube

    def setup() {
        playlistJob = new PlaylistJob()
        youtube = Mock(YouTube)
    }

    @Unroll
    def '#ids.size playlists are saved'() {
        given:
            def playlists = initEntityList(Playlist.class, ids.size())

            // don't care what youtube service methods are called outside of update() for this test
            playlistJob.youTubeService = Mock(YouTubeService) {
                _ * populateNewVideoList(*_) >> []
            }
            playlistJob.youTubeFactory = Mock(YouTubeFactory) {
                1 * create() >> Mock(YouTube)
                0 * _
            }
            playlistJob.jsonService = Mock(JsonService) {
                1 * getPlaylistIdsFromStream(_ as InputStreamReader) >> ids
                0 * _
            }
            // number of expected data service invocations dependant on the number of mocked playlist ids
            playlistJob.dataService = Mock(DataService) {
                for (def i = 0; i < ids.size(); i++) {
                    1 * findOrCreatePlaylist(ids[i] as String) >> playlists[i]
                    1 * savePlaylist(playlists[i] as Playlist)
                }
                0 * _
            }
        when:
            playlistJob.update()
        then:
            notThrown(Throwable)
        where:
            ids << [[], ["foo"], ["foo", "bar"]]
    }

    @Unroll
    def '#count videos are added to a playlist'() {
        given:
            def videos = initEntityList(Video.class, count)
            def videoIds = []
            def response = []

            // number of expected playlist invocations dependant on the number of mocked videos
            def playlist = Mock(Playlist) {
                for (def i = 0; i < count; i++) {
                    1 * addVideo(videos[i] as Video)
                }
                1 * getId() >> "id"
                0 * _
            }
            playlistJob.youTubeService = Mock(YouTubeService) {
                1 * listPlaylistVideoIds(youtube as YouTube, "id" as String) >> videoIds
                1 * populateNewVideoList(videoIds as List<String>, playlist as Playlist) >> videos

                // number of expected youtube service invocations for these methods dependant on the number of mocked videos
                for (def i = 0; i < count; i++) {
                    1 * listCaptionsForVideo(youtube as YouTube, null) >> response
                    1 * findSuitableCaptionId(response as List<CaptionSnippet>) >> ""
                }
                0 * _
            }
        when:
            playlistJob.addVideosForPlaylist(youtube, playlist)
        then:
            notThrown(Throwable)
        where:
            count << [0, 1, 2]
    }

    def 'caption id is empty'() {
        given:
            playlistJob.youTubeService = Mock(YouTubeService) {
                0 * _
            }
            playlistJob.srtService = Mock(SrtService) {
                0 * _
            }
        when:
            playlistJob.downloadAndParseIfValid(youtube, null, "")
        then:
            notThrown(Throwable)
    }

    def 'null is returned when downloading srt data'() {
        given:
            playlistJob.youTubeService = Mock(YouTubeService) {
                1 * downloadSRT(youtube as YouTube, "foo" as String) >> null
                0 * _
            }
            playlistJob.srtService = Mock(SrtService) {
                0 * _
            }
        when:
            playlistJob.downloadAndParseIfValid(youtube, null, "foo")
        then:
            notThrown(Throwable)
    }

    def 'srt stream data is parsed'() {
        given:
            def video = Mock(Video)
            def stream = Mock(FileInputStream)
            playlistJob.youTubeService = Mock(YouTubeService) {
                1 * downloadSRT(youtube as YouTube, "foo" as String) >> stream
                0 * _
            }
            playlistJob.srtService = Mock(SrtService) {
                1 * parseSRTForVideo(stream as FileInputStream, video)
                0 * _
            }
        when:
            playlistJob.downloadAndParseIfValid(youtube, video, "foo")
        then:
            notThrown(Throwable)
    }

    // initializes a list of mocked entities
    def initEntityList(Class entity, int size) {
        def list = []
        size.times {
            list << Mock(entity)
        }
        list
    }
}
