package flickrpics.test.com.flickrpicsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends Activity {

    EditText mEditText;
    Button mDownloadPhotos;
    ListView mImageList;
    ImageAdapter mAdapter;
    ArrayList<ImageContainer> mDataList;
    ProgressDialog progress;
    private static int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDownloadPhotos = (Button) findViewById(R.id.button1);
        mEditText = (EditText) findViewById(R.id.editText1);
        mImageList = (ListView) findViewById(R.id.gallery1);
        mDataList = new ArrayList<>();
        mAdapter = new ImageAdapter(this, mDataList);
        mImageList.setAdapter(mAdapter);
    }

    public void buttonClicked(View v) {
        String tag = mEditText.getText().toString().trim();
        Log.d("Amit", "enetered tag " + tag);
        new FlickrPicDownloader().execute(tag);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<ImageContainer> imageContainer;
        LayoutInflater mInflater;

        public ImageAdapter(Context c, ArrayList<ImageContainer> imageContainer) {
            mContext = c;
            this.imageContainer = imageContainer;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(ArrayList<ImageContainer> data) {
            imageContainer = data;
        }
        public int getCount() {
            return imageContainer.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            if (imageContainer != null) {
                holder.imageView.setImageBitmap(imageContainer.get(position).getThumbnail());
            }
            return view;
        }

    }

    private class ViewHolder {
        ImageView imageView;

        public ViewHolder(View v) {
            imageView = (ImageView) v.findViewById(R.id.picView);
        }
    }

    class FlickrPicDownloader extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Amit", "onPreExecute");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("Amit", "onPostExecute " + mDataList.size());
            mAdapter.setData(mDataList);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Log.d("Amit", "onProgressUpdate " + mDataList.size());
            mAdapter.setData(mDataList);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.d("Amit", "doInBackground");
            String tag = params[0];
            String url = createURL(tag, mPage);
            String jsonString = null;
            if (URLConnector.isOnline(MainActivity.this)) {
                ByteArrayOutputStream baos = URLConnector.readBytes(url);
                jsonString = baos.toString();
                //Log.d("Amit", "json response: " + jsonString);
                try {
                    JSONObject rootObject = new JSONObject(jsonString);
                    JSONObject photos = rootObject.getJSONObject("photos");
                    JSONArray imageJSONArray = photos.getJSONArray("photo");
                    for (int i = 0; i < imageJSONArray.length(); i++) {
                        JSONObject item = imageJSONArray.getJSONObject(i);
                        String thumUrl = createPhotoURL(item.getString("farm"), item.getString("server"),
                                item.getString("id"), item.getString("secret"));
                        Log.d("Amit", "thumbnail url " + thumUrl);
                        ImageContainer temp = new ImageContainer();
                        temp.setThumbnailURL(thumUrl);
                        temp.setThumbnail(getThumbnail(thumUrl));
                        mDataList.add(temp);
                        publishProgress();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private static final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?method=";
    private static final String FLICKR_PHOTOS_SEARCH_STRING = "flickr.photos.search";
    private static final String APIKEY_SEARCH_STRING = "&api_key=04913f5bf37d5ada4a68a07ffa9a7066";
    private static final String TAGS_STRING = "&tags=";
    private static final String FORMAT_STRING = "&format=json&nojsoncallback=";

    private static String createURL(String tag, int page) {
        String url = null;
        url = FLICKR_BASE_URL + FLICKR_PHOTOS_SEARCH_STRING +
                APIKEY_SEARCH_STRING + TAGS_STRING + tag + FORMAT_STRING + page;
        Log.d("Amit", "url: " + url);
        return url;
    }

    private String createPhotoURL(String farm, String server, String id, String secret) {
        String tmp = null;
        tmp = "http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "_t" + ".jpg";
        return tmp;
    }

    public static Bitmap getThumbnail(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e("FlickrManager", e.getMessage());
        }
        return bm;
    }
}