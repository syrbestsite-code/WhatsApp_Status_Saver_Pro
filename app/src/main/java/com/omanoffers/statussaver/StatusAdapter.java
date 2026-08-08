package com.omanoffers.statussaver;

import android.content.*;
import android.net.Uri;
import android.graphics.*;
import android.media.ThumbnailUtils;
import android.media.MediaScannerConnection;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.FileProvider;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.VH>{
 private final Context c; private final ArrayList<File> data;
 StatusAdapter(Context c,ArrayList<File>d){this.c=c;data=d;}

 @Override public VH onCreateViewHolder(ViewGroup p,int t){
  LinearLayout box=new LinearLayout(c); box.setOrientation(LinearLayout.VERTICAL);
  box.setBackgroundColor(Color.WHITE); box.setPadding(5,5,5,8);
  ImageView img=new ImageView(c); img.setScaleType(ImageView.ScaleType.CENTER_CROP);
  box.addView(img,new LinearLayout.LayoutParams(-1,420));
  LinearLayout bar=new LinearLayout(c); bar.setGravity(Gravity.CENTER);
  bar.setPadding(0,8,0,0);
  Button save=new Button(c); save.setText("💾 حفظ"); save.setTextSize(12);
  Button view=new Button(c); view.setText("👁 عرض"); view.setTextSize(12);
  Button share=new Button(c); share.setText("📤 مشاركة"); share.setTextSize(12);
  bar.addView(view,new LinearLayout.LayoutParams(0,-2,1));
  bar.addView(save,new LinearLayout.LayoutParams(0,-2,1));
  bar.addView(share,new LinearLayout.LayoutParams(0,-2,1));
  box.addView(bar);
  return new VH(box,img,save,view,share);
 }

 @Override public void onBindViewHolder(VH h,int pos){
  File f=data.get(pos);
  Bitmap b=thumb(f);
  if(b!=null)h.img.setImageBitmap(b);
  else h.img.setImageResource(android.R.drawable.ic_menu_report_image);
  h.save.setOnClickListener(v->save(f));
  h.view.setOnClickListener(v->open(f));
  h.share.setOnClickListener(v->share(f));
 }

 @Override public int getItemCount(){return data.size();}

 private Bitmap thumb(File f){
  String n=f.getName().toLowerCase(Locale.ROOT);
  if(n.endsWith(".mp4")||n.endsWith(".3gp")||n.endsWith(".mkv")||n.endsWith(".mov"))
    return ThumbnailUtils.createVideoThumbnail(f.getAbsolutePath(),MediaStore.Video.Thumbnails.MINI_KIND);
  return BitmapFactory.decodeFile(f.getAbsolutePath());
 }

 private void open(File f){
  try {
   Uri uri = FileProvider.getUriForFile(c, c.getPackageName() + ".provider", f);
   Intent i = new Intent(Intent.ACTION_VIEW);
   i.setDataAndType(uri, mime(f));
   i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
   c.startActivity(i);
  } catch(Exception e) {
   Toast.makeText(c, "تعذر فتح الملف", Toast.LENGTH_SHORT).show();
  }
 }

 private void share(File f){
  try {
   Uri uri = FileProvider.getUriForFile(c, c.getPackageName() + ".provider", f);
   Intent i = new Intent(Intent.ACTION_SEND);
   i.setType(mime(f));
   i.putExtra(Intent.EXTRA_STREAM, uri);
   i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
   c.startActivity(Intent.createChooser(i, "مشاركة عبر"));
  } catch(Exception e) {
   Toast.makeText(c, "تعذرت المشاركة", Toast.LENGTH_SHORT).show();
  }
 }

 private String mime(File f){
  String n=f.getName().toLowerCase(Locale.ROOT);
  if(n.endsWith(".mp4"))return "video/mp4";
  if(n.endsWith(".3gp"))return "video/3gpp";
  if(n.endsWith(".mkv"))return "video/x-matroska";
  if(n.endsWith(".mov"))return "video/quicktime";
  if(n.endsWith(".png"))return "image/png";
  if(n.endsWith(".webp"))return "image/webp";
  return "image/jpeg";
 }

 private void save(File src){
  boolean video=src.getName().toLowerCase(Locale.ROOT).matches(".*\\.(mp4|3gp|mkv|mov)$");
  String folder = video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES;
  File dir=new File(Environment.getExternalStoragePublicDirectory(folder),"WhatsApp Statuses");
  if(!dir.exists())dir.mkdirs();
  File dst=new File(dir,src.getName());
  try(InputStream in=new FileInputStream(src);OutputStream out=new FileOutputStream(dst)){
   byte[] buf=new byte[16384]; int n;
   while((n=in.read(buf))!=-1)out.write(buf,0,n);
   out.flush();
   MediaScannerConnection.scanFile(c, new String[]{dst.getAbsolutePath()},
    new String[]{mime(dst)}, null);
   Toast.makeText(c,"✅ تم الحفظ في المعرض",Toast.LENGTH_LONG).show();
  }catch(Exception e){Toast.makeText(c,"فشل الحفظ: "+e.getMessage(),Toast.LENGTH_LONG).show();}
 }

 static class VH extends RecyclerView.ViewHolder{
  ImageView img; Button save,view,share;
  VH(View v,ImageView i,Button s,Button w,Button sh){super(v);img=i;save=s;view=w;share=sh;}
 }
}
