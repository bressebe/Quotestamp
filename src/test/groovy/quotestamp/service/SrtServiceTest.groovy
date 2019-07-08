package quotestamp.service

import quotestamp.entity.Quote
import quotestamp.entity.Video
import quotestamp.factory.QuoteFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalTime

class SrtServiceTest extends Specification {
    def srtService

    def setup() {
        srtService = new SrtService()
    }

    def 'an srt stream containing no subtitles will add no quotes to a video'() {
        given:
            def video = Mock(Video) {
                0 * _
            }
            srtService.quoteFactory = Mock(QuoteFactory) {
                0 * _
            }
        when:
            srtService.parseSRTForVideo(new ByteArrayInputStream("".getBytes()), video)
        then:
            notThrown(Throwable)
    }

    def 'an srt stream containing 2 subtitles is parsed and 2 quotes are added to a video'() {
        given:
            // mocked srt stream
            def start1 = "00:00:01.000"
            def end1 = "00:00:09.000"
            def start2 = "00:00:10.000"
            def end2 = "00:00:19.000"
            def content1 = "foo"
            def content2 = "bar"
            def srt = "1\n$start1 --> $end1\n$content1\n\n2\n$start2 --> $end2\n$content2"

            // mocked quote entities
            def quote1 = Mock(Quote)
            def quote2 = Mock(Quote)
            def video = Mock(Video) {
                1 * addQuote(quote1)
                1 * addQuote(quote2)
                0 * _
            }
            // expected factory invocations
            srtService.quoteFactory = Mock(QuoteFactory) {
                1 * create(LocalTime.parse(start1) as LocalTime, LocalTime.parse(end1) as LocalTime,
                            "$content1 " as String, video as Video) >> quote1
                1 * create(LocalTime.parse(start2) as LocalTime, LocalTime.parse(end2) as LocalTime,
                            "$content2 " as String, video as Video) >> quote2
                0 * _
            }
        when:
            srtService.parseSRTForVideo(new ByteArrayInputStream(srt.getBytes()), video)
        then:
            notThrown(Throwable)
    }

    @Unroll
    def 'a string is built from a list of #size strings'() {
        given:
            def expected = ""
            def list = []
            size.times {
                list << "foo"
                expected += "foo "
            }
        expect:
            srtService.appendTextLines(list) == expected
        where:
            size << [0, 1, 3]
    }
}
