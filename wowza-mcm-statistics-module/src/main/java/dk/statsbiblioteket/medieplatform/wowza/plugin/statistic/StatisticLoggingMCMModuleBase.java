package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;

import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.mcm.MCMPortalInterfaceStatisticsImpl;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.mcm.StreamingMCMEventLogger;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class StatisticLoggingMCMModuleBase extends ModuleBase implements IModuleOnApp, IModuleOnStream {

    private static final String PLUGIN_NAME = "CHAOS Wowza plugin - Statistics MCM";
       private static final String PLUGIN_VERSION = "${project.version}";

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    private static final String PROPERTY_GENERAL_MCM_SERVER_URL = "GeneralMCMServerURL";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_SESSION
            = "StatisticsLoggingMCMStatisticsMethodCreateStatSession";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CLIENT_SETTING_ID
            = "StatisticsLoggingMCMValueClientSettingID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_REPOSITORY_ID
            = "StatisticsLoggingMCMValueRepositoryID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_OBJECT_SESSION
            = "StatisticsLoggingMCMStatisticsMethodCreateStatObjectSession";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TYPE_ID
            = "StatisticsLoggingMCMValueObjectTypeID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_TYPE_ID
            = "StatisticsLoggingMCMValueChannelTypeID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_IDENTIFIER
            = "StatisticsLoggingMCMValueChannelIdentifier";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TITLE
            = "StatisticsLoggingMCMValueObjectTitle";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_EVENT_TYPE_ID
            = "StatisticsLoggingMCMValueEventTypeID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_COLLECTION_ID
            = "StatisticsLoggingMCMValueObjectCollectionID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_DURATION_SESSION
            = "StatisticsLoggingMCMStatisticsMethodCreateDurationSession";

    public StatisticLoggingMCMModuleBase() {
        super();
    }

    @Override
    public void onAppStart(IApplicationInstance appInstance) {
        String appName = appInstance.getApplication().getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger()
                .info("***Entered onAppStart: " + appName + "\n  Plugin: " + PLUGIN_NAME + " version " + PLUGIN_VERSION
                              + "\n  VHost home path: " + vhostDir + " VHost storage dir: " + storageDir);
        try {
            //Initialise the config reader
            ConfigReader cr;
            cr = new ConfigReader(new File(vhostDir + "/conf/" + appName + "/wowza-modules.properties"),
                                  PROPERTY_GENERAL_MCM_SERVER_URL,
                                  PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_SESSION,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CLIENT_SETTING_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_REPOSITORY_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_OBJECT_SESSION,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TYPE_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_TYPE_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_IDENTIFIER,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TITLE,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_EVENT_TYPE_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_COLLECTION_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_DURATION_SESSION);

            //Read parameters
            String mcmConnectionURLString = cr.get(PROPERTY_GENERAL_MCM_SERVER_URL);
            String mcmStatisticsMethodCreateStatSession = cr
                    .get(PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_SESSION);
            String clientSettingID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CLIENT_SETTING_ID);
            String repositoryID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_REPOSITORY_ID);
            String mcmStatisticsMethodCreateStatObjectSession = cr
                    .get(PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_OBJECT_SESSION);
            String objectTypeID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TYPE_ID);
            String channelTypeID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_TYPE_ID);
            String channelIdentifier = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_IDENTIFIER);
            String objectTitle = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TITLE);
            String eventTypeID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_EVENT_TYPE_ID);
            String objectCollectionID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_COLLECTION_ID);
            String mcmStatisticsMethodCreateDurationSession = cr
                    .get(PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_DURATION_SESSION);
            if (StreamingMCMEventLogger.getInstance() == null) {
                StreamingMCMEventLogger.createInstance(getLogger());
            }
            if (MCMPortalInterfaceStatisticsImpl.getInstance() == null) {
                MCMPortalInterfaceStatisticsImpl
                        .createInstance(getLogger(), appInstance.getVHost().getHomePath(), mcmConnectionURLString,
                                        mcmStatisticsMethodCreateStatSession, clientSettingID, repositoryID,
                                        mcmStatisticsMethodCreateStatObjectSession, objectTypeID, channelTypeID,
                                        channelIdentifier, objectTitle, eventTypeID, objectCollectionID,
                                        mcmStatisticsMethodCreateDurationSession);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize StreamingDatabaseEventLogger.", e);
        }
    }

    @Override
    public void onAppStop(IApplicationInstance appInstance) {
        getLogger().info("onAppStop: " + PLUGIN_NAME + " version " + PLUGIN_VERSION);
    }

    @Override
    public void onStreamCreate(IMediaStream stream) {
        getLogger().info("onStreamCreate by: " + stream.getClientId());
        String queryString = String.valueOf(stream.getClient().getQueryStr());
        String statisticsParameter;

        //Check if statistics are turned off
        try {
            statisticsParameter = StringAndTextUtil.extractValueFromQueryStringAndKey("statistics", queryString);
            if (statisticsParameter.equalsIgnoreCase("off")) {
                return;
            }
        } catch (IllegallyFormattedQueryStringException e) {
            //Not turned off, so ignore
        }

        IMediaStreamActionNotify streamActionNotify = new StatisticLoggingStreamListener(getLogger(), stream,
                                                                                         StreamingMCMEventLogger
                                                                                                 .getInstance());
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            props.put("streamActionNotifierForStatistics", streamActionNotify);
        }
        stream.addClientListener(streamActionNotify);
    }

    @Override
    public void onStreamDestroy(IMediaStream stream) {
        getLogger().info("onStreamDestroy by: " + stream.getClientId());
        IMediaStreamActionNotify actionNotify = null;
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            actionNotify = (IMediaStreamActionNotify) stream.getProperties().get("streamActionNotifierForStatistics");
        }
        if (actionNotify != null) {
            stream.removeClientListener(actionNotify);
            getLogger().info("removeClientListener: " + stream.getSrc());
        }
    }
}
