package app.note;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

/** Мини-разметка: строка "# текст" — заголовок, **текст** — жирный. */
final class Fmt {
    static final String H = "# ";
    static final String B = "**";

    static boolean isHeading(String line) {
        return line.startsWith(H);
    }

    /** Для виджета: без маркеров, жирный через StyleSpan. */
    static CharSequence plain(String s) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        int i = 0;
        while (i < s.length()) {
            int a = s.indexOf(B, i);
            int b = a < 0 ? -1 : s.indexOf(B, a + 2);
            if (a < 0 || b < 0) {
                out.append(s, i, s.length());
                break;
            }
            out.append(s, i, a);
            int st = out.length();
            out.append(s, a + 2, b);
            if (out.length() > st) {
                out.setSpan(new StyleSpan(Typeface.BOLD), st, out.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            i = b + 2;
        }
        return out;
    }

    /** Для редактора: подсвечиваем форматирование, маркеры делаем серыми. */
    static void style(Editable e, int gray) {
        for (StyleSpan x : e.getSpans(0, e.length(), StyleSpan.class)) e.removeSpan(x);
        for (RelativeSizeSpan x : e.getSpans(0, e.length(), RelativeSizeSpan.class)) e.removeSpan(x);
        for (ForegroundColorSpan x : e.getSpans(0, e.length(), ForegroundColorSpan.class)) e.removeSpan(x);

        int f = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        String s = e.toString();
        int pos = 0;
        while (true) {
            int nl = s.indexOf('\n', pos);
            int end = nl < 0 ? s.length() : nl;

            if (s.startsWith(H, pos)) {
                e.setSpan(new RelativeSizeSpan(1.35f), pos, end, f);
                e.setSpan(new StyleSpan(Typeface.BOLD), pos, end, f);
                e.setSpan(new ForegroundColorSpan(gray), pos, pos + 2, f);
            }

            int i = pos;
            while (true) {
                int a = s.indexOf(B, i);
                if (a < 0 || a >= end) break;
                int b = s.indexOf(B, a + 2);
                if (b < 0 || b + 2 > end) break;
                if (b > a + 2) e.setSpan(new StyleSpan(Typeface.BOLD), a + 2, b, f);
                e.setSpan(new ForegroundColorSpan(gray), a, a + 2, f);
                e.setSpan(new ForegroundColorSpan(gray), b, b + 2, f);
                i = b + 2;
            }

            if (nl < 0) break;
            pos = nl + 1;
        }
    }
}
