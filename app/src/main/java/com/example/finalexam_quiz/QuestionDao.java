package com.example.finalexam_quiz;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface QuestionDao {
    @Insert
    void insert(Question question);
    @Insert
    void insertAll(List<Question> questions);
    @Query("SELECT * FROM question WHERE id = :questionId")
    Question getById(int questionId);

    @Update
    void update(Question question);

    @Delete
    void delete(Question question);

    @Query("SELECT * FROM question")
    List<Question> getAll();
}