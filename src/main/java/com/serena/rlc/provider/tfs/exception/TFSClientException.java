/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.tfs.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TFS Release Manager Client Exceptions
 * @author klee@serena.com
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
