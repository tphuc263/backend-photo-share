package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.PhotoTag;

import java.util.List;

public interface PhotoTagRepository extends MongoRepository<PhotoTag, String> {
    List<PhotoTag> findByPhotoId(String photoId);
    List<PhotoTag> findByTagId(String tagId);
    List<PhotoTag> findByTagIdIn(List<String> tagIds);
    void deleteByPhotoId(String photoId);
}