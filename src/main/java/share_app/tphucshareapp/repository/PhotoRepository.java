package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Photo;

public interface PhotoRepository extends MongoRepository<Photo, String> {
}