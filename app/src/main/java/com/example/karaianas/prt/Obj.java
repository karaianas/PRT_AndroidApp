package com.example.karaianas.prt;

/**
 * Created by User on 1/8/2018.
 */

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;

public class Obj {
    private int program;
    private Context mContext;

    private List<String> verticesList = new ArrayList<>();
    private List<String> facesList = new ArrayList<>();

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;

    public Obj(Context context)
    {
        mContext = context;

        InputStream is = mContext.getResources().openRawResource(R.raw.model01);


    }
}
