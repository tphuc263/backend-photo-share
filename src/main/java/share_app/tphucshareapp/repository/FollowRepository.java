package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Follow;

public interface FollowRepository extends MongoRepository<Follow, String> {

}
