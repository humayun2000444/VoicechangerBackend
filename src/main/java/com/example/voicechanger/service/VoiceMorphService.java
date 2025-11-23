package com.example.voicechanger.service;

import com.example.voicechanger.service.esl.CallHandlerService;
import com.example.voicechanger.service.esl.CallTransferService;
import org.springframework.stereotype.Service;

@Service
public class VoiceMorphService {
    private final CallTransferService callTransferService;
    private final CallHandlerService callHandlerService;

    public VoiceMorphService(CallTransferService callTransferService, CallHandlerService callHandlerService) {
        this.callTransferService = callTransferService;
        this.callHandlerService = callHandlerService;
    }


    public String setVoiceByEmail(String email, String code) {
        try {
            // Find B-Leg UUID from active bridges using event headers
            String bLegUuid = callHandlerService.findBLegUuidByEmail(email);

            if (bLegUuid == null) {
                return "No active call found for email: " + email;
            }

            return executeVoiceCommand(bLegUuid, code);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String executeVoiceCommand(String bLegUuid, String code) {
        try {
            String cmd;
            switch (code) {
                case "901": // female
                    callTransferService.setVoiceChangerParams(bLegUuid, "10", "2", "100");

                    break;
                case "902": // monster
                    callTransferService.setVoiceChangerParams(bLegUuid, "-15", "-4", "300");
                    break;
                case "903": // child
                    callTransferService.setVoiceChangerParams(bLegUuid, "8", "4", "120");
                    break;
                case "904": // stop
                    callTransferService.setVoiceChangerParams(bLegUuid, "1", "0", "0");
                    break;
                default:
                    return "Invalid code: " + code;
            }

            return "✅ Voice morph applied for B-leg UUID: " + bLegUuid + " (code " + code + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing voice morph: " + e.getMessage();
        }
    }
}