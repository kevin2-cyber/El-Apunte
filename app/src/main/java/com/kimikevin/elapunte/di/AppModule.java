package com.kimikevin.elapunte.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kimikevin.elapunte.BuildConfig;
import com.kimikevin.elapunte.model.NoteDatabase;
import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.network.AuthApi;
import com.kimikevin.elapunte.model.network.AuthInterceptor;
import com.kimikevin.elapunte.model.network.NoteApi;
import com.kimikevin.elapunte.util.TokenManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    private static final String API_BASE_URL =
            "http://" + BuildConfig.API_HOST + ":" + BuildConfig.API_PORT + "/api/v1/";

    @Provides
    @Singleton
    public NoteDatabase provideNoteDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, NoteDatabase.class, "note_database")
                .addMigrations(MIGRATION)
                .build();
    }

    static final Migration MIGRATION = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE note_table ADD COLUMN pending_action TEXT");
        }
    };

    @Provides
    @Singleton
    public NoteDao provideNoteDao(NoteDatabase database) {
        return database.getNoteDao();
    }

    @Provides
    @Singleton
    @Named("networkExecutor")
    public ExecutorService provideNetworkExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Provides
    @Named("apiBaseUrl")
    public String provideApiBaseUrl() {
        return API_BASE_URL;
    }

    // Plain client used only for token refresh (no auth interceptor to avoid loops)
    @Provides
    @Singleton
    @Named("refreshClient")
    public OkHttpClient provideRefreshClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(TokenManager tokenManager,
                                                  @Named("refreshClient") OkHttpClient refreshClient,
                                                  @Named("apiBaseUrl") String baseUrl) {
        return new AuthInterceptor(tokenManager, refreshClient, baseUrl);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public NoteApi provideNoteApi(Retrofit retrofit) {
        return retrofit.create(NoteApi.class);
    }

    @Provides
    @Singleton
    public AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }
}
