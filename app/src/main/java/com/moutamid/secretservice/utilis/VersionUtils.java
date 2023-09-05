package com.moutamid.secretservice.utilis;

import android.os.Build;

public class VersionUtils {

    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= 18;
    }


}
