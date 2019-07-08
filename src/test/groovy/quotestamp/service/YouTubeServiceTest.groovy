package quotestamp.service

import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.CaptionListResponse
import quotestamp.entity.Playlist
import quotestamp.entity.Video
import quotestamp.factory.CaptionSnippetFactory
import quotestamp.factory.PlaylistItemListResponseFactory
import quotestamp.factory.VideoFactory
import quotestamp.model.CaptionSnippet
import quotestamp.model.PlaylistItemListResponse
import spock.lang.Specification
import spock.lang.Unroll

class YouTubeServiceTest extends Specification {
    def youtubeService, youtube

    def setup() {
        youtubeService = new YouTubeService()
    }

    def 'an empty list returned when an error is thrown while retrieving playlist items'() {
        given:
            youtube = Mock(YouTube) {
                1 * playlistItems() >> Mock(YouTube.PlaylistItems) {
                    1 * list("contentDetails" as String) >> {
                        throw new IOException()
                    }
                    0 * _
                }
                0 * _
            }
        expect:
            youtubeService.listPlaylistVideoIds(youtube, "foo").isEmpty()
    }

    def '3 video ids are listed'() {
        given:
            // loops three times; returns 0 ids, then 1 id, then 2 ids
            def videoIds = [[], ["video1"], ["video2", "video3"]]
            def expected = []
            def responses = []
            def items = []

            // generate the expected list of ids
            videoIds.size().times {
                expected << videoIds[it]
                responses << GroovyMock(com.google.api.services.youtube.model.PlaylistItemListResponse)
            }
            // map containing various mock values for each loop iteration
            def map = [setToken : ["", "token1", "token2"], getToken : ["token1", "token2", null], getVideoIds : videoIds, responses : responses,  count : 3]

            // expected invocations for each mocked playlist item list response
            youtubeService.playlistItemListResponseFactory = Mock(PlaylistItemListResponseFactory) {
                for (def index = 0; index < map.count; index++) {
                    1 * create(responses[index] as com.google.api.services.youtube.model.PlaylistItemListResponse) >> Mock(PlaylistItemListResponse) {
                        1 * getVideoIds() >> map.getVideoIds[index]
                        1 * getNextPageToken() >> map.getToken[index]
                        0 * _
                    }
                }
                0 * _
            }
            // expected invocations for each mocked list builder
            map.count.times {
                def index = it
                items << Mock(YouTube.PlaylistItems) {
                    1 * list("contentDetails" as String) >> Mock(YouTube.PlaylistItems.List) {
                        1 * setMaxResults(_) >> it
                        1 * setPlaylistId("foo" as String) >> it
                        1 * setKey(_ as String) >> it
                        1 * setPageToken(map.setToken[index] as String) >> it
                        1 * execute() >> responses[index]
                        0 * _
                    }
                    0 * _
                }
            }
            youtube = Mock(YouTube) {
                map.count * playlistItems() >>> items
                0 * _
            }
        when:
            def results = youtubeService.listPlaylistVideoIds(youtube, "foo")
        then:
            map.count.times {
                results[it] == expected[it]
            }
            results.size() == expected.size()
    }

    @Unroll
    def '#count new video(s) are created with ids = #videoIds'() {
        given:
            def expected = []
            def playlist = Mock(Playlist) {
                // invocation amount is dependent on the video id count
                for (def i = 0; i < videoIds.size(); i++) {
                    1 * containsVideo(videoIds[i] as String) >> (videoIds[i] == "old") // return true if "old"
                }
                1 * getId()
                0 * _
            }
            count.times {
                expected << Mock(Video)
            }
            youtubeService.videoFactory = Mock(VideoFactory) {
                // invocation amount is dependant on the expected amount of videos created
                count * create("new" as String, playlist as Playlist) >>> expected
                0 * _
            }
        when:
            def results = youtubeService.populateNewVideoList(videoIds, playlist).reverse()
        then:
            results[0] == expected[0]
            results[1] == expected[1]
            results.size() == expected.size()
        where:
            videoIds        | count
            ["new", "new"]  | 2
            ["new", "old"]  | 1
            ["old", "new"]  | 1
            ["old", "old"]  | 0
            ["new"]         | 1
            ["old"]         | 0
            []              | 0
    }

    def 'an empty list returned when an error is thrown while listing captions for a video'() {
        given:
            youtube = Mock(YouTube) {
                1 * captions() >> Mock(YouTube.Captions) {
                    1 * list("snippet", "foo") >> {
                        throw new IOException()
                    }
                    0 * _
                }
                0 * _
            }
        expect:
            youtubeService.listCaptionsForVideo(youtube, "foo").isEmpty()
    }

    def 'a caption snippet list is returned for a video'() {
        given:
            def snippets = []
            def response = GroovyMock(CaptionListResponse)
            youtube = Mock(YouTube) {
                1 * captions() >> Mock(YouTube.Captions) {
                    1 * list("snippet", "foo") >> Mock(YouTube.Captions.List) {
                        1 * execute() >> response
                        0 * _
                    }
                    0 * _
                }
                0 * _
            }
            youtubeService.captionSnippetFactory = Mock(CaptionSnippetFactory) {
                1 * createList(response as CaptionListResponse) >> snippets
                0 * _
            }
        expect:
            youtubeService.listCaptionsForVideo(youtube, "foo") == snippets
    }

    @Unroll
    def '#desc is returned'() {
        expect:
            youtubeService.findSuitableCaptionId(initSnippetMocks(test)) == expected
        where:
            test    | expected  | desc
            1       | "std"     | "standard caption id listed after an auto caption id"
            2       | "std"     | "standard caption id listed before an auto caption id"
            3       | "auto"    | "auto caption id listed before invalid caption ids"
            4       | ""        | "no caption id from a list"
            5       | ""        | "no caption id from an empty list"
    }

    def initSnippetMocks(def test) {
        def mocks
        def nonEnglish = Mock(CaptionSnippet) {
            getLanguage() >> "foo"
            getTrackKind() >> "Standard"
            getId() >> "foo"
        }
        def nonStandard = Mock(CaptionSnippet) {
            getLanguage() >> "EN"
            getTrackKind() >> "bar"
            getId() >> "bar"
        }
        def automatic = Mock(CaptionSnippet) {
            getLanguage() >> "EN"
            getTrackKind() >> "ASR"
            getId() >> "auto"
        }
        def standard = Mock(CaptionSnippet) {
            getLanguage() >> "EN"
            getTrackKind() >> "Standard"
            getId() >> "std"
        }
        switch(test) {
            case 1: mocks = [nonEnglish, nonStandard, automatic, standard]; break
            case 2: mocks = [nonEnglish, nonStandard, standard, automatic]; break
            case 3: mocks = [automatic, nonEnglish, nonStandard]; break
            case 4: mocks = [nonEnglish, nonStandard]; break
            case 5: mocks = []; break
            default: mocks = []
        }
        mocks
    }

    def 'an empty list returned when an error is thrown while downloading srt data'() {
        given:
            youtube = Mock(YouTube) {
                1 * captions() >> Mock(YouTube.Captions) {
                    1 * download("foo") >> {
                        throw new IOException()
                    }
                    0 * _
                }
                0 * _
            }
        expect:
            youtubeService.downloadSRT(youtube, "foo") == null
    }

    def 'srt data is returned for a video'() {
        given:
            def stream = Mock(FileInputStream)
            youtube = Mock(YouTube) {
                1 * captions() >> Mock(YouTube.Captions) {
                    1 * download("foo") >> Mock(YouTube.Captions.Download) {
                        1 * setTfmt("srt") >> it
                        1 * executeMediaAsInputStream() >> stream
                        0 * _
                    }
                    0 * _
                }
                0 * _
            }
        expect:
            youtubeService.downloadSRT(youtube, "foo") == stream
    }
}
