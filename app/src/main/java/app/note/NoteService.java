package app.note;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/** Каждая строка заметки — элемент списка; список виджет умеет прокручивать. */
public class NoteService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    static class Factory implements RemoteViewsFactory {
        private final Context c;
        private String[] lines = new String[0];

        Factory(Context c) { this.c = c; }

        private void load() {
            String s = Store.get(c);
            lines = s.trim().isEmpty()
                ? new String[] { "Тапни, чтобы написать" }
                : s.split("\n", -1);
        }

        @Override public void onCreate() { load(); }
        @Override public void onDataSetChanged() { load(); }
        @Override public void onDestroy() {}
        @Override public int getCount() { return lines.length; }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews r = new RemoteViews(c.getPackageName(), R.layout.item);
            r.setTextViewText(R.id.t, lines[i].isEmpty() ? " " : lines[i]);
            r.setOnClickFillInIntent(R.id.t, new Intent());
            return r;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int i) { return i; }
        @Override public boolean hasStableIds() { return false; }
    }
}
