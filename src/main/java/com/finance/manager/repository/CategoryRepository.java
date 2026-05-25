package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity persistence operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsCustomFalse();

    List<Category> findByUserAndIsCustomTrue(User user);

    Optional<Category> findByNameAndIsCustomFalse(String name);

    Optional<Category> findByNameAndUser(String name, User user);

    @Query("SELECT c FROM Category c WHERE c.isCustom = false OR c.user = :user")
    List<Category> findAllAccessibleByUser(@Param("user") User user);

    boolean existsByNameAndUser(String name, User user);

    Optional<Category> findByNameAndUserOrNameAndIsCustomFalse(String name1, User user, String name2);
}
