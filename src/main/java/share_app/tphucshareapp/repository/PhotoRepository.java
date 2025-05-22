package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Photo;

import java.util.List;

public interface PhotoRepository extends MongoRepository<Photo, String> {
    Page<Photo> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<Photo> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByUserId(String userId);

    List<Photo> findByUserIdOrderByCreatedAtDesc(String userId);
}