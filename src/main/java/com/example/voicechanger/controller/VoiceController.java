package com.example.voicechanger.controller;


import com.example.voicechanger.dto.VoiceProcessRequest;
import com.example.voicechanger.exception.AudioProcessingException;
import com.example.voicechanger.exception.InvalidRequestException;
import com.example.voicechanger.service.VoiceProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api")
public class VoiceController {

    private static final Logger logger = LoggerFactory.getLogger(VoiceController.class);

    @Autowired
    private VoiceProcessingService voiceProcessingService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<byte[]> processAudio(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "shift", defaultValue = "10.0") double shift,
            @RequestParam(value = "formant", defaultValue = "2.0") double formant,
            @RequestParam(value = "base", defaultValue = "100.0") double base) {

        logger.info("Received /process request");
        logger.info("Shift: {}, Formant: {}, Base: {}", shift, formant, base);
        logger.info("Received audio file: name={}, size={} bytes, type={}",
                audioFile.getOriginalFilename(), audioFile.getSize(), audioFile.getContentType());

        // Validate audio file
        if (audioFile == null || audioFile.isEmpty()) {
            throw new InvalidRequestException("Audio file is required");
        }

        try {
            // Create request object
            VoiceProcessRequest request = new VoiceProcessRequest();
            request.setShift((float) shift);
            request.setFormant((float) formant);
            request.setBase((float) base);

            // Process audio
            byte[] processedAudio = voiceProcessingService.processAudio(audioFile.getBytes(), request);

            // Return processed audio
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "processed_audio.wav");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(processedAudio);

        } catch (Exception e) {
            logger.error("Error processing audio", e);
            throw new AudioProcessingException("Failed to process audio file: " + e.getMessage(), e);
        }
    }

    @PostMapping("/process-live")
    @ResponseBody
    public ResponseEntity<byte[]> processLiveAudio(
            @RequestBody byte[] audioData,
            @RequestParam(value = "shift", defaultValue = "10.0") double shift,
            @RequestParam(value = "formant", defaultValue = "2.0") double formant,
            @RequestParam(value = "base", defaultValue = "100.0") double base) {

        logger.info("Received live audio processing request");
        logger.info("Shift: {}, Formant: {}, Base: {}", shift, formant, base);

        // Validate audio data
        if (audioData == null || audioData.length == 0) {
            throw new InvalidRequestException("Audio data is required");
        }

        try {
            VoiceProcessRequest request = new VoiceProcessRequest();
            request.setShift((float) shift);
            request.setFormant((float) formant);
            request.setBase((float) base);

            byte[] processedAudio = voiceProcessingService.processAudio(audioData, request);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(processedAudio);

        } catch (Exception e) {
            logger.error("Error processing live audio", e);
            throw new AudioProcessingException("Failed to process live audio: " + e.getMessage(), e);
        }
    }

    @PostMapping("/voiceTest")
    @ResponseBody
    public ResponseEntity<byte[]> voiceTest(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("code") int code) {

        logger.info("Received /voiceTest request with code: {}", code);
        logger.info("Received audio file: name={}, size={} bytes, type={}",
                audioFile.getOriginalFilename(), audioFile.getSize(), audioFile.getContentType());

        // Validate audio file
        if (audioFile == null || audioFile.isEmpty()) {
            throw new InvalidRequestException("Audio file is required");
        }

        VoiceProcessRequest request;
        String transformationType;

        // Determine transformation based on code
        switch (code) {
            case 901:
                request = VoiceProcessRequest.maleToFemale();
                transformationType = "Male to Female";
                break;
            case 902:
                request = VoiceProcessRequest.femaleToMale();
                transformationType = "Female to Male";
                break;
            case 903:
                request = VoiceProcessRequest.robotVoice();
                transformationType = "Robot Voice";
                break;
            default:
                logger.error("Invalid code: {}. Valid codes are 901, 902, 903", code);
                throw new InvalidRequestException("Invalid transformation code. Valid codes are 901 (Male to Female), 902 (Female to Male), 903 (Robot Voice)");
        }

        logger.info("Applying transformation: {}", transformationType);

        try {
            // Process audio
            byte[] processedAudio = voiceProcessingService.processAudio(audioFile.getBytes(), request);

            // Return processed audio
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "processed_audio.wav");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(processedAudio);

        } catch (Exception e) {
            logger.error("Error processing audio in voiceTest", e);
            throw new AudioProcessingException("Failed to process audio with transformation code " + code + ": " + e.getMessage(), e);
        }
    }
}