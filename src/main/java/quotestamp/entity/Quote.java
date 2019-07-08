package quotestamp.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "quote")
public class Quote {

    @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "quote_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false) private UUID id;

    @Column(name = "quote_start", columnDefinition = "time(3)") private LocalTime start;
    @Column(name = "quote_end", columnDefinition = "time(3)") private LocalTime end;
    @Column(name = "quote_content") private String content;
    @ManyToOne @JoinColumn(name = "video_id", nullable = false) private Video fk;

    public Quote(LocalTime start, LocalTime end, String content, Video fk) {
        //this.id = UUID.randomUUID();
        this.start = start;
        this.end = end;
        this.content = content;
        this.fk = fk;
    }

    public Quote() {}

    public String getContent() {
        return content;
    }
}
