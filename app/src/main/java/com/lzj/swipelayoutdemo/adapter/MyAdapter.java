package com.lzj.swipelayoutdemo.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lzj.swipelayoutdemo.Commons;
import com.lzj.swipelayoutdemo.R;
import com.lzj.swipelayoutdemo.view.SwipeLayout;

import java.util.HashSet;

/**
 * Created by LZJ on 2017/2/26.
 */

public class MyAdapter extends BaseAdapter implements View.OnClickListener, SwipeLayout.OnSwipingListener {
    private String[] names = Commons.NAMES;
    private SwipeLayout swipeLayout;
    private HashSet<SwipeLayout> layouts = new HashSet<>();

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item, null);
        }
        ViewHolder holder = ViewHolder.getViewHolder(convertView);
        holder.tv_add.setOnClickListener(this);
        holder.tv_delete.setOnClickListener(this);
        holder.tv_content.setText(names[position]);
        swipeLayout = (SwipeLayout) convertView;
        swipeLayout.setOnSwipingListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        SwipeLayout sl = (SwipeLayout) v.getParent().getParent();
        switch (v.getId()) {
            case R.id.tv_add:
                Toast.makeText(v.getContext(), "关注", Toast.LENGTH_SHORT).show();
                sl.close(true);
                break;
            case R.id.tv_delete:
                Toast.makeText(v.getContext(), "删除", Toast.LENGTH_SHORT).show();
                sl.close(true);
                break;
        }
    }

    @Override
    public void onOpened(SwipeLayout layout) {
        System.out.println("打开了");
        layouts.add(layout);
    }

    @Override
    public void onClosed(SwipeLayout layout) {
        System.out.println("关闭了");
        layouts.remove(layout);
    }

    @Override
    public void onSwiping(SwipeLayout layout) {
        closeAllItem();
    }

    public void closeAllItem() {
        for (SwipeLayout layout : layouts) {
            layout.close(true);
        }
        layouts.clear();
    }


    static class ViewHolder {
        private TextView tv_add;
        private TextView tv_delete;
        private TextView tv_content;

        private static ViewHolder getViewHolder(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.tv_add = (TextView) view.findViewById(R.id.tv_add);
                holder.tv_delete = (TextView) view.findViewById(R.id.tv_delete);
                holder.tv_content = (TextView) view.findViewById(R.id.tv_name);
                view.setTag(holder);
            }
            return holder;
        }
    }
}
