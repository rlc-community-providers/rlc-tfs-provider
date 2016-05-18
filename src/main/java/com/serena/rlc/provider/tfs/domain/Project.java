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
 * TFS Project POJO
 * @author klee@serena.com
 */
@XmlRootElement
public class Project extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Project.class);

    public static List<Project> parse(String options) {
        List<Project> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray array = (JSONArray) ((JSONObject) parsedObject).get("value");
            for (Object object : array) {
                Project obj = new Project();
                JSONObject projObj = (JSONObject) object;
                obj.setId((String) projObj.get("id"));
                obj.setName((String) projObj.get("name"));
                obj.setDescription((String) projObj.get("description"));
                obj.setUrl((String) projObj.get("url"));
                obj.setState((String) projObj.get("state"));
                list.add(obj);
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
            JSONObject jsonObject = (JSONObject) parsedObject;
            Project project = parseSingle(jsonObject);
            return project;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Project parseSingle(JSONObject jsonObject) {
        Project obj = new Project();
        if (jsonObject != null) {
            obj.setId((String) jsonObject.get("id"));
            obj.setName((String) jsonObject.get("name"));
            obj.setDescription((String) jsonObject.get("description"));
            obj.setUrl((String) jsonObject.get("url"));
            obj.setState((String) jsonObject.get("state"));
        }
        return obj;
    }

}