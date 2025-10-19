package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.data.entity.FavoriteSong;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long like(FavoriteSong favoriteSong);

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    int unlike(long songId);

    @Query("SELECT songId FROM favorite_songs ORDER BY likedAtEpochMs DESC")
    List<Long> getFavoriteSongIds();

    @Query("SELECT COUNT(*) FROM favorite_songs WHERE songId = :songId")
    int isFavorite(long songId);
}


