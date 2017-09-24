package com.rnhotfix;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by easoll on 2017/9/22.
 */

public class MyReactNativeHost extends ReactNativeHost {

    public MyReactNativeHost(Application application){
        super(application);
    }

    @Override
    public boolean getUseDeveloperSupport() {
        return false;
    }

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
                new MainReactPackage()
        );
    }

    @Nullable
    @Override
    protected String getJSBundleFile() {
        String jsBundleFile = UpdateManager.getInstance(getApplication()).getJsBundleFileAbsolutePath();
        if(new File(jsBundleFile).exists()){
            return jsBundleFile;
        }else {
            return null;
        }
    }
}
