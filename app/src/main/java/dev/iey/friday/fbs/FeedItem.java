package dev.iey.friday.fbs;

/**
 * Created by rishabh on 24-02-2016.
 */
public class FeedItem {
    private String key;
    private String title;
    private String description;
    private String thumbnailUrl;

    public FeedItem(String key, String title, String description, String thumbnailUrl){
        this.key=key;
        this.title=title;
        this.description=description;
        this.thumbnailUrl=thumbnailUrl;
    }

    public FeedItem(){

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
