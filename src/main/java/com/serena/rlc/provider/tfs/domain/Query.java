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
 * TFS Query Object
 * @author klee@serena.com
 */
@XmlRootElement
public class Query extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Query.class);

    private String path;
    private boolean isFolder;

    public Query() {

    }

    public Query(String id, String name) {
        super.setId(id);
        super.setTitle(name);
    }

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
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject)parsedObject, "children");
            for (Object object : jsonArray) {
                Query queryObj = parseSingle((JSONObject)object);
                list.add(queryObj);
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
            Query query = parseSingle((JSONObject) parsedObject);
            return query;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Query parseSingle(JSONObject jsonObject) {
        Query queryObj = new Query();
        if (jsonObject != null) {
            queryObj = new Query(
                (String) getJSONValue(jsonObject, "id"),
                (String) getJSONValue(jsonObject, "name")
            );
            queryObj.setPath((String) getJSONValue(jsonObject, "path"));
            queryObj.setUrl((String) getJSONValue(jsonObject, "url"));
            if (jsonObject.containsKey("isFolder")) {
                queryObj.setIsFolder((boolean) getJSONValue(jsonObject, "isFolder"));
            }
        }
        return queryObj;
    }

    @Override
    public String toString() {
        return "Query{" + "id=" + super.getId() + ", name=" + super.getTitle() + '}';
    }

}