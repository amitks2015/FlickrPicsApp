package flickrpics.test.com.flickrpicsapp;

import android.graphics.Bitmap;

/**
 * Created by NMB384 on 9/19/2015.
 */
public class ImageContainer {
    String mThumbURL;
    Bitmap mThumbnail;

    public ImageContainer() {

    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        mThumbnail = thumbnail;
    }

    public String getThumbnailURL() {
        return mThumbURL;
    }

    public void setThumbnailURL(String thumbURL) {
        mThumbURL = thumbURL;
    }
}
