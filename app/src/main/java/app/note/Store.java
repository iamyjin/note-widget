package app.note;

import android.content.Context;
import android.content.SharedPreferences;

final class Store {
    private static SharedPreferences p(Context c) { return c.getSharedPreferences("n", 0); }

    static String get(Context c) { return p(c).getString("t", ""); }
    static void set(Context c, String s) { p(c).edit().putString("t", s).apply(); }

    // 4 цвета: "#RRGGBB" | "sys" | "" (у текста пусто = авто-контраст)
    static String widgetBg(Context c)   { return p(c).getString("cw", p(c).getString("c", Pal.DEF)); }
    static String widgetText(Context c) { return p(c).getString("cwt", p(c).getString("ct", "")); }
    static String appBg(Context c)      { return p(c).getString("cb", p(c).getString("c", Pal.DEF)); }
    static String appText(Context c)    { return p(c).getString("cat", p(c).getString("ct", "")); }

    static void setColors(Context c, String wBg, String wText, String aBg, String aText) {
        p(c).edit().putString("cw", wBg).putString("cwt", wText)
                .putString("cb", aBg).putString("cat", aText).apply();
    }

    // чётность — какой из двух одинаковых макетов виджета использовать
    static int variant(Context c) { return p(c).getInt("wv", 0); }
    static void flipVariant(Context c) {
        p(c).edit().putInt("wv", (variant(c) + 1) & 1).apply();
    }
}
