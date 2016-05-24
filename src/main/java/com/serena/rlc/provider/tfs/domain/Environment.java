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
 * Visual Studio Release Management - Environment Object
 * @author klee@serena.com
 */
public class Environment  extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Environment.class);

    public Environment(){

    }

    public Environment(Long id, String name, String status) {
        super.setId(id);
        super.setTitle(name);
        super.setState(status);
    }

    public static List<Environment> parse(String options) {
        List<Environment> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray jsonArray = (JSONArray) parsedObject;
            for (Object object : jsonArray) {
                Environment envObj = parseSingle((JSONObject)object);
                list.add(envObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Environment parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            Environment environment = parseSingle((JSONObject) parsedObject);
            return environment;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Environment parseSingle(JSONObject jsonObject) {
        Environment envObj = null;
        if (jsonObject != null) {
            envObj = new Environment(
                (Long) getJSONValue(jsonObject, "id"),
                (String) getJSONValue(jsonObject, "name"),
                (String) getJSONValue(jsonObject, "status")
            );
        }
        return envObj;
    }

    @Override
    public String toString() {
        return "Environment{" + "id=" + super.getId() + ", name=" + super.getTitle() + '}';
    }

}
