package com.example.voicechanger.controller;

import com.example.voicechanger.service.VoiceMorphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoiceMorphController {

    private final VoiceMorphService voiceMorphService;

    public VoiceMorphController(VoiceMorphService voiceMorphService) {
        this.voiceMorphService = voiceMorphService;
    }

    @GetMapping("/set-voice-by-username")
    public String setVoiceByUsername(
            @RequestParam String username,
            @RequestParam String code
    ) {
        return voiceMorphService.setVoiceByUsername(username, code);
    }
}
