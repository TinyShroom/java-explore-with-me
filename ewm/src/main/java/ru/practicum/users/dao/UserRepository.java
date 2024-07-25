package ru.practicum.users.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.users.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAllByIdIn(List<Long> ids, Pageable pageable);

    @Modifying
    @Query(value = "MERGE INTO user_subscriptions(user_id, subscription_id) values (?1, ?2)", nativeQuery = true)
    void addSubscription(long userId, long subscriptionId);

    @Modifying
    @Query(value = "DELETE FROM user_subscriptions WHERE user_id = ?1 AND subscription_id = ?2", nativeQuery = true)
    void deleteSubscription(long userId, long subscriptionId);
}
