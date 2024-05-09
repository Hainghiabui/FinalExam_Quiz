package com.example.finalexam_quiz;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "options")
    public String options;

    @ColumnInfo(name = "answer")
    public String answer;
    public boolean answered = false;
}