package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Tag;

import java.util.List;
import java.util.Set;

public interface TagRepository extends MongoRepository<Tag, String> {
    List<Tag> findByNameIn(Set<String> names);

    List<Tag> findByNameContainingIgnoreCase(String name);
}
