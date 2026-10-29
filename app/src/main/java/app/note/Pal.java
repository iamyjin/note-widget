package app.note;

import android.content.Context;
import android.graphics.Color;

final class Pal {
    static final String DEF = "#EFEBDF";

    static String norm(String s) {
        s = s.trim();
        return s.startsWith("#") ? s : "#" + s;
    }

    /** "" -> "", "sys" -> "sys", hex -> "#RRGGBB"; иначе null (ошибка). */
    static String clean(String s) {
        s = s.trim();
        if (s.isEmpty()) return "";
        if (s.equalsIgnoreCase("sys")) return "sys";
        s = norm(s);
        return s.matches("#[0-9a-fA-F]{6}") ? s.toUpperCase() : null;
    }

    static int parse(String s) {
        try {
            return Color.parseColor(norm(s)) | 0xFF000000;
        } catch (Exception e) {
            return Color.parseColor(DEF);
        }
    }

    static int bgOf(Context c, String s) {
        return "sys".equals(s) ? c.getColor(R.color.sys_bg) : parse(s);
    }

    static int widgetBg(Context c) { return bgOf(c, Store.widgetBg(c)); }
    static int appBg(Context c) { return bgOf(c, Store.appBg(c)); }

    /** Цвет текста t ("" авто, "sys", hex) поверх фона bg. */
    static int text(Context c, int bg, String t) {
        if (t.isEmpty()) return fgFor(bg);
        if ("sys".equals(t)) return c.getColor(R.color.sys_fg);
        return parse(t);
    }

    static boolean isLight(int bg) { return lum(bg) > 0.55; }

    static int fgFor(int bg) { return isLight(bg) ? 0xFF171715 : 0xFFF4F1E8; }

    private static double lum(int c) {
        return (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255.0;
    }

    static int blend(int a, int b, float t) {
        int r = (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t);
        int g = (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t);
        int bl = (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t);
        return Color.rgb(r, g, bl);
    }
}
