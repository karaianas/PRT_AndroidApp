package com.example.karaianas.prt;

/**
 * Created by karaianas on 12/11/2017.
 */

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;

public class cGLRenderer extends GvrActivity implements GvrView.StereoRenderer{
    private Context mContext;

    private Skybox mSkybox;
    private Obj mObj;

    // Matrices
    private float[] M_skybox = new float[16];
    private float[] M_object = new float[16];
    private float[] C = new float[16];
    private float[] V = new float[16];
    private float[] MV_skybox = new float[16];
    private float[] VP = new float[16];
    private float[] MVP_skybox = new float[16];
    private float[] R = new float[16];

    private float [] headRotation = new float[4];
    private float [] headView = new float[16];

    // Settings
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.0f;
    private static final float CAMERA_Z = -5.0f;// was positive
    public volatile float rangle;

    public cGLRenderer(Context context)
    {
        mContext = context;
    }

    public void initializeMatrices()
    {
        Matrix.setIdentityM(M_skybox, 0);
        Matrix.scaleM(M_skybox, 0, 10, 10, 10);
        Matrix.setIdentityM(M_object, 0);
        Matrix.translateM(M_object, 0, 0, -2.0f, 0);
        //Matrix.rotateM(M_object, 0, 90, 1, 0, 0);
        //Matrix.scaleM(M_object, 0, 2, 2, 2);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig)
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        initializeMatrices();

        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        mSkybox = new Skybox(mContext);

        // Choose which model to draw;
        // ultimately you load all of them at once and just change them on the fly
        // model01 is wrong and model03 is too big to be rendered
        int modelId = 1;
        mObj = new Obj(mContext, modelId);
        rangle = 0;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform)
    {
        // Build the camera matrix and apply it to the ModelView.
        float eye_y = 0.0f;
        Matrix.setLookAtM(C, 0, 0.0f, eye_y, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);
        headTransform.getQuaternion(headRotation, 0);
    }

    @Override
    public void onDrawEye(Eye eye)
    {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(V, 0, eye.getEyeView(), 0, C, 0);

        // Build the MV and MVP matrices
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
//        Matrix.multiplyMM(MV_skybox, 0, V, 0, M_skybox, 0);
//        Matrix.multiplyMM(MVP_skybox, 0, perspective, 0, MV_skybox, 0);
//        Matrix.multiplyMM(VP, 0, perspective, 0, V, 0);

        mSkybox.draw(M_skybox, V, perspective);
        mObj.draw(M_object, V, perspective);
        mObj.rotateCoeff(1.0f, 2.0f, 3.0f);

        // Rotating the skybox on XY-plane
        rangle = 0.2f;
        Matrix.setRotateM(R, 0, rangle, 0.0f, 1.0f, 0.0f);
//        Matrix.multiplyMM(M_skybox, 0, R, 0, M_skybox, 0);
        Matrix.multiplyMM(M_object, 0, R, 0, M_object, 0);
    }

    @Override
    public void onFinishFrame(Viewport viewport)
    {

    }

    @Override
    public void onSurfaceChanged(int i, int i1)
    {

    }

    @Override
    public void onRendererShutdown()
    {

    }
}
