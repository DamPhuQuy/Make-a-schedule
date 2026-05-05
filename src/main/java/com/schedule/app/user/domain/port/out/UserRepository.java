package com.schedule.app.user.domain.port.out;

import com.schedule.app.user.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    Optional<User> findByPhoneNumber(String phoneNumber);
}
