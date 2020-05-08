package com.cindura.evamomentsapp.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.cindura.evamomentsapp.R;
import com.cindura.evamomentsapp.helper.ItemMoveCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

//Class to show the selected images from Media library in Grid view
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    LayoutInflater inflater;
     List<Uri> items;
    Context context;
    boolean askEdit;

    public GridAdapter(Context context, List<Uri> items) {
        this.items = items;
        this.context=context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public GridAdapter(Context context, List<Uri> items,boolean askEdit) {
        this.items = items;
        this.context=context;
        this.askEdit=askEdit;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType=cR.getType(items.get(position));
        String extension = mime.getExtensionFromMimeType(cR.getType(items.get(position)));

        System.out.println("mime type : "+mimeType);
        if(mimeType==null){
            holder.imageView.setImageResource(R.drawable.media_not_found_image);
        }
        else if(mimeType.contains("image")) {
            Cursor returnCursor =
                    context.getContentResolver().query(items.get(position), null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long size = returnCursor.getLong(sizeIndex);

            System.out.println("file size: "+size);
            if(size < 100000) {
                holder.imageView.setImageURI(items.get(position));
            }
            else
            holder.imageView.setImageBitmap(getBitmap(items.get(position)));
        }
        else if(mimeType.contains("video"))
        {
            MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
            mMMR.setDataSource(context, items.get(position));
            Bitmap bitmap = mMMR.getFrameAtTime();
            holder.imageView.setImageBitmap(bitmap);
            holder.video_icon.setVisibility(View.VISIBLE);
        }
        if(askEdit){
            holder.deleteItem.setVisibility(View.VISIBLE);
            holder.deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    items.remove(position);
                    notifyDataSetChanged();
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView,video_icon,deleteItem;
        View rowView;
        public MyViewHolder(View itemView) {
            super(itemView);
            rowView = itemView;
            video_icon=itemView.findViewById(R.id.video_icon);
            imageView=itemView.findViewById(R.id.imageView);
            deleteItem=itemView.findViewById(R.id.deleteItem);
        }
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(items, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(items, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
    }
        @Override
        public void onRowSelected (MyViewHolder myViewHolder){
            myViewHolder.rowView.setBackgroundColor(Color.GRAY);

        }

        @Override
        public void onRowClear (MyViewHolder myViewHolder){
            myViewHolder.rowView.setBackgroundColor(Color.WHITE);

        }

    private Bitmap getBitmap(Uri uri) {
        System.out.println("uri in grid: "+uri);
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
                if(items.size()<=8)
                options.inSampleSize = 3;
                else if(items.size()<=15)
                    options.inSampleSize = 5;
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

}
