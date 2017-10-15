package hajalibayram.hnotes_android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hajalibayram.hnotes_android.R;
import hajalibayram.hnotes_android.model.HistoryItem;

/**
 * Copyright (C) 2017 Kerimov's Creations.
 * <p>
 * For OnbranchV2 project
 * <p>
 * Contact
 * email: kerimovscreations@gmail.com
 * phone: +994 (50) 6325560
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private OnItemClickListener mListener;
    private List<HistoryItem> mList;
    private Context mContext;

    public HistoryAdapter(Context context, List<HistoryItem> list) {
        mList = list;
        mContext = context;
    }

    public void setOnItemClickListener(HistoryAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.list_item_history, parent, false);

        return new HistoryAdapter.ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder viewHolder, int position) {
        HistoryItem bItem = mList.get(position);

        viewHolder.webView.loadData(bItem.getImg_url(), "text/html; charset=utf-8", "UTF-8");

        viewHolder.title.setText(bItem.getTitle());
        viewHolder.date.setText(bItem.getDate());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);

        void onDeleteClick(View itemView, int position);

        void onShareClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        ImageView eye, share, delete;
        WebView webView;

        ViewHolder(final View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.history_item_title);
            date = itemView.findViewById(R.id.history_item_date);
            webView = itemView.findViewById(R.id.history_item_img);
            eye = itemView.findViewById(R.id.history_item_eye);
            share = itemView.findViewById(R.id.history_item_share);
            delete = itemView.findViewById(R.id.history_item_delete);

            webView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(itemView, position);
                        }
                    }
                }
            });
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onShareClick(itemView, position);
                        }
                    }
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteClick(itemView, position);
                        }
                    }
                }
            });
        }
    }

}
