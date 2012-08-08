package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.compat.backendcompat.Path;

public class OVirtUpgrader extends VdsInstaller {

    private final String runUpgradeCommand = Config.<String> GetValue(ConfigValues.oVirtUpgradeScriptName);
    private String _oVirtISOFile;

    public OVirtUpgrader(VDS vds, String oVirtISOFile) // Call base constructor
                                                        // with null password
                                                        // (because we are using
                                                        // know public key)
    {
        super(vds, null, false);
        _oVirtISOFile = oVirtISOFile;
        _translatedMessages.put(VdsInstallStages.UploadScript, "Upload upgrade ISO to oVirt Node");
        _translatedMessages.put(VdsInstallStages.RunScript, "Running upgrade / reinstall script on oVirt Node");

    }

    @Override
    protected void RunStage() {
        switch (_currentInstallStage) {
        case Start: {
            log.infoFormat("Installation of {0}. Executing oVirt reinstall/upgrade stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            super.RunStage();
            break;
        }
            // Use connect method which does not need password
            // (relies on public key existing on cbc image)
        case ConnectToServer: {
            log.infoFormat("Installation of {0}. Executing oVirt reinstall/upgrade stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            _executionSucceded = _wrapper.connect(_serverName);
            break;
        }
        case UploadScript:
            log.infoFormat("Installation of {0}. Executing oVirt reinstall/upgrade stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            String path = Path.Combine(Config.resolveOVirtISOsRepositoryPath(), _oVirtISOFile);
            _executionSucceded = _wrapper.sendFile(path, Config.<String> GetValue(ConfigValues.oVirtUploadPath));
            break;

        case RunScript:
            log.infoFormat("Installation of {0}. Executing oVirt reinstall/upgrade stage. (Stage: {1})", _serverName,
                    getCurrentInstallStage());
            _wrapper.executeCommand(runUpgradeCommand);
            break;
        // Skip unused states
        default: {
            _executionSucceded = true;
            _currentInstallStage = VdsInstallStages.forValue(_currentInstallStage.getValue() + 1);
            break;
        }
        }
    }

    private static Log log = LogFactory.getLog(OVirtUpgrader.class);
}
