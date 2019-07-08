package quotestamp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import quotestamp.entity.Quote;

import java.util.UUID;

@Repository
public interface QuoteDAO extends CrudRepository<Quote, UUID> {
}
