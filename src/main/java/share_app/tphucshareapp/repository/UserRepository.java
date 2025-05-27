package share_app.tphucshareapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import share_app.tphucshareapp.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String username);

    boolean existsByEmail(String email);

    // Text search simplified - fallback to regex if text index not available
    @Query("{ $or: [ " +
            "{ 'username': { $regex: ?0, $options: 'i' } }, " +
            "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'bio': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<User> findByTextSearch(String searchText, Pageable pageable);

    // Username search (partial match)
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    // Combined name search
    @Query("{ $or: [ " +
            "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'username': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<User> findByNameFields(String searchTerm, Pageable pageable);
}

