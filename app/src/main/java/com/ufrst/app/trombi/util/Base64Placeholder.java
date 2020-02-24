package com.ufrst.app.trombi.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Classe contenant un bitmap contenu dans les assets, convertit en base64, utlisé pour le HTML
public class Base64Placeholder {

    private static final String PLACEHOLDER_FILENAME = "placeholder.png";

    private static Base64Placeholder placeholder;
    private String base64Placeholder;

    private Base64Placeholder(Context context){
        try{
            Bitmap bm = BitmapFactory.decodeStream(context.getAssets().open(PLACEHOLDER_FILENAME));

            if(bm != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.close();
                base64Placeholder = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            }
        } catch(IOException e){
            Logger.handleException(e);
        }
    }

    public static synchronized Base64Placeholder getInstance(Context context){
        if(placeholder == null){
            placeholder = new Base64Placeholder(context);
        }

        return placeholder;
    }

    public String getBase64Placeholder(){
        return base64Placeholder;
    }
}
