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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * TFS Query POJO
 * @author klee@serena.com
 */
@XmlRootElement
public class Query extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Query.class);

    private String path;
    private boolean isFolder;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public boolean getIsFolder() {
        return this.isFolder;
    }

    public static List<Query> parse(String options) {
        List<Query> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray array = (JSONArray) ((JSONObject) parsedObject).get("children");
            for (Object object : array) {
                Query obj = new Query();
                JSONObject projObj = (JSONObject) object;
                obj.setId((String) projObj.get("id"));
                obj.setName((String) projObj.get("name"));
                obj.setPath((String) projObj.get("path"));
                obj.setUrl((String) projObj.get("url"));
                if (projObj.containsKey("isFolder")) {
                    obj.setIsFolder((boolean)projObj.get("isFolder"));
                }
                list.add(obj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Query parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONObject jsonObject = (JSONObject) parsedObject;
            Query query = parseSingle(jsonObject);
            return query;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Query parseSingle(JSONObject jsonObject) {
        Query obj = new Query();
        if (jsonObject != null) {
            obj.setId((String) jsonObject.get("id"));
            obj.setName((String) jsonObject.get("name"));
            obj.setPath((String) jsonObject.get("path"));
            obj.setUrl((String) jsonObject.get("url"));
        }
        return obj;
    }

}