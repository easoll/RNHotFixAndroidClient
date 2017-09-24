package com.rnhotfix;

import java.io.File;

/**
 * Created by easoll on 2017/9/22.
 */

public class FileUtil {
    public static void deleteFile(File file){
        if(file.exists()){
            if(file.isDirectory()){
                for(File f : file.listFiles()){
                    deleteFile(f);
                }
            }else{
                if(!file.delete()){
                    throw new RuntimeException("文件删除失败");
                }
            }
        }
    }
}
