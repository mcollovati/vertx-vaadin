package com.github.mcollovati.vertxvaadin.quarkus;

import java.lang.reflect.Method;

import com.github.mcollovati.vertx.support.BootstrapHelper;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BootstrapHelperRecorder {

    public void loadScanResult(String text) {
        System.out.println(text + "================ BootstrapHelperRecorder::init static scan result");
        try {
            Method method = BootstrapHelper.class.getDeclaredMethod("tryLoadStaticScan");
            method.setAccessible(true);
            method.invoke(null);
        } catch (Exception ex) {
            System.out.println("================ BootstrapHelperRecorder::init static scan result:: ERR " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}

