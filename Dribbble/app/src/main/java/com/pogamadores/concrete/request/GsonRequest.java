package com.pogamadores.concrete.request;

import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {

    private final Gson mGson = new Gson();
    private final Response.Listener<T> mListener;
    private final Class<T> mClass;
    private final Map<String, String> mHeaders;
    private final String mBody;

    public GsonRequest(int method, Map<String, String> headers, String url, String body, Response.Listener<T> listener, Response.ErrorListener errorListener, Class<T> clazz) {
        super(method, url, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(3000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mListener = listener;
        mClass = clazz;
        mHeaders = (headers != null?headers:GsonRequest.getDefaultHeaders());
        mBody = body;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(mBody == null)
            return super.getBody();
        else
            return mBody.getBytes();
    }

    @Override
    public String getBodyContentType() {
        if(mBody != null)
            return "application/json";
        else
            return super.getBodyContentType();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {

        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    mGson.fromJson(json, mClass),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception exception) {
            return Response.error(new ParseError(exception));
        }

    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return super.parseNetworkError(volleyError);
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    public int compareTo(Request<T> other) {
        return super.compareTo(other);
    }

    public static String buildUrl(String serverUrl, String path, Map<String,String> urlParameters) {
        Uri.Builder builder = Uri.parse(serverUrl).buildUpon();
        builder.appendPath(path);
        for(String key : urlParameters.keySet())
            builder.appendQueryParameter(key,urlParameters.get(key));
        return builder.toString();
    }

    public static Map<String, String> getDefaultHeaders() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Accept","application/json");
        defaultHeaders.put("Content-Type","application/json");
        return defaultHeaders;
    }
}
