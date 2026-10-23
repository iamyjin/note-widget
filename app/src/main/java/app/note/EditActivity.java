package app.note;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class EditActivity extends Activity {
    private EditText et;
    private final int gray = Color.parseColor("#A8A496");

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.edit);
        et = (EditText) findViewById(R.id.et);
        et.setText(Store.get(this));
        Fmt.style(et.getText(), gray);
        et.setSelection(et.getText().length());

        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int c, int d) {}
            @Override public void afterTextChanged(Editable e) { Fmt.style(e, gray); }
        });

        findViewById(R.id.bP).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { heading(false); }
        });
        findViewById(R.id.bH).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { heading(true); }
        });
        findViewById(R.id.bB).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { bold(); }
        });
    }

    /** on=true — сделать строку заголовком, false — обычным абзацем. */
    private void heading(boolean on) {
        Editable t = et.getText();
        String s = t.toString();
        int sel = Math.max(0, et.getSelectionStart());
        int ls = s.lastIndexOf('\n', sel - 1) + 1;
        boolean has = s.startsWith(Fmt.H, ls);
        if (on && !has) {
            t.insert(ls, Fmt.H);
            et.setSelection(sel + 2);
        } else if (!on && has) {
            t.delete(ls, ls + 2);
            et.setSelection(Math.max(ls, sel - 2));
        }
    }

    /** Жирный: оборачивает выделение в ** ** (повторное нажатие снимает). */
    private void bold() {
        Editable t = et.getText();
        String s = t.toString();
        int a = Math.max(0, Math.min(et.getSelectionStart(), et.getSelectionEnd()));
        int b = Math.max(0, Math.max(et.getSelectionStart(), et.getSelectionEnd()));
        if (a == b) {
            t.insert(a, "****");
            et.setSelection(a + 2);
        } else if (a >= 2 && s.startsWith(Fmt.B, a - 2) && s.startsWith(Fmt.B, b)) {
            t.delete(b, b + 2);
            t.delete(a - 2, a);
            et.setSelection(a - 2, b - 2);
        } else {
            t.insert(b, Fmt.B);
            t.insert(a, Fmt.B);
            et.setSelection(a + 2, b + 2);
        }
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
