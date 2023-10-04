package com.kimikevin.elapunte.view.util;

import android.view.View;

import com.kimikevin.elapunte.R;

import java.util.Random;

public class NoteUtil {

    private final int[] colors = {R.color.beige,R.color.pale_blue,R.color.pale_green,
            R.color.pale_orange,R.color.pale_yellow,R.color.pale_pink,
            R.color.light_grey,R.color.light_blue,R.color.light_pink,R.color.mint_green};

    public int getColor(View view) {
        Random random = new Random();
        return colors[random.nextInt(colors.length)];
    }
}
