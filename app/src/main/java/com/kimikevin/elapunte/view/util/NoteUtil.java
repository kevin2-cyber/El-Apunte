package com.kimikevin.elapunte.view.util;



public class NoteUtil {
    static int color;

    public NoteUtil(int color) {
        NoteUtil.color = color;
    }

    public static int getColor() {
        return color;
    }

    public void setColor(int color) {
        NoteUtil.color = color;
    }

    //    private static final int[] colors = {R.color.beige,R.color.pale_blue,R.color.pale_green,
//            R.color.pale_orange,R.color.pale_yellow,R.color.pale_pink,
//            R.color.light_grey,R.color.light_blue, R.color.light_pink,R.color.mint_green};
//
//    public static int getRandomColor() {
//        Random random = new Random();
//        return colors[random.nextInt(colors.length)];
//    }
//
//    public static int getColor(Context context) {
//        int[] colors;
//        if (Math.random() >= 0.6) {
//            colors = context.getResources().getIntArray(R.array.note_accent_colors);
//        } else {
//            colors = context.getResources().getIntArray(R.array.note_neutral_colors);
//        }
//        return colors[((int) (Math.random() * colors.length))];
//    }

    //TODO: search and filter functionalities
    static {
        getColor();
    }

}
