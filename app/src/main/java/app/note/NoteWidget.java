package app.note;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NoteWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
        m.notifyAppWidgetViewDataChanged(ids, R.id.list);
    }

    /** Макет виджета без цветов: фон и цвет текста задаются в строках списка (NoteService). */
    static RemoteViews build(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        v.setRemoteAdapter(R.id.list, new Intent(c, NoteService.class));
        Intent edit = new Intent(c, EditActivity.class);
        v.setPendingIntentTemplate(R.id.list,
            PendingIntent.getActivity(c, 0, edit,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        v.setOnClickPendingIntent(android.R.id.background,
            PendingIntent.getActivity(c, 1, edit,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        return v;
    }

    /** Перечитать текст и цвета. */
    static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, NoteWidget.class));
        if (ids.length == 0) return;
        for (int id : ids) m.updateAppWidget(id, build(c));
        m.notifyAppWidgetViewDataChanged(ids, R.id.list);
    }
}
