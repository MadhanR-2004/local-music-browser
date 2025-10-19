package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "playlist_song_cross_ref",
        primaryKeys = {"playlistId", "songId"},
        foreignKeys = {
                @ForeignKey(entity = Playlist.class, parentColumns = "id", childColumns = "playlistId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class, parentColumns = "id", childColumns = "songId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("playlistId"), @Index("songId")})
public class PlaylistSongCrossRef {
    public long playlistId;
    public long songId;
    public int positionInPlaylist;
}


