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
 * TFS Build Definition Object
 * @author klee@serena.com
 */
public class BuildDefinition extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(BuildDefinition.class);

    private String quality;
    private String type;

    public BuildDefinition(){}
    public BuildDefinition(String id, String name) {
        super.setId(id);
        super.setTitle(name);
    }
    
    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static List<BuildDefinition> parse(String options) {
        List<BuildDefinition> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "value");
            for (Object object : jsonArray) {
                BuildDefinition bdObj = parseSingle((JSONObject)object);
                list.add(bdObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static BuildDefinition parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            BuildDefinition buildDefinition = parseSingle((JSONObject) parsedObject);
            return buildDefinition;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static BuildDefinition parseSingle(JSONObject jsonObject) {
        BuildDefinition bdObj = null;
        if (jsonObject != null) {
            bdObj = new BuildDefinition(
                String.valueOf((Long) getJSONValue(jsonObject, "id")),
                (String) getJSONValue(jsonObject, "name"));
            bdObj.setQuality((String) jsonObject.get("quality"));
            bdObj.setUrl((String) jsonObject.get("url"));
            bdObj.setType((String) jsonObject.get("type"));
            bdObj.setRev((Long) jsonObject.get("rev"));
        }
        return bdObj;
    }

    @Override
    public String toString() {
        return "BuildDefinition{" + "id=" + super.getId() + ", name=" + super.getTitle() + '}';
    }

}
