package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.PhotoTag;

public interface PhotoTagRepository extends MongoRepository<PhotoTag, String> {

}