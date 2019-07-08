package quotestamp.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist")
public class Playlist {

    @Id @Column(name = "playlist_id") private String id;
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "fk", fetch = FetchType.EAGER) private List<Video> videos;

    public Playlist(String id) {
        this.id = id;
        this.videos = new ArrayList<>();
    }

    public Playlist() {}

    public String getId() {
        return id;
    }

    public void addVideo(Video video) {
        this.videos.add(video);
    }

    public boolean containsVideo(String id) {
        boolean unique = true;
        for (Video video : videos) {
            if (video.getId().equals(id)) {
                unique = false;
                break;
            }
        }
        return !unique;
    }
}
