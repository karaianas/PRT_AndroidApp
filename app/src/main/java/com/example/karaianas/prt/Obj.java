package com.example.karaianas.prt;

/**
 * Created by User on 1/8/2018.
 */

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Obj {
    private int program;
    private int positionIdx;
    private int colorIdx;
    private int facesIdx;

    private Context mContext;

    private List<String> verticesList = new ArrayList<>();
    private List<String> facesList = new ArrayList<>();
    private List<String> colorsList = new ArrayList<>();

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;
    private FloatBuffer colorsBuffer;

    private int mPosHandle;
    private int mColorHandle;
    private int mIndHandle;
    private int mMVHandle;
    private int mMVPHandle;

    // Light coefficients
    float [] L00 = {0.79f, 0.44f, 0.54f};
    float [] L1_1 = {0.39f, 0.35f, 0.60f};
    float [] L10 = {-0.34f, -0.18f, -0.27f};
    float [] L11 = {-0.29f, -0.06f, 0.01f};
    float [] L2_2 = {-0.11f, -0.05f, -0.12f};
    float [] L2_1 = {-0.26f, -0.22f, -0.47f};
    float [] L20 = {-0.16f, -0.09f, -0.15f};
    float [] L21 = {0.56f, 0.21f, 0.14f};
    float [] L22 = {0.21f, -0.05f, -0.30f};

    int fcounter = 0;
    int vcounter = 0;
    int ccounter = 0;

    public Obj(Context context, int modelId)
    {
        mContext = context;

        int mId, cId, oId;
        if(modelId == 0)
        {
            mId = R.raw.model01;
            cId = R.raw.model01_coefficients;
            oId = R.raw.model01_order;
        }
        else if(modelId == 1)
        {
            mId = R.raw.model02;
            cId = R.raw.model02_coefficients;
            oId = R.raw.model02_order;
        }
        else
        {
            mId = R.raw.model03;
            cId = R.raw.model03_coeff;
            oId = R.raw.model03_order;
        }

        // Read in positions
        InputStream is_pos = mContext.getResources().openRawResource(mId);
        Scanner scanner = new Scanner(is_pos);

        // Loop through all its lines
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("ply"))
                scanner.nextLine();
            else if (line.startsWith("format"))
                scanner.nextLine();
            else if (line.startsWith("comment"))
                scanner.nextLine();
            else if (line.startsWith("element"))
                scanner.nextLine();
            else if (line.startsWith("property"))
                scanner.nextLine();
            else if (line.startsWith("end_header")) {
                //scanner.nextLine();
            }
            else
            {
                Pattern p = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
                        " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)");

                Matcher m = p.matcher(line);
                m.find();

                String v1 = m.group(1);
                String v2 = m.group(2);
                String v3 = m.group(3);

                v1 = String.valueOf(Float.valueOf(v1));
                v2 = String.valueOf(Float.valueOf(v2));
                v3 = String.valueOf(Float.valueOf(v3));
                verticesList.add(v1 + " " + v2 + " " + v3);

                //Log.i("TEST", v1 + " " + v2 + " " + v3);
            }
        }
        scanner.close();

        // Read in colors
        InputStream is_color = mContext.getResources().openRawResource(cId);
        scanner = new Scanner(is_color);

        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            Pattern p = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
                    " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
                    " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)");
            Matcher m = p.matcher(line);
            m.find();

            String v1 = m.group(1);
            String v2 = m.group(2);
            String v3 = m.group(3);
            String v4 = m.group(4);
            String v5 = m.group(5);
            String v6 = m.group(6);
            String v7 = m.group(7);
            String v8 = m.group(8);
            String v9 = m.group(9);

            //Log.i("TEST", v1 + " " + v9 + " " + v3);
            float val_r =  (Float.parseFloat(v1) *L00[0] +Float.parseFloat(v2) * L1_1[0] + Float.parseFloat(v3) *L10[0] +
                    Float.parseFloat(v4) *L11[0] + Float.parseFloat(v5) *L2_2[0] + Float.parseFloat(v6) *L2_1[0] +
                    Float.parseFloat(v7) *L20[0] + Float.parseFloat(v8) *L21[0] + Float.parseFloat(v9) *L22[0]);

            float val_g =  (Float.parseFloat(v1) *L00[1] +Float.parseFloat(v2) * L1_1[1] + Float.parseFloat(v3) *L10[1] +
                    Float.parseFloat(v4) *L11[1] + Float.parseFloat(v5) *L2_2[1] + Float.parseFloat(v6) *L2_1[1] +
                    Float.parseFloat(v7) *L20[1] + Float.parseFloat(v8) *L21[1] + Float.parseFloat(v9) *L22[1]);

            float val_b =  (Float.parseFloat(v1) *L00[2] +Float.parseFloat(v2) * L1_1[2] + Float.parseFloat(v3) *L10[2] +
                    Float.parseFloat(v4) *L11[2] + Float.parseFloat(v5) *L2_2[2] + Float.parseFloat(v6) *L2_1[2] +
                    Float.parseFloat(v7) *L20[2] + Float.parseFloat(v8) *L21[2] + Float.parseFloat(v9) *L22[2]);

//            float val_r = 1.0f;
//            float val_g = 1.0f;
//            float val_b = 1.0f;

            colorsList.add(Float.toString(val_r) + " " + Float.toString(val_g) + " " + Float.toString(val_b));
        }
        scanner.close();

        // Read in indices
        InputStream is_ind = mContext.getResources().openRawResource(oId);
        scanner = new Scanner(is_ind);

        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            Pattern p = Pattern.compile("(\\d+) (\\d+) (\\d+) (\\d+)");
            Matcher m = p.matcher(line);
            m.find();
            String v1 = m.group(2);
            String v2 = m.group(3);
            String v3 = m.group(4);

            //Log.d("FACE:", v1 + " " + v2 + " " + v3);
            facesList.add(v1 + " " + v2 + " " + v3);
        }
        scanner.close();

        // Create buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4);
        buffer1.order(ByteOrder.nativeOrder());
        verticesBuffer = buffer1.asFloatBuffer();

        // Create buffer for colors
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(colorsList.size() * 3 * 4);
        buffer2.order(ByteOrder.nativeOrder());
        colorsBuffer = buffer2.asFloatBuffer();

        // Create buffer for indices
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2);
        buffer3.order(ByteOrder.nativeOrder());
        facesBuffer = buffer3.asShortBuffer();


        for (String vertex : verticesList) {
            String coords[] = vertex.split(" ");

            float x = Float.parseFloat(coords[0]);
            float y = Float.parseFloat(coords[1]);
            float z = Float.parseFloat(coords[2]);

            //Log.d("WTH", ":" + x + " " + y + " " + z);

            verticesBuffer.put(x);
            verticesBuffer.put(y);
            verticesBuffer.put(z);
//            verticesBuffer.put(x);
//            verticesBuffer.put(z);
//            verticesBuffer.put(y);

            vcounter++;
        }
        verticesBuffer.position(0);

        int counter = 0;
        for (String color : colorsList) {
            String coords[] = color.split(" ");

            float x = Float.parseFloat(coords[0]);
            float y = Float.parseFloat(coords[1]);
            float z = Float.parseFloat(coords[2]);

            //Log.d("WTH", ccounter + ":" + x + " " + y + " " + z);

            colorsBuffer.put(x);
            colorsBuffer.put(y);
            colorsBuffer.put(z);

            ccounter++;
        }
        colorsBuffer.position(0);

        for (String face : facesList) {
            String vertexIndices[] = face.split(" ");

            short vertex1 = Short.parseShort(vertexIndices[0]);//(short)(Short.parseShort(vertexIndices[0]) + 1);
            short vertex2 = Short.parseShort(vertexIndices[1]);//(short)(Short.parseShort(vertexIndices[1]) + 1);
            short vertex3 = Short.parseShort(vertexIndices[2]);//(short)(Short.parseShort(vertexIndices[2]) + 1);

            facesBuffer.put((short) vertex1);
            facesBuffer.put((short) vertex2);
            facesBuffer.put((short) vertex3);
            //Log.d("WTH", ":" + vertex1 + " " + vertex2 + " " + vertex3);
            fcounter++;
        }
        facesBuffer.position(0);

        setupShader();
    }

    void setupShader()
    {
        // Convert vertex_shader.txt to a string
        Scanner vScanner = new Scanner(mContext.getResources().openRawResource(R.raw.object_vert), "UTF-8");
        String vertexShaderCode = vScanner.useDelimiter("\\A").next();
        vScanner.close();

        Scanner fScanner = new Scanner(mContext.getResources().openRawResource(R.raw.object_frag), "UTF-8");
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

        // Create new program
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        // Link the main program
        GLES20.glLinkProgram(program);

        // Create buffers for vertices and normals and colors
        final int buffers[] = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesBuffer.capacity() * 4, verticesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorsBuffer.capacity() * 4, colorsBuffer, GLES20.GL_STATIC_DRAW);
//
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[2]);
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, facesBuffer.capacity() * 2, facesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        positionIdx = buffers[0];
        colorIdx = buffers[1];
        //facesIdx = buffers[2];
    }

    public void draw(float[] M, float[] V, float[] P)
    {
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        GLES20.glUseProgram(program);

        mPosHandle = GLES20.glGetAttribLocation(program, "a_position");
        mColorHandle = GLES20.glGetAttribLocation(program, "a_color");
        mMVPHandle = GLES20.glGetUniformLocation(program, "u_MVP");
        //mMVHandle = GLES20.glGetUniformLocation(program, "u_MV");

        // Bind vertices
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionIdx);
        GLES20.glEnableVertexAttribArray(mPosHandle);
        GLES20.glVertexAttribPointer(mPosHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
        //GLES20.glDisableVertexAttribArray(positionAttribute);

        // Bind colors
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorIdx);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
//        //GLES20.glDisableVertexAttribArray(colorAttribute);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Pass MVP matrix
        float [] temp = new float[16];
        Matrix.multiplyMM(temp, 0, V, 0, M, 0);
        Matrix.multiplyMM(temp, 0, P, 0, temp, 0);
        GLES20.glUniformMatrix4fv(mMVPHandle, 1, false, temp, 0);

        // Draw
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, facesIdx);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);
        //Log.d("ST", verticesList.size() + " " + colorsList.size() + " " + facesList.size());
        //Log.d("TT", vcounter + " " + ccounter + " " + fcounter);
       // GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, fcounter * 3);
        //GLES20.glDisableVertexAttribArray(position);

    }
}
