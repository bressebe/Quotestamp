package quotestamp.factory;

import org.springframework.stereotype.Component;
import quotestamp.entity.Quote;
import quotestamp.entity.Video;

import java.time.LocalTime;

@Component
public class QuoteFactory {
    public Quote create(LocalTime start, LocalTime end, String content, Video fk) {
        return new Quote(start, end, content, fk);
    }
}
