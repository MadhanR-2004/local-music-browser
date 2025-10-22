package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = {"playlistId", "songId"},
    foreignKeys = {
        @ForeignKey(
            entity = Playlist.class,
            parentColumns = "id",
            childColumns = "playlistId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Song.class,
            parentColumns = "id",
            childColumns = "songId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("playlistId"),
        @Index("songId")
    }
)
public class PlaylistSongCrossRef {
    public long playlistId;
    public long songId;
    public int position; // Position in the playlist
    public long addedAtEpochMs; // When the song was added to the playlist
    
    public PlaylistSongCrossRef() {}
    
    public PlaylistSongCrossRef(long playlistId, long songId, int position) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.position = position;
        this.addedAtEpochMs = System.currentTimeMillis();
    }
}