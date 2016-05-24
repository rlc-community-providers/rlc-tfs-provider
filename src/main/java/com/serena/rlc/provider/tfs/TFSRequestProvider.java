/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.tfs;

import com.serena.rlc.provider.BaseRequestProvider;
import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.tfs.client.TFSClient;
import com.serena.rlc.provider.tfs.domain.Query;
import com.serena.rlc.provider.tfs.domain.WorkItem;
import com.serena.rlc.provider.tfs.domain.TFSObject;
import com.serena.rlc.provider.tfs.domain.Project;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import java.util.ArrayList;
import java.util.List;

/**
 * TFS Request Provider
 * @author klee@serena.com
 */
public class TFSRequestProvider extends BaseRequestProvider {

    final static Logger logger = LoggerFactory.getLogger(TFSRequestProvider.class);
    final static String PROJECT = "project";
    final static String QUERY = "query";
    final static String TITLE_FILTER = "titleFilter";

    private TFSClient tfsClient;
    private Integer resultLimit;


    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================
    
    @ConfigProperty(name = "request_provider_name", displayName = "Request Provider Name",
            description = "provider name",
            defaultValue = "TFS Work Item Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "request_provider_description", displayName = "Request Provider Description",
            description = "provider description",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String providerDescription;

    @ConfigProperty(name = "tfs_url", displayName = "TFS URL",
            description = "TFS Server URL.",
            defaultValue = "http://<servername>",
            dataType = DataType.TEXT)
    private String tfsUrl;
    
    @ConfigProperty(name = "tfs_api_version", displayName = "TFS API Version",
            description = "The TFS API Version.",
            defaultValue = "1.0",
            dataType = DataType.TEXT)
    private String tfsApiVersion;

    @ConfigProperty(name = "tfs_collection", displayName = "TFS Collection",
            description = "The TFS domain to query.",
            defaultValue = "DefaultCollection",
            dataType = DataType.TEXT)
    private String tfsCollection;    

    @ConfigProperty(name = "tfs_serviceuser", displayName = "User Name",
            description = "TFS service username.",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String serviceUser;

    @ConfigProperty(name = "tfs_servicepassword", displayName = "Password",
            description = "TFS service password.",
            defaultValue = "",
            dataType = DataType.PASSWORD)
    private String servicePassword;

    @ConfigProperty(name = "request_result_limit", displayName = "Result Limit",
            description = "Result limit for find requests action",
            defaultValue = "200",
            dataType = DataType.TEXT)
    private String requestResultLimit;

    @Override
    public String getProviderName() {
        return this.providerName;
    }

    @Autowired(required = false)
    @Override
    public void setProviderName(String providerName) {
        if (StringUtils.isNotEmpty(providerName)) {
            providerName = providerName.trim();
        }

        this.providerName = providerName;
    }

    @Override
    public String getProviderDescription() {
        return this.providerDescription;
    }

    @Autowired(required = false)
    @Override
    public void setProviderDescription(String providerDescription) {
        if (StringUtils.isNotEmpty(providerDescription)) {
            providerDescription = providerDescription.trim();
        }

        this.providerDescription = providerDescription;
    }

    public String getTfsUrl() {
        return tfsUrl;
    }

    @Autowired(required = false)
    public void setTfsUrl(String tfsUrl) {
        if (StringUtils.isNotEmpty(tfsUrl)) {
            this.tfsUrl = tfsUrl.replaceAll("^\\s+", "");
        } else {
            this.tfsUrl = "http://localhost";
        }
    }

    public String getTfsApiVersion() {
        return tfsApiVersion;
    }

    @Autowired(required = false)
    public void setTfsApiVersion(String tfsApiVersion) {
        if (!StringUtils.isEmpty(tfsApiVersion)) {
            tfsApiVersion = tfsApiVersion.trim();
        }
        else {
            this.tfsApiVersion = "1.0";
        }

        this.tfsApiVersion = tfsApiVersion;
    }

    public String getTfsCollection() {
        return tfsCollection;
    }

    @Autowired(required = false)
    public void setTfsCollection(String tfsCollection) {
        if (!StringUtils.isEmpty(tfsCollection)) {
            tfsCollection = tfsCollection.trim();
        } else {
            tfsCollection = "DefaultCollection";
        }

        this.tfsCollection = tfsCollection;
    }

    public String getServiceUser() {
        return serviceUser;
    }

    @Autowired(required = false)
    public void setServiceUser(String serviceUser) {
        if (!StringUtils.isEmpty(serviceUser)) {
            this.serviceUser = serviceUser.replaceAll("^\\s+", "");
        }
    }

    public String getServicePassword() {
        return servicePassword;
    }

    @Autowired(required = false)
    public void setServicePassword(String servicePassword) {
        if (!StringUtils.isEmpty(servicePassword)) {
            this.servicePassword = servicePassword.replaceAll("^\\s+", "");
        }
    }

    public String getRequestResultLimit() {
        return requestResultLimit;
    }

    @Autowired(required = false)
    public void setRequestResultLimit(String requestResultLimit) {
        this.requestResultLimit = requestResultLimit;
    }


    //================================================================================
    // IRequestProvider Overrides
    //================================================================================

    @Override
    @Service(name = FIND_REQUESTS, displayName = "Find Work Items", description = "Find TFS Work Items.")
    @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "TFS project name", required = true, dataType = DataType.SELECT),
            @Param(fieldName = QUERY, displayName = "Query", description = "TFS Query name", required = true, dataType = DataType.SELECT),
            @Param(fieldName = TITLE_FILTER, displayName = "Title Filter", description = "Work Item Title filter."),})
    public ProviderInfoResult findRequests(List<Field> properties, Long startIndex, Long resultCount) throws ProviderException {
        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null) {
            throw new ProviderException("Missing required property: " + PROJECT);
        }

        String projectId = field.getValue();
        logger.debug("Filtering on project: " + projectId);

        field = Field.getFieldByName(properties, QUERY);
        if (field == null) {
            throw new ProviderException("Missing required property: " + QUERY);
        }

        String queryId = field.getValue();
        logger.debug("Using query: " + queryId);

        String titleFilter = null;
        field = Field.getFieldByName(properties, TITLE_FILTER);
        if (field != null) {
            titleFilter = field.getValue();
        }

        List<ProviderInfo> list = new ArrayList<>();

        setTFSClientConnectionDetails();
        try {
            List<WorkItem> requests = getTFSClient().getWorkItems(queryId, titleFilter, getResultLimit());
            if (requests != null) {
                ProviderInfo pReqInfo;
                for (WorkItem request : requests) {
                    pReqInfo = new ProviderInfo(request.getId().toString(), request.getTitle(), request.getType(), request.getTitle(), request.getUrl());
                    pReqInfo.setId(request.getId().toString());
                    pReqInfo.setName(request.getTitle());
                    pReqInfo.setTitle(request.getTitle());
                    if (StringUtils.isEmpty(request.getDescription())) {
                        pReqInfo.setDescription(request.getTitle());
                    } else {
                        pReqInfo.setDescription(request.getDescription());
                    }
                    pReqInfo.setUrl(getTfsUrl() + "/" + request.getProject().replaceAll(" ", "%20") + "/_workitems?id=" + request.getId() + "&_a=edit");

                    List<Field> fields = new ArrayList<>();
                    addField(fields, "project", "Project", request.getProject());
                    addField(fields, "owner", "Owner", request.getAssignedTo());
                    addField(fields, "status", "Status", request.getState());
                    addField(fields, "severity", "Severity", request.getSeverity());
                    addField(fields, "creator", "Creator", request.getCreatedBy());
                    addField(fields, "dateCreated", "Date Created", request.getDateCreated());
                    addField(fields, "lastUpdated", "Last Updated", request.getDateChanged());

                    pReqInfo.setProperties(fields);
                    list.add(pReqInfo);
                }
            }
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Work Items: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }

        return new ProviderInfoResult(0, list.size(), list.toArray(new ProviderInfo[list.size()]));
    }

    @Override
    @Service(name = GET_REQUEST, displayName = "Get Work Item", description = "Get TFS Work Item information.")
    @Params(params = {
            @Param(fieldName = REQUEST_ID, displayName = "Work Item Id", description = "TFS Work Item identifier", required = true, deployUnit = false, dataType = DataType.SELECT)
    })
    public ProviderInfo getRequest(Field property) throws ProviderException {
        if (StringUtils.isEmpty(property.getValue())) {
            throw new ProviderException("Missing required field: " + REQUEST_ID);
        }

        setTFSClientConnectionDetails();
        try {
            WorkItem request = getTFSClient().getWorkItem(property.getValue());
            if (request == null) {
                throw new ProviderException("Unable to find request: " + property.getValue());
            }

            ProviderInfo pReqInfo = new ProviderInfo(request.getId().toString(), request.getTitle(), request.getType(), request.getTitle(), request.getUrl());
            pReqInfo.setId(request.getId().toString());
            pReqInfo.setName(request.getTitle());
            pReqInfo.setTitle(request.getTitle());
            if (StringUtils.isEmpty(request.getDescription())) {
                pReqInfo.setDescription(request.getTitle());
            } else {
                pReqInfo.setDescription(request.getDescription());
            }
            pReqInfo.setUrl(getTfsUrl() + "/" + request.getProject().replaceAll(" ", "%20") + "/_workitems?id=" + request.getId() + "&_a=edit");

            List<Field> fields = new ArrayList<>();
            addField(fields, "project", "Project", request.getProject());
            addField(fields, "owner", "Owner", request.getAssignedTo());
            addField(fields, "status", "Status", request.getState());
            addField(fields, "severity", "Severity", request.getSeverity());
            addField(fields, "creator", "Creator", request.getCreatedBy());
            addField(fields, "dateCreated", "Date Created", request.getDateCreated());
            addField(fields, "lastUpdated", "Last Updated", request.getDateChanged());

            pReqInfo.setProperties(fields);
            return pReqInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Work Item: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }
    }

    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties)
            throws ProviderException {

        if (fieldName.equalsIgnoreCase(PROJECT)) {
            return getProjectFieldValues(fieldName);
        } else if (fieldName.equalsIgnoreCase(QUERY)) {
            return getQueryFieldValues(fieldName, properties);
        }

        throw new ProviderException("Unsupported get values for field name: " + fieldName);
    }

    //================================================================================
    // Getter Methods
    // -------------------------------------------------------------------------------
    // These methods are used to get the field values. The @Getter annotation is used
    // by the system to generate a user interface and pass the correct parameters to
    // to the provider
    //================================================================================

    @Getter(name = PROJECT, displayName = "Project", description = "Get TFS project field values.")
    public FieldInfo getProjectFieldValues(String fieldName) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setTFSClientConnectionDetails();

        try {
            List<Project> tfsProjects = tfsClient.getProjects();
            if (tfsProjects == null || tfsProjects.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (Project tfsProj : tfsProjects) {

                value = new FieldValueInfo(tfsProj.getProjectId(), tfsProj.getTitle());
                if (tfsProj.getProjectId() == null || StringUtils.isEmpty(tfsProj.getProjectId())) {
                    value.setId(tfsProj.getTitle());
                }

                value.setDescription(tfsProj.getTitle());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Projects: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }
    }

    @Getter(name = QUERY, displayName = "Query", description = "Get TFS Query field values.")
        @Params(params = {@Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS project field values", required = true, dataType = DataType.SELECT)
    })
    public FieldInfo getQueryFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setTFSClientConnectionDetails();

        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + PROJECT);

        String projectId = field.getValue();

        try {
            List<Query> tfsQueries = tfsClient.getQueries(projectId, "Shared Queries");
            if (tfsQueries == null || tfsQueries.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (Query tfsQuery : tfsQueries) {

                value = new FieldValueInfo(tfsQuery.getQueryId(), tfsQuery.getTitle());
                if (tfsQuery.getQueryId() == null || StringUtils.isEmpty(tfsQuery.getQueryId())) {
                    value.setId(tfsQuery.getTitle());
                }

                value.setDescription(tfsQuery.getTitle());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Queries: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }
    }


    //================================================================================
    // Private Methods
    //================================================================================

	private TFSClient getTFSClient() {
        if (tfsClient == null) {
            tfsClient = new TFSClient();
        }
        
        return tfsClient;
    }
	
    private void addField(List<Field> fieldCollection, String fieldName, String fieldDisplayName, String fieldValue) {
        if (StringUtils.isNotEmpty(fieldValue)) {
            Field field = new Field(fieldName, fieldDisplayName);
            field.setValue(fieldValue);
            fieldCollection.add(field);
        }
    }

    private void setTFSClientConnectionDetails() {
        getTFSClient().createConnection(getSession(), getTfsUrl(), getTfsApiVersion(), null, null, getTfsCollection(), getServiceUser(), getServicePassword());
    }

    private int getResultLimit() {
        if (resultLimit == null) {
            resultLimit = 200;

            if (StringUtils.isNotBlank(requestResultLimit)) {
                try {
                    resultLimit = Integer.parseInt(requestResultLimit);
                } catch (Throwable e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }

        return resultLimit;
    }

}
