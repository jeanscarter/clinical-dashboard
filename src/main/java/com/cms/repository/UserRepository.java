package com.cms.repository;

import com.cms.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    List<User> findActive();

    User save(User user);

    void update(User user);

    void deactivate(Integer id);

    void delete(Integer id);

    boolean existsByUsername(String username);
}
