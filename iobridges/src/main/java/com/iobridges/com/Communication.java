package com.iobridges.com;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * author DXL
 *
 * @version 1.0
 */
public interface Communication extends Serializable {
    /**
     * Get OutputStream implement!
     */
    OutputStream getOutput();

    /**
     * Get InputStream implement!
     */
    InputStream getInput();
}