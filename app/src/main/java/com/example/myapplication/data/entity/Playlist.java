package com.example.myapplication.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    public String description;

    public long createdAtEpochMs;
}


