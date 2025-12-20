package com.example.voicechanger.service;

import com.example.voicechanger.exception.FreeSwitchConfigException;
import com.example.voicechanger.service.esl.EslService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreeSwitchConfigService {

    private final EslService eslService;

    @Value("${freeswitch.config.directory:/usr/local/freeswitch/conf/directory/default}")
    private String freeswitchConfigDir;

    @Value("${freeswitch.user.password:humayun200044}")
    private String defaultPassword;

    /**
     * Create FreeSWITCH user configuration XML file
     * @param username The username for the FreeSWITCH extension
     * @throws FreeSwitchConfigException if config creation fails
     */
    public void createUserConfig(String username) {
        try {
            // Validate username
            if (username == null || username.trim().isEmpty()) {
                log.error("Username cannot be null or empty");
                throw new FreeSwitchConfigException("Username cannot be null or empty");
            }

            // Sanitize username to prevent directory traversal
            String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9_-]", "");
            if (!sanitizedUsername.equals(username)) {
                log.warn("Username contains invalid characters. Sanitized: {} -> {}", username, sanitizedUsername);
            }

            // Create file path
            String filePath = freeswitchConfigDir + File.separator + sanitizedUsername + ".xml";
            File configFile = new File(filePath);

            // Check if directory exists
            File directory = new File(freeswitchConfigDir);
            if (!directory.exists()) {
                log.warn("FreeSWITCH config directory does not exist: {}", freeswitchConfigDir);
                log.info("Attempting to create directory: {}", freeswitchConfigDir);
                if (!directory.mkdirs()) {
                    log.error("Failed to create FreeSWITCH config directory: {}", freeswitchConfigDir);
                    throw new FreeSwitchConfigException(
                        "Failed to create FreeSWITCH config directory. Please ensure the directory exists and has proper permissions: " + freeswitchConfigDir
                    );
                }
            }

            // Check if file already exists
            if (configFile.exists()) {
                log.warn("FreeSWITCH config file already exists for username: {}", username);
                throw new FreeSwitchConfigException("FreeSWITCH configuration already exists for username: " + username);
            }

            // Generate XML content
            String xmlContent = generateUserXml(sanitizedUsername);

            // Write to file
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(xmlContent);
                log.info("FreeSWITCH config file created successfully: {}", filePath);
            } catch (Exception e) {
                log.error("Failed to write FreeSWITCH config file: {}", filePath, e);
                throw new FreeSwitchConfigException("Failed to write FreeSWITCH config file: " + e.getMessage(), e);
            }

            // Reload FreeSWITCH XML configuration
            boolean reloadSuccess = reloadFreeSwitchXml();
            if (reloadSuccess) {
                log.info("FreeSWITCH XML reloaded successfully for user: {}", username);
            } else {
                log.warn("FreeSWITCH XML reload failed for user: {} - manual reload may be required", username);
                // Don't fail registration if reload fails, just warn
            }

        } catch (FreeSwitchConfigException e) {
            // Re-throw FreeSwitchConfigException
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating FreeSWITCH config for user: {}", username, e);
            throw new FreeSwitchConfigException("Failed to create FreeSWITCH configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Generate XML content for FreeSWITCH user configuration
     * @param username The username
     * @return XML content as string
     */
    private String generateUserXml(String username) {
        return String.format(
                "<include>\n" +
                "  <user id=\"%s\">\n" +
                "    <params>\n" +
                "      <param name=\"password\" value=\"%s\"/>\n" +
                "      <param name=\"vm-password\" value=\"%s\"/>\n" +
                "    </params>\n" +
                "    <variables>\n" +
                "      <variable name=\"toll_allow\" value=\"domestic,international,local\"/>\n" +
                "      <variable name=\"accountcode\" value=\"%s\"/>\n" +
                "      <variable name=\"user_context\" value=\"default\"/>\n" +
                "      <variable name=\"effective_caller_id_name\" value=\"Extension %s\"/>\n" +
                "      <variable name=\"effective_caller_id_number\" value=\"%s\"/>\n" +
                "      <variable name=\"outbound_caller_id_name\" value=\"$${outbound_caller_name}\"/>\n" +
                "      <variable name=\"outbound_caller_id_number\" value=\"$${outbound_caller_id}\"/>\n" +
                "    </variables>\n" +
                "  </user>\n" +
                "</include>\n",
                username,      // user id
                defaultPassword, // password
                username,      // vm-password
                username,      // accountcode
                username,      // effective_caller_id_name
                username       // effective_caller_id_number
        );
    }

    /**
     * Reload FreeSWITCH XML configuration using ESL
     * @return true if successful, false otherwise
     */
    private boolean reloadFreeSwitchXml() {
        try {
            log.info("Sending reloadxml command to FreeSWITCH via ESL");
            String response = eslService.sendCommand("reloadxml");

            if (response != null && (response.contains("+OK") || response.contains("Success"))) {
                log.info("FreeSWITCH XML reloaded successfully via ESL");
                log.debug("FreeSWITCH response: {}", response.trim());
                return true;
            } else {
                log.error("FreeSWITCH reloadxml failed. Response: {}", response);
                return false;
            }

        } catch (Exception e) {
            log.error("Error executing FreeSWITCH reloadxml command via ESL", e);
            return false;
        }
    }

    /**
     * Delete FreeSWITCH user configuration file
     * @param username The username
     * @return true if successful, false otherwise
     */
    public boolean deleteUserConfig(String username) {
        try {
            String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9_-]", "");
            String filePath = freeswitchConfigDir + File.separator + sanitizedUsername + ".xml";
            File configFile = new File(filePath);

            if (!configFile.exists()) {
                log.warn("FreeSWITCH config file does not exist for username: {}", username);
                return false;
            }

            if (configFile.delete()) {
                log.info("FreeSWITCH config file deleted successfully: {}", filePath);
                reloadFreeSwitchXml();
                return true;
            } else {
                log.error("Failed to delete FreeSWITCH config file: {}", filePath);
                return false;
            }

        } catch (Exception e) {
            log.error("Error deleting FreeSWITCH config for user: {}", username, e);
            return false;
        }
    }
}
