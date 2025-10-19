package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "mix_song_cross_ref",
        primaryKeys = {"mixId", "songId"},
        foreignKeys = {
                @ForeignKey(entity = Mix.class, parentColumns = "id", childColumns = "mixId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class, parentColumns = "id", childColumns = "songId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("mixId"), @Index("songId")})
public class MixSongCrossRef {
    public long mixId;
    public long songId;
    public int positionInMix;
}


