package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    private String photoId;
    private String userId;
    private String text;
    private Instant createdAt;
    private EmbeddedUser user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddedUser {
        private String username;
        private String userImageUrl;
    }
}
