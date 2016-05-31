package com.example.android.wifidirect;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import okhttp3.Response;

/**
 * Created by micha on 3/10/2016.
 */
public class BleResponse {
  private static final int BODY = 0;
  private static final int CODE = 1;

  private String body;
  private int code;

  public static BleResponse parseResponse(String json) {
    try {
      JSONArray arr = new JSONArray(json);
      return new BleResponse.Builder(arr.getString(BODY), arr.getInt(CODE)).build();
    } catch (Exception e) {
      return null;
    }
  }

  private BleResponse(Builder builder) {
    this.body = builder.body;
    this.code = builder.code;
  }

  public String getBody() {
    return body;
  }

  public int getCode() {
    return code;
  }

  public boolean isSuccessful() {
    return code >= 200 && code < 300;
  }

  public String toJsonString() {
    try {
      JSONArray arr = new JSONArray();
      arr.put(BODY, body);
      arr.put(CODE, code);
      return arr.toString();
    } catch (JSONException e) {
      return null;
    }
  }

  public static class Builder {
    private String body;
    private int code;

    public Builder() {
    }

    public Builder(String body, int code) {
      this.body = body;
      this.code = code;
    }

    public Builder response(Response response) {
      try {
        this.body = response.body().string();
        this.code = response.code();
        return this;
      } catch (Exception e) {
        Log.e("BleResponse", "Response body string is null");
        return null;
      }
    }

    public BleResponse build() {
      return new BleResponse(this);
    }
  }

}
