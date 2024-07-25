package ru.practicum.users.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.practicum.users.dto.SubscriptionDto;
import ru.practicum.users.dto.UserShortDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DbSubscriptionsStorage implements SubscriptionsStorage {

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public Optional<SubscriptionDto> getSubscriptions(long userId) {
        String sqlReadFilmQuery =
                "SELECT u.id,\n" +
                "       u.name user_name,\n" +
                "       u.email,\n" +
                "       ARRAY_AGG(us.subscription_id) AS subscriptions_id,\n" +
                "       ARRAY_AGG(s.name) AS subscription_names,\n" +
                "FROM users AS u\n" +
                "JOIN user_subscriptions AS us ON u.id = us.user_id\n" +
                "JOIN users AS s ON us.subscription_id = s.id\n" +
                "WHERE u.id = :id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", userId);
        return jdbcTemplate.query(sqlReadFilmQuery, namedParameters, this::makeUser);
    }

    private Optional<SubscriptionDto> makeUser(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            var resultBuilder = SubscriptionDto.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("user_name"))
                    .email(resultSet.getString("email"));

            var subscriptionIds = resultSet.getArray("subscriptions_id");
            var subscriptionNames = resultSet.getArray("subscription_names");
            Set<UserShortDto> subscriptions = new HashSet<>();
            if (subscriptionIds != null &&  subscriptionNames != null) {
                var subscriptionIdsArray = Arrays.stream((Object[]) subscriptionIds.getArray())
                        .filter(Objects::nonNull)
                        .mapToLong((t) -> (Long) t)
                        .toArray();
                var subscriptionNamesArray = Arrays.stream((Object[]) subscriptionNames.getArray())
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .toArray(String[]::new);
                for (var i = 0; i < subscriptionIdsArray.length && i < subscriptionNamesArray.length; ++i) {
                    subscriptions.add(new UserShortDto(subscriptionIdsArray[i], subscriptionNamesArray[i]));
                }
            }
            return Optional.of(resultBuilder.subscriptions(subscriptions).build());
        }
        return Optional.empty();
    }

}
