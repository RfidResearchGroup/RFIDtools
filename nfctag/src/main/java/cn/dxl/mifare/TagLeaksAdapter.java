package cn.dxl.mifare;

import java.io.Serializable;

/**
 * @author DXL the TagLeaksAdapter can scan a leak for tag
 * impl at subclass
 */
public interface TagLeaksAdapter extends Serializable {
    /*
     * Darkside test, form a nack(4bit) recovery a key.
     * don't need any key.
     * if tag no nack response, the darkside can't wrok.
     * */
    boolean isDarksideSupported();

    /*
     * Nested test, from a available auth data pack recovery a key.
     * need a key
     * if tag no prng available, the nested can't running.
     * */
    boolean isNestedSupported();

    /*
     * Harden nested, must have a key available and powerful processors.
     * */
    boolean isHardnestedSupported();
}