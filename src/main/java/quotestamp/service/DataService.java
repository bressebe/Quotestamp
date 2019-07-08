package quotestamp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quotestamp.dao.PlaylistDAO;
import quotestamp.entity.Playlist;
import quotestamp.factory.PlaylistFactory;

@Service
public class DataService {
    @Autowired private PlaylistDAO playlistDAO;
    @Autowired private PlaylistFactory playlistFactory;

    public Playlist findOrCreatePlaylist(String id) {
        return playlistDAO.findById(id).orElse(playlistFactory.create(id));
    }

    public void savePlaylist(Playlist playlist) {
        playlistDAO.save(playlist);
    }
}
