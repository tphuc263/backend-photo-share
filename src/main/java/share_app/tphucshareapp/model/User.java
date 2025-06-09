package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import share_app.tphucshareapp.enums.UserRole;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private String imageUrl;
    private String bio;
    private Instant createdAt;

    private long photoCount;
    private long followerCount;
    private long followingCount;

    private List<String> followingIds;
}
