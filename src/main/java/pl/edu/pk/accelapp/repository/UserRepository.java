package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.pk.accelapp.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //@Query("SELECT u FROM User u LEFT JOIN FETCH u.uploadedFiles WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);


}