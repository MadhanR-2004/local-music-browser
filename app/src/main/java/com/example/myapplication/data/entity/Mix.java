package com.example.myapplication.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mixes")
public class Mix {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public long generatedAt;
}


