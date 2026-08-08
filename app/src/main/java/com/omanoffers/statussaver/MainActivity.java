package com.omanoffers.statussaver;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private RecyclerView grid;
    private TextView info;
    private StatusAdapter adapter;
    private ArrayList<File> items=new ArrayList<>();
    private final String BASE="/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        grid=findViewById(R.id.grid); info=findViewById(R.id.info);
        grid.setLayoutManager(new GridLayoutManager(this,2));
        findViewById(R.id.refresh).setOnClickListener(v->load());
        findViewById(R.id.delete_all).setOnClickListener(v->deleteAll());
        if(Build.VERSION.SDK_INT>=30 && !Environment.isExternalStorageManager()) requestFilesAccess();
        load();
    }

    private void requestFilesAccess(){
        try{
            Intent i=new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            i.setData(Uri.parse("package:"+getPackageName()));
            startActivity(i);
        }catch(Exception e){
            try{startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));}catch(Exception ignored){}
        }
    }

    private void load(){
        items.clear();
        File root=new File(BASE+"accounts");
        ArrayList<File> statusDirs=new ArrayList<>();
        statusDirs.add(new File(BASE+"Media/.Statuses"));
        if(root.isDirectory()){
            File[] accounts=root.listFiles();
            if(accounts!=null) for(File a:accounts)
                if(a.isDirectory()) statusDirs.add(new File(a,"Media/.Statuses"));
        }
        for(File d:statusDirs){
            File[] fs=d.listFiles();
            if(fs==null) continue;
            for(File f:fs) if(f.isFile() && media(f)) items.add(f);
        }
        Collections.sort(items,(a,b)->Long.compare(b.lastModified(),a.lastModified()));
        info.setText("تم العثور على "+items.size()+" حالة");
        adapter=new StatusAdapter(this,items);
        grid.setAdapter(adapter);
    }

    private void deleteAll(){
        if(items.isEmpty()){
            Toast.makeText(this,"لا توجد حالات لحذفها",Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("حذف الكل")
            .setMessage("هل أنت متأكد من حذف جميع الحالات ("+items.size()+" ملف)؟")
            .setPositiveButton("نعم",(d,w)->{
                int deleted=0;
                for(File f:items){
                    if(f.exists() && f.delete()) deleted++;
                }
                Toast.makeText(this,"تم حذف "+deleted+" ملف",Toast.LENGTH_LONG).show();
                load();
            })
            .setNegativeButton("إلغاء",null)
            .show();
    }

    private boolean media(File f){
        String n=f.getName().toLowerCase(Locale.ROOT);
        return n.endsWith(".jpg")||n.endsWith(".jpeg")||n.endsWith(".png")||n.endsWith(".webp")||
               n.endsWith(".mp4")||n.endsWith(".3gp")||n.endsWith(".mkv")||n.endsWith(".mov");
    }
}
