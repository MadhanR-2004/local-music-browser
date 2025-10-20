
package com.example.myapplication.data.dao;
import androidx.room.Delete;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.entity.Song;

import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface SongDao {
    @Delete
    void deleteAll(List<Song> songs);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Song song);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Song> songs);

    @Update
    int update(Song song);

    @Query("SELECT * FROM songs ORDER BY title ASC")
    List<Song> getAll();

    // Reactive queries for live UI updates
    @Query("SELECT * FROM songs ORDER BY title ASC")
    Flow<List<Song>> getAllFlow();

    @Query("DELETE FROM songs")
    void clear();

    @Query("SELECT * FROM songs WHERE path = :path LIMIT 1")
    Song getByPath(String path);

        @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
        Song getById(long id);

    @Query("SELECT s.* FROM songs s INNER JOIN favorite_songs f ON s.id = f.songId ORDER BY f.likedAtEpochMs DESC")
    List<Song> getLikedSongs();

    @Query("SELECT album AS name, artist AS artist, COUNT(*) AS count FROM songs GROUP BY album, artist ORDER BY album ASC")
    List<com.example.myapplication.ui.library.AlbumItem> getAlbumsDerived();

    @Query("SELECT artist AS name, COUNT(*) AS count FROM songs GROUP BY artist ORDER BY artist ASC")
    List<com.example.myapplication.ui.library.ArtistItem> getArtistsDerived();

    @Query("SELECT * FROM songs WHERE album = :albumName AND artist = :artistName ORDER BY trackNumber ASC, title ASC")
    List<Song> getSongsByAlbum(String albumName, String artistName);

    @Query("SELECT * FROM songs WHERE artist = :artistName ORDER BY album ASC, trackNumber ASC, title ASC")
    List<Song> getSongsByArtist(String artistName);

    @Query("SELECT songs.* FROM songs JOIN songs_fts ON(songs.rowid = songs_fts.rowid) WHERE songs_fts MATCH :q LIMIT 100")
    List<Song> searchFts(String q);

    @Query("SELECT * FROM songs ORDER BY title ASC")
    List<Song> getAllOrderedByTitle();

    @Query("SELECT * FROM songs ORDER BY dateAddedEpochMs DESC")
    List<Song> getRecentlyAdded();

    @Query("SELECT * FROM songs ORDER BY dateAddedEpochMs DESC")
    Flow<List<Song>> getRecentlyAddedFlow();

    @Query("SELECT * FROM songs WHERE playCount < 3 ORDER BY RANDOM() LIMIT 10")
    List<Song> getDiscoverSongs();
}



