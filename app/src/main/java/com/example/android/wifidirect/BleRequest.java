package com.example.android.wifidirect;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by micha on 3/9/2016.
 */
public class BleRequest {

  public static final String GET = "GET";
  public static final String POST = "POST";
  private static final int URL = 0;
  private static final int METHOD = 1;
  private static final int BODY = 2;
  private static final String TAG = BleRequest.TAG;

  private final String url;
  private final String method;
  private final String body;

  private BleRequest(Builder builder) {
    this.url = builder.url;
    this.method = builder.method;
    this.body = builder.body;
  }

  public static BleRequest parseJson(String json) {
    try {
      JSONArray arr = new JSONArray(json);
      BleRequest bleRequest = new BleRequest.Builder()
          .url(arr.getString(URL))
          .body(arr.getString(BODY))
          .method(arr.getString(METHOD))
          .build();
      return bleRequest;
    } catch (Exception e) {
      Log.e(TAG, e.toString());
      return null;
    }
  }

  public static String parseMethod(String input) {
    if (input.equals("GET")) {
      return "GET";
    } else if (input.equals(POST)) {
      return "POST";
    } else {
      throw new IllegalArgumentException("method != GET || method != POST");
    }
  }

  public String getUrl() {
    return url;
  }

  public String getMethod() {
    return method;
  }

  public String getBody() {
    return body;
  }

  public String toJsonString() {
    JSONArray arr = new JSONArray();
    try {
      arr.put(URL, url);
      arr.put(METHOD, method);
      arr.put(BODY, body);
      return arr.toString();
    } catch (JSONException e) {
      return null;
    }
  }


  public static class Builder {
    private String url;
    private String method;
    private String body;

    public Builder() {
      this.method = "GET";
    }

    public Builder url(String url) {
      if (url == null || url.length() == 0) {
        throw new IllegalArgumentException("url == null || url.length == 0");
      }
      this.url = url;
      return this;
    }

    public Builder method(String method) {
      if (method == null || method.length() == 0) {
        throw new IllegalArgumentException("method == null || method.length() == 0");
      }
      if (method.equals("GET") || method.equals("POST")) {
        this.method = method;
      } else {
        throw new IllegalArgumentException("method != GET || method != POST");
      }
      return this;
    }

    public Builder body(String body) {
      if (body == null || body.length() == 0) {
        throw new IllegalArgumentException("body == null || body.length() == 0");
      }
      this.body = body;
      return this;
    }

    public BleRequest build() {
      if (url == null) throw new IllegalStateException("url == null");
      return new BleRequest(this);
    }
  }
}
