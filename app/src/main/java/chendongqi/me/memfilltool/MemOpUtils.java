package chendongqi.me.memfilltool;

/**
 * Created by chendongqi on 17-3-21.
 */

public class MemOpUtils {

    static {
        System.loadLibrary("jnimemop");
    }
    public static native int memfill();
    public static native int memfree();
    public static native int setflag();

}
