/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.tfs.client;

import com.serena.rlc.provider.domain.SessionData;
import com.serena.rlc.provider.tfs.domain.Project;
import com.serena.rlc.provider.tfs.domain.Query;
import com.serena.rlc.provider.tfs.domain.WorkItem;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

/**
 * TFS Client
 * @author klee@serena.com
 */
@Component
public class TFSClient {
    private static final Logger logger = LoggerFactory.getLogger(TFSClient.class);

    public static String DEFAULT_HTTP_CONTENT_TYPE = "application/json";

    private String tfsUrl;
    private String tfsUsername;
    private String tfsPassword;
    private String tfsCollection;
    private String tfsProject;
    private String apiVersion;
    private SessionData session;

    public TFSClient() {
    }

    public TFSClient(SessionData session, String apiVersion, String collection, String url, String username, String password) {
        this.session = session;
        this.tfsUrl = url;
        this.apiVersion = apiVersion;
        this.tfsCollection = collection;
        this.tfsProject = "";
        this.tfsUsername = username;
        this.tfsPassword = password;
        this.createConnection(this.session, this.apiVersion, this.tfsCollection, this.tfsUrl, this.tfsUsername, this.tfsPassword);
    }

    public SessionData getSession() {
        return session;
    }

    public void setSession(SessionData session) {
        this.session = session;
    }

    public String getTFSUrl() {
        return tfsUrl;
    }

    public void setTFSUrl(String url) {
        this.tfsUrl = url;
    }

    public String getTFSCollection() {
        return tfsCollection;
    }

    public void setTFSProject(String project) {
        this.tfsProject = project;
    }

    public String getTFSProject() {
        return tfsProject;
    }

    public void setTFSCollection(String collection) {
        this.tfsCollection = collection;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getTFSUsername() {
        return tfsUsername;
    }

    public void setTFSUsername(String username) {
        this.tfsUsername = username;
    }

    public String getTFSPassword() {
        return tfsPassword;
    }

    public void setTFSPassword(String password) {
        this.tfsPassword = password;
    }

    /**
     * Create a new connection to TFS.
     *
     * @param url  the url to Ansible, e.g. https://servername
     * @param collection  the TFS collection
     * @param apiVersion  the version of the REST API to use
     * @param username  the username of the TFS user
     * @param password  the password/private token of the TFS user
     * @throws TFSClientException
     */
    public void createConnection(SessionData session, String url, String apiVersion, String collection, String username, String password) {
        this.session = session;
        this.tfsUrl = url;
        this.tfsCollection = collection;
        this.apiVersion = apiVersion;
        this.tfsUsername = username;
        this.tfsPassword = password;
    }

    /**
     * Get a list of workItems
     *
     * @param queryId  the id of the query to run
     * @param resultLimit  the number of workItems to return
     * @return  a list of workItems
     * @throws TFSClientException
     */
    public List<WorkItem> getWorkItems(String queryId, String titleFilter, Integer resultLimit) throws TFSClientException {
        logger.debug("Using TFS URL: " + this.tfsUrl);
        logger.debug("Using TFS Credential: " + this.tfsUsername);
        logger.debug("Using TFS Query: " + queryId);
        logger.debug("Using Title Filter: " + titleFilter);
        logger.debug("Limiting results to: " + resultLimit.toString());

        logger.debug("Retrieving TFS Item Ids from Query \"{}\"", queryId);
        String queryResponse = processGet(getTFSUrl(), getTFSCollection() + "/_apis/wit/wiql/" + queryId, "");
        logger.debug(queryResponse);

        List<WorkItem> workItems = null;
        List<WorkItem> workItemsTmp = WorkItem.parseQuery(queryResponse);
        String idList = "";
        int count = 0;
        if (!workItemsTmp.isEmpty()) {
            idList="ids=";
            for (WorkItem wi : workItemsTmp) {
                if (count++ > resultLimit) break;
                idList += wi.getId() + ",";
            }
            if (idList.endsWith(",")) {
                idList = idList.substring(0, idList.length() - 1);
            }
            logger.debug("Retrieving TFS Item Details for Work Item \"{}\"", idList);
            String wiResponse = processGet(getTFSUrl(), getTFSCollection() + "/_apis/wit/workitems", idList);
            workItems = WorkItem.parseDetails(wiResponse);
        }

        return workItems;

    }

    /**
     * Get a specfic workItem
     *
     * @param projectId  the id of the project, e.g. Demo
     * @param workItemId  the id of the workItem, e.g. 1
     * @return the workItem if found
     * @throws TFSClientException
     */
    public WorkItem getWorkItem(String workItemId) throws TFSClientException {
        logger.debug("Using TFS URL: " + this.tfsUrl);
        logger.debug("Using TFS Credential: " + this.tfsUsername);
        logger.debug("Using TFS Work Item Id: " + workItemId);

        logger.debug("Retrieving TFS Work Item");
        String wiResponse = processGet(getTFSUrl(), getTFSCollection() + "/_apis/wit/workitems/" + workItemId, "");
        logger.debug(wiResponse);

        WorkItem workItem = WorkItem.parseSingle(wiResponse);
        return workItem;
    }

    /**
     * Get a list of projects in the domain
     * @return a list of projects
     * @throws TFSClientException
     */
    public List<Project> getProjects() throws TFSClientException {
        logger.debug("Using TFS URL: " + this.tfsUrl);
        logger.debug("Using TFS Collection: " + this.tfsCollection);
        logger.debug("Using TFS Credential: " + this.tfsUsername);

        logger.debug("Retrieving TFS Projects");
        String projResponse = processGet(getTFSUrl(), getTFSCollection() + "/_apis/projects", "statefilter=All");
        logger.debug(projResponse);

        List<Project> projects = Project.parse(projResponse);
        return projects;
    }

    /**
     * Get a list of work item queries in the specified project
     * @return a list of queries
     * @throws TFSClientException
     */
    public List<Query> getQueries(String projectId, String folderPath) throws TFSClientException {
        logger.debug("Using TFS URL: " + this.tfsUrl);
        logger.debug("Using TFS Collection: " + this.tfsCollection);
        logger.debug("Using TFS Credential: " + this.tfsUsername);
        logger.debug("Using TFS Project Id: " + projectId);
        logger.debug("Using TFS Folder Path: " + folderPath);
        this.setTFSProject(projectId);

        logger.debug("Retrieving TFS Queries for Project \"{}\" in folder path \"{}\"", projectId, folderPath);
        String queryResponse = processGet(getTFSUrl(), getTFSCollection() + "/" + projectId + "/_apis/wit/queries/" + folderPath, "$depth=2");
        logger.debug(queryResponse);

        List<Query> queries = Query.parse(queryResponse);
        return queries;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Execute a get request to TFS
     *
     * @param tfsUrl  the URL to TFS
     * @param getPath  the path for the specific request
     * @return String containing the response body
     * @throws TFSClientException
     */
    protected String processGet(String tfsUrl, String getPath, String parameters) throws TFSClientException {
        String uri = createUrl(tfsUrl, getPath, parameters);

        logger.debug("Start executing TFS GET request to url=\"{}\"", uri);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(uri);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getTFSUsername(), getTFSPassword());
        getRequest.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false) );
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_HTTP_CONTENT_TYPE);
        getRequest.addHeader(HttpHeaders.ACCEPT, DEFAULT_HTTP_CONTENT_TYPE);
        String result = "";

        try {
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new TFSClientException("Server not available", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        logger.debug("End executing TFS GET request to url=\"{}\" and receive this result={}", uri, result);

        return result;
    }

    /**
     * Create a TFS URL from base and path
     * @param tfsUrl  the base TFS URL, e.g. http://servername
     * @param path  the path to the request
     * @return a String containing a complete TFS path
     */
    public String createUrl(String tfsUrl, String path, String parameters) {
        String params = "";
        String base = "";
        String result = "";
        path = path.replaceAll(" ", "%20");
        // if path doesn't start with "/" add it
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        // if tfsUrl ends with "/" remove it
        if (tfsUrl.endsWith("/")) {
            tfsUrl.substring(0, tfsUrl.length()-1);
        }
        if (StringUtils.isNotEmpty(getApiVersion())) {
            if (StringUtils.isEmpty(parameters)) {
                params = "?api-version=" + getApiVersion();
            } else {
                params = "?" + parameters + "&api-version=" + getApiVersion();
            }
        } else {
            params = parameters;
        }
        result = tfsUrl + path + params;

        return result;
    }

    /**
     * @param path
     * @return
     */
    private String encodePath(String path) {
        String result;
        URI uri;
        try {
            uri = new URI(null, null, path, null);
            result = uri.toASCIIString();
        } catch (Exception e) {
            result = path;
        }
        return result;
    }

    /**
     * Returns a TFS Client specific Client Exception
     * @param response  the exception to throw
     * @return
     */
    private TFSClientException createHttpError(HttpResponse response) {
        String message;
        try {
            StatusLine statusLine = response.getStatusLine();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            StringBuffer responsePayload = new StringBuffer();
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                responsePayload.append(line);
            }

            message = String.format(" request not successful: %d %s. Reason: %s", statusLine.getStatusCode(), statusLine.getReasonPhrase(), responsePayload);

            logger.debug(message);

            if (new Integer(HttpStatus.SC_UNAUTHORIZED).equals(statusLine.getStatusCode())) {
                return new TFSClientException("TFS: Invalid credentials provided.");
            } else if (new Integer(HttpStatus.SC_NOT_FOUND).equals(statusLine.getStatusCode())) {
                return new TFSClientException("TFS: Request URL not found.");
            } else if (new Integer(HttpStatus.SC_BAD_REQUEST).equals(statusLine.getStatusCode())) {
                return new TFSClientException("TFS: Bad request. " + responsePayload);
            }
        } catch (IOException e) {
            return new TFSClientException("TFS: Can't read response");
        }

        return new TFSClientException(message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Testing API
    static public void main(String[] args) {
        TFSClient tfs = new TFSClient(null, "https://digitalparkingsolutions.visualstudio.com", "1.0", "DefaultCollection", "kevinalee", "67popgoaxbdm2ducvhaf54fgmtzbouronq22rfdb6fddt542c3va");

        String firstProj = "";
        String firstQuery = "";
        String firstWorkItem = "";

        System.out.println("Retrieving TFS Projects...");
        List<Project> projects = null;
        try {
            projects = tfs.getProjects();
            for (Project p : projects) {
                if (firstProj.length() == 0) firstProj = p.getName();
                System.out.println("Found Project " + p.getName());
                System.out.println("Description: " + p.getDescription());
                System.out.println("URL: " + p.getUrl());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Retrieving TFS Queries...");
        List<Query> queries = null;
        try {
            queries = tfs.getQueries(firstProj, "Shared Queries");
            for (Query q : queries) {
                if (firstQuery.length() == 0) firstQuery = q.getId();
                System.out.println("Found Query: " + q.getName());
                System.out.println("Path: " + q.getPath());
                System.out.println("URL: " + q.getUrl());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        for (Query query : queries) {
            System.out.println("Retrieving Work Items for query: " + query.getName());
            if (query.getIsFolder()) continue;
            List<WorkItem> workItems = null;
            try {
                workItems = tfs.getWorkItems(query.getId(), "", 2);
                if (workItems != null) {
                    for (WorkItem d : workItems) {
                        if (firstWorkItem.length() == 0) firstWorkItem = d.getId();
                        System.out.println("Found Work Item: " + d.getId());
                        System.out.println("Title: " + d.getName());
                        System.out.println("Description: " + d.getDescription());
                        System.out.println("Severity: " + d.getSeverity());
                    }
                }
            } catch (TFSClientException e){
                System.out.print(e.toString());
            }
        }

        System.out.println("Retrieving Work Item: " + firstWorkItem + "...");
        try {
            WorkItem wi = tfs.getWorkItem(firstWorkItem);
            System.out.println("Found Work Item: " + wi.getId());
            System.out.println("Title: " + wi.getName());
            System.out.println("State: " + wi.getState());
            System.out.println("Severity: " + wi.getSeverity());
            System.out.println("Type: " + wi.getType());
            System.out.println("Project: " + wi.getProject());
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }
    }
}
