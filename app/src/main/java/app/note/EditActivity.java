package app.note;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity {
    private EditText et;
    private View root;
    private TextView[] btns;
    private int gray;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.edit);
        root = findViewById(R.id.er);
        et = (EditText) findViewById(R.id.et);
        btns = new TextView[] {
            (TextView) findViewById(R.id.bP), (TextView) findViewById(R.id.bH),
            (TextView) findViewById(R.id.bB), (TextView) findViewById(R.id.bC)
        };

        et.setText(Store.get(this));
        recolor();
        et.setSelection(et.getText().length());

        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int c, int d) {}
            @Override public void afterTextChanged(Editable e) { Fmt.style(e, gray); }
        });

        btns[0].setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { heading(false); }
        });
        btns[1].setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { heading(true); }
        });
        btns[2].setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { bold(); }
        });
        btns[3].setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { pickColor(); }
        });
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable round(int c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(c);
        g.setCornerRadius(dp(14));
        return g;
    }

    /** Применяет выбранный цвет к редактору и виджету. */
    private void recolor() {
        int bg = Pal.appBg(this), fg = Pal.text(this, bg, Store.appText(this));
        gray = Pal.blend(bg, fg, 0.45f);

        root.setBackgroundColor(bg);
        getWindow().setBackgroundDrawable(new ColorDrawable(bg));
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        int flags = View.SYSTEM_UI_FLAG_VISIBLE;
        if (Pal.isLight(bg)) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        et.setTextColor(fg);
        for (TextView t : btns) {
            t.setTextColor(fg);
            StateListDrawable s = new StateListDrawable();
            s.addState(new int[] { android.R.attr.state_pressed }, round(Pal.blend(bg, fg, 0.22f)));
            s.addState(new int[] {}, round(Pal.blend(bg, fg, 0.12f)));
            t.setBackground(s);
        }
        Fmt.style(et.getText(), gray);
    }

    private EditText field(LinearLayout box, String label, String val) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(13);
        l.setPadding(0, dp(10), 0, 0);
        box.addView(l);
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint("#RRGGBB или sys");
        e.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        e.setText(val);
        box.addView(e);
        return e;
    }

    private void pickColor() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(4), dp(24), dp(8));
        final EditText wb = field(box, "Фон виджета", Store.widgetBg(this));
        final EditText wt = field(box, "Текст виджета (пусто — авто)", Store.widgetText(this));
        final EditText ab = field(box, "Фон приложения", Store.appBg(this));
        final EditText at = field(box, "Текст приложения (пусто — авто)", Store.appText(this));
        ScrollView sv = new ScrollView(this);
        sv.addView(box);

        new AlertDialog.Builder(this)
            .setTitle("Цвета")
            .setView(sv)
            .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int x) {
                    String c1 = Pal.clean(wb.getText().toString());
                    String c2 = Pal.clean(wt.getText().toString());
                    String c3 = Pal.clean(ab.getText().toString());
                    String c4 = Pal.clean(at.getText().toString());
                    if (c1 == null || c2 == null || c3 == null || c4 == null) {
                        Toast.makeText(EditActivity.this, "Формат: #RRGGBB, sys или пусто",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    applyColors(c1, c2, c3, c4);
                }
            })
            .setNeutralButton("Система", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int x) {
                    applyColors("sys", "sys", "sys", "sys");
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void applyColors(String wBg, String wText, String aBg, String aText) {
        Store.setColors(this, wBg, wText, aBg, aText);
        recolor();
        NoteWidget.refresh(this);
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
