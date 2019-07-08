package quotestamp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import quotestamp.entity.Playlist;

@Repository
public interface PlaylistDAO extends CrudRepository<Playlist, String> {
}