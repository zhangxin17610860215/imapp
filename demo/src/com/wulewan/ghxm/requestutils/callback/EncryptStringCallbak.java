package com.wulewan.ghxm.requestutils.callback;

import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.convert.StringConvert;

import okhttp3.Response;

public class EncryptStringCallbak extends StringCallback {


    private StringConvert convert;

    public EncryptStringCallbak() {
        convert = new StringConvert();
    }

    @Override
    public String convertResponse(Response response) throws Throwable {
        String s = convert.convertResponse(response);
        response.close();
        return s;
    }

    @Override
    public void onSuccess(com.lzy.okgo.model.Response<String> response) {

    }
}
