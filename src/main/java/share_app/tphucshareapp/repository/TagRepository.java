package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Tag;

public interface TagRepository extends MongoRepository<Tag, String> {

}
