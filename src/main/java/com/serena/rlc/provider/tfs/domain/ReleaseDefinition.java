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
 * Visual Studio Release Management - Release Definition Object
 * @author klee@serena.com
 */
@XmlRootElement
public class ReleaseDefinition extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(ReleaseDefinition.class);

    private Project project;
    private List<Environment> environments;

    public ReleaseDefinition() {

    }

    public ReleaseDefinition(String id, String name) {
        super.setId(id);
        super.setTitle(name);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }
    
    public static List<ReleaseDefinition> parse(String options) {
        List<ReleaseDefinition> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "value");
            for (Object object : jsonArray) {
                ReleaseDefinition rdObj = parseSingle((JSONObject)object);
                list.add(rdObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static ReleaseDefinition parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            ReleaseDefinition releaseDefinition = parseSingle((JSONObject) parsedObject);
            return releaseDefinition;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static ReleaseDefinition parseSingle(JSONObject jsonObject) {
        ReleaseDefinition rdObj = null;
        if (jsonObject != null) {
            rdObj = new ReleaseDefinition(
                    String.valueOf((Long) getJSONValue(jsonObject, "id")),
                    (String) getJSONValue(jsonObject, "name"));
            rdObj.setRev((Long) getJSONValue(jsonObject, "rev"));
            rdObj.setUrl((String) getJSONValue(jsonObject, "url"));
            // TODO: Set project id
            if (jsonObject.containsKey("environments")) {
                rdObj.setEnvironments(Environment.parse(jsonObject.get("environments").toString()));
            }
        }
        return rdObj;
    }

    @Override
    public String toString() {
        return "ReleaseDefinition{" + "id=" + super.getId() + ", name=" + super.getTitle() + '}';
    }
    
    
}
