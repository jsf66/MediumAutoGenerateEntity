package com.unionpay.ost.utils;

import com.unionpay.ost.exception.MyException;

import java.io.File;

/**
 * 封装了文件常用的操作
 * Created by jsf on 16/8/3..
 */
public class FileUtils {
    /**
     * 迭代删除目录下的所有文件
     * @param file
     */
    public static void deleteAllFile(File file){
        if(file==null){
            throw new MyException("无法删除一个空的文件");
        }
        if(file.exists()){
            File[] files=file.listFiles();
            if(files!=null && files.length!=0){
                for(File f:files){
                    if(f.isDirectory()){
                        deleteAllFile(f);
                    }else{
                        f.delete();
                    }
                }
            }
        }

    }
    /**
     * 创建目录,如果该目录下有同名文件,则进行删除
     * @param file
     */
    public static void deleteExistFile(File file) {
        if (file == null) {
            return ;
        }
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        } else {
            //如果原来有相同目录下有同名文件,则删除后再进行重新生成
            File[] files = parentFile.listFiles();
            if (files != null && files.length != 0) {
                for (File f : files) {
                    if (f.getName().equals(file.getName())) {
                        f.delete();
                        break;
                    }
                }
            }
        }
    }

}
