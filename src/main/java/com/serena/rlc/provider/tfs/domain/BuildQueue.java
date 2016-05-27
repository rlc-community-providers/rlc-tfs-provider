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

import java.util.ArrayList;
import java.util.List;


/**
 * TFS Build BuildQueue Object
 * @author klee@serena.com
 */
public class BuildQueue extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(BuildQueue.class);

    private String type;

    public BuildQueue(){}
    public BuildQueue(String id, String name) {
        super.setId(id);
        super.setTitle(name);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static List<BuildQueue> parse(String options) {
        List<BuildQueue> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "value");
            for (Object object : jsonArray) {
                BuildQueue bqObj = parseSingle((JSONObject)object);
                list.add(bqObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static BuildQueue parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            BuildQueue buildQueue = parseSingle((JSONObject) parsedObject);
            return buildQueue;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static BuildQueue parseSingle(JSONObject jsonObject) {
        BuildQueue bqObj = null;
        if (jsonObject != null) {
            bqObj = new BuildQueue(
                String.valueOf((Long) getJSONValue(jsonObject, "id")),
                (String) getJSONValue(jsonObject, "name"));
            bqObj.setUrl((String) jsonObject.get("url"));
            bqObj.setType((String) jsonObject.get("type"));
            // TODO: Get Build Controller
        }
        return bqObj;
    }

    @Override
    public String toString() {
        return "BuildQueue{" + "id=" + super.getId() + ", name=" + super.getTitle() + '}';
    }

}
