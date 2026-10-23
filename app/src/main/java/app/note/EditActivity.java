package app.note;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;

public class EditActivity extends Activity {
    private EditText et;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        et = new EditText(this);
        et.setText(Store.get(this));
        et.setGravity(Gravity.TOP | Gravity.START);
        et.setBackground(null);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        et.setPadding(p, p * 2, p, p);
        setContentView(et);
        et.setSelection(et.getText().length());
    }

    // сохраняем при любом выходе — кнопки «сохранить» нет
    @Override
    protected void onPause() {
        super.onPause();
        String now = et.getText().toString();
        if (!now.equals(Store.get(this))) {
            Store.set(this, now);
            NoteWidget.refresh(this);
        }
    }
}
