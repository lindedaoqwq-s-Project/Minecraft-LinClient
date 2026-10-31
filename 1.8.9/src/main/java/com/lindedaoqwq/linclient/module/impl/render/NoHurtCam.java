package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;

/** Disables the hurt camera shake. The actual suppression happens inside
 *  {@code EngineHooks.noHurtCam()} (bytecode-injected into EntityRenderer), which
 *  reads this module's switch. Zero extra injection points. */
public class NoHurtCam extends Module {
    public NoHurtCam() {
        super("nohurtcam", Category.RENDER, "No Hurt Cam", "\u5173\u95ed\u53d7\u51fb\u6655\u52a8",
                "Remove the view shake when hit", "\u53d7\u51fb\u65f6\u4e0d\u518d\u6643\u52a8\u89c6\u89d2");
    }
}
