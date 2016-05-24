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

import com.serena.rlc.provider.BaseExecutionProvider;
import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.tfs.client.TFSClient;
import com.serena.rlc.provider.tfs.domain.*;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * TFS Release Manager Execution Provider
 * @author klee@serena.com
 */
public class TFSExecutionProvider extends BaseExecutionProvider {

    final static Logger logger = LoggerFactory.getLogger(TFSExecutionProvider.class);
    final static String PROJECT = "project";
    final static String RELEASE_DEFINITION = "releaseDefinition";
    final static String RELEASE = "release";
    final static String ENVIRONMENT = "environment";
    final static String DEPLOY_RELEASE = "deployRelease";
    final static String DEPLOY_RELEASE_VARS = "deployReleaseVars";

    private TFSClient tfsClient;

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================
    @ConfigProperty(name = "execution_provider_name", displayName = "Execution Provider Name",
            description = "provider name",
            defaultValue = "TFS Release Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "execution_provider_description", displayName = "Execution Provider Description",
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

    @ConfigProperty(name = "vsrm_url", displayName = "Release Management URL",
            description = "TFS Release Management Server URL.",
            defaultValue = "http://<servername>.vsrm",
            dataType = DataType.TEXT)
    private String vsrmUrl;

    @ConfigProperty(name = "vsrm_api_version", displayName = "Release Management API Version",
            description = "The TFS Release Management API Version.",
            defaultValue = "3.0-preview.1",
            dataType = DataType.TEXT)
    private String vsrmApiVersion;

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

    @ConfigProperty(name = "execution_action_wait_for_callback", displayName = "Wait for Callback",
            description = "Set this to false to set the execution status to Completed when the action is executed and a job number is provided.",
            defaultValue = "false",
            dataType = DataType.TEXT)
    private String waitForCallback;

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

    public String getVsrmUrl() {
        return vsrmUrl;
    }

    @Autowired(required = false)
    public void setVsrmUrl(String vsrmUrl) {
        if (StringUtils.isNotEmpty(vsrmUrl)) {
            this.vsrmUrl = vsrmUrl.replaceAll("^\\s+", "");
        } else {
            this.vsrmUrl = "http://localhost.vsrm";
        }
    }

    public String getVsrmApiVersion() {
        return vsrmApiVersion;
    }

    @Autowired(required = false)
    public void setVsrmApiVersion(String vsrmApiVersion) {
        if (!StringUtils.isEmpty(vsrmApiVersion)) {
            vsrmApiVersion = vsrmApiVersion.trim();
        }
        else {
            this.vsrmApiVersion = "3.0-preview.1";
        }

        this.vsrmApiVersion = vsrmApiVersion;
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

    @Autowired(required = false)
    public void setWaitForCallback(String waitForCallback) {
        if (StringUtils.isNotEmpty(waitForCallback)) {
            this.waitForCallback = waitForCallback;
        }
    }

    public String getWaitForCallback() {
        return waitForCallback;
    }

    @Override
    @Service(name = EXECUTE, displayName = "Execute", description = "Execute Release Management action.")
    @Params(params = {
        @Param(fieldName = ACTION, description = "Release Management action to execute", required = true, dataType = DataType.SELECT),
        @Param(fieldName = PROPERTIES, description = "Release Management action properties", required = true)})
    public ExecutionInfo execute(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        return launchJob(taskTitle, taskDescription, properties);
    }

    @Service(name = VALIDATE, displayName = "Validate", description = "Validate")
    @Params(params = {
        @Param(fieldName = ACTION, displayName = "Action", description = "Release Management action to validate", required = true, dataType = DataType.SELECT),
        @Param(fieldName = PROPERTIES, displayName = "Properties", description = "Release Management action properties", required = true)
    })
    @Override
    public ExecutionInfo validate(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        return new ExecutionInfo("task is valid", true);

    }

    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (fieldName.equalsIgnoreCase(PROJECT)) {
            return getProjectFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(RELEASE_DEFINITION)) {
            return getReleaseDefinitionFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(RELEASE)) {
            return getReleaseFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(ENVIRONMENT)) {
            return getEnvironmentFieldValues(fieldName, properties);
        } else {
            return null;
        }
    }

    @Action(name = DEPLOY_RELEASE, displayName = "Deploy Release", description = "Deploy a Release to one or more Environments")
    @Params(params = {
        @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project field values", required = true, dataType = DataType.SELECT),
        @Param(fieldName = RELEASE_DEFINITION, displayName = "Release Definition", description = "Get TFS Release Definition field values", required = true, dataType = DataType.SELECT),
        @Param(fieldName = RELEASE, displayName = "Release", description = "Get TFS Release field values", required = true, dataType = DataType.SELECT),
        @Param(fieldName = ENVIRONMENT, displayName = "Environment", description = "Get TFS Environment field values", required = true, environmentProperty = true, deployUnit = false, dataType = DataType.SELECT)
    })
    public ExecutionInfo launchJob(String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + PROJECT);
        String projectId = field.getValue();
        String projectName = field.getDisplayValue();

        field = Field.getFieldByName(properties, RELEASE_DEFINITION);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + RELEASE_DEFINITION);
        String releaseDefinitionId = field.getValue();
        String releaseDefinitionName = field.getDisplayValue();

        field = Field.getFieldByName(properties, RELEASE);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + RELEASE);
        String releaseId = field.getValue();
        String releaseName = field.getDisplayValue();

        field = Field.getFieldByName(properties, ENVIRONMENT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + ENVIRONMENT);
        String environmentId = field.getValue();
        String environmentName = field.getDisplayValue();

        ExecutionInfo retVal = new ExecutionInfo();
        TFSClient client = new TFSClient(null, this.getTfsUrl(), this.getTfsApiVersion(),
                this.getVsrmUrl(), this.getVsrmApiVersion(), this.getTfsCollection(),
                this.getServiceUser(), this.getServicePassword());

        String deployStatus = null;
        try {
            logger.debug("Deploying release id: {} to environment id {}: " + releaseId, environmentId);
            client.deployRelease(projectId, releaseId, environmentId);
            deployStatus = client.getReleaseEnvironmentStatus(projectId, releaseId, environmentId);
            retVal.setExecutionUrl(this.getTfsUrl() + "/" + this.getTfsCollection() + "/" +
                    projectId + "/_apps/hub/ms.vss-releaseManagement-web.hub-explorer?definitionId=" + releaseDefinitionId +
                    "&_a=release-summary&releaseId=" + releaseId);
            retVal.setMessage(String.format("Release: %s, Environment %s", releaseId, environmentId));
            retVal.setExecutionId("vsrm-"+releaseId+"-"+environmentId);
            if (deployStatus != null) {
                if (Boolean.parseBoolean(getWaitForCallback())) {
                    retVal.setStatus(ExecutionStatus.PENDING);
                } else {
                    retVal.setStatus(ExecutionStatus.COMPLETED);
                }
            }
            else {
                retVal.setMessage("Unable to start deployment of Release.");
                retVal.setStatus(ExecutionStatus.FAILED);
            }
        } catch (TFSClientException ex) {
            logger.error("Error starting deployment of Release: {}", ex.getMessage());
            retVal.setStatus(ExecutionStatus.FAILED);
            retVal.setExecutionUrl(this.getTfsUrl() + "/" + this.getTfsCollection() + "/" +
                    projectId + "/_apps/hub/ms.vss-releaseManagement-web.hub-explorer?definitionId=" + releaseDefinitionId +
                    "&_a=release-summary&releaseId=" + releaseId);
            retVal.setMessage("Error starting deployment of Release");
        }

        return retVal;
    }

/*    private boolean matchesJobFilter(String jobName) {
        String pattern = getJobTemplateFilter();

        if (pattern == null || StringUtils.isEmpty(pattern)) {
            return true;
        } else {
            return jobName.matches(pattern.replace("?", ".?").replace("*", ".*?"));
        }
    }
*/

    @Getter(name = PROJECT, displayName = "Project", description = "Get TFS project field values.")
    public FieldInfo getProjectFieldValues(String fieldName, List<Field> properties) throws ProviderException {
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

    @Getter(name = RELEASE_DEFINITION, displayName = "Release Definition", description = "Get TFS Release Definition field values.")
    @Params(params = {@Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project field values", required = true, dataType = DataType.SELECT)
    })
    public FieldInfo getReleaseDefinitionFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setTFSClientConnectionDetails();

        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + PROJECT);

        String projectId = field.getValue();

        try {
            List<ReleaseDefinition> tfsReleaseDefs = tfsClient.getReleaseDefinitions(projectId);
            if (tfsReleaseDefs == null || tfsReleaseDefs.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (ReleaseDefinition tfsRds : tfsReleaseDefs) {

                value = new FieldValueInfo(tfsRds.getId().toString(), tfsRds.getTitle());
                if (tfsRds.getId() == null || StringUtils.isEmpty(tfsRds.getId().toString())) {
                    value.setId(tfsRds.getTitle());
                }

                value.setDescription(tfsRds.getTitle());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Release Definitions: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }
    }

    @Getter(name = RELEASE, displayName = "Release", description = "Get TFS Release field values.")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project field values", required = true, dataType = DataType.SELECT),
            @Param(fieldName = RELEASE_DEFINITION, displayName = "Release Definition", description = "Get TFS Release Definition field values", required = true, dataType = DataType.SELECT)
    })
    public FieldInfo getReleaseFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setTFSClientConnectionDetails();

        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + PROJECT);

        String projectId = field.getValue();

        field = Field.getFieldByName(properties, RELEASE_DEFINITION);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + RELEASE_DEFINITION);

        String releaseDefinitionId = field.getValue();

        try {
            List<Release> tfsReleases = tfsClient.getReleases(projectId, releaseDefinitionId);
            if (tfsReleases == null || tfsReleases.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (Release tfsRel : tfsReleases) {

                value = new FieldValueInfo(tfsRel.getId().toString(), tfsRel.getTitle());
                if (tfsRel.getId() == null || StringUtils.isEmpty(tfsRel.getId().toString())) {
                    value.setId(tfsRel.getTitle());
                }

                value.setDescription(tfsRel.getTitle());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Releases: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }
    }

    @Getter(name = ENVIRONMENT, displayName = "Environment", description = "Get TFS Release Environmentfield values.")
    @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project field values", required = true, dataType = DataType.SELECT),
            @Param(fieldName = RELEASE, displayName = "Release", description = "Get TFS Release field values", required = true, dataType = DataType.SELECT)
    })
    public FieldInfo getEnvironmentFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setTFSClientConnectionDetails();

        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + PROJECT);

        String projectId = field.getValue();

        field = Field.getFieldByName(properties, RELEASE);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + RELEASE);

        String releaseId = field.getValue();

        try {
            List<Environment> tfsRelEnvs = tfsClient.getRelease(projectId, releaseId).getEnvironments();
            if (tfsRelEnvs == null || tfsRelEnvs.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (Environment tfsEnv : tfsRelEnvs) {

                value = new FieldValueInfo(tfsEnv.getId().toString(), tfsEnv.getTitle());
                if (tfsEnv.getId() == null || StringUtils.isEmpty(tfsEnv.getId().toString())) {
                    value.setId(tfsEnv.getTitle());
                }

                value.setDescription(tfsEnv.getTitle());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Environments: {}", ex.getLocalizedMessage());
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
        getTFSClient().createConnection(getSession(), getTfsUrl(), getTfsApiVersion(), getVsrmUrl(), getVsrmApiVersion(), getTfsCollection(), getServiceUser(), getServicePassword());
    }
}
