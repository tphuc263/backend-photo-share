package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import share_app.tphucshareapp.model.Follow;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends MongoRepository<Follow, String> {

    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    @Query(value = "{ 'followerId': ?0 }", fields = "{ 'followingId': 1, '_id': 0 }")
    List<String> findFollowingIdsByFollowerId(String followerId);

    @Query(value = "{ 'followingId': ?0 }", fields = "{ 'followerId': 1, '_id': 0 }")
    List<String> findFollowerIdsByFollowingId(String followingId);

    @Query(value = "{ 'followerId': ?0 }", count = true)
    long countFollowingByFollowerId(String followerId);

    @Query(value = "{ 'followingId': ?0 }", count = true)
    long countFollowersByFollowingId(String followingId);

    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);
}
