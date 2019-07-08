package quotestamp.entity

import spock.lang.Specification

import java.time.LocalTime

class QuoteTest extends Specification {
    def quote, start, end, video

    def setup() {
        start = GroovyMock(LocalTime)
        end = GroovyMock(LocalTime)
        video = Mock(Video)
        quote = new Quote(start, end, "foo", video)
    }

    def 'the constructor initializes every member from the params except id'() {
        expect:
            quote.id == null
            quote.start == start
            quote.end == end
            quote.content == "foo"
            quote.fk == video
    }

    def 'content is returned'() {
        expect:
            quote.getContent() == "foo"
    }
}
