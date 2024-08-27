package net.onlyid.common;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

import net.onlyid.databinding.DialogPermissionBinding;

public class PermissionDialog extends Dialog {
    DialogPermissionBinding binding;

    public PermissionDialog(Context context, String title, String text) {
        super(context);
        binding = DialogPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0;
        lp.gravity = Gravity.TOP;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;

        binding.title.setText(title);
        binding.text.setText(text);
    }
}
