package share_app.tphucshareapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    private String userId;
    private String photoId;

    private String text;
    private Instant createdAt;
}
