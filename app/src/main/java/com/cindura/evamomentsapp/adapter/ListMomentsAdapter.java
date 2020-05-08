package com.cindura.evamomentsapp.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cindura.evamomentsapp.R;
import com.cindura.evamomentsapp.helper.IRecyclerViewClickListener;
import com.cindura.evamomentsapp.model.Presentation;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.recyclerview.widget.RecyclerView;

//Class to show the list of shows
public class ListMomentsAdapter extends RecyclerView.Adapter<ListMomentsAdapter.CustomViewHolder> {
    List<Presentation> responseMessages;
    Context context;
    IRecyclerViewClickListener listener;
    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView userQuery,numPlayed,mostRecent;
        ImageView image,videoIcon,menu;
        RelativeLayout clickLayout;
        public CustomViewHolder(View itemView) {
            super(itemView);
            clickLayout=(RelativeLayout)itemView.findViewById(R.id.clickLayout);
            menu=(ImageView) itemView.findViewById(R.id.menu);
            userQuery = (TextView) itemView.findViewById(R.id.keywords);
            image=(ImageView) itemView.findViewById(R.id.image);
            videoIcon=(ImageView) itemView.findViewById(R.id.video_icon);
            numPlayed=itemView.findViewById(R.id.numOfTimesPlayed);
            mostRecent=itemView.findViewById(R.id.mostRecentlyPlayed);
        }
    }

    public ListMomentsAdapter(List<Presentation> responseMessages, Context context, IRecyclerViewClickListener listener) {
        this.responseMessages = responseMessages;
        this.context = context;
        this.listener=listener;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.list_item;
    }

    @Override
    public int getItemCount() {
        return  responseMessages.size();
    }

    @Override
    public ListMomentsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListMomentsAdapter.CustomViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(ListMomentsAdapter.CustomViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        String key="";
        for(int i=0;i<responseMessages.get(position).getKeywords().size();i++){
            key=key+responseMessages.get(position).getKeywords().get(i)+", ";
        }
        holder.userQuery.setText(key.substring(0,key.length()-2));

        if(responseMessages.get(position).getNumOfTimesPlayed()!=null && responseMessages.get(position).getLastPlayed()!=null) {
            holder.numPlayed.setVisibility(View.VISIBLE);
            if(responseMessages.get(position).getNumOfTimesPlayed().equalsIgnoreCase("1"))
            holder.numPlayed.setText(responseMessages.get(position).getNumOfTimesPlayed()+" view, "+timeAgo(responseMessages.get(position).getLastPlayed()));
            else
                holder.numPlayed.setText(responseMessages.get(position).getNumOfTimesPlayed()+" views, "+timeAgo(responseMessages.get(position).getLastPlayed()));
        }

        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType=cR.getType(Uri.parse(responseMessages.get(position).getSelectedItems().get(0)));

        System.out.println("mime type : "+mimeType);
        if(mimeType==null){
                holder.image.setImageResource(R.drawable.media_not_found_image);
        }
        else if(mimeType.contains("image")) {
            Cursor returnCursor =
                    context.getContentResolver().query(Uri.parse(responseMessages.get(position).getSelectedItems().get(0)), null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long size = returnCursor.getLong(sizeIndex);

            System.out.println("file size: "+size);
            if(size < 100000) {
                holder.image.setImageURI(Uri.parse(responseMessages.get(position).getSelectedItems().get(0)));
            }
            else
                holder.image.setImageBitmap(getBitmap(Uri.parse(responseMessages.get(position).getSelectedItems().get(0))));
        }
        else if(mimeType.contains("video"))
        {
            MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
            mMMR.setDataSource(context, Uri.parse(responseMessages.get(position).getSelectedItems().get(0)));
            Bitmap bitmap = mMMR.getFrameAtTime();
            holder.image.setImageBitmap(bitmap);
            holder.videoIcon.setVisibility(View.VISIBLE);
        }

        holder.clickLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongClicked(position);
                return false;
            }
        });
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMenuClicked(position);
            }
        });
        holder.clickLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSingleClick(position);
            }
        });
    }
    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = context.getContentResolver().openInputStream(uri);

            //check Orientation
            int orientation=0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                ExifInterface exifInterface = new ExifInterface(in);
                orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
            }

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("Bitmap ", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap bitmap = null;
            in =context.getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                bitmap = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                Log.d("Bitmap", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) x,
                        (int) y, true);
                bitmap.recycle();
                bitmap = scaledBitmap;

                System.gc();
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                if(responseMessages.size()<=8)
                options.inSampleSize = 3;
                else if(responseMessages.size()<=15)
                    options.inSampleSize=5;
                else
                    options.inSampleSize=9;
                bitmap = BitmapFactory.decodeStream(in,null,options);
            }
            in.close();

            Log.d("Bitmap ", "bitmap size - width: " + bitmap.getWidth() + ", height: " +
                    bitmap.getHeight());
           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                bitmap = rotateBitmap(bitmap, orientation);
            return bitmap;
        } catch (IOException e) {
            Log.e("Bitmap ", e.getMessage(), e);
            return null;
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        System.out.println("orientation : "+orientation);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private String timeAgo(String lastSeen){
        long daysDiff = 0;
        long hoursDiff=0;
        long minsDiff=0;
        long secondsDiff=0;
        int monthsDiff=0;
        int weeksDiff=0;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault());
            fmt.setTimeZone(TimeZone.getDefault());

            Calendar cal = Calendar.getInstance();
            cal.setTime(fmt.parse(lastSeen));
            long timeToFormat=cal.getTimeInMillis();
            long msDiff = Calendar.getInstance().getTimeInMillis() - timeToFormat;
            daysDiff= TimeUnit.MILLISECONDS.toDays(msDiff);
            hoursDiff= TimeUnit.MILLISECONDS.toHours(msDiff);
            secondsDiff= TimeUnit.MILLISECONDS.toSeconds(msDiff);
            minsDiff= TimeUnit.MILLISECONDS.toMinutes(msDiff);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(daysDiff>=30){
            monthsDiff= (int) (daysDiff/30);
            if(monthsDiff==1)
                return (String.valueOf(monthsDiff)+" month ago");
            else
                return (String.valueOf(monthsDiff)+" months ago");
        }
        else if(daysDiff>=7){
            weeksDiff= (int) (daysDiff/7);
            if(weeksDiff==1)
                return (String.valueOf(weeksDiff)+" week ago");
            else
               return (String.valueOf(weeksDiff)+" weeks ago");
        }
        else if(daysDiff<7 && daysDiff!=0) {
            if(daysDiff==1)
                return (String.valueOf(daysDiff) + " day ago");
            else
                return (String.valueOf(daysDiff) + " days ago");
        }
        else if(hoursDiff<=23 && hoursDiff!=0 && hoursDiff>0) {
            if (hoursDiff == 1)
                return (String.valueOf(hoursDiff) + " hour ago");
            else
                return (String.valueOf(hoursDiff) + " hours ago");
        }
        else if(minsDiff<=59 && minsDiff!=0 && minsDiff>0) {
            if(minsDiff==1)
                return (String.valueOf(minsDiff) + " minute ago");
            else
                return (String.valueOf(minsDiff) + " minutes ago");
        }
        else if(secondsDiff<=59) {
            if(secondsDiff<0)
                secondsDiff=2;
            return (String.valueOf(secondsDiff) + " seconds ago");
        }
        return "";
    }
}
