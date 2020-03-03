package com.yqbj.ghxm.requestutils;

public abstract class requestCallback<T> {
    public abstract void onSuccess(int code,T object);
    public abstract void onFailed(String errMessage);
}

