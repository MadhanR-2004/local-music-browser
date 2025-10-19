package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.Fts4;

@Fts4(contentEntity = Song.class)
@Entity(tableName = "songs_fts")
public class SongFts {
    public String title;
    public String artist;
    public String album;
    public String genre;
}


