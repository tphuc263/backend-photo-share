package share_app.tphucshareapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "follows")
public class Follow {

    @Id
    private String id;

    private String followerId;

    private String followingId;

    private Instant createdAt;
}
