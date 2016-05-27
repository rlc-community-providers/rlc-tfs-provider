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
import com.serena.rlc.provider.spi.IDeployUnitProvider;
import com.serena.rlc.provider.tfs.client.TFSClient;
import com.serena.rlc.provider.tfs.domain.Build;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * TFS Deployment Unit Provider
 * @author klee@serena.com
 */
public class TFSDeploymentUnitProvider extends TFSBaseServiceProvider implements IDeployUnitProvider {

    final static Logger logger = LoggerFactory.getLogger(TFSDeploymentUnitProvider.class);

    final static String BUILD_SPEC = "buildSpecification";
    final static String BUILD_STATUS_FILTER = "buildStatusFilter";
    final static String BUILD_RESULT_FILTER = "buildResultFilter";

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================

    @ConfigProperty(name = "deploy_unit_provider_name", displayName = "Deployment Unit Provider Name",
            description = "provider name",
            defaultValue = "TFS Deployment Unit Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "deploy_unit_provider_description", displayName = "Deployment Unit Provider Description",
            description = "provider description",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String providerDescription;

    @ConfigProperty(name = "tfs_build_api_version", displayName = "Build API Version",
            description = "TFS Build API Version.",
            defaultValue = "2.0",
            dataType = DataType.TEXT)
    private String tfsBuildApiVersion;

    @ConfigProperty(name = "deploy_unit_result_limit", displayName = "Result Limit",
            description = "Result limit for find deployment units action",
            defaultValue = "200",
            dataType = DataType.TEXT)
    private String deployUnitResultLimit;

    @ConfigProperty(name = "build_status_filter", displayName = "Build Status Filter",
            description = "Build Status filter find deployment units action",
            defaultValue = "inProgress,completed,cancelling,postponed,notStarted,all",
            dataType = DataType.TEXT)
    private String buildStatusFilter;

    @ConfigProperty(name = "build_result_filter", displayName = "Build Result Filter",
            description = "Build Result filter find deployment units action",
            defaultValue = "succeeded,partiallySucceeded,failed,canceled ",
            dataType = DataType.TEXT)
    private String buildResultFilter;

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

    public String getTfsBuildApiVersion() {
        return tfsBuildApiVersion;
    }

    @Autowired(required = false)
    public void setTfsBuildApiVersion(String tfsBuildApiVersion) {
        if (!StringUtils.isEmpty(tfsBuildApiVersion)) {
            this.tfsBuildApiVersion = tfsBuildApiVersion.trim();
        }
        else {
            this.tfsBuildApiVersion = "2.0";
        }
    }

    public String getDeployUnitResultLimit() {
        return deployUnitResultLimit;
    }

    @Autowired(required = false)
    public void setDeployUnitResultLimit(String deployUnitResultLimit) {
        this.deployUnitResultLimit = deployUnitResultLimit;
    }

    public String getBuildStatusFilter() {
        return buildStatusFilter;
    }

    @Autowired(required = false)
    public void setBuildStatusFilter(String buildStatusFilter) {
        this.buildStatusFilter = buildStatusFilter;
    }

    public String getBuildResultFilter() {
        return buildResultFilter;
    }

    @Autowired(required = false)
    public void setBuildResultFilter(String buildResultFilter) {
        this.buildResultFilter = buildResultFilter;
    }


    //================================================================================
    // Services Methods
    // -------------------------------------------------------------------------------
    //================================================================================

    @Override
    @Service(name = FIND_DEPLOY_UNITS, displayName = "Find Deploy Units", description = "Find TFS Builds to be used as Deployment Units")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "TFS Project", required = true, dataType = DataType.SELECT),
            @Param(fieldName = BUILD_DEFINITION, displayName = "Build Definition", description = "TFS Build Definition", required = true, dataType = DataType.SELECT),
            @Param(fieldName = BUILD_STATUS_FILTER, displayName = "Build Status", description = "TFS Build Status Filter", dataType = DataType.MULTI_SELECT),
            @Param(fieldName = BUILD_RESULT_FILTER, displayName = "Build Result", description = "TFS Build Result Filter", dataType = DataType.MULTI_SELECT),
        })
    public ProviderInfoResult findDeployUnits(List<Field> properties, Long startIndex, Long resultCount) throws ProviderException {
        List<ProviderInfo> list = new ArrayList<ProviderInfo>();

        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + PROJECT);
        String projectId = field.getValue();
        String projectName = field.getDisplayValue();

        field = Field.getFieldByName(properties, BUILD_DEFINITION);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + BUILD_DEFINITION);
        String buildDefinitionId = field.getValue();
        String buildDefinitionName = field.getDisplayValue();

        String statusFilter = "";
        field = Field.getFieldByName(properties, BUILD_STATUS_FILTER);
        List<String> filters = null;
        List<Field> fields = Field.getFieldsByName(properties, BUILD_STATUS_FILTER);
        if (fields != null && fields.size() > 0) {
            filters = new ArrayList<>();
            for (Field fieldFilter : fields) {
                filters.add(fieldFilter.getValue());
            }
            statusFilter = StringUtils.join(filters, ',');
        }

        String resultFilter = "";
        field = Field.getFieldByName(properties, BUILD_RESULT_FILTER);
        filters = null;
        fields = Field.getFieldsByName(properties, BUILD_RESULT_FILTER);
        if (fields != null && fields.size() > 0) {
            filters = new ArrayList<>();
            for (Field fieldFilter : fields) {
                filters.add(fieldFilter.getValue());
            }
            resultFilter = StringUtils.join(filters, ',');
        }

        TFSClient client = new TFSClient(null, this.getTfsUrl(), this.getTfsApiVersion(),
                null, null, this.getTfsBuildApiVersion(), this.getTfsCollection(),
                this.getServiceUser(), this.getServicePassword());

        List<Build> builds = null;
        try {
            logger.debug("Retrieving Builds for Build Definition: {} in Project: {} using filters Status \"{}\" and Result \"{}\"",
                    buildDefinitionName, projectName, statusFilter, resultFilter);
            builds = client.getBuilds(projectId, buildDefinitionId, statusFilter, resultFilter, Integer.valueOf(getDeployUnitResultLimit()));
            for (Build b : builds) {
                list.add(getProviderInfo(b, projectId + ":" + b.getId(), projectName + ":" + b.getBuildNumber()));
            }
        } catch (TFSClientException ex) {
            logger.error("Error retrieving Builds: {}", ex.getMessage());
        }

        return new ProviderInfoResult(0, list.size(), list.toArray(new ProviderInfo[list.size()]));
    }

    @Override
    @Service(name = GET_DEPLOY_UNIT, displayName = "Get Deploy Unit", description = "Get TFS Build as a Deployment Unit")
        @Params(params = {
            @Param(fieldName = BUILD_SPEC, displayName = "Build Specification", description = "Build Specification", required = false, deployUnit = true)
    })
    public ProviderInfo getDeployUnit(Field property) throws ProviderException {
        String projectId = null;
        String buildId = null;
        String buildSpec = property.getValue();
        if (StringUtils.isEmpty(buildSpec))
            throw new ProviderException("Missing required field: " + BUILD_SPEC);

        String[] buildSpecParts = buildSpec.split(":");
        if (buildSpecParts.length > 1) {
            projectId = buildSpecParts[0];
            buildId = buildSpecParts[1];
        } else
            throw new ProviderException("Invalid build specification: " + BUILD_SPEC);

        TFSClient client = new TFSClient(null, this.getTfsUrl(), this.getTfsApiVersion(),
                null, null, this.getTfsBuildApiVersion(), this.getTfsCollection(),
                this.getServiceUser(), this.getServicePassword());

        Build build = null;
        try {
            logger.debug("Retrieving Build id: {} for Project id: {} " + buildId, projectId);
            build = client.getBuild(projectId, buildId);
        } catch (TFSClientException ex) {
            logger.error("Error retrieving Build: {}", ex.getMessage());
        }

        if (build == null) {
            return null;
        }

        return getProviderInfo(build, projectId + ":" + build.getId(), build.getBuildNumber());
    }

    //================================================================================
    // Getter Methods
    // -------------------------------------------------------------------------------
    //================================================================================

    @Getter(name = BUILD_STATUS_FILTER, displayName = "Build Status Filter", description = "Build Status Filter.")
    public FieldInfo getBuildStatusFilterFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (StringUtils.isEmpty(buildStatusFilter)) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(buildStatusFilter, ",;");
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value;
        String status;
        while (st.hasMoreElements()) {
            status = (String) st.nextElement();
            status = status.trim();
            value = new FieldValueInfo(status, status);
            values.add(value);
        }

        fieldInfo.setValues(values);
        return fieldInfo;
    }

    @Getter(name = BUILD_RESULT_FILTER, displayName = "Build Result Filter", description = "Build Result Filter.")
    public FieldInfo getBuildResultFilterFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (StringUtils.isEmpty(buildResultFilter)) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(buildResultFilter, ",;");
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value;
        String status;
        while (st.hasMoreElements()) {
            status = (String) st.nextElement();
            status = status.trim();
            value = new FieldValueInfo(status, status);
            values.add(value);
        }

        fieldInfo.setValues(values);
        return fieldInfo;
    }

    //

    private ProviderInfo getProviderInfo(Build build, String id, String title) {
        ProviderInfo providerInfo = new ProviderInfo(id, build.getBuildNumber(), "Build", build.getBuildNumber());
        providerInfo.setDescription(build.getBuildNumber());
        // TODO: set URL to UI URL not JSON
        providerInfo.setUrl(build.getUrl());

        List<Field> fields = new ArrayList<Field>();
        Field field;

        if (build.getState() != null) {
            field = new Field("status", "Status");
            field.setValue(build.getState());
            fields.add(field);
        }

        if (build.getBuildResult() != null) {
            field = new Field("result", "Result");
            field.setValue(build.getBuildResult());
            fields.add(field);
        }

        // TODO: add more fields

        providerInfo.setProperties(fields);

        return providerInfo;
    }

    //

    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (fieldName.equalsIgnoreCase(PROJECT)) {
            return getProjectFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(BUILD_DEFINITION)) {
            return getBuildDefinitionFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(BUILD_QUEUE)) {
            return getBuildQueueFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(QUEUE_TYPE)) {
            return getQueueTypeFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(BUILD_STATUS_FILTER)) {
            return getBuildStatusFilterFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(BUILD_RESULT_FILTER)) {
            return getBuildResultFilterFieldValues(fieldName, properties);
        } else {
            return null;
        }
    }

    @Override
    public void setTFSClientConnectionDetails() {
        getTFSClient().createConnection(getSession(), getTfsUrl(), getTfsApiVersion(), null, null, getTfsBuildApiVersion(), getTfsCollection(), getServiceUser(), getServicePassword());
    }

    @Override
    public ServiceInfo getServiceInfo(String service) throws ProviderException {
        return AnnotationUtil.getServiceInfo(this.getClass(), service);
    }

    @Override
    public ServiceInfoResult getServices() throws ProviderException {
        List<ServiceInfo> services = AnnotationUtil.getServices(this.getClass());
        return new ServiceInfoResult(0, services.size(), services.toArray(new ServiceInfo[services.size()]));
    }

    @Override
    public FieldValuesGetterFunction findFieldValuesGetterFunction(String fieldName) throws ProviderException {
        return AnnotationUtil.findFieldValuesGetterFunction(this.getClass(), fieldName);
    }

    @Override
    public FieldValuesGetterFunctionResult findFieldValuesGetterFunctions() throws ProviderException {
        List<FieldValuesGetterFunction> getters = AnnotationUtil.findFieldValuesGetterFunctions(this.getClass());
        return new FieldValuesGetterFunctionResult(0, getters.size(), getters.toArray(new FieldValuesGetterFunction[getters.size()]));
    }

    @Override
    public ConfigurationPropertyResult getConfigurationProperties() throws ProviderException {
        List<ConfigurationProperty> configProps = AnnotationUtil.getConfigurationProperties(this.getClass(), this);
        return new ConfigurationPropertyResult(0, configProps.size(), configProps.toArray(new ConfigurationProperty[configProps.size()]));
    }

}
