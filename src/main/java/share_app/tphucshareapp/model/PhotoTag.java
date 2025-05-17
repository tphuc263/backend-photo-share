package share_app.tphucshareapp.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "photo_tag")
@CompoundIndex(name = "photo_tag_unique", def = "{'photoId': 1, 'tagId': 1}", unique = true)
public class PhotoTag {

    @Id
    private String id;

    private String photoId;
    private String tagId;

    private Instant createdAt;
}
