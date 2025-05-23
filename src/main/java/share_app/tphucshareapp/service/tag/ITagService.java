package share_app.tphucshareapp.service.tag;

import share_app.tphucshareapp.model.Tag;

import java.util.List;

public interface ITagService {

    List<Tag> createOrGetTags(List<String> tagNames);

    void addTagsToPhoto(String photoId, List<Tag> tags);

    void removeTagsFromPhoto(String photoId);

    List<Tag> getPhotoTags(String photoId);

    List<Tag> getAllTags();

    List<Tag> searchTags(String query);
}
