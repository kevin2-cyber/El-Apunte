package com.kimikevin.el_apunte.model;

import android.content.Context;
import android.icu.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kimikevin.el_apunte.model.dao.NoteDao;
import com.kimikevin.el_apunte.model.entity.Note;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class}, version = 4, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao getNoteDao();

    // Singleton
    private static NoteDatabase instance;

    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NoteDatabase.class,
                            "note_db")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            // insert data when database is created
            initializeData();
            super.onCreate(db);
        }
    };

    private static void initializeData() {
        NoteDao noteDao = instance.getNoteDao();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date());

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(() ->  {
           // notes
            // Check if notes already exist
            if (noteDao.getCount() == 0) {
                Note noteOne = new Note();
                noteOne.setTitle("Like and Subscribe");
                noteOne.setContent("A FREE way to support the channel is to give us a LIKE . It does not cost you but means a lot to us.\nIf you are new here please Subscribe");
                noteOne.setFormattedDate(formattedDate);

                Note noteTwo = new Note();
                noteTwo.setTitle("Recipes to Try");
                noteTwo.setContent("1. Chicken Alfredo\n2. Vegan chili\n3. Spaghetti carbonara\n4. Chocolate lava cake");
                noteTwo.setFormattedDate(formattedDate);

                Note noteThree = new Note();
                noteThree.setTitle("Books to Read");
                noteThree.setContent("1. To Kill a Mockingbird\n2. 1984\n3. The Great Gatsby\n4. The Catcher in the Rye");
                noteThree.setFormattedDate(formattedDate);

                Note noteFour = new Note();
                noteFour.setTitle("Gift Ideas for Mom");
                noteFour.setContent("1. Jewelry box\n2. Cookbook\n3. Scarf\n4. Spa day gift card");
                noteFour.setFormattedDate(formattedDate);

                noteDao.insertAll(noteOne, noteTwo, noteThree, noteFour);
        }
    });
}
}