package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.data.entity.Mix;
import com.example.myapplication.data.entity.MixSongCrossRef;

import java.util.List;

@Dao
public interface MixDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMix(Mix mix);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCrossRefs(List<MixSongCrossRef> refs);

    @Query("SELECT * FROM mixes ORDER BY generatedAt DESC")
    List<Mix> getAllMixes();

    @SuppressWarnings("RoomWarnings.CURSOR_MISMATCH")
    @Query("SELECT s.id, s.title, s.artist, s.album, s.path, s.durationMs, s.trackNumber, s.year, s.playCount, s.dateAddedEpochMs FROM songs s INNER JOIN mix_song_cross_ref r ON s.id = r.songId WHERE r.mixId = :mixId ORDER BY r.positionInMix ASC")
    List<com.example.myapplication.data.entity.Song> getSongsForMix(long mixId);
}


