package share_app.tphucshareapp.service.tag;

import share_app.tphucshareapp.model.Tag;

import java.util.List;

public interface ITagService {

    List<Tag> createOrGetTags(List<String> tagNames);

    List<Tag> getAllTags();

    List<Tag> searchTags(String query);
}
