package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import share_app.tphucshareapp.model.Photo;

import java.util.List;

public interface PhotoRepository extends MongoRepository<Photo, String> {

    @Query(value = "{ 'photoId': ?0 }", count = true)
    long countLikes(String photoId);

    @Query(value = "{ 'photoId': ?0 }", count = true)
    long countComments(String photoId);

    @Query(value = "{ 'userId': ?0 }", count = true)
    long countByUserId(String userId);
}