package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import share_app.tphucshareapp.model.Like;

import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {

    @Query("{ 'photoId': ?0, 'userId': ?1 }")
    Optional<Like> findByPhotoIdAndUserId(String photoId, String userId);

    @Query(value = "{ 'photoId': ?0 }", count = true)
    long countByPhotoId(String photoId);
}
