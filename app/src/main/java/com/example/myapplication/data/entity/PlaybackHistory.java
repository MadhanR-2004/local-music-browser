package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playback_history")
public class PlaybackHistory {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long songId;
    public long timestamp;
    public long durationListened;
    public float completionPercentage;
}
