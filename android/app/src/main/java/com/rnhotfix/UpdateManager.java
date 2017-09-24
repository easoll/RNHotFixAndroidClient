package com.rnhotfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by easoll on 2017/9/22.
 */

public class UpdateManager {
    private static final String TAG = UpdateManager.class.getName();

    private static final String PREFERENCE_KEY_JS_BUNDLE_VERSION = "js_bundle_version";
    private static final String PREFERENCE_KEY_USING_DIR = "using_dir";
    private static final String DIR_LEFT = "left";
    private static final String DIR_RIGHT = "right";

    private static UpdateManager mInstance;
    private OkHttpClient mClient = new OkHttpClient();
    private WeakReference<Context> mContextRef;
    private String mUsingDir;
    private SharedPreferences mSp;
    private String mBaseDir;

    public static UpdateManager getInstance(Context context){
        if(mInstance == null){
            synchronized (UpdateManager.class){
                if(mInstance == null){
                    mInstance = new UpdateManager(context);
                }
            }
        }

        return mInstance;
    }

    private UpdateManager(Context context){
        mContextRef = new WeakReference<>(context);
        mSp = context.getSharedPreferences("update", Context.MODE_PRIVATE);
        mUsingDir = mSp.getString(PREFERENCE_KEY_USING_DIR, DIR_LEFT);
        mBaseDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }


    public void update() {
        Context context = mContextRef.get();
        if (context == null) {
            return;
        }
        String jsBundleVersion = mSp.getString(PREFERENCE_KEY_JS_BUNDLE_VERSION, "1.0.0.0");
        final Request request = new Request.Builder().url("http://192.168.73.90:5000/update/" + jsBundleVersion).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "get update info failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "get update info success");
                ResponseBody body = response.body();
                if (body == null){
                    return;
                }
                String str = body.string();
                UpdateModel model = new Gson().fromJson(str, UpdateModel.class);
                if(model.needUpdate){
                    Log.i(TAG, "need to download update bundle");
                    downloadJSBundle(model);
                }else {
                    Log.i(TAG, "don't need to download update bundle");
                }
            }
        });
    }

    private void downloadJSBundle(final UpdateModel updateModel){
        final Request request = new Request.Builder().url(updateModel.downloadUrl).build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "download js bundle failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String fileName;
                if(DIR_LEFT.equals(mUsingDir)){
                    fileName = mBaseDir + DIR_RIGHT + File.separator + "index.android.bundle";
                }else{
                    fileName = mBaseDir + DIR_LEFT + File.separator + "index.android.bundle";
                }

                File bundleFile = new File(fileName);
                if(bundleFile.exists()){
                    FileUtil.deleteFile(bundleFile);
                }
                if(!bundleFile.getParentFile().exists()){
                    if(!bundleFile.getParentFile().mkdirs()){
                        throw new RuntimeException("new dir failed");
                    }
                }

                OutputStream os = new FileOutputStream(bundleFile);
                ResponseBody body = response.body();
                if(body == null){
                    return;
                }
                InputStream is = body.byteStream();
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) != -1){
                    os.write(buffer, 0, count);
                }

                is.close();
                os.close();

                //TODO 添加文件完整性验证

                if(DIR_RIGHT.equals(mUsingDir)){
                    mUsingDir = DIR_LEFT;
                }else {
                    mUsingDir = DIR_RIGHT;
                }

                SharedPreferences.Editor editor = mSp.edit();
                editor.putString(PREFERENCE_KEY_USING_DIR, mUsingDir);
                editor.putString(PREFERENCE_KEY_JS_BUNDLE_VERSION, updateModel.latestVersion);
                editor.apply();

                Log.i(TAG, "download js bundle success");
            }
        });
    }

    public String getJsBundleFileAbsolutePath(){
       return  mBaseDir + mUsingDir + File.separator + "index.android.bundle";
    }
}
