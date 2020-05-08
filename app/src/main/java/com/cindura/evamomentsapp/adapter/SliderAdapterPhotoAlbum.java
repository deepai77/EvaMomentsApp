package com.cindura.evamomentsapp.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.cindura.evamomentsapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

//Class to show the slide show of the album contents
public class SliderAdapterPhotoAlbum extends PagerAdapter {
    Context context;
    LayoutInflater inflater;
    List<Uri> items;

    public SliderAdapterPhotoAlbum(Context context, List<Uri> items) {
        this.context = context;
        this.items = items;
    }
    @Override
    public int getCount() {
       return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==(RelativeLayout)object;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {  //provides the slider effect by creating multiple views

        inflater=(LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.photo_album_slide_layout,container,false);  //(resource layout,viewGroup,attachtoroot)

        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType=cR.getType(items.get(position));

        System.out.println("mime type : "+mimeType);
        if(mimeType==null){
            ImageView imageView=(ImageView)view.findViewById(R.id.profile);
            imageView.setImageResource(R.drawable.media_not_found_image);
        }
        else if(mimeType.contains("image")) {
            ImageView imageView=(ImageView)view.findViewById(R.id.profile);

            VideoView videoView=(VideoView)view.findViewById(R.id.videoView);
            videoView.setVisibility(View.GONE);
            View viewSpace=view.findViewById(R.id.viewSpace);
            viewSpace.setVisibility(View.GONE);
            Cursor returnCursor =
                    context.getContentResolver().query(items.get(position), null, null, null, null);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            long size = returnCursor.getLong(sizeIndex);

            System.out.println("file size: "+size);
            if(size < 100000) {
                imageView.setImageURI(items.get(position));
            }
            else
                imageView.setImageBitmap(getBitmap(items.get(position)));
        }
        else if(mimeType.contains("video"))
        {
            ImageView imageView=(ImageView)view.findViewById(R.id.profile);
            VideoView videoView=(VideoView)view.findViewById(R.id.videoView);
            View viewSpace=view.findViewById(R.id.viewSpace);
            viewSpace.setVisibility(View.VISIBLE);
            RelativeLayout relativeLayout=view.findViewById(R.id.layout);
            MediaController mediaController=new MediaController(context);
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);

            relativeLayout.setTag("r"+position);
           videoView.setTag("video"+position);
           mediaController.setTag("MC"+position);
           viewSpace.setTag("vSpace"+position);
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {   //this method helps in stopping on the last slide(view) i.e,removes the unwanted multiple views created

        container.removeView((RelativeLayout)object);

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
                options.inSampleSize = 3;
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
