package quotestamp.entity

import spock.lang.Specification
import spock.lang.Unroll

class PlaylistTest extends Specification {
    def playlist

    def setup() {
        playlist = new Playlist("foo")
    }

    def 'the contructor initializes it\'s members from the params'() {
        expect:
            playlist.id == "foo"
            playlist.videos.isEmpty()
    }

    def 'the playlist id is returned'() {
        expect:
            playlist.getId() == "foo"
    }

    @Unroll
    def '#amt videos are added to the list'() {
        given:
            def expected = []
            amt.times {
                expected << Mock(Video)
            }
        when:
            amt.times {
                playlist.addVideo(expected[it])
            }
        then:
            amt.times {
                expected[it] == playlist.videos[it]
            }
        where:
            amt << [1, 2, 3]
    }

    @Unroll
    def 'when #id is searched for in videos containing ids [a, b] #expected is returned'() {
        given:
            ['a', 'b'].each {
                def id = it
                playlist.videos << Mock(Video) {
                    _ * getId() >> id
                    0 * _
                }
            }
        expect:
            playlist.containsVideo(id) == expected
        where:
            id  | expected
            'a' | true
            'b' | true
            'c' | false
    }
}
