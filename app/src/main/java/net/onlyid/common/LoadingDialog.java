package net.onlyid.common;

import android.app.Dialog;
import android.content.Context;

import net.onlyid.R;

public class LoadingDialog extends Dialog {
    public LoadingDialog(Context context) {
        super(context, R.style.LoadingDialog);
        setContentView(R.layout.dialog_loading);
        setCanceledOnTouchOutside(false);
    }
}
