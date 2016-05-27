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
 * TFS Build Object
 * @author klee@serena.com
 */
public class Build extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Build.class);

    private String buildNumber;
    private String buildResult;
    private BuildDefinition buildDefinition;

    public Build(){}
    public Build(String id, String buildNumber) {
        super.setId(id);
        setBuildNumber(buildNumber);
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildResult() {
        return buildResult;
    }

    public void setBuildResult(String buildResult) {
        this.buildResult = buildResult;
    }
    
    public BuildDefinition getBuildDefinition() {
        return buildDefinition;
    }

    public void setBuildDefinition(BuildDefinition buildDefinition) {
        this.buildDefinition = buildDefinition;
    }

    public static List<Build> parse(String options) {
        List<Build> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "value");
            for (Object object : jsonArray) {
                Build bObj = parseSingle((JSONObject)object);
                list.add(bObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Build parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            Build build = parseSingle((JSONObject) parsedObject);
            return build;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Build parseSingle(JSONObject jsonObject) {
        Build bObj = null;
        if (jsonObject != null) {
            bObj = new Build(
                String.valueOf((Long) getJSONValue(jsonObject, "id")),
                (String) getJSONValue(jsonObject, "buildNumber"));
            bObj.setBuildResult((String) jsonObject.get("result"));
            bObj.setState((String) jsonObject.get("status"));
            bObj.setUrl((String) jsonObject.get("url"));
            // TODO: Set build definition id
        }
        return bObj;
    }

    @Override
    public String toString() {
        return "Build{" + "id=" + super.getId() + ", number=" + getBuildNumber() + '}';
    }

}
