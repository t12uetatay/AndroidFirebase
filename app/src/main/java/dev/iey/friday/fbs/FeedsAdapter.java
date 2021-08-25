package dev.iey.friday.fbs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.MyViewHolder> {
    List<FeedItem> feedItems;
    Context context;
    private AdapterOnClick listener;
    public FeedsAdapter(Context context, List<FeedItem>feedItems){
        this.feedItems=feedItems;
        this.context=context;
        this.listener = (AdapterOnClick) context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.custom_row_item,parent,false);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        YoYo.with(Techniques.FadeIn).playOn(holder.cardView);
        FeedItem current=feedItems.get(position);
        holder.Title.setText(current.getTitle());
        holder.Description.setText(current.getDescription());
        Glide.with(context).load(current.getThumbnailUrl()).into(holder.Thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                listener.onKlik(feedItems.get(position), position);
            }
        });

    }



    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Title , Description;
        ImageView Thumbnail;
        CardView cardView;
        public View view;
        public MyViewHolder(View itemView) {
            super(itemView);
            Title= (TextView) itemView.findViewById(R.id.title_text);
            Description= (TextView) itemView.findViewById(R.id.description_text);
            Thumbnail= (ImageView) itemView.findViewById(R.id.thumb_img);
            cardView= (CardView) itemView.findViewById(R.id.cardview);
            this.view = view;
        }
    }

    public interface AdapterOnClick {
        void onKlik(FeedItem feedItem, int position);
    }
}
