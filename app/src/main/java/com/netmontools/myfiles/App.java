package com.netmontools.myfiles;

import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class App extends Application {

    public static App instance;

    public static ArrayList<Folder> folders = new ArrayList<Folder>();
    public static String[] share = null;
    public static String rootPath, previousPath;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        folders.clear();
        ScanThread scanRootPath = new ScanThread();
        scanRootPath.start();
    }

    public static App getInstance() {
        return instance;
    }

    public class ScanThread extends Thread {
        @Override
        public void run() {
            doInBackground();
        }
    }

    protected void doInBackground(Void... voids) {

        try {
            Folder fd;
            File file = new File("/");
            if (file.exists()) {
                rootPath = file.getPath();
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.exists()) {
                        fd = new Folder();
                        if (f.isDirectory()) {
                            fd.setName(f.getName());
                            fd.setPath(f.getPath());
                            folders.add(fd);
                        }
                    }
                }
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.exists()) {
                        fd = new Folder();
                        if (f.isFile()) {
                            fd.setName(f.getName());
                            fd.setPath(f.getPath());
                            folders.add(fd);
                        }
                    }
                }
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            try {
                Folder fd;
                File file = new File(Environment.getExternalStorageDirectory().getPath());
                if (file.exists()) {
                    rootPath = file.getPath();
                    for (File f : Objects.requireNonNull(file.listFiles())) {
                        if (f.exists()) {
                            fd = new Folder();
                            if (f.isDirectory()) {
                                fd.setName(f.getName());
                                fd.setPath(f.getPath());
                                folders.add(fd);
                            }
                        }
                    }
                    for (File f : Objects.requireNonNull(file.listFiles())) {
                        if (f.exists()) {
                            fd = new Folder();
                            if (f.isFile()) {
                                fd.setName(f.getName());
                                fd.setPath(f.getPath());
                                folders.add(fd);
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }//2 catch
        }
    }//doInBackground
}
