package com.schedule.app.user.adapter.out.persistence;

import com.schedule.app.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getPhoneNumber(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
