# Voice Changer API Documentation

## Overview
The Voice Changer API provides endpoints for real-time voice transformation with preset and custom parameters.

**Base URL:** `http://localhost:8080/api`

---

## Endpoints

### 1. Voice Test (Preset Transformations)

**Endpoint:** `/voiceTest`
**Method:** `POST`
**Content-Type:** `multipart/form-data`

#### Description
Transform audio files using preset voice transformation codes. This endpoint simplifies voice transformation by using predefined configurations.

#### Request Parameters

| Parameter | Type | Required | Location | Description |
|-----------|------|----------|----------|-------------|
| audio | File | Yes | Form Data | Audio file to be processed (WAV format recommended) |
| code | Integer | Yes | Query/Form Data | Transformation code (901, 902, or 903) |

#### Transformation Codes

| Code | Transformation | Parameters |
|------|---------------|------------|
| 901 | Male to Female | shift: 10.0, formant: 2.0, base: 100.0 |
| 902 | Female to Male | shift: -15.0, formant: -4.0, base: 300.0 |
| 903 | Robot Voice | shift: 0.8, formant: 4.0, base: 120.0 |

#### Request Example

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/voiceTest?code=901" \
  -F "audio=@/path/to/audio.wav" \
  --output processed_audio.wav
```

**JavaScript (Fetch API):**
```javascript
const formData = new FormData();
formData.append('audio', audioFile);

fetch('http://localhost:8080/api/voiceTest?code=901', {
  method: 'POST',
  body: formData
})
.then(response => response.blob())
.then(blob => {
  // Handle the processed audio
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'processed_audio.wav';
  a.click();
});
```

**Python (requests):**
```python
import requests

url = "http://localhost:8080/api/voiceTest"
files = {'audio': open('audio.wav', 'rb')}
params = {'code': 901}

response = requests.post(url, files=files, params=params)

if response.status_code == 200:
    with open('processed_audio.wav', 'wb') as f:
        f.write(response.content)
```

#### Response

**Success (200 OK):**
- **Content-Type:** `application/octet-stream`
- **Body:** Binary audio data (WAV format)
- **Headers:** `Content-Disposition: attachment; filename="processed_audio.wav"`

**Error Responses:**

| Status Code | Description |
|-------------|-------------|
| 400 Bad Request | Invalid code parameter (not 901, 902, or 903) |
| 500 Internal Server Error | Audio processing failed |

#### Example Usage by Code

##### Male to Female (Code 901)
```bash
curl -X POST "http://localhost:8080/api/voiceTest?code=901" \
  -F "audio=@male_voice.wav" \
  --output female_voice.wav
```

##### Female to Male (Code 902)
```bash
curl -X POST "http://localhost:8080/api/voiceTest?code=902" \
  -F "audio=@female_voice.wav" \
  --output male_voice.wav
```

##### Robot Voice (Code 903)
```bash
curl -X POST "http://localhost:8080/api/voiceTest?code=903" \
  -F "audio=@original_voice.wav" \
  --output robot_voice.wav
```

---

### 2. Process Audio (Custom Parameters)

**Endpoint:** `/process`
**Method:** `POST`
**Content-Type:** `multipart/form-data`

#### Description
Process audio files with custom transformation parameters for fine-tuned voice modification.

#### Request Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| audio | File | Yes | - | Audio file to be processed |
| shift | Double | No | 10.0 | Pitch shift value |
| formant | Double | No | 2.0 | Formant shift value |
| base | Double | No | 100.0 | Base frequency value |

#### Request Example

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/process?shift=10.0&formant=2.0&base=100.0" \
  -F "audio=@/path/to/audio.wav" \
  --output processed_audio.wav
```

#### Response

**Success (200 OK):**
- **Content-Type:** `application/octet-stream`
- **Body:** Binary audio data (WAV format)

**Error (500 Internal Server Error):**
- Audio processing failed

---

### 3. Process Live Audio

**Endpoint:** `/process-live`
**Method:** `POST`
**Content-Type:** `application/octet-stream`

#### Description
Process raw audio data in real-time with custom parameters.

#### Request Parameters

| Parameter | Type | Required | Default | Location |
|-----------|------|----------|---------|----------|
| audioData | Binary | Yes | - | Request Body |
| shift | Double | No | 10.0 | Query Parameter |
| formant | Double | No | 2.0 | Query Parameter |
| base | Double | No | 100.0 | Query Parameter |

#### Request Example

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/process-live?shift=10.0&formant=2.0&base=100.0" \
  -H "Content-Type: application/octet-stream" \
  --data-binary "@audio_data.raw" \
  --output processed_audio.wav
```

#### Response

**Success (200 OK):**
- **Content-Type:** `application/octet-stream`
- **Body:** Binary audio data

---

## Parameter Guidelines

### Shift Parameter
- **Range:** -20.0 to 20.0
- **Purpose:** Controls pitch shifting
- **Positive values:** Increase pitch (higher voice)
- **Negative values:** Decrease pitch (deeper voice)

### Formant Parameter
- **Range:** -5.0 to 5.0
- **Purpose:** Controls formant shifting (vocal tract characteristics)
- **Positive values:** Characteristic of higher/feminine voices
- **Negative values:** Characteristic of lower/masculine voices

### Base Parameter
- **Range:** 50.0 to 400.0
- **Purpose:** Sets the base frequency
- **Lower values:** Generally used for higher pitch transformations
- **Higher values:** Generally used for lower pitch transformations

---

## Error Handling

### Common Error Codes

| Status Code | Meaning | Possible Cause |
|-------------|---------|----------------|
| 400 | Bad Request | Invalid code parameter in /voiceTest |
| 500 | Internal Server Error | Audio processing failure, corrupt file, or unsupported format |

### Error Response Example
```json
{
  "timestamp": "2025-11-23T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/voiceTest"
}
```

---

## Postman Collection

Import the provided `VoiceChanger_API.postman_collection.json` file into Postman to test all endpoints with pre-configured requests.

### How to Import:
1. Open Postman
2. Click "Import" button
3. Select the `VoiceChanger_API.postman_collection.json` file
4. Click "Import"

---

## Testing Guidelines

### Supported Audio Formats
- WAV (recommended)
- Other formats may be supported depending on backend configuration

### File Size Recommendations
- Keep audio files under 10MB for optimal performance
- Longer files may take more time to process

### Best Practices
1. Use WAV format for best quality
2. Test with short audio samples first
3. Adjust parameters incrementally for custom transformations
4. Save processed audio files immediately after receiving response

---

## Quick Start Guide

### Step 1: Start the Server
```bash
./mvnw spring-boot:run
```

### Step 2: Test with voiceTest Endpoint
```bash
# Male to Female transformation
curl -X POST "http://localhost:8080/api/voiceTest?code=901" \
  -F "audio=@sample.wav" \
  --output output.wav
```

### Step 3: Verify Output
Play the `output.wav` file to hear the transformed audio.

---

## Troubleshooting

### Issue: 400 Bad Request on /voiceTest
**Solution:** Verify that the code parameter is exactly 901, 902, or 903

### Issue: 500 Internal Server Error
**Solution:**
- Check audio file format (use WAV)
- Verify file is not corrupted
- Check server logs for detailed error messages

### Issue: No audio output received
**Solution:**
- Ensure the `--output` flag is used in cURL
- Check that response headers indicate successful processing
- Verify server is running and accessible

---

## Version Information
- **API Version:** 1.0
- **Last Updated:** 2025-11-23

---

## Support
For issues or questions, check the application logs at runtime for detailed error information.
