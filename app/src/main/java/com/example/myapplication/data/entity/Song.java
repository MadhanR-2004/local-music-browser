package com.example.myapplication.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs")
public class Song {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title = "";

    @NonNull
    public String artist = "Unknown Artist";

    @NonNull
    public String album = "Unknown Album";

    public String genre;

    @NonNull
    public String path = "";

    public long durationMs;

    public int trackNumber;

    public int year;

    public int playCount;

    public long dateAddedEpochMs;
}


