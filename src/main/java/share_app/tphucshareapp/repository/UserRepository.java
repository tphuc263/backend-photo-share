package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import share_app.tphucshareapp.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String username);

    boolean existsByEmail(String email);

    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    Page<User> findByUsernameContaining(String username, Pageable pageable);

    @Query("{ $or: [ " +
            "{ 'username': { $regex: ?0, $options: 'i' } }, " +
            "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'lastName': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<User> findByNameFields(String searchTerm, Pageable pageable);

    @Query("{ 'bio': { $regex: ?0, $options: 'i' } }")
    Page<User> findByBioContaining(String bio, Pageable pageable);
}

