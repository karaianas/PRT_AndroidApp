package com.example.karaianas.prt;

/**
 * Created by karaianas on 12/11/2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

import android.opengl.GLUtils.*;

public class Skybox {
    private int program;
    private Context mContext;

    // Buffers
    private final FloatBuffer positionBuffer;
//    private final FloatBuffer normalBuffer;

    // Handles
    private int mMHandle;
    private int mTexHandle;
    private int mPositionHandle;

    int positionIdx;
//    int normalIdx;
    int skyboxIdx;


    public Skybox(Context context)
    {
        mContext = context;

        // Compile shaders
        Scanner vScanner = new Scanner(context.getResources().openRawResource(R.raw.skybox_vert), "UTF-8");
        String vertexShaderCode = vScanner.useDelimiter("\\A").next();
        vScanner.close();

        Scanner fScanner = new Scanner(context.getResources().openRawResource(R.raw.skybox_frag), "UTF-8");
        String fragmentShaderCode = fScanner.useDelimiter("\\A").next();
        fScanner.close();

        // Create shader objects
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

        // Pass shader objects to the compiler
        GLES20.glCompileShader(vertexShader);
        GLES20.glCompileShader(fragmentShader);

        // Create new program and link
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        // Initialize the buffers
        positionBuffer = ByteBuffer.allocateDirect(position.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        positionBuffer.put(position).position(0);

//        normalBuffer = ByteBuffer.allocateDirect(normal.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
//        normalBuffer.put(normal).position(0);

        final int buffers[] = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, positionBuffer.capacity() * 4, positionBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normalBuffer.capacity() * 4, normalBuffer, GLES20.GL_STATIC_DRAW);
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        positionIdx = buffers[0];
//        normalIdx = buffers[1];

        skyboxIdx = loadTextures();
    }

    public void draw(float[] M, float[] V, float[] P)
    {
        GLES20.glUseProgram(program);

        mMHandle = GLES20.glGetUniformLocation(program, "u_matrix");
        mPositionHandle = GLES20.glGetAttribLocation(program, "a_position");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, skyboxIdx);
        mTexHandle = GLES20.glGetUniformLocation(program, "u_texture");
        GLES20.glUniform1i(mTexHandle, 0);

        // Bind vertices
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionIdx);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //Log.d("VAL", positionIdx + ", " + normalIdx + "\n" + mPositionHandle + ", " + mNormalHandle);

        // Pass MVP matrix
        float [] temp = new float[16];
        Matrix.multiplyMM(temp, 0, V, 0, M, 0);
        Matrix.multiplyMM(temp, 0, P, 0, temp, 0);
        GLES20.glUniformMatrix4fv(mMHandle, 1, false, temp, 0);

        // Draw
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    public int loadTextures()
    {
        ByteBuffer fcbuffer = null;

        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP,texture[0]);

        Bitmap img = null;
        img = BitmapFactory.decodeResource(mContext.getResources(), R.raw.right);
        fcbuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() * 4);

        img.copyPixelsToBuffer(fcbuffer);
        fcbuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGBA,
                img.getWidth(),img.getHeight() , 0,GLES20.GL_RGBA ,GLES20.GL_UNSIGNED_BYTE, fcbuffer);
        fcbuffer = null;
        img.recycle();

        img = BitmapFactory.decodeResource(mContext.getResources(), R.raw.left);
        fcbuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() * 4);
        img.copyPixelsToBuffer(fcbuffer);
        fcbuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES20.GL_RGBA,
                img.getWidth(),img.getHeight(), 0,GLES20.GL_RGBA ,GLES20.GL_UNSIGNED_BYTE, fcbuffer);
        fcbuffer = null;
        img.recycle();

        img = BitmapFactory.decodeResource(mContext.getResources(), R.raw.top);
        fcbuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() * 4);
        img.copyPixelsToBuffer(fcbuffer);
        fcbuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGBA,
                img.getWidth(),img.getHeight(), 0,GLES20.GL_RGBA ,GLES20.GL_UNSIGNED_BYTE, fcbuffer);
        fcbuffer = null;
        img.recycle();


        img = BitmapFactory.decodeResource(mContext.getResources(), R.raw.bottom);
        fcbuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() * 4);
        img.copyPixelsToBuffer(fcbuffer);
        fcbuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGBA,
                img.getWidth(),img.getHeight(), 0,GLES20.GL_RGBA ,GLES20.GL_UNSIGNED_BYTE, fcbuffer);
        fcbuffer = null;
        img.recycle();

        img = BitmapFactory.decodeResource(mContext.getResources(), R.raw.far);
        fcbuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() * 4);
        img.copyPixelsToBuffer(fcbuffer);
        fcbuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGBA,
                img.getWidth(),img.getHeight(), 0,GLES20.GL_RGBA ,GLES20.GL_UNSIGNED_BYTE, fcbuffer);
        fcbuffer = null;
        img.recycle();

        img = BitmapFactory.decodeResource(mContext.getResources(), R.raw.near);
        fcbuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() * 4);
        img.copyPixelsToBuffer(fcbuffer);
        fcbuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGBA,
                img.getWidth(),img.getHeight(), 0,GLES20.GL_RGBA ,GLES20.GL_UNSIGNED_BYTE, fcbuffer);
        fcbuffer = null;
        img.recycle();

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


        return texture[0];
    }

    public static final float[] position = new float[]{
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
    };

    public static final float[] normal = new float[]{
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
    };

    public static final float[] texCoord = new float[]{
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Right face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Back face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Left face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Top face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Bottom face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };
}
