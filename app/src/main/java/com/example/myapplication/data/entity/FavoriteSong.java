package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "favorite_songs",
        primaryKeys = {"songId"},
        foreignKeys = @ForeignKey(entity = Song.class, parentColumns = "id", childColumns = "songId", onDelete = ForeignKey.CASCADE),
        indices = @Index("songId"))
public class FavoriteSong {
    public long songId;
    public long likedAtEpochMs;
}


