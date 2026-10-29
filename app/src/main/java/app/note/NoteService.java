package app.note;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class NoteService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    static class Factory implements RemoteViewsFactory {
        private final Context c;
        private String[] lines = new String[0];
        private boolean bgSys, txSys;
        private int bg, fg;

        Factory(Context c) { this.c = c; }

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
            if (i == lines.length + 1) return row('f');
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
