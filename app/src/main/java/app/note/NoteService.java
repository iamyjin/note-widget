package app.note;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class NoteService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        return new Factory(getApplicationContext(), id);
    }

    static class Factory implements RemoteViewsFactory {
        private static final float BOTTOM_MIN_DP = 24f;   // нижний отступ, он же запас на неточность расчёта

        private final Context c;
        private final int widgetId;
        private String[] lines = new String[0];
        private boolean bgSys, txSys;
        private int bg, fg;
        private float fillerDp = BOTTOM_MIN_DP;

        Factory(Context c, int widgetId) { this.c = c; this.widgetId = widgetId; }

        private void load() {
            String s = Store.get(c);
            lines = s.trim().isEmpty()
                ? new String[] { "Тапни, чтобы написать" }
                : s.split("\n", -1);
            String w = Store.widgetBg(c), t = Store.widgetText(c);
            bgSys = "sys".equals(w);
            txSys = "sys".equals(t) || (t.isEmpty() && bgSys);
            bg = bgSys ? 0 : Pal.parse(w);
            fg = txSys ? 0 : Pal.text(c, bg, t);
            fillerDp = computeFiller();
        }

        /** Высота подложки под текстом: ровно столько, чтобы закрасить остаток виджета. */
        private float computeFiller() {
            try {
                if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return BOTTOM_MIN_DP;
                Bundle o = AppWidgetManager.getInstance(c).getAppWidgetOptions(widgetId);
                float wDp = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
                float hDp = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0);
                if (wDp <= 0 || hDp <= 0) return BOTTOM_MIN_DP;

                DisplayMetrics dm = c.getResources().getDisplayMetrics();
                int textWidthPx = (int) ((wDp - 40f) * dm.density);   // 20dp слева и справа
                TextPaint normal = new TextPaint();
                normal.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, dm));
                TextPaint head = new TextPaint();
                head.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, dm));
                head.setTypeface(Typeface.DEFAULT_BOLD);

                float contentDp = 12f;                                  // верхний отступ
                for (String raw : lines) {
                    boolean h = Fmt.isHeading(raw);
                    String body = h ? raw.substring(2) : raw;
                    CharSequence txt = body.isEmpty() ? " " : Fmt.plain(body);
                    StaticLayout l = StaticLayout.Builder
                            .obtain(txt, 0, txt.length(), h ? head : normal, Math.max(1, textWidthPx))
                            .build();
                    contentDp += l.getHeight() / dm.density + (h ? 15f : 6f);   // вертикальные отступы строки
                }
                return Math.max(BOTTOM_MIN_DP, hDp - contentDp + 16f);
            } catch (Exception e) {
                return BOTTOM_MIN_DP;
            }
        }

        /** Строка нужного вида: n — текст, h — заголовок, p — отступ сверху, f — подложка снизу. */
        private RemoteViews row(char kind) {
            String name = "it_" + kind + "_" + (bgSys ? "s" : "x") + "_" + (txSys ? "s" : "c");
            int id = c.getResources().getIdentifier(name, "layout", c.getPackageName());
            RemoteViews r = new RemoteViews(c.getPackageName(), id);
            if (!bgSys) r.setInt(R.id.ir, "setBackgroundColor", bg);
            r.setOnClickFillInIntent(R.id.ir, new Intent());
            return r;
        }

        @Override public void onCreate() { load(); }
        @Override public void onDataSetChanged() { load(); }
        @Override public void onDestroy() {}
        @Override public int getCount() { return lines.length + 2; }

        @Override
        public RemoteViews getViewAt(int i) {
            if (i == 0) return row('p');
            if (i == lines.length + 1) {
                RemoteViews f = row('f');
                if (Build.VERSION.SDK_INT >= 31) {
                    f.setViewLayoutHeight(R.id.ir, fillerDp, TypedValue.COMPLEX_UNIT_DIP);
                } else {
                    f.setViewVisibility(R.id.ir, android.view.View.GONE);
                }
                return f;
            }
            String raw = lines[i - 1];
            boolean h = Fmt.isHeading(raw);
            RemoteViews r = row(h ? 'h' : 'n');
            String body = h ? raw.substring(2) : raw;
            r.setTextViewText(R.id.t, body.isEmpty() ? " " : Fmt.plain(body));
            if (!txSys) r.setTextColor(R.id.t, fg);
            return r;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 16; }
        @Override public long getItemId(int i) { return i; }
        @Override public boolean hasStableIds() { return false; }
    }
}
