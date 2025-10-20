package com.example.myapplication.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.DatabaseProvider;
import com.example.myapplication.data.entity.Song;
import com.example.myapplication.media.MediaStoreScanner;
import java.util.List;

public class MusicScanWorker extends Worker {
    public MusicScanWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AppDatabase db = DatabaseProvider.get(getApplicationContext());
            List<Song> existingSongs = db.songDao().getAll();
            List<Song> newSongs = MediaStoreScanner.rescan(getApplicationContext(), existingSongs);
            // Optionally notify UI or save result
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
