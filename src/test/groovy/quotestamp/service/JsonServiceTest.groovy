package quotestamp.service

import spock.lang.Specification

class JsonServiceTest extends Specification {
    def jsonService

    def setup() {
        jsonService = new JsonService()
    }

    def 'an exception is thrown when the playlist ids fail to parse'() {
        when:
            jsonService.getPlaylistIdsFromStream(new InputStreamReader(new ByteArrayInputStream("foo".getBytes())))
        then:
            thrown(RuntimeException)
    }

    def 'playlist ids are parsed from a stream containing 2 items'() {
        given:
            def json = "{\"playlists\":[{\"id\":\"foo\"},{\"id\":\"bar\"}]}"
        when:
            def results = jsonService.getPlaylistIdsFromStream(new InputStreamReader(new ByteArrayInputStream(json.getBytes())))
        then:
            results[0] == "foo"
            results[1] == "bar"
            results.size() == 2
    }
}
