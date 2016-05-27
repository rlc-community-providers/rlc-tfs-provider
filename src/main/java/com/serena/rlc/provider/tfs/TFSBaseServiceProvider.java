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

import com.serena.rlc.provider.annotations.ConfigProperty;
import com.serena.rlc.provider.annotations.Getter;
import com.serena.rlc.provider.annotations.Param;
import com.serena.rlc.provider.annotations.Params;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.spi.IBaseServiceProvider;
import com.serena.rlc.provider.tfs.client.TFSClient;
import com.serena.rlc.provider.tfs.domain.*;
import com.serena.rlc.provider.tfs.exception.TFSClientException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * TFS Base Service Provider
 * @author klee@serena.com
 */
public abstract class TFSBaseServiceProvider implements IBaseServiceProvider {

    final static Logger logger = LoggerFactory.getLogger(TFSBaseServiceProvider.class);

    final static String PROJECT = "project";
    final static String QUERY = "query";
    final static String RELEASE_DEFINITION = "releaseDefinition";
    final static String RELEASE = "release";
    final static String ENVIRONMENT = "environment";
    final static String ENVIRONMENT_USERNAME = "environmentUsername";
    final static String ENVIRONMENT_PASSWORD = "environmentPassword";
    final static String BUILD_DEFINITION = "buildDefinition";
    final static String BUILD_QUEUE = "buildQueue";
    final static String NAME_FILTER = "nameFilter";
    final static String QUEUE_TYPE = "queueType";
    final static String BUILD = "build";

    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================

    @ConfigProperty(name = "tfs_url", displayName = "TFS URL",
            description = "TFS Server URL.",
            defaultValue = "http://<servername>",
            dataType = DataType.TEXT)
    private String tfsUrl;

    @ConfigProperty(name = "tfs_api_version", displayName = "TFS API Version",
            description = "TFS API Version.",
            defaultValue = "1.0",
            dataType = DataType.TEXT)
    private String tfsApiVersion;

    @ConfigProperty(name = "tfs_build_api_version", displayName = "Build API Version",
            description = "TFS Build API Version.",
            defaultValue = "2.0",
            dataType = DataType.TEXT)
    private String tfsBuildApiVersion;

    @ConfigProperty(name = "tfs_collection", displayName = "TFS Collection",
            description = "TFS Collection.",
            defaultValue = "DefaultCollection",
            dataType = DataType.TEXT)
    private String tfsCollection;

    @ConfigProperty(name = "tfs_serviceuser", displayName = "User Name",
            description = "TFS service username.",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String serviceUser;

    @ConfigProperty(name = "tfs_servicepassword", displayName = "Password/Token",
            description = "TFS service password/token",
            defaultValue = "",
            dataType = DataType.PASSWORD)
    private String servicePassword;

    private SessionData session;
    private Long providerId;
    private String providerUuid;
    private String providerNamespaceId;

    @Autowired
    TFSClient tfsClient;

    public SessionData getSession() {
        return session;
    }

    @Override
    public void setSession(SessionData session) {
        this.session = null;
    }

    @Override
    public Long getProviderId() {
        return providerId;
    }

    @Override
    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    @Override
    public String getProviderNamespaceId() {
        return providerNamespaceId;
    }

    @Override
    public void setProviderNamespaceId(String providerNamespaceId) {
        this.providerNamespaceId = providerNamespaceId;
    }

    @Override
    public String getProviderUuid() {
        return providerUuid;
    }

    @Override
    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
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

    //================================================================================
    // Getter Methods
    // -------------------------------------------------------------------------------
    // These methods are used to get the field values. The @Getter annotation is used
    // by the system to generate a user interface and pass the correct parameters to
    // to the provider
    //================================================================================

    @Getter(name = PROJECT, displayName = "Project", description = "Get TFS project.")
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

                value = new FieldValueInfo(tfsProj.getId(), tfsProj.getTitle());
                if (tfsProj.getId() == null || StringUtils.isEmpty(tfsProj.getId())) {
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

    @Getter(name = QUERY, displayName = "Query", description = "Get TFS Work Item Query.")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project", required = true, dataType = DataType.SELECT)
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

                value = new FieldValueInfo(tfsQuery.getId(), tfsQuery.getTitle());
                if (tfsQuery.getId() == null || StringUtils.isEmpty(tfsQuery.getId())) {
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

    @Getter(name = RELEASE_DEFINITION, displayName = "Release Definition", description = "Get TFS Release Definition.")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project", required = true, dataType = DataType.SELECT)
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

                value = new FieldValueInfo(tfsRds.getId(), tfsRds.getTitle());
                if (tfsRds.getId() == null || StringUtils.isEmpty(tfsRds.getId())) {
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

    @Getter(name = RELEASE, displayName = "Release", description = "Get TFS Release.")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project", required = true, dataType = DataType.SELECT),
            @Param(fieldName = RELEASE_DEFINITION, displayName = "Release Definition", description = "Get TFS Release Definition", required = true, dataType = DataType.SELECT)
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

                value = new FieldValueInfo(tfsRel.getId(), tfsRel.getTitle());
                if (tfsRel.getId() == null || StringUtils.isEmpty(tfsRel.getId())) {
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

    @Getter(name = ENVIRONMENT, displayName = "Environment", description = "Get TFS Release Environment.")
        @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project", required = true, dataType = DataType.SELECT),
            @Param(fieldName = RELEASE, displayName = "Release", description = "Get TFS Release", required = true, dataType = DataType.SELECT)
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

                value = new FieldValueInfo(tfsEnv.getId(), tfsEnv.getTitle());
                if (tfsEnv.getId() == null || StringUtils.isEmpty(tfsEnv.getId())) {
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

    @Getter(name = BUILD_DEFINITION, displayName = "Build Definition", description = "Get TFS Build Definition.")
    @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "Get TFS Project field", required = true, dataType = DataType.SELECT),
    })
    public FieldInfo getBuildDefinitionFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setTFSClientConnectionDetails();

        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null || StringUtils.isEmpty(field.getValue()))
            throw new ProviderException("Missing required property: " + PROJECT);

        String projectId = field.getValue();

        String startsWith = "";
/*        field = Field.getFieldByName(properties, NAME_FILTER);
        if (field != null || StringUtils.isNotEmpty(field.getValue())) {
            startsWith = field.getValue();
        }
*/
        try {
            List<BuildDefinition> tfsBuildDefs = tfsClient.getBuildDefinitions(projectId, startsWith);
            if (tfsBuildDefs == null || tfsBuildDefs.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (BuildDefinition tfsBds : tfsBuildDefs) {

                value = new FieldValueInfo(tfsBds.getId(), tfsBds.getTitle());
                if (tfsBds.getId() == null || StringUtils.isEmpty(tfsBds.getId())) {
                    value.setId(tfsBds.getTitle());
                }

                value.setDescription(tfsBds.getTitle());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Build Definitions: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }
    }

    @Getter(name = BUILD_QUEUE, displayName = "Build Queue", description = "Get TFS Build Queue field values.")
    @Params(params = {
//            @Param(fieldName = QUEUE_TYPE, displayName = "Queue Type", description = "Get TFS Build Queue Type", required = true, dataType = DataType.SELECT),
//            @Param(fieldName = NAME_FILTER, displayName = "Name Filter", description = "Name the Definition starts with", required = false, dataType = DataType.TEXT)
    })
    public FieldInfo getBuildQueueFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setTFSClientConnectionDetails();

        // TODO: allow user to select queue type
        String queueType = "";
        Field field = Field.getFieldByName(properties, QUEUE_TYPE);
        if (field != null && StringUtils.isNotEmpty(field.getValue()))
            queueType = field.getValue();

        // TODO: allow user to filter queue type by name
        String startsWith = "";
        //field = Field.getFieldByName(properties, NAME_FILTER);
        //if (field != null || StringUtils.isNotEmpty(field.getValue())) {
        //    startsWith = field.getValue();
        //}

        try {
            List<BuildQueue> tfsBuildQueues = tfsClient.getBuildQueues(queueType, startsWith);
            if (tfsBuildQueues == null || tfsBuildQueues.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (BuildQueue tfsBqs : tfsBuildQueues) {

                value = new FieldValueInfo(tfsBqs.getId(), tfsBqs.getTitle());
                if (tfsBqs.getId() == null || StringUtils.isEmpty(tfsBqs.getId())) {
                    value.setId(tfsBqs.getTitle());
                }

                value.setDescription(tfsBqs.getTitle());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (TFSClientException ex) {
            logger.error("Unable to retrieve TFS Build Queues: {}", ex.getLocalizedMessage());
            throw new ProviderException(ex.getLocalizedMessage());
        }
    }

    @Getter(name = QUEUE_TYPE, displayName = "Queue Type", description = "Get Build Queue Type.")
    public FieldInfo getQueueTypeFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        //TODO: queueTypes in provider configuration
        String queueTypes = "buildController,agentPool";
        if (StringUtils.isEmpty(queueTypes)) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(queueTypes, ",;");
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value;
        String qType;
        while (st.hasMoreElements()) {
            qType = (String) st.nextElement();
            qType = qType.trim();
            value = new FieldValueInfo(qType, qType);
            values.add(value);
        }

        fieldInfo.setValues(values);
        return fieldInfo;
    }

    //================================================================================
    // Additional Public Methods
    //================================================================================

    public TFSClient getTFSClient() {
        if (tfsClient == null) {
            tfsClient = new TFSClient();
        }

        return tfsClient;
    }

    public void addField(List<Field> fieldCollection, String fieldName, String fieldDisplayName, String fieldValue) {
        if (StringUtils.isNotEmpty(fieldValue)) {
            Field field = new Field(fieldName, fieldDisplayName);
            field.setValue(fieldValue);
            fieldCollection.add(field);
        }
    }

    public void setTFSClientConnectionDetails() {
        getTFSClient().createConnection(getSession(), getTfsUrl(), getTfsApiVersion(), null, null, null, getTfsCollection(), getServiceUser(), getServicePassword());
    }

}
