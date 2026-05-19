package com.kimikevin.elapunte.model.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NoteApi {

    @GET("notes")
    Call<List<NoteDto>> getAllNotes(@Query("page") int page, @Query("size") int size);

    @POST("notes")
    Call<NoteDto> createNote(@Body NoteDto note);

    @PUT("notes/{id}")
    Call<NoteDto> updateNote(@Path("id") String id, @Body NoteDto note);

    @DELETE("notes/{id}")
    Call<Void> deleteNote(@Path("id") String id);
}
