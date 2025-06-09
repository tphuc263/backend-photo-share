package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Follow;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends MongoRepository<Follow, String> {
    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    Page<Follow> findByFollowingIdOrderByCreatedAtDesc(String followingId, Pageable pageable);

    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(String followerId, Pageable pageable);

    List<Follow> findByFollowerId(String followerId);

    List<Follow> findByFollowingId(String followingId);
}
