package share_app.tphucshareapp.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection="likes")
@CompoundIndex(name = "photo_user_index", def = "{'photoId': 1, 'userId': 1}", unique = true)
public class Like {

    @Id
    private String id;

    private String photoId;

    private String userId;

    private Instant createdAt;
}
