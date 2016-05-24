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
 * TFS Project Object
 * @author klee@serena.com
 */
@XmlRootElement
public class Project extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Project.class);

    public Project() {

    }

    public Project(String id, String name) {
        super.setId(id);
        super.setTitle(name);
    }

    public static List<Project> parse(String options) {
        List<Project> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "value");
            for (Object object : jsonArray) {
                Project projObj = parseSingle((JSONObject)object);
                list.add(projObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Project parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            Project project = parseSingle((JSONObject)parsedObject);
            return project;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Project parseSingle(JSONObject jsonObject) {
        Project projObj = null;
        if (jsonObject != null) {
            projObj = new Project(
                (String) getJSONValue(jsonObject, "id"),
                (String) getJSONValue(jsonObject, "name")
            );
            projObj.setDescription((String) getJSONValue(jsonObject, "description"));
            projObj.setUrl((String) getJSONValue(jsonObject, "url"));
            projObj.setState((String) getJSONValue(jsonObject, "state"));
        }
        return projObj;
    }

    @Override
    public String toString() {
        return "Project{" + "id=" + super.getId() + ", name=" + super.getTitle() + '}';
    }

}