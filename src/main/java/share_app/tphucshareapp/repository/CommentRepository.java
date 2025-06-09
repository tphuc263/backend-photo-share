package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Comment;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPhotoIdOrderByCreatedAtAsc(String photoId);

    long countByPhotoId(String photoId);

    void deleteAllByPhotoId(String photoId);
}
