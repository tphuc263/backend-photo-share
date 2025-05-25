package share_app.tphucshareapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import share_app.tphucshareapp.enums.UserRole;

import java.time.Instant;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @TextIndexed(weight = 3)
    private String username;

    private String email;

    private String password;

    private UserRole role;

    private String imageUrl;

    @TextIndexed(weight = 2)
    private String firstName;

    @TextIndexed(weight = 2)
    private String lastName;

    @TextIndexed(weight = 1)
    private String bio;

    private Instant createdAt;
}
