package com.wulewan.ghxm.utils;

import java.io.File;

public interface ImageDownLoadCallBack {
    void onDownLoadSuccess(File file);

    void onDownLoadFailed();
}
