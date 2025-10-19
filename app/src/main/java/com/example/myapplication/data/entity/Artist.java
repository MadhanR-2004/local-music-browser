package com.example.myapplication.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "artists")
public class Artist {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";
}


