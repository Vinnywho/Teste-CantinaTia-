package com.example.teste; // Use o pacote correto do seu app

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

public class BinaryUploadRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;
    private final byte[] mBody;
    private final String mMimeType;

    public BinaryUploadRequest(int method, String url, byte[] body, String mimeType,
                               Response.Listener<byte[]> listener,
                               Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mBody = body;
        this.mListener = listener;
        this.mMimeType = mimeType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mBody;
    }

    @Override
    public String getBodyContentType() {
        return mMimeType;
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }
}