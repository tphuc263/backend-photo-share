package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {
    List<Like> findByPhotoIdOrderByCreatedAtDesc(String photoId);

    long countByPhotoId(String photoId);

    Optional<Like> findByPhotoIdAndUserId(String photoId, String userId);

    boolean existsByPhotoIdAndUserId(String photoId, String userId);
}
