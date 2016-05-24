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
 * Visual Studio Release Management - Release Object
 * @author klee@serena.com
 */
public class Release extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Release.class);

    private ReleaseDefinition releaseDefinition;
    private List<Environment> environments;

    public Release(){}
    public Release(String id, String name) {
        super.setId(id);
        super.setTitle(name);
    }
    
    public ReleaseDefinition getReleaseDefinition() {
        return releaseDefinition;
    }

    public void setReleaseDefinition(ReleaseDefinition releaseDefinition) {
        this.releaseDefinition = releaseDefinition;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }


    public static List<Release> parse(String options) {
        List<Release> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "value");
            for (Object object : jsonArray) {
                Release rObj = parseSingle((JSONObject)object);
                list.add(rObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Release parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            Release release = parseSingle((JSONObject) parsedObject);
            return release;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Release parseSingle(JSONObject jsonObject) {
        Release rObj = null;
        if (jsonObject != null) {
            rObj = new Release(
                String.valueOf((Long) getJSONValue(jsonObject, "id")),
                (String) getJSONValue(jsonObject, "name"));
            rObj.setState((String) jsonObject.get("status"));
            // TODO: Set release definition id
            if (jsonObject.containsKey("environments")) {
                rObj.setEnvironments(Environment.parse(jsonObject.get("environments").toString()));
            }
        }
        return rObj;
    }

    @Override
    public String toString() {
        return "Release{" + "id=" + super.getId() + ", name=" + super.getTitle() + '}';
    }

}
