package com.example.myapplication.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myapplication.data.dao.AlbumDao;
import com.example.myapplication.data.dao.ArtistDao;
import com.example.myapplication.data.dao.FavoriteDao;
import com.example.myapplication.data.dao.PlaylistDao;
import com.example.myapplication.data.dao.SongDao;
import com.example.myapplication.data.entity.Album;
import com.example.myapplication.data.entity.Artist;
import com.example.myapplication.data.entity.FavoriteSong;
import com.example.myapplication.data.entity.Mix;
import com.example.myapplication.data.entity.MixSongCrossRef;
import com.example.myapplication.data.entity.Playlist;
import com.example.myapplication.data.entity.PlaylistSongCrossRef;
import com.example.myapplication.data.entity.Song;
import com.example.myapplication.data.entity.SongFts;

@Database(
        entities = {
                Song.class,
                Artist.class,
                Album.class,
                Playlist.class,
                PlaylistSongCrossRef.class,
                FavoriteSong.class,
                Mix.class,
                MixSongCrossRef.class,
                SongFts.class,
                com.example.myapplication.data.entity.PlaybackHistory.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SongDao songDao();
    public abstract ArtistDao artistDao();
    public abstract AlbumDao albumDao();
    public abstract PlaylistDao playlistDao();
    public abstract FavoriteDao favoriteDao();
        public abstract com.example.myapplication.data.dao.MixDao mixDao();
        public abstract com.example.myapplication.data.dao.PlaybackHistoryDao playbackHistoryDao();
}


