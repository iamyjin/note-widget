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
    }

    static RemoteViews build(Context c) {
        String saved = Store.get(c);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        v.setTextViewText(R.id.text, saved.isEmpty() ? "Тапни, чтобы написать" : saved);
        v.setOnClickPendingIntent(R.id.text,
            PendingIntent.getActivity(c, 0, new Intent(c, EditActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        return v;
    }

    static void refresh(Context c) {
        AppWidgetManager.getInstance(c)
            .updateAppWidget(new ComponentName(c, NoteWidget.class), build(c));
    }
}
