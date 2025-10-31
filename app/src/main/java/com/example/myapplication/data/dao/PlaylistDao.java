package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.myapplication.data.entity.Playlist;
import com.example.myapplication.data.entity.PlaylistSongCrossRef;
import com.example.myapplication.data.entity.Song;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Playlist playlist);

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    List<Playlist> getAll();
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    Playlist getById(long playlistId);
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    void deleteById(long playlistId);
    
    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    void updateName(long playlistId, String newName);

    @Query("UPDATE playlists SET description = :newDescription WHERE id = :playlistId")
    void updateDescription(long playlistId, String newDescription);
    
    // PlaylistSongCrossRef methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlaylistSong(PlaylistSongCrossRef crossRef);
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    void removeSongFromPlaylist(long playlistId, long songId);
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    void clearPlaylist(long playlistId);
    
    @Query("SELECT s.* FROM songs s " +
           "INNER JOIN playlist_song_cross_ref psc ON s.id = psc.songId " +
           "WHERE psc.playlistId = :playlistId " +
           "ORDER BY psc.position ASC")
    List<Song> getSongsInPlaylist(long playlistId);
    
    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    int getPlaylistSongCount(long playlistId);
    
    @Query("SELECT MAX(position) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    Integer getMaxPosition(long playlistId);

    // Position helpers for reordering
    @Query("SELECT position FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    Integer getPosition(long playlistId, long songId);

    @Query("UPDATE playlist_song_cross_ref SET position = :newPosition WHERE playlistId = :playlistId AND songId = :songId")
    void updateSongPosition(long playlistId, long songId, int newPosition);
}


