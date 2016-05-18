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
import java.util.Iterator;
import java.util.List;

/**
 * TFS Work Item POJO
 * @author klee@serena.com
 */
public class WorkItem extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(WorkItem.class);

    private String owner;
    private String project;
    private String priority;
    private String severity;
    private String type;
    private String dateCreated;
    private String creator;
    private String lastUpdated;
    private String assignee;
    private String dueDate;
    private String estimatedEffort;
    private String actualEffort;
    private String subject;
    private String targetRel;

    public WorkItem() {

    }

    public WorkItem(String id, String name, String description, String status, String url) {
        this.setId(id);
        this.setName(name);
        this.setDescription(description);
        this.setState(status);
        this.setUrl(url);
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getProject() {
        return project;
    }
    public void setProject(String project) {
        this.project = project;
    }
    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(String lastModifier) {
        this.lastUpdated = lastModifier;
    }
    public String getAssignee() {
        return assignee;
    }
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
    public String getDueDate() {
        return dueDate;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    public String getEstimatedEffort() {
        return estimatedEffort;
    }
    public void setEstimatedEffort(String estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
    }
    public String getActualEffort() {
        return actualEffort;
    }
    public void setActualEffort(String actualEffort) {
        this.actualEffort = actualEffort;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getTargetRelease() {
        return targetRel;
    }
    public void setTargetRelease(String targetRel) {
        this.targetRel = targetRel;
    }

    public static WorkItem parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONObject jsonObject = (JSONObject) parsedObject;
            WorkItem workItem = parseSingle(jsonObject);
            return workItem;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static List<WorkItem> parseQuery(String options) {
        List<WorkItem> wiList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONObject queryObj = (JSONObject) parsedObject;
            String queryType = (String)queryObj.get("queryType");
            if (!queryType.equals("flat")) return wiList;
            JSONArray array = (JSONArray) ((JSONObject) parsedObject).get("workItems");
            for (Object object : array) {
                WorkItem obj = new WorkItem();
                JSONObject wiObj = (JSONObject) object;
                obj.setId((String) wiObj.get("id").toString());
                obj.setUrl((String) wiObj.get("url"));
                wiList.add(obj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return wiList;
    }

    public static List<WorkItem> parseDetails(String options) {
        List<WorkItem> wiList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray array = (JSONArray) ((JSONObject) parsedObject).get("value");
            for (Object object : array) {
                WorkItem wiObj = parseSingle((JSONObject)object);
                wiList.add(wiObj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return wiList;
    }

    public static WorkItem parseSingle(JSONObject jsonObject) {
        WorkItem wiObj = new WorkItem();
        wiObj.setId((String) jsonObject.get("id").toString());
        wiObj.setUrl((String) jsonObject.get("url"));
        JSONObject fieldsObj = (JSONObject) jsonObject.get("fields");
        for (Iterator iterator = fieldsObj.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            switch (key) {
                case "System.Title":
                    wiObj.setName((String)fieldsObj.get(key));
                    break;
                case "System.State":
                    wiObj.setState((String)fieldsObj.get(key));
                    break;
                case "System.Description":
                    wiObj.setDescription((String)fieldsObj.get(key));
                    break;
                case "System.TeamProject":
                    wiObj.setProject((String)fieldsObj.get(key));
                    break;
                case "System.CreatedBy":
                    wiObj.setCreator((String)fieldsObj.get(key));
                    break;
                case "System.WorkItemType":
                    wiObj.setType((String)fieldsObj.get(key));
                    break;
                case "Microsoft.VSTS.Common.Severity":
                    wiObj.setSeverity((String)fieldsObj.get(key));
                default:
                    break;
            }
        }
        return wiObj;
    }

}