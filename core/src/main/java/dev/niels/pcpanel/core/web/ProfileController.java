package dev.niels.pcpanel.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niels.pcpanel.core.device.ConnectedDeviceService;
import dev.niels.pcpanel.core.profile.Profile;
import dev.niels.pcpanel.core.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProfileController {
  private final ConnectedDeviceService deviceService;
  private final ProfileRepository profileRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  @PostMapping("profile/{deviceId}")
  public List<Profile> addProfile(@PathVariable String deviceId, @RequestParam String name) {
    var device = deviceService.getDevice(deviceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    var newProfile = new Profile().setDevice(deviceId).setName(name);
    device.getProfiles().add(profileRepository.save(newProfile));

    if (device.getProfiles().size() == 1) {
      device.setActiveProfile(device.getProfiles().get(0));
    }

    return device.getProfiles();
  }

  @Transactional
  @PutMapping("profile/{deviceId}")
  public List<Profile> renameProfile(@PathVariable String deviceId, @RequestBody Profile body) {
    var device = deviceService.getDevice(deviceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    var profile = StreamEx.of(device.getProfiles()).filterBy(Profile::getId, body.getId()).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    device.getProfiles().remove(profile);
    device.getProfiles().add(body);
    profileRepository.save(body.prepareForSave(objectMapper));
    return device.getProfiles();
  }

  @Transactional
  @DeleteMapping("profile/{deviceId}/{profileId}")
  public List<Profile> deleteProfile(@PathVariable String deviceId, @PathVariable Long profileId) {
    var device = deviceService.getDevice(deviceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    var profile = StreamEx.of(device.getProfiles()).filterBy(Profile::getId, profileId).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    device.getProfiles().remove(profile);
    profileRepository.delete(profile);
    return device.getProfiles();
  }

  @Transactional
  @PostMapping("profile/{deviceId}/save")
  public boolean save(@PathVariable String deviceId) {
    var device = deviceService.getDevice(deviceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    profileRepository.save(device.getActiveProfile().prepareForSave(objectMapper));
    deviceService.updateProfiles(device);
    return true;
  }

  @PutMapping("profile/{deviceId}/{profileId}")
  public boolean selectProfile(@PathVariable String deviceId, @PathVariable Long profileId) throws JsonProcessingException {
    var device = deviceService.getDevice(deviceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    var profile = StreamEx.of(device.getProfiles()).findFirst(p -> p.getId().equals(profileId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

    device.setActiveProfile(objectMapper.readValue(objectMapper.writeValueAsString(profile), Profile.class));
    return true;
  }
}
