package quotestamp.entity

import spock.lang.Specification
import spock.lang.Unroll

class VideoTest extends Specification {
    def video, playlist

    def setup() {
        playlist = Mock(Playlist)
        video = new Video("foo", playlist)
    }

    def 'the contructor initializes it\'s members from the params'() {
        expect:
            video.id == "foo"
            video.fk == playlist
            video.quotes.isEmpty()
    }

    def 'the video id is returned'() {
        expect:
            video.getId() == "foo"
    }

    @Unroll
    def '#amt quotes are added to the list'() {
        given:
            def expected = []
            amt.times {
                expected << Mock(Quote)
            }
        when:
            amt.times {
                video.addQuote(expected[it])
            }
        then:
            amt.times {
                expected[it] == video.quotes[it]
            }
        where:
            amt << [1, 2, 3]
    }
}
