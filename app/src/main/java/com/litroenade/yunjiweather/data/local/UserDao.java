package com.litroenade.yunjiweather.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.litroenade.yunjiweather.data.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert
    long insert(UserEntity user);

    @Query("SELECT * FROM `user` WHERE username = :username LIMIT 1")
    UserEntity findByNormalizedUsername(String username);

    @Query("SELECT * FROM `user` WHERE id = :id LIMIT 1")
    UserEntity findById(long id);

    @Query("UPDATE `user` SET lastLoginTime = :lastLoginTime WHERE id = :id")
    void updateLastLoginTime(long id, long lastLoginTime);
}
