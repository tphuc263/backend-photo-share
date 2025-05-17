package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Tag;

import java.util.Optional;

public interface TagRepository extends MongoRepository<Tag, String> {

    Optional<Tag> findByName(String name);
}
