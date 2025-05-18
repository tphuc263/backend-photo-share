package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Like;

public interface LikeRepository extends MongoRepository<Like, String> {

}
