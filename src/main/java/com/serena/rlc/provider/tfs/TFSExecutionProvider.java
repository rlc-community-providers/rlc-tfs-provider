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
import com.serena.rlc.provider.spi.IExecutionProvider;
import com.serena.rlc.provider.tfs.client.TFSClient;
import com.serena.rlc.provider.tfs.domain.Build;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * TFS Execution Provider
 * @author klee@serena.com
 */
public class TFSExecutionProvider extends TFSBaseServiceProvider implements IExecutionProvider {

    final static Logger logger = LoggerFactory.getLogger(TFSExecutionProvider.class);

    final static String DEPLOY_RELEASE = "deployRelease";
    final static String QUEUE_BUILD = "queueBuild";

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================

    @ConfigProperty(name = "execution_provider_name", displayName = "Execution Provider Name",
            description = "provider name",
            defaultValue = "TFS Execution Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "execution_provider_description", displayName = "Execution Provider Description",
            description = "provider description",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String providerDescription;

    @ConfigProperty(name = "vsrm_url", displayName = "Release Management URL",
            description = "TFS Release Management Server URL.",
            defaultValue = "http://<servername>.vsrm",
            dataType = DataType.TEXT)
    private String vsrmUrl;

    @ConfigProperty(name = "vsrm_api_version", displayName = "Release Management API Version",
            description = "TFS Release Management API Version.",
            defaultValue = "3.0-preview.1",
            dataType = DataType.TEXT)
    private String vsrmApiVersion;

    @ConfigProperty(name = "execution_action_wait_for_callback", displayName = "Wait for Callback",
            description = "Set this to false to set the execution status to Completed when the action is executed.",
            defaultValue = "false",
            dataType = DataType.TEXT)
    private String waitForCallback;

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
            this.vsrmApiVersion = vsrmApiVersion.trim();
        }
        else {
            this.vsrmApiVersion = "3.0-preview.1";
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

    //================================================================================
    // Services Methods
    // -------------------------------------------------------------------------------
    //================================================================================

    @Override
    @Service(name = EXECUTE, displayName = "Execute", description = "Execute TFS action.")
        @Params(params = {
            @Param(fieldName = ACTION, description = "TFS action to execute", required = true, dataType = DataType.SELECT),
            @Param(fieldName = PROPERTIES, description = "TFS action properties", required = true)
    })
    public ExecutionInfo execute(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        if (action.equalsIgnoreCase(DEPLOY_RELEASE))
            return deployRelease(properties, false);
        else if (action.equalsIgnoreCase(QUEUE_BUILD))
            return queueBuild(properties, false);

        throw new ProviderException("Unsupported execution action: " + action);
    }

    @Service(name = VALIDATE, displayName = "Validate", description = "Validate")
        @Params(params = {
            @Param(fieldName = ACTION, displayName = "Action", description = "TFS action to validate", required = true, dataType = DataType.SELECT),
            @Param(fieldName = PROPERTIES, displayName = "Properties", description = "TFS action properties", required = true)
    })
    @Override
    public ExecutionInfo validate(String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        if (action.equalsIgnoreCase(DEPLOY_RELEASE))
            return deployRelease(properties, true);
        else if (action.equalsIgnoreCase(QUEUE_BUILD))
            return queueBuild(properties, true);

        throw new ProviderException("Unsupported execution action: " + action);
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
        } else if (fieldName.equalsIgnoreCase(BUILD_DEFINITION)) {
            return getBuildDefinitionFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(BUILD_QUEUE)) {
            return getBuildQueueFieldValues(fieldName, properties);
        } else if (fieldName.equalsIgnoreCase(QUEUE_TYPE)) {
            return getQueueTypeFieldValues(fieldName, properties);
        } else {
            return null;
        }
    }

    //================================================================================
    // Action Methods
    // -------------------------------------------------------------------------------
    //================================================================================

    @Action(name = DEPLOY_RELEASE, displayName = "Deploy Release", description = "Deploy a Release to one or more Environments")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "TFS Project", required = true, dataType = DataType.SELECT),
            @Param(fieldName = RELEASE_DEFINITION, displayName = "Release Definition", description = "TFS Release Definition", required = true, dataType = DataType.SELECT),
            @Param(fieldName = RELEASE, displayName = "Release", description = "TFS Release", required = true, dataType = DataType.SELECT),
            @Param(fieldName = ENVIRONMENT, displayName = "Environment", description = "TFS Environment mapping", required = true, environmentProperty = true, deployUnit = false, dataType = DataType.SELECT)
        })
    public ExecutionInfo deployRelease(List<Field> properties, Boolean validateOnly) throws ProviderException {
        ExecutionInfo retVal = new ExecutionInfo();

        Boolean bValid = true; // TODO: carry out validation
        if (validateOnly) {
            retVal.setSuccess(bValid);
            retVal.setMessage("Valid TFS Execution action: " + DEPLOY_RELEASE);
            return retVal;
        }

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

/*        field = Field.getFieldByName(properties, ENVIRONMENT_USERNAME);
        String envUsername = field.getValue();
        field = Field.getFieldByName(properties, ENVIRONMENT_PASSWORD);
        String envPassword = field.getValue();
*/
        setTFSClientConnectionDetails();
/*        if (envUsername != null && StringUtils.isNotEmpty(envUsername)) {
            client.setTFSUsername(envUsername);
            client.setTFSPassword(envPassword);
        }
*/
        String deployStatus = null;
        try {
            logger.debug("Deploying Release: {} to Environment: {} " + releaseName, environmentName);
            getTFSClient().deployRelease(projectId, releaseId, environmentId);
            deployStatus = getTFSClient().getReleaseEnvironmentStatus(projectId, releaseId, environmentId);
            retVal.setExecutionUrl(this.getTfsUrl() + "/" + this.getTfsCollection() + "/" +
                    projectId + "/_apps/hub/ms.vss-releaseManagement-web.hub-explorer?definitionId=" + releaseDefinitionId +
                    "&_a=release-summary&releaseId=" + releaseId);
            retVal.setMessage(String.format("Release: %s, Environment %s", releaseName, environmentName));
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

    @Action(name = QUEUE_BUILD, displayName = "Queue Build", description = "Queue a TFS Build")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "TFS Project", required = true, dataType = DataType.SELECT),
            @Param(fieldName = BUILD_DEFINITION, displayName = "Build Definition", description = "TFS Build Definition", required = true, dataType = DataType.SELECT),
            @Param(fieldName = BUILD_QUEUE, displayName = "Build Queue", description = "TFS Build Queue", required = true, dataType = DataType.SELECT)
        })
    public ExecutionInfo queueBuild(List<Field> properties, Boolean validateOnly) throws ProviderException {
        ExecutionInfo retVal = new ExecutionInfo();

        Boolean bValid = true; // TODO: carry out validation
        if (validateOnly) {
            retVal.setSuccess(bValid);
            retVal.setMessage("Valid TFS Execution action: " + QUEUE_BUILD);
            return retVal;
        }

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

        field = Field.getFieldByName(properties, BUILD_QUEUE);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + BUILD_QUEUE);
        String buildQueueId = field.getValue();
        String buildQueueName = field.getDisplayValue();

/*        field = Field.getFieldByName(properties, ENVIRONMENT_USERNAME);
        String envUsername = field.getValue();
        field = Field.getFieldByName(properties, ENVIRONMENT_PASSWORD);
        String envPassword = field.getValue();
*/
        setTFSClientConnectionDetails();
/*        if (envUsername != null && StringUtils.isNotEmpty(envUsername)) {
            client.setTFSUsername(envUsername);
            client.setTFSPassword(envPassword);
        }
*/
        Build queuedBuild;
        String buildStatus = null;
        try {
            logger.debug("Queueing Build for Definition: {} using Queue: {} " + buildDefinitionName, buildQueueName);
            queuedBuild = getTFSClient().queueBuild(projectId, buildDefinitionId, buildQueueId, null);
            buildStatus = queuedBuild.getState();
            retVal.setExecutionUrl(this.getTfsUrl() + "/" + this.getTfsCollection() + "/" +
                    projectId + "/_build?buildId=" + queuedBuild.getId() + "&_a=summary");
            retVal.setMessage(String.format("Build: %s, Build Queue %s", queuedBuild.getBuildNumber(), buildQueueName));
            retVal.setExecutionId("tfsbuild-"+queuedBuild.getBuildNumber()+"-"+buildQueueId);
            if (buildStatus != null) {
                if (Boolean.parseBoolean(getWaitForCallback())) {
                    retVal.setStatus(ExecutionStatus.PENDING);
                } else {
                    retVal.setStatus(ExecutionStatus.COMPLETED);
                }
            }
            else {
                retVal.setMessage("Unable to queue Build.");
                retVal.setStatus(ExecutionStatus.FAILED);
            }
        } catch (TFSClientException ex) {
            logger.error("Error queuing Build: {}", ex.getMessage());
            retVal.setStatus(ExecutionStatus.FAILED);
            retVal.setExecutionUrl(this.getTfsUrl() + "/" + this.getTfsCollection() + "/" +
                    projectId + "/_build?definitionId=" + buildDefinitionId + "&_a=queued");
            retVal.setMessage("Error queueing Build");
        }

        return retVal;
    }

/*    private boolean matchesReleaseFilter(String releaseName) {
        String pattern = getReleaseDefinitionFilter();

        if (pattern == null || StringUtils.isEmpty(pattern)) {
            return true;
        } else {
            return releaseName.matches(pattern.replace("?", ".?").replace("*", ".*?"));
        }
    }
*/

    //

    @Override
    public void setTFSClientConnectionDetails() {
        getTFSClient().createConnection(getSession(), getTfsUrl(), getTfsApiVersion(), getVsrmUrl(), getVsrmApiVersion(), getTfsBuildApiVersion(), getTfsCollection(), getServiceUser(), getServicePassword());
    }

    @Override
    public ExecutionInfo cancelExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        return new ExecutionInfo("Cancellation not required", true);
    }

    @Override
    public ExecutionInfo retryExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        return execute(action, taskTitle, taskDescription, properties);
    }

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
    public ActionInfo getActionInfo(String action)
            throws ProviderException {
        return AnnotationUtil.getActionInfo(this.getClass(), action);
    }

    @Override
    public ActionInfoResult getActions()
            throws ProviderException {
        List<ActionInfo> actions = AnnotationUtil.getActions(this.getClass());
        return new ActionInfoResult(0, actions.size(), actions.toArray(new ActionInfo[actions.size()]));
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
