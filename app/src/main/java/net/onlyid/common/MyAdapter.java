package net.onlyid.common;

import android.widget.BaseAdapter;

import java.util.List;

public abstract class MyAdapter extends BaseAdapter {
    ListCallback listCallback;

    public MyAdapter(ListCallback callback) {
        listCallback = callback;
    }

    @Override
    public int getCount() {
        return listCallback.get().size();
    }

    @Override
    public Object getItem(int position) {
        return listCallback.get().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface ListCallback {
        List<?> get();
    }
}
