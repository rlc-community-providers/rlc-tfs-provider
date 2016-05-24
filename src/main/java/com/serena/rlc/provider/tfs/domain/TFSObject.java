/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.tfs.domain;

import org.json.simple.JSONObject;

import java.io.Serializable;

/**
 * Base TFS Object
 * @author klee@serena.com
 */
public class TFSObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long rev;
    private String title;
    private String description;
    private Long created;
    private String state;
    private String url;

    public TFSObject() {

    }

    public TFSObject(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Long getRev() {
        return rev;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getState() {
        return state;
    }

    public Long getCreated() {
        return created;
    }

    public String getUrl() {
        return url;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRev(Long rev) {
        this.rev = rev;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static Object getJSONValue(JSONObject obj, String key) {
        Object retObj = null;
        if (obj.containsKey(key)) {
            return obj.get(key);
        }
        return retObj;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}