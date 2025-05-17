package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.Comment;

public interface CommentRepository extends MongoRepository<Comment, String> {

    void deleteByPhotoId(String photoId);
}
