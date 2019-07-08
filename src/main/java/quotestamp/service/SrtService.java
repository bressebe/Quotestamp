package quotestamp.service;

import com.github.dnbn.submerge.api.parser.SRTParser;
import com.github.dnbn.submerge.api.subtitle.srt.SRTLine;
import com.github.dnbn.submerge.api.subtitle.srt.SRTSub;
import com.github.dnbn.submerge.api.subtitle.srt.SRTTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quotestamp.entity.Video;
import quotestamp.factory.QuoteFactory;

import java.io.InputStream;
import java.util.List;

@Service
public class SrtService {
    @Autowired private QuoteFactory quoteFactory;

    /**
     * Parses an SRT caption data stream and for each subtitle
     * within it, adds a quote entity to the given video entity
     *
     * @param stream SRT data stream
     * @param video video entity to be populated with a list of quotes
     */
    public void parseSRTForVideo(InputStream stream, Video video) {
        SRTSub sub = new SRTParser().parse(stream, "");
        for (SRTLine line : sub.getLines()) {
            SRTTime time = line.getTime();
            video.addQuote(quoteFactory.create(time.getStart(), time.getEnd(), appendTextLines(line.getTextLines()), video));
        }
    }

    /**
     * Appends and returns a single string built out of a list of strings
     *
     * @param lines list of strings associated with each individual subtitle
     * @return content for quote entity
     */
    private String appendTextLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append(" ");
        }
        return builder.toString();
    }
}
