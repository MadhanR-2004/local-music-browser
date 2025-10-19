package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.myapplication.data.entity.PlaybackHistory;
import java.util.List;

@Dao
public interface PlaybackHistoryDao {
    @Insert
    void insert(PlaybackHistory history);

    @Query("SELECT * FROM playback_history WHERE songId = :songId ORDER BY timestamp DESC LIMIT 1")
    PlaybackHistory getLastForSong(long songId);

    @Query("SELECT * FROM playback_history WHERE timestamp > :since ORDER BY timestamp DESC")
    List<PlaybackHistory> getRecent(long since);

    @Query("DELETE FROM playback_history WHERE timestamp < :before")
    void purgeOld(long before);
}
