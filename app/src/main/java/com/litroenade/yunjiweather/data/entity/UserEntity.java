package com.litroenade.yunjiweather.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "user",
        indices = {
                @Index(value = {"username"}, unique = true)
        }
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String username;

    @NonNull
    public String passwordHash;

    @NonNull
    public String passwordSalt;

    @NonNull
    public String displayName;

    public long createTime;

    public long lastLoginTime;

    public UserEntity(
            @NonNull String username,
            @NonNull String passwordHash,
            @NonNull String passwordSalt,
            @NonNull String displayName,
            long createTime,
            long lastLoginTime
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.displayName = displayName;
        this.createTime = createTime;
        this.lastLoginTime = lastLoginTime;
    }
}
