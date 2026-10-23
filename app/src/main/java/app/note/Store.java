package app.note;

import android.content.Context;

final class Store {
    static String get(Context c) {
        return c.getSharedPreferences("n", 0).getString("t", "");
    }

    static void set(Context c, String s) {
        c.getSharedPreferences("n", 0).edit().putString("t", s).apply();
    }
}
