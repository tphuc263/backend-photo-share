package share_app.tphucshareapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import share_app.tphucshareapp.enums.UserRole;

import java.time.Instant;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true, sparse = true)
    private String email;

    private String password;

    private UserRole role;

    private String imageUrl;
    private String firstName;
    private String lastName;
    private String bio;

    private Instant createdAt;
}
