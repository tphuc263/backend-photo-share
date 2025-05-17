package share_app.tphucshareapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "photos")
@CompoundIndexes({
        @CompoundIndex(name = "user_created_index", def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "created_at_index", def = "{'createdAt': -1}")
})
public class Photo {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String imageURL;

    private String caption;

    private Instant createdAt;
}
