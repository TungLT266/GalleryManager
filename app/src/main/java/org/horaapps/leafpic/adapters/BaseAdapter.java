package org.horaapps.leafpic.adapters;

import android.content.Context;

import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.Themed;
import org.horaapps.liz.ThemedAdapter;
import org.horaapps.liz.ThemedViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T, VH extends ThemedViewHolder> extends ThemedAdapter<VH> implements Themed {

    private List<T> items;

    private Context context;

    public BaseAdapter(Context context) {
        super(context);
        items = new ArrayList<>();
        this.context = context;
    }

    public BaseAdapter(Context context, List<T> items) {
        this(context);
        this.items = items;
    }

    public Context getContext() {
        return context;
    }




    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void add(T item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void add(int position, T item) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(T item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void addAll(List<T> items) {
        int start = this.items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(start, items.size());
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public T getElement(int pos) {
        return items.get(pos);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void refreshTheme(ThemeHelper theme) {
        setThemeHelper(theme);
        notifyDataSetChanged();
    }
}
