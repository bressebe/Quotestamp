package quotestamp.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "video")
public class Video {

    @Id @Column(name = "video_id") private String id;
    @ManyToOne @JoinColumn(name = "playlist_id", nullable = false) private Playlist fk;
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "fk") private List<Quote> quotes;

    public Video(String id, Playlist fk) {
        this.id = id;
        this.fk = fk;
        this.quotes = new ArrayList<>();
    }

    public Video() {}

    public String getId() {
        return id;
    }

    public void addQuote(Quote quote) {
        this.quotes.add(quote);
    }
}
