package share_app.tphucshareapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "follows")
@CompoundIndex(
        name = "follower_following_id",
        def = "{'followerId': 1, 'followingId': 1}",
        unique = true)
public class Follow {

    @Id
    private String id;

    private String followerId;

    private String followingId;
}
