package com.master.voice;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.*;

public class Assets {

    public static void copyAssetDirToInternalStorage(Context context, String assetDir, String destDir) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] assets = assetManager.list(assetDir);
        if (assets == null || assets.length == 0) {
            copyAssetToFile(context, assetDir, destDir);
        } else {
            File dir = new File(context.getFilesDir(), destDir);
            if (!dir.exists()) dir.mkdirs();

            for (String asset : assets) {
                copyAssetDirToInternalStorage(context, assetDir + "/" + asset, destDir + "/" + asset);
            }
        }
    }

    public static void copyAssetToFile(Context context, String assetPath, String destPath) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream in = assetManager.open(assetPath);
        File outFile = new File(context.getFilesDir(), destPath);
        OutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        in.close();
        out.flush();
        out.close();
    }
}
