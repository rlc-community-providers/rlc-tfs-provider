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
import com.serena.rlc.provider.tfs.domain.*;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * TFS Client
 * @author klee@serena.com
 */
@Component
public class TFSClient {
    private static final Logger logger = LoggerFactory.getLogger(TFSClient.class);

    public static String DEFAULT_HTTP_CONTENT_TYPE = "application/json";
    public enum VisualStudioApi { TFS_API, TFSBUILD_API, RM_API }

    private String tfsUrl;
    private String vsrmUrl;
    private String tfsUsername;
    private String tfsPassword;
    private String tfsCollection;
    private String tfsProject;
    private String tfsApiVersion;
    private String vsrmApiVersion;
    private String tfsBuildApiVersion;
    private SessionData session;

    public TFSClient() {
    }

    public TFSClient(SessionData session, String tfsUrl, String tfsApiVersion, String vsrmUrl, String vsrmApiVersion, String tfsBuildApiVersion, String collection, String username, String password) {
        this.createConnection(session, tfsUrl, tfsApiVersion, vsrmUrl, vsrmApiVersion, tfsBuildApiVersion, collection, username, password);
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

    public String getVSRMUrl() {
        return vsrmUrl;
    }

    public void setVSRMUrl(String url) {
        this.vsrmUrl = url;
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

    public String getTfsApiVersion() {
        return tfsApiVersion;
    }

    public void setTfsApiVersion(String tfsApiVersion) {
        this.tfsApiVersion = tfsApiVersion;
    }

    public String getVsrmApiVersion() {
        return vsrmApiVersion;
    }

    public void setVsrmApiVersion(String vsrmApiVersion) {
        this.vsrmApiVersion = vsrmApiVersion;
    }

    public void setTfsBuildApiVersion(String tfsBuildApiVersion) {
        this.tfsBuildApiVersion = tfsBuildApiVersion;
    }

    public String getTfsBuildApiVersion() {
        return tfsBuildApiVersion;
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
     * Create a new connection to TFS/VSRM.
     *
     * @param tfsUrl  the url to TFS, e.g. https://servername
     * @param tfsApiVersion  the version of the TFS REST API to use
     * @param vsrmUrl  the url to VSRM, e.g. https://servername.vsrm
     * @param vsrmApiVersion  the version of the VSRM REST API to use
     * @param tfsBuildApiVersion  the version of the TFS Build REST API to use
     * @param collection  the TFS collection
     * @param username  the username of the TFS user
     * @param password  the password/private token of the TFS user
     */
    public void createConnection(SessionData session, String tfsUrl, String tfsApiVersion, String vsrmUrl, String vsrmApiVersion, String tfsBuildApiVersion, String collection, String username, String password) {
        this.session = session;
        this.tfsUrl = tfsUrl;
        this.tfsApiVersion = tfsApiVersion;
        this.vsrmUrl = vsrmUrl;
        this.vsrmApiVersion = vsrmApiVersion;
        this.tfsBuildApiVersion = tfsBuildApiVersion;
        this.tfsCollection = collection;
        this.tfsProject = "";
        this.tfsUsername = username;
        this.tfsPassword = password;
    }

    /**
     * Get a list of Work Items from a Query.
     *
     * @param queryId  the id of the query to run
     * @param titleFilter  the title/name to filter work items on
     * @param resultLimit  the maximum number of Work Items to return
     * @return  a list of Work Items
     * @throws TFSClientException
     */
    public List<WorkItem> getWorkItems(String queryId, String titleFilter, Integer resultLimit) throws TFSClientException {
        logger.debug("Retrieving TFS Item Ids using Query \"{}\"", queryId);
        logger.debug("Using Title Filter: " + titleFilter);
        logger.debug("Limiting results to: " + resultLimit.toString());

        String queryResponse = processGet(VisualStudioApi.TFS_API, getTFSCollection() + "/_apis/wit/wiql/" + queryId, "");
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
            logger.debug("Retrieving TFS Item Details for Work Items \"{}\"", idList);
            String wiResponse = processGet(VisualStudioApi.TFS_API, getTFSCollection() + "/_apis/wit/workitems", idList);
            workItems = WorkItem.parseDetails(wiResponse);
        }

        return workItems;

    }

    /**
     * Get the details of a specific Work Item.
     *
     * @param workItemId  the id of the Work Item, e.g. 1
     * @return the Work Item if found
     * @throws TFSClientException
     */
    public WorkItem getWorkItem(String workItemId) throws TFSClientException {
        logger.debug("Retrieving TFS Work Item \"{}\"", workItemId);
        logger.debug("Using TFS Work Item Id: " + workItemId);

        String wiResponse = processGet(VisualStudioApi.TFS_API, getTFSCollection() + "/_apis/wit/workitems/" + workItemId, "");
        logger.debug(wiResponse);

        WorkItem workItem = WorkItem.parseSingle(wiResponse);
        return workItem;
    }

    /**
     * Get a list of Projects in the Collection.
     *
     * @return a list of projects
     * @throws TFSClientException
     */
    public List<Project> getProjects() throws TFSClientException {
        logger.debug("Retrieving TFS Projects");

        String projResponse = processGet(VisualStudioApi.TFS_API, getTFSCollection() + "/_apis/projects", "statefilter=All");
        logger.debug(projResponse);

        List<Project> projects = Project.parse(projResponse);
        return projects;
    }

    /**
     * Get a list of Work Item Queries in the specified Project.
     *
     * @param projectId  the id of the workItem, e.g. 1
     * @param folderPath  the path to the queries, e.g. Shared Queries
     *
     * @return a list of queries
     * @throws TFSClientException
     */
    public List<Query> getQueries(String projectId, String folderPath) throws TFSClientException {
        logger.debug("Retrieving TFS Queries for Project \"{}\" in folder path \"{}\"", projectId, folderPath);
        this.setTFSProject(projectId);

        String queryResponse = processGet(VisualStudioApi.TFS_API, getTFSCollection() + "/" + projectId + "/_apis/wit/queries/" + folderPath, "$depth=2");
        logger.debug(queryResponse);

        List<Query> queries = Query.parse(queryResponse);
        return queries;
    }

    /**
     * Get a list of Build Definitions for the specified Project.
     *
     * @param projectId  the identifier of the project
     * @return a list of build definitions
     */
    public List<BuildDefinition> getBuildDefinitions(String projectId, String startsWith) throws TFSClientException {
        logger.debug("Retrieving TFS Build Definitions for Project \"{}\" starting with \"{}\"", projectId, startsWith);
        this.setTFSProject(projectId);

        String params = "";
        if (startsWith != null && StringUtils.isNotEmpty(startsWith)) {
            params = "name="+startsWith;
        }

        String buildResponse = processGet(VisualStudioApi.TFSBUILD_API, getTFSCollection() + "/" + projectId + "/_apis/build/definitions", params);
        logger.debug(buildResponse);

        List<BuildDefinition> buildDefinitions = BuildDefinition.parse(buildResponse);
        return buildDefinitions;
    }

    /**
     * Get a list of Build Queues for the specified Project.
     *
     * @param projectId  the identifier of the project
     * @return a list of build definitions
     */
    public List<BuildQueue> getBuildQueues(String queueType, String startsWith) throws TFSClientException {
        logger.debug("Retrieving TFS Build Queues of type \"{}\" starting with \"{}\"", queueType, startsWith);

        String params = "";
        if (queueType != null && StringUtils.isNotEmpty(queueType)) {
            params = "type=" + queueType;
        }
        if (startsWith != null && StringUtils.isNotEmpty(startsWith)) {
            params += "&name="+startsWith;
        }
        String queueResponse = processGet(VisualStudioApi.TFSBUILD_API, getTFSCollection() + "/_apis/build/queues", params);
        logger.debug(queueResponse);

        List<BuildQueue> buildQueues = BuildQueue.parse(queueResponse);
        return buildQueues;
    }

    /**
     * Get a list of Builds for a Build Definition
     *
     * @param projectId  the identifier of the project
     * @param buildDefinitionId  the identifier of the build definition
     * @param statusFilter
     * @return a list of releases
     */
    public List<Build> getBuilds(String projectId, String buildDefinitionId, String statusFilter, String resultFilter, int resultLimit) throws TFSClientException {
        logger.debug("Retrieving TFS Builds for Builds Definition \"{}\" in Project \"{}\"", buildDefinitionId, projectId);
        this.setTFSProject(projectId);

        String maxBuilds = "100";
        if (resultLimit > 0) {
            maxBuilds = String.valueOf(resultLimit);
        }

        String buildResponse = processGet(VisualStudioApi.TFSBUILD_API, getTFSCollection() + "/" + projectId + "/_apis/build/builds",
                "definitions="+buildDefinitionId+"&statusFilter="+statusFilter+"&resultFilter="+resultFilter+"&maxBuildsPerDefinition="+maxBuilds);
        logger.debug(buildResponse);

        List<Build> builds = Build.parse(buildResponse);
        return builds;
    }

    /**
     * Get a specific Build
     *
     * @param projectId  the identifier of the project
     * @param buildId  the identifier of the build
     * @return the release
     */
    public Build getBuild(String projectId, String buildId) throws TFSClientException {
        logger.debug("Retrieving TFS Build \"{}\" in Project \"{}\"", buildId, projectId);
        this.setTFSProject(projectId);

        String buildResponse = processGet(VisualStudioApi.TFSBUILD_API, getTFSCollection() + "/" + projectId + "/_apis/build/builds/" + buildId, "");
        logger.debug(buildResponse);

        Build build = Build.parseSingle(buildResponse);
        return build;
    }

    /**
     * BuildQueue a new build for the specified definition.
     *
     * @param projectId  the identifier of the project
     * @param buildDefinitionId  the identifier of the build definition
     * @param queueId  the identifier of the queue to use (optional)
     * @param branchId  the identifier of the branch to use (optional)
     * @return the Build
     * @throws TFSClientException
     */
    public Build queueBuild(String projectId, String buildDefinitionId, String queueId, String branchId) throws TFSClientException {
        logger.debug("Queueing TFS Build Definition \"{}\" to queue \"{}\" in Project \"{}\"", buildDefinitionId, queueId, projectId);
        this.setTFSProject(projectId);

        JSONObject definition = new JSONObject();
        definition.put("id", Long.parseLong(buildDefinitionId));

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("definition", definition);

        if (queueId != null && StringUtils.isNotEmpty(queueId)) {
            jsonBody.put("queue.id", Long.parseLong(queueId));
        }
        if (branchId != null && StringUtils.isNotEmpty(branchId)) {
            jsonBody.put("sourceBranch", branchId);
        }

        String buildResponse = processPost(VisualStudioApi.TFSBUILD_API, getTFSCollection() + "/" + projectId + "/_apis/build/builds",
                "", jsonBody.toJSONString());
        logger.debug(buildResponse);

        Build build = Build.parseSingle(buildResponse);
        return build;
    }

    /**
     * Get a list of Release Definitions for the specified Project.
     *
     * @param projectId  the identifier of the project
     * @return a list of release definitions
     */
    public List<ReleaseDefinition> getReleaseDefinitions(String projectId) throws TFSClientException {
        logger.debug("Retrieving TFS Release Definitions for Project \"{}\"", projectId);
        this.setTFSProject(projectId);

        String releaseResponse = processGet(VisualStudioApi.RM_API, getTFSCollection() + "/" + projectId + "/_apis/release/definitions", "$expand=environments");
        logger.debug(releaseResponse);

        List<ReleaseDefinition> releaseDefinitions = ReleaseDefinition.parse(releaseResponse);
        return releaseDefinitions;
    }

    /**
     * Get a list of Releases for a Release Definition
     *
     * @param projectId  the identifier of the project
     * @param releaseDefinitionId  the identifier of the release definition
     * @return a list of releases
     */
    public List<Release> getReleases(String projectId, String releaseDefinitionId) throws TFSClientException {
        logger.debug("Retrieving TFS Releases for Release Definition \"{}\" in Project \"{}\"", releaseDefinitionId, projectId);
        this.setTFSProject(projectId);

        String releaseResponse = processGet(VisualStudioApi.RM_API, getTFSCollection() + "/" + projectId + "/_apis/release/releases", "definitionId="+releaseDefinitionId+"&$expand=environments");
        logger.debug(releaseResponse);

        List<Release> releases = Release.parse(releaseResponse);
        return releases;
    }

    /**
     * Get a specific Release
     *
     * @param projectId  the identifier of the project
     * @param releaseId  the identifier of the release
     * @return the release
     */
    public Release getRelease(String projectId, String releaseId) throws TFSClientException {
        logger.debug("Retrieving TFS Release \"{}\" in Project \"{}\"", releaseId, projectId);
        this.setTFSProject(projectId);

        String releaseResponse = processGet(VisualStudioApi.RM_API, getTFSCollection() + "/" + projectId + "/_apis/release/releases/" + releaseId, "");
        logger.debug(releaseResponse);

        Release release = Release.parseSingle(releaseResponse);
        return release;
    }

    /**
     * Get a specific Release Environment status
     *
     * @param projectId  the identifier of the project
     * @param releaseId  the identifier of the release
     * @param environmentId  the identifier of the environment
     * @return the release environment status, e.g. queued
     */
    public String getReleaseEnvironmentStatus(String projectId, String releaseId, String environmentId) throws TFSClientException {
        logger.debug("Retrieving TFS Release \"{}\" status for Environment \"{}\" in Project \"{}\"", releaseId, environmentId, projectId);
        this.setTFSProject(projectId);

        String releaseResponse = processGet(VisualStudioApi.RM_API, getTFSCollection() + "/" + projectId + "/_apis/release/releases/" + releaseId, "");
        logger.debug(releaseResponse);

        String status = "unknown";
        Release release = Release.parseSingle(releaseResponse);
        for (Environment e : release.getEnvironments()) {
            if (e.getId().equals(environmentId)) {
                status = e.getState();
                logger.debug("Environment \"{}\" Environment \"{}\" in Release \"{}\" has status \"{}\"", environmentId, releaseId, status);
                break;
            }
        }
        return status;
    }

    /**
     * Deploy a release to one of its environments.
     *
     * @param projectId  the identifier of the project
     * @param releaseId  the identifier of the release
     * @param environmentId  the identifier of the environment
     * @return the Release
     * @throws TFSClientException
     */
    public Release deployRelease(String projectId, String releaseId, String environmentId) throws TFSClientException {
        logger.debug("Deploying TFS Release \"{}\" to environment \"{}\" in Project \"{}\"", releaseId, environmentId, projectId);
        this.setTFSProject(projectId);
        this.setVsrmApiVersion("3.0-preview.2"); // TODO: hack for deployment

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("status", "InProgress"); // 2=inprogress

        String releaseResponse = processPatch(VisualStudioApi.RM_API, getTFSCollection() + "/" + projectId + "/_apis/release/releases/" +
                releaseId + "/environments/" + environmentId,
                "", jsonBody.toJSONString());
        logger.debug(releaseResponse);

        Release release = Release.parseSingle(releaseResponse);
        return release;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Execute a get request to TFS.
     *
     * @param whichApi  the API to use
     * @param path  the path for the specific request
     * @param parameters  parameters to send with the query
     * @return String containing the response body
     * @throws TFSClientException
     */
    protected String processGet(VisualStudioApi whichApi, String path, String parameters) throws TFSClientException {
        String uri = createUrl(whichApi, path, parameters);

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
            if (response.getStatusLine().getStatusCode() != org.apache.http.HttpStatus.SC_OK) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            throw new TFSClientException("Server not available", ex);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        logger.debug("End executing TFS GET request to url=\"{}\" and receive this result={}", uri, result);

        return result;
    }

    /**
     * Execute a post request to TFS.
     *
     * @param whichApi  the API to use
     * @param path  the path for the specific request
     * @param parameters  parameters to send with the query
     * @param body  the body to send with the request
     * @return String containing the response body
     * @throws TFSClientException
     */
    public String processPost(VisualStudioApi whichApi, String path, String parameters, String body) throws TFSClientException {
        String uri = createUrl(whichApi, path, parameters);

        logger.debug("Start executing TFS POST request to url=\"{}\" with data: {}", uri, body);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(uri);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getTFSUsername(), getTFSPassword());
        postRequest.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false) );
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_HTTP_CONTENT_TYPE);
        postRequest.addHeader(HttpHeaders.ACCEPT, DEFAULT_HTTP_CONTENT_TYPE);

        try {
            postRequest.setEntity(new StringEntity(body,"UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
            throw new TFSClientException("Error creating body for POST request", ex);
        }
        String result = "";

        try {
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_OK && response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_CREATED &&
                    response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_ACCEPTED) {
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
        }

        logger.debug("End executing TFS POST request to url=\"{}\" and received this result={}", uri, result);

        return result;
    }

    /**
     * Execute a patch request to TFS.
     *
     * @param whichApi  the API to use
     * @param path  the path for the specific request
     * @param parameters  parameters to send with the query
     * @param body  the body to send with the request
     * @return String containing the response body
     * @throws TFSClientException
     */
    public String processPatch(VisualStudioApi whichApi, String path, String parameters, String body) throws TFSClientException {
        String uri = createUrl(whichApi, path, parameters);

        logger.debug("Start executing TFS PATCH request to url=\"{}\" with data: {}", uri, body);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPatch patchRequest = new HttpPatch(uri);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getTFSUsername(), getTFSPassword());
        patchRequest.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false) );
        patchRequest.addHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_HTTP_CONTENT_TYPE);
        patchRequest.addHeader(HttpHeaders.ACCEPT, DEFAULT_HTTP_CONTENT_TYPE);

        try {
            patchRequest.setEntity(new StringEntity(body,"UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
            throw new TFSClientException("Error creating body for PATCH request", ex);
        }
        String result = "";

        try {
            HttpResponse response = httpClient.execute(patchRequest);
            if (response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_OK && response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_CREATED &&
                    response.getStatusLine().getStatusCode() != org.apache.commons.httpclient.HttpStatus.SC_ACCEPTED) {
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
        }

        logger.debug("End executing TFS PATCH request to url=\"{}\" and received this result={}", uri, result);

        return result;
    }

    /**
     * Create a TFS URL from base and path.
     *
     * @param whichApi  is this a TFS of RM URL
     * @param path  the path to the request
     * @param parameters  the parameters to send with the request
     * @return a String containing a complete TFS path
     */
    public String createUrl(VisualStudioApi whichApi, String path, String parameters) {
        String base = getTFSUrl();
        String apiVersion = getTfsApiVersion();
        String apiParams;

        // which API are we using?
        if (whichApi == VisualStudioApi.RM_API) {
            base = getVSRMUrl();
            apiVersion = getVsrmApiVersion();
        } else if (whichApi == VisualStudioApi.TFSBUILD_API) {
            apiVersion = getTfsBuildApiVersion();
        }

        // trim and encode path
        path = path.trim().replaceAll(" ", "%20");
        // if path doesn't start with "/" add it
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // set parameters
        if (StringUtils.isEmpty(parameters)) {
            apiParams = "?api-version=" + apiVersion;
        } else {
            apiParams = "?" + parameters + "&api-version=" + apiVersion;
        }

        System.out.println(base + path + apiParams);
        return base + path + apiParams;
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
        TFSClient tfs = new TFSClient(null, "https://digitalparkingsolutions.visualstudio.com",
                "1.0", "https://digitalparkingsolutions.vsrm.visualstudio.com", "3.0-preview.1", "2.0",
                "DefaultCollection", "kevinalee", "67popgoaxbdm2ducvhaf54fgmtzbouronq22rfdb6fddt542c3va");

        Project firstProj = null;
        Query firstQuery = null;
        WorkItem firstWorkItem = null;
        ReleaseDefinition firstReleaseDefinition = null;
        Release firstRelease = null;
        Environment firstEnvironment = null;
        BuildDefinition firstBuildDefinition = null;
        BuildQueue firstBuildQueue = null;
        Build firstBuild = null;

        System.out.println("Retrieving TFS Projects...");
        List<Project> projects = null;
        try {
            projects = tfs.getProjects();
            for (Project p : projects) {
                if (firstProj == null) firstProj = p;
                System.out.println("Found Project: " + p.getTitle());
                System.out.println("Description: " + p.getDescription());
                System.out.println("URL: " + p.getUrl());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Retrieving TFS Queries...");
        List<Query> queries = null;
        try {
            queries = tfs.getQueries(firstProj.getId(), "Shared Queries");
            for (Query q : queries) {
                if (firstQuery == null) firstQuery = q;
                System.out.println("Found Query: " + q.getTitle());
                System.out.println("Path: " + q.getPath());
                System.out.println("URL: " + q.getUrl());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        for (Query query : queries) {
            System.out.println("Retrieving Work Items for Query: " + query.getTitle());
            if (query.getIsFolder()) continue;
            List<WorkItem> workItems = null;
            try {
                workItems = tfs.getWorkItems(query.getId(), "", 2);
                if (workItems != null) {
                    for (WorkItem wi : workItems) {
                        if (firstWorkItem == null) firstWorkItem = wi;
                        System.out.println("Found Work Item: " + wi.getId());
                        System.out.println("Title: " + wi.getTitle());
                        System.out.println("Description: " + wi.getDescription());
                        System.out.println("Severity: " + wi.getSeverity());
                    }
                }
            } catch (TFSClientException e){
                System.out.print(e.toString());
            }
        }

        System.out.println("Retrieving Work Item: " + firstWorkItem.getId() + "...");
        try {
            WorkItem wi = tfs.getWorkItem(firstWorkItem.getId());
            System.out.println("Found Work Item: " + wi.getId());
            System.out.println("Title: " + wi.getTitle());
            System.out.println("State: " + wi.getState());
            System.out.println("Severity: " + wi.getSeverity());
            System.out.println("Type: " + wi.getType());
            System.out.println("Project: " + wi.getProject());
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Retrieving TFS Release Definitions...");
        List<ReleaseDefinition> releaseDefinitions = null;
        try {
            releaseDefinitions = tfs.getReleaseDefinitions(firstProj.getId());
            for (ReleaseDefinition rd : releaseDefinitions) {
                if (firstReleaseDefinition == null) firstReleaseDefinition = rd;
                System.out.println("Found Release Definition: " + rd.getId() + " - " + rd.getTitle());
                System.out.println("URL: " + rd.getUrl());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Retrieving TFS Releases...");
        List<Release> releases = null;
        try {
            releases = tfs.getReleases(firstProj.getId(), firstReleaseDefinition.getId());
            for (Release r : releases) {
                if (firstRelease == null) firstRelease = r;
                System.out.println("Found Release: " + r.getId() + " - " + r.getTitle());
                System.out.println("Status: " + r.getState());
                for (Environment e : r.getEnvironments()) {
                    if (firstEnvironment == null) { firstEnvironment = e; }
                    System.out.println("with Environment: " + e.getId() + " - " + e.getTitle());
                }
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Deploying Release " + firstRelease.getTitle() + " to " + firstEnvironment.getTitle());
        try {
            Release release = tfs.deployRelease(firstProj.getId(), firstRelease.getId(), firstEnvironment.getId());
            System.out.println("Release " + release.getTitle() + " has status: " + release.getState());
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        int pollCount = 0;
        String deployStatus = null;
        while (pollCount < 100) {
            try {
                Thread.sleep(6000);
                deployStatus = tfs.getReleaseEnvironmentStatus(firstProj.getId(), firstRelease.getId(),
                        firstEnvironment.getId());
                System.out.println("Environment Deployment Status = " + deployStatus);
            } catch (TFSClientException e) {
                logger.debug ("Error checking release status ({}) - {}", firstRelease.getId(), e.getMessage());
            } catch (InterruptedException e) {
            }
            if (deployStatus != null && (deployStatus.equals("succeeded") ||
                    deployStatus.equals("rejected") ||
                    deployStatus.equals("failed"))) {
                break;
            }

            pollCount++;
        }

        if (deployStatus != null && deployStatus.equals("succeeded")) {
            System.out.println("Release " + firstRelease.getTitle() + " has succeeded");
        } else {
            System.out.println("Release " + firstRelease.getTitle() + " has failed/rejected or its status cannot be retrieved.");
        }

        System.out.println("Retrieving TFS Build Definitions...");
        List<BuildDefinition> buildDefinitions = null;
        try {
            buildDefinitions = tfs.getBuildDefinitions(firstProj.getId(), "");
            for (BuildDefinition bd : buildDefinitions) {
                if (firstBuildDefinition == null) firstBuildDefinition = bd;
                System.out.println("Found Build Definition: " + bd.getId() + " - " + bd.getTitle());
                System.out.println("URL: " + bd.getUrl());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Retrieving TFS Build Queues...");
        List<BuildQueue> buildQueues = null;
        try {
            buildQueues = tfs.getBuildQueues("buildController", "");
            for (BuildQueue bq : buildQueues) {
                if (firstBuildQueue == null) firstBuildQueue = bq;
                System.out.println("Found Build Queue: " + bq.getId() + " - " + bq.getTitle());
                System.out.println("URL: " + bq.getUrl());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        System.out.println("Retrieving TFS Builds...");
        List<Build> builds = null;
        try {
            builds = tfs.getBuilds(firstProj.getId(), firstBuildDefinition.getId(), "", "", 50);
            for (Build b : builds) {
                if (firstBuild == null) firstBuild = b;
                System.out.println("Found Build: " + b.getId() + " - " + b.getBuildNumber());
                System.out.println("Status: " + b.getState());
                System.out.println("Result: " + b.getBuildResult());
            }
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        Build queuedBuild = null;
        System.out.println("Queuing Build for Definition " + firstBuildDefinition.getTitle());
        try {
            queuedBuild = tfs.queueBuild(firstProj.getId(), firstBuildDefinition.getId(), firstBuildQueue.getId(), null);
            System.out.println("Build " + queuedBuild.getBuildNumber() + " has status: " + queuedBuild.getState());
        } catch (TFSClientException e) {
            System.out.print(e.toString());
        }

        pollCount = 0;
        String buildStatus = null;
        String buildResult = null;
        while (pollCount < 100) {
            try {
                Thread.sleep(6000);
                Build tmpBuild = tfs.getBuild(firstProj.getId(), queuedBuild.getId());
                buildStatus = tmpBuild.getState();
                System.out.println("Build Status = " + buildStatus);
                System.out.println("Build Result = " + buildResult);
            } catch (TFSClientException e) {
                logger.debug ("Error checking build status ({}) - {}", queuedBuild.getId(), e.getMessage());
            } catch (InterruptedException e) {
            }
            if (buildStatus != null && buildStatus.equals("completed")) {
                break;
            }

            pollCount++;
        }

        if (buildStatus != null && buildStatus.equals("completed")) {
            System.out.println("Build " + queuedBuild.getId() + " has completed with result " + buildResult);
        } else {
            System.out.println("Build " + queuedBuild.getId() + " result cannot be retrieved.");
        }


    }


}
