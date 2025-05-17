package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.User;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);

    List<User> findByUsernameContainingOrFirstNameContainingOrLastNameContaining(
            String username, String firstName, String lastName);

    User findByUsername(String username);
}

