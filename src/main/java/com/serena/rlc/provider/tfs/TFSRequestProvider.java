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

import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.spi.IRequestProvider;
import com.serena.rlc.provider.tfs.domain.WorkItem;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * TFS Request Provider
 * @author klee@serena.com
 */
public class TFSRequestProvider extends TFSBaseServiceProvider implements IRequestProvider {

    final static Logger logger = LoggerFactory.getLogger(TFSRequestProvider.class);

    final static String TITLE_FILTER = "titleFilter";

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
            defaultValue = "TFS Request Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "request_provider_description", displayName = "Request Provider Description",
            description = "provider description",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String providerDescription;

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
    public void setProviderDescription(String providerDescription) {
        if (StringUtils.isNotEmpty(providerDescription)) {
            providerDescription = providerDescription.trim();
        }

        this.providerDescription = providerDescription;
    }

    public String getRequestResultLimit() {
        return requestResultLimit;
    }

    @Autowired(required = false)
    public void setRequestResultLimit(String requestResultLimit) {
        this.requestResultLimit = requestResultLimit;
    }


    //================================================================================
    // Services Methods
    // -------------------------------------------------------------------------------
    //================================================================================

    @Service(name = FIND_REQUESTS, displayName = "Find Work Items", description = "Find TFS Work Items")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "TFS project", required = true, dataType = DataType.SELECT),
            @Param(fieldName = QUERY, displayName = "Query", description = "TFS Query", required = true, dataType = DataType.SELECT),
            @Param(fieldName = TITLE_FILTER, displayName = "Title Filter", description = "Work Item Title filter")
    })
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
                    pReqInfo = new ProviderInfo(request.getId(), request.getTitle(), request.getType(), request.getTitle(), request.getUrl());
                    pReqInfo.setId(request.getId());
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

    @Service(name = GET_REQUEST, displayName = "Get Work Item", description = "Get TFS Work Item information")
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

            ProviderInfo pReqInfo = new ProviderInfo(request.getId(), request.getTitle(), request.getType(), request.getTitle(), request.getUrl());
            pReqInfo.setId(request.getId());
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
            return getProjectFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(QUERY)) {
            return getQueryFieldValues(fieldName, properties);
        }

        throw new ProviderException("Unsupported get values for field name: " + fieldName);
    }

    //================================================================================
    // Private Methods
    //================================================================================

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

    //

    @Override
    public ServiceInfo getServiceInfo(String service)
            throws ProviderException {
        return AnnotationUtil.getServiceInfo(this.getClass(), service);
    }

    @Override
    public ServiceInfoResult getServices()
            throws ProviderException {
        List<ServiceInfo> services = AnnotationUtil.getServices(this.getClass());
        return new ServiceInfoResult(0, services.size(), services.toArray(new ServiceInfo[services.size()]));
    }

    @Override
    public FieldValuesGetterFunction findFieldValuesGetterFunction(String fieldName)
            throws ProviderException {
        return AnnotationUtil.findFieldValuesGetterFunction(this.getClass(), fieldName);
    }

    @Override
    public FieldValuesGetterFunctionResult findFieldValuesGetterFunctions()
            throws ProviderException {
        List<FieldValuesGetterFunction> getters = AnnotationUtil.findFieldValuesGetterFunctions(this.getClass());
        return new FieldValuesGetterFunctionResult(0, getters.size(), getters.toArray(new FieldValuesGetterFunction[getters.size()]));
    }

    @Override
    public ConfigurationPropertyResult getConfigurationProperties() throws ProviderException {
        List<ConfigurationProperty> configProps = AnnotationUtil.getConfigurationProperties(this.getClass(), this);
        return new ConfigurationPropertyResult(0, configProps.size(), configProps.toArray(new ConfigurationProperty[configProps.size()]));
    }

}
