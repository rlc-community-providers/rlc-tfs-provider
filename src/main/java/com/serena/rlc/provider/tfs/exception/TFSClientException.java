/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 */

package com.serena.rlc.provider.tfs.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin Lee
 */

public class TFSClientException extends Exception {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TFSClientException.class);

    public TFSClientException() {
    }

    public TFSClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public TFSClientException(String message) {
        super(message);
    }

    public TFSClientException(Throwable cause) {
        super(cause);
    }
}
