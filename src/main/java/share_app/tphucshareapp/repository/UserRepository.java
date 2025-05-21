package share_app.tphucshareapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import share_app.tphucshareapp.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String username);

    boolean existsByEmail(String email);
}

