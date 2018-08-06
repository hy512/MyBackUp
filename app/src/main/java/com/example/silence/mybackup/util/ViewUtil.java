package com.example.silence.mybackup.util;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;


public class ViewUtil {
    public static AlertDialog dialogOptions(Activity context, List<DialogOption> options) {
        float density = getLogicDensity(context);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        // 布局
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(layoutParams);
        layout.setPadding((int) (density * 15), (int) (density * 10), (int) (density * 15), (int) (density * 10));

        TextView view;

        // 迭代所有选项
        Iterator<DialogOption> iterator = options.iterator();
        while (iterator.hasNext()) {
            view = new TextView(context);
            view.setTextSize(16);
            view.setPadding(0, (int) (density * 8), 0, (int) (density * 8));
            DialogOption option = iterator.next();
            // 字符串可以是字符串字面值也可以是 string 资源文件中的值
            if (option.getText() != null) view.setText(option.getText());
            else view.setText(option.getResId());
            view.setOnClickListener(option.getClick());
            layout.addView(view);
        }
        dialog.setView(layout);
        AlertDialog alertDialog = dialog.create();

        layout.setOnClickListener((View v) -> {
            Log.d("--->", "layout 点击");
            alertDialog.dismiss();
        });

        return alertDialog;
    }


    public static float getLogicDensity(Activity context) {
        // 确定逻辑密度
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }


    public static class DialogOption {
        String text;
        int resId;
        View.OnClickListener click;

        public DialogOption(String text, View.OnClickListener click) {
            this.text = text;
            this.click = click;
        }

        public DialogOption(int resId, View.OnClickListener click) {
            this.resId = resId;
            this.click = click;
        }

        public String getText() {
            return text;
        }

        public int getResId() {
            return resId;
        }

        public View.OnClickListener getClick() {
            return click;
        }

        public void setClick(View.OnClickListener click) {
            this.click = click;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
