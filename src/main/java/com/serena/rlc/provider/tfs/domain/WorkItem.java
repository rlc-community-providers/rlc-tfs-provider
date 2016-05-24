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
 * TFS Work Item Object
 * @author klee@serena.com
 */
public class WorkItem extends TFSObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(WorkItem.class);

    private String assignedTo;
    private String project;
    private String severity;
    private String type;
    private String dateCreated;
    private String createdBy;
    private String dateChanged;
    private String changedBy;
    private String effort;
    private String reason;
    private String areaPath;
    private String iterationPath;

    public WorkItem() {

    }

    public WorkItem(Long id, String title, String description, String status, String url) {
        this.setId(id);
        this.setTitle(title);
        this.setDescription(description);
        this.setState(status);
        this.setUrl(url);
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(String dateChanged) {
        this.dateChanged = dateChanged;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getEffort() {
        return effort;
    }

    public void setEffort(String effort) {
        this.effort = effort;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAreaPath() {
        return areaPath;
    }

    public void setAreaPath(String areaPath) {
        this.areaPath = areaPath;
    }

    public String getIterationPath() {
        return iterationPath;
    }

    public void setIterationPath(String iterationPath) {
        this.iterationPath = iterationPath;
    }

    public static WorkItem parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            WorkItem workItem = parseSingle((JSONObject) parsedObject);
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
            String queryType = (String) getJSONValue((JSONObject) parsedObject, "queryType");
            if (!queryType.equals("flat")) return wiList;
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "workItems");
            for (Object object : jsonArray) {
                WorkItem wiObj = new WorkItem();
                wiObj.setId((Long) getJSONValue((JSONObject) object, "id"));
                wiObj.setUrl((String) getJSONValue((JSONObject) object, "url"));
                wiList.add(wiObj);
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
            JSONArray jsonArray = (JSONArray) getJSONValue((JSONObject) parsedObject, "value");
            for (Object object : jsonArray) {
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
        wiObj.setId((Long) getJSONValue(jsonObject, "id"));
        wiObj.setRev((Long) getJSONValue(jsonObject, "rev"));
        wiObj.setUrl((String) getJSONValue(jsonObject, "url"));
        if (jsonObject.containsKey("fields")) {
            JSONObject fieldsObj = (JSONObject) getJSONValue(jsonObject, "fields");
            for (Iterator iterator = fieldsObj.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                switch (key) {
                    case "System.Title":
                        wiObj.setTitle((String) fieldsObj.get(key));
                        break;
                    case "System.State":
                        wiObj.setState((String) fieldsObj.get(key));
                        break;
                    case "System.Description":
                        wiObj.setDescription((String) fieldsObj.get(key));
                        break;
                    case "System.TeamProject":
                        wiObj.setProject((String) fieldsObj.get(key));
                        break;
                    case "System.CreatedDate":
                        wiObj.setDateCreated((String) fieldsObj.get(key));
                        break;
                    case "System.CreatedBy":
                        wiObj.setCreatedBy((String) fieldsObj.get(key));
                        break;
                    case "System.ChangedDate":
                        wiObj.setDateChanged((String) fieldsObj.get(key));
                        break;
                    case "System.ChangedBy":
                        wiObj.setChangedBy((String) fieldsObj.get(key));
                        break;
                    case "System.WorkItemType":
                        wiObj.setType((String) fieldsObj.get(key));
                        break;
                    case "Microsoft.VSTS.Common.Severity":
                        wiObj.setSeverity((String) fieldsObj.get(key));
                        break;
                    case "System.AreaPath":
                        wiObj.setAreaPath((String) fieldsObj.get(key));
                        break;
                    case "System.IterationPath":
                        wiObj.setIterationPath((String) fieldsObj.get(key));
                        break;
                    case "System.Reason":
                        wiObj.setReason((String) fieldsObj.get(key));
                        break;
                    default:
                        break;
                }
            }
        }

        return wiObj;
    }

}