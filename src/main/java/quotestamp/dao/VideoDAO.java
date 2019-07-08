package quotestamp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import quotestamp.entity.Video;

@Repository
public interface VideoDAO extends CrudRepository<Video, String> {
}
