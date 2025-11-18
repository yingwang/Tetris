package com.tetris.game;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class RetroDialog {
    private Dialog dialog;
    private Context context;

    public RetroDialog(Context context) {
        this.context = context;
        this.dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_retro);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public RetroDialog setTitle(String title) {
        TextView titleView = dialog.findViewById(R.id.dialogTitle);
        titleView.setText(title);
        titleView.setVisibility(View.VISIBLE);
        return this;
    }

    public RetroDialog setMessage(String message) {
        TextView messageView = dialog.findViewById(R.id.dialogMessage);
        messageView.setText(message);
        return this;
    }

    public RetroDialog setButton(String buttonText, View.OnClickListener listener) {
        Button button = dialog.findViewById(R.id.dialogButton);
        button.setText(buttonText);
        button.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(v);
            }
            dialog.dismiss();
        });
        return this;
    }

    public RetroDialog setSecondButton(String buttonText, View.OnClickListener listener) {
        Button button2 = dialog.findViewById(R.id.dialogButton2);
        button2.setText(buttonText);
        button2.setVisibility(View.VISIBLE);
        button2.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(v);
            }
            dialog.dismiss();
        });
        return this;
    }

    public RetroDialog setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public static void showMessage(Context context, String title, String message) {
        new RetroDialog(context)
                .setTitle(title)
                .setMessage(message)
                .setButton("OK", null)
                .show();
    }

    public static void showMessage(Context context, String message) {
        new RetroDialog(context)
                .setMessage(message)
                .setButton("OK", null)
                .show();
    }
}
