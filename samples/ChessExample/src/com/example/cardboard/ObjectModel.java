package com.example.cardboard;

import android.content.Context;
import android.util.Log;
import edu.union.graphics.FloatMesh;
import edu.union.graphics.Mesh;
import edu.union.graphics.Model;
import edu.union.graphics.ObjLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ObjectModel {
    private int mResId;
    private Context mContext;

    private FloatBuffer mObjectVertices;
    private FloatBuffer mObjectColors;
    private FloatBuffer mObjectNormals;
    Mesh mMesh;

    private float[] mColor;
    private final ObjLoader mObjLoader = new ObjLoader();

    private static final String TAG = "ChessExample";

    ObjectModel(Context context, int resId) {
        mContext = context;
        mResId = resId;

        mColor = new float[4];
        mColor[0] = 0.5f;
        mColor[1] = 0.5f;
        mColor[2] = 0.5f;
        mColor[3] = 1.0f;
    }

    private Model loadModelFromResource(int resId) {
        try {
            mObjLoader.setFactory(FloatMesh.factory());
            InputStream inputStream = mContext.getResources().openRawResource(resId);

            return mObjLoader.load(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    void setColor(float[] color) {
        mColor[0] = color[0];
        mColor[1] = color[1];
        mColor[2] = color[2];
        mColor[3] = color[3];
    }

    void load() {
        Model objModel = loadModelFromResource(mResId);
        mMesh = objModel.getFrame(0).getMesh();

        int numFaces = mMesh.getFaceCount();
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(numFaces * 3 * 3 * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        mObjectVertices = bbVertices.asFloatBuffer();

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(numFaces * 3 * 3 * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        mObjectNormals = bbNormals.asFloatBuffer();

        ByteBuffer bbColors = ByteBuffer.allocateDirect(numFaces * 3 * 4 * 4);
        bbColors.order(ByteOrder.nativeOrder());
        mObjectColors = bbColors.asFloatBuffer();

        for (int i = 0; i < numFaces; ++i) {
            int[] vertexIndices = mMesh.getFace(i);
            int[] normalIndices = mMesh.getFaceNormals(i);

            for (int j = 0; j < 3; ++j) {
                mObjectVertices.put(mMesh.getVertexf(vertexIndices[j]));
                mObjectNormals.put(mMesh.getNormalf(normalIndices[j]));
                mObjectColors.put(mColor);
            }
        }
        mObjectVertices.position(0);
        mObjectNormals.position(0);
        mObjectColors.position(0);
    }

    FloatBuffer getVertices() {
        return mObjectVertices;
    }

    FloatBuffer getNormals() {
        return mObjectNormals;
    }

    FloatBuffer getColors() {
        return mObjectColors;
    }

    int getNumFaces() {
        return mMesh.getFaceCount();
    }
}
