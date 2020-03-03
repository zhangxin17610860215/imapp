package com.yqbj.ghxm.login;

import com.yqbj.ghxm.pay.AliPayResult;

/**
 * 授权状态
 * */
public interface AuthorizationState {
    public abstract void onSuccess(String code,AliPayResult object);
    public abstract void onFailed(String errMessage);
}
