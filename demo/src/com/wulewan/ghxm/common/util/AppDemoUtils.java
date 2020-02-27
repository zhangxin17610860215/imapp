package com.wulewan.ghxm.common.util;

import android.content.Context;
import android.content.Intent;

public class AppDemoUtils {
    public static void simpleToAct(Context context, Class toClass) {
        Intent intent = new Intent(context, toClass);
        context.startActivity(intent);
    }
}
