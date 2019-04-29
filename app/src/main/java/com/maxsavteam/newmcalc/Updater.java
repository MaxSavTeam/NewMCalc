package com.maxsavteam.newmcalc;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class Updater extends AppCompatActivity {

    public StorageReference mStorageRef;
    Boolean downloading = false;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference("versionCode");
    DatabaseReference refCount = db.getReference("version");
    File localFile;
    StorageReference riversRef;
    String file_url_path = "http://maxsavteam.tk/apk/NewMCalc.apk";

    SharedPreferences sp;
    AlertDialog deval;

    String newversion;
    File outputFile = null;


    protected void backPressed(){
        if(downloading){
            Toast.makeText(getApplicationContext(), "Wait for the download to finish...", Toast.LENGTH_LONG).show();
        }else{
            finish();
            overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha);
        }
    }


    @Override
    public void onBackPressed(){
        backPressed();
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
        if(id == android.R.id.home){
            backPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    String vk = "maksin.colf", insta = "maksin.colf/", facebook = "profile.php?id=100022307565005", tw = "maks_savitsky", site = "maxsavteam.tk";

    ValueEventListener list = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            switch (Objects.requireNonNull(dataSnapshot.getKey())) {
                case "vk":
                    vk = dataSnapshot.getValue(String.class);
                    break;
                case "insta":
                    insta = dataSnapshot.getValue(String.class);
                    break;
                case "twitter":
                    tw = dataSnapshot.getValue(String.class);
                    break;
                case "facebook":
                    facebook = dataSnapshot.getValue(String.class);
                    break;
                case "site":
                    site = dataSnapshot.getValue(String.class);
                    break;
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(), "cancelled", Toast.LENGTH_SHORT).show();
        }
    };

    AlertDialog dl;


    View.OnClickListener notJoin = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            //findViewById(R.id.layDev).setVisibility(View.GONE);
            sp.edit().putBoolean("show_laydev", false).apply();
            deval.cancel();
        }
    };



    public void social(View v){
        Intent in = new Intent(Intent.ACTION_VIEW);
        if(v.getId() == R.id.imgBtnVk){
            in.setData(Uri.parse("https://vk.com/" + vk));
        }else if(v.getId() == R.id.imgBtnInsta){
            in.setData(Uri.parse("https://instagram.com/" + insta));
        }else if(v.getId() == R.id.imgBtnTw){
            in.setData(Uri.parse("https://twitter.com/" + tw));
        }else if(v.getId() == R.id.imgBtnWeb){
            in.setData(Uri.parse("https://" + site));
        }
        startActivity(in);
    }

    public void clear_history(View v){
        dl.show();
    }

    View.OnLongClickListener show_join = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View v){
            sp.edit().remove("stop_receive_all").apply();
            sp.edit().remove("show_laydev").apply();
            Toast.makeText(getApplicationContext(), "You can join to testers community", Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    View.OnClickListener join = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*setContentView(R.layout.layout_updater);
            TextView t = findViewById(R.id.txtDownloading);
            t.setText(R.string.please_wait);*/
            sp.edit().putBoolean("isdev", true).apply();
            /*try{
                Thread.sleep(1500);
            }catch(Exception e){
                e.printStackTrace();
            }*/
            setContentView(R.layout.updater_main);
            //findViewById(R.id.layDev).setVisibility(View.GONE);
            sp.edit().putBoolean("show_laydev", false).apply();
            findViewById(R.id.btnStopReceive).setVisibility(View.VISIBLE);
            deval.cancel();
            Toast.makeText(getApplicationContext(), "Now you are a tester!\nTo apply the settings, restart the application.", Toast.LENGTH_SHORT).show();
        }
    };

    AlertDialog report_al;
    Intent send = new Intent(Intent.ACTION_SEND);

    public void report(View v){
        send.putExtra(Intent.EXTRA_EMAIL, "maxsavsu@gmail.com");
        send.putExtra(Intent.EXTRA_SUBJECT, "Problem in New MCalc");
        send.putExtra(Intent.EXTRA_TEXT, "[" + BuildConfig.VERSION_NAME + "," + BuildConfig.VERSION_CODE + "]\nProblem:\n");
        send.setType("message/rfc822");
        report_al.show();
        //Toast.makeText(getApplicationContext(), R.string.donot_clear_info_email, Toast.LENGTH_LONG).show();
    }

    public void stop_receive(View v){
        AlertDialog.Builder build = new AlertDialog.Builder(Updater.this);
        build.setTitle(R.string.confirm)
                .setMessage(R.string.stop_receive_mes)
                .setCancelable(false)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //findViewById(R.id.layDev).setVisibility(View.GONE);
                        sp.edit().putBoolean("show_laydev", false).apply();
                        sp.edit().putBoolean("isdev", false).apply();
                        sp.edit().putBoolean("stop_receive_all", true).apply();
                        findViewById(R.id.btnStopReceive).setVisibility(View.INVISIBLE);
                        findViewById(R.id.layoutUpdate).setVisibility(View.INVISIBLE);
                    }
                });
        AlertDialog dialog = build.create();
        dialog.show();
    }

    String up_path = "";
    String up_type = "simple", newDevVer = "";
    int newCodeDev = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updater_main);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        findViewById(R.id.imgBtnWeb).setOnLongClickListener(show_join);

        if(sp.getBoolean("isdev", false)){
            if(!sp.getBoolean("stop_receive_all", false) && !sp.getBoolean("show_laydev", true))
                findViewById(R.id.btnStopReceive).setVisibility(View.VISIBLE);
            //findViewById(R.id.layDev).setVisibility(View.GONE);
        }else{
            if(sp.getBoolean("show_laydev", true)){
                View mView = getLayoutInflater().inflate(R.layout.join_testers, null);
                mView.findViewById(R.id.btnNotJoin).setOnClickListener(notJoin);
                mView.findViewById(R.id.btnGetBuilds).setOnClickListener(join);
                AlertDialog.Builder builddev = new AlertDialog.Builder(this);
                builddev.setView(mView).setCancelable(false);
                deval = builddev.create();
                deval.show();
            }
        }
       // StrictMode.enableDefaults();
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

        }
        DatabaseReference devCode = db.getReference("dev/versionCodeDev");
        DatabaseReference devVer = db.getReference("dev/versionDev");


        if(sp.getBoolean("isdev", false)){
            devVer.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    newDevVer = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        refCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newversion = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                newversion = "";
            }
        });

        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle(R.string.confirm)
                .setMessage(R.string.confirm_cls_history)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sp.edit().putString("history", "").apply();
                        findViewById(R.id.btnClsHistory).setVisibility(View.INVISIBLE);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        dl = build.create();
        build = new AlertDialog.Builder(this);
        build.setCancelable(true).setMessage(R.string.donot_clear_info_email).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startActivity(Intent.createChooser(send, "Choose email client"));
            }
        });
        report_al = build.create();

        if(!sp.getString("history", "").equals("")){
            findViewById(R.id.btnClsHistory).setVisibility(View.VISIBLE);
        }
        Intent action = getIntent();
        if(action.getStringExtra("action").equals("update")){
            setContentView(R.layout.layout_updater);
            downloading = true;
            try{
                Thread.sleep(250);
            }catch(Exception e){
                e.printStackTrace();
            }
            new DownloadingTask().execute();
            return;
        }
        up_path = action.getStringExtra("update_path");

        DatabaseReference dbm = db.getReference("links/vk");
        dbm.addValueEventListener(list);
        dbm = db.getReference("links/insta");
        dbm.addValueEventListener(list);
        dbm = db.getReference("links/facebook");
        dbm.addValueEventListener(list);
        dbm = db.getReference("links/twitter");
        dbm.addValueEventListener(list);
        dbm = db.getReference("links/site");
        dbm.addValueEventListener(list);

        if (sp.getBoolean("isdev", false)){
            devCode.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue().toString();
                    //int versionMy = Integer.valueOf(vercode);
                    newCodeDev = Integer.valueOf(value);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        if(sp.getBoolean("isdev", false)){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String vercode = Integer.toString(BuildConfig.VERSION_CODE);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                Integer versionMy = Integer.valueOf(vercode);
                Integer versionNew = Integer.valueOf(value);
                TextView up = findViewById(R.id.txtUpdate);
                if(versionNew > versionMy && (newCodeDev == versionNew || newCodeDev == 0)){
                    up_type = "simple";
                    up_path = "/NewMCalc.apk";
                    LinearLayout l = findViewById(R.id.layoutUpdate);
                    l.setVisibility(LinearLayout.VISIBLE);
                    up.setText(R.string.updateavail);
                }else if(versionNew < newCodeDev && versionMy < newCodeDev){
                    up_type = "dev";
                    up_path = "/forTesters/NewMCalc.apk";
                    LinearLayout l = findViewById(R.id.layoutUpdate);
                    l.setVisibility(LinearLayout.VISIBLE);
                    up.setText(R.string.updateavail_tc);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseDB", "Cancelled: " + databaseError.toString());
            }
        });
    }

    public void installupdate(String path){
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("file://" + path),
                        "application/vnd.android.package-archive");
        startActivity(promptInstall);
    }

    public void update(View v){
        setContentView(R.layout.layout_updater);
        downloading = true;
        try{
            Thread.sleep(250);
        }catch(Exception e){
            e.printStackTrace();
        }
        new DownloadingTask().execute();
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void> {

        File apkStorage = null;


        @Override
        protected void onPostExecute(Void result) {

            try {
                if (outputFile != null) {
                    //Susses Download
                    setContentView(R.layout.downloaded);
                    Thread.sleep(1000);
                    downloading = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(Updater.this);
                    builder.setTitle(R.string.succesful)
                            .setMessage(getResources().getString(R.string.savedto) + " " + outputFile.getPath())
                            .setCancelable(false)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog al = builder.create();
                    al.show();
                    installupdate(outputFile.getPath());
                } else {
                    //Failed Download
                    downloading = false;
                    setContentView(R.layout.downloaded);
                    TextView t = findViewById(R.id.textViewDownloaded);
                    t.setText(getResources().getString(R.string.downloaderr));
                    ImageView img = (ImageView)findViewById(R.id.imageView);
                    img.setImageResource(R.drawable.error);
                    t = findViewById(R.id.textView3);
                    t.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                Toast.makeText(Updater.this, "Download Failed", Toast.LENGTH_SHORT).show();
            }

            super.onPostExecute(result);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            sp.edit().putInt("notremindfor", 0).apply();*/

            try {
                int permissionStatus = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(permissionStatus == PackageManager.PERMISSION_GRANTED){
                    boolean success = false;
                    //That is url file you want to download
                    URL url = new URL("https://maxsavteam.tk/apk" + up_path);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("GET");
                    c.connect();
                    //Toast.makeText(getApplicationContext(), Environment.getExternalStorageDirectory().toString(), Toast.LENGTH_LONG).show();
                    //Creating Path
                    apkStorage = new File(
                            Environment.getExternalStorageDirectory().getPath() + "/"
                                    + "MST files");

                    if (!apkStorage.exists()) {
                        //Create Folder From Path
                        success = apkStorage.mkdir();
                    }else{
                        success = true;
                    }
                    if(success){
                        outputFile = new File(apkStorage, "/NewMCalc " + newversion + ".apk");

                        if (!outputFile.exists()) {
                            success = outputFile.createNewFile();
                            Log.e("clipcodes", "File Created");
                        }
                        if(success){
                            FileOutputStream fos = new FileOutputStream(outputFile);

                            InputStream is = c.getInputStream();


                            byte[] buffer = new byte[1024];
                            int len1 = 0;
                            while ((len1 = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len1);
                            }

                            fos.close();
                            is.close();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Make dir error", Toast.LENGTH_SHORT).show();
                    }
                }

                //Path And Filename.type
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }

            return null;
        }
    }
}