package hajalibayram.hnotes_android.model;

/**
 * Created by hajali on 10/14/17
 * <p>
 * Contacts: +(994 51 744 11 07)
 * <p>
 * Email: hajalibayram@outlook.com
 */

public class HistoryItem {
    private String title, date, img_url,content;

    public HistoryItem(String title, String date, String img_url,String content) {
        this.title = title;
        this.date = date;
        this.img_url = img_url;
        this.content = content;
    }

    public HistoryItem() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
