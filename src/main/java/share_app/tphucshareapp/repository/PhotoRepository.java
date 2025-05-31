package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import share_app.tphucshareapp.model.Photo;

import java.util.List;

public interface PhotoRepository extends MongoRepository<Photo, String> {
    Page<Photo> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<Photo> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Photo> findByUserIdOrderByCreatedAtDesc(String userId);

    // Text search in captions - simplified approach
    @Query("{ 'caption': { $regex: ?0, $options: 'i' } }")
    Page<Photo> findByTextSearch(String searchText, Pageable pageable);

    // Caption search (partial match) - this works better for simple cases
    Page<Photo> findByCaptionContainingIgnoreCase(String caption, Pageable pageable);

    // Search photos by IDs with pagination
    @Query("{ '_id': { $in: ?0 } }")
    Page<Photo> findByPhotoIds(List<String> photoIds, Pageable pageable);
}