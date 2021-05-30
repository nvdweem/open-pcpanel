package dev.niels.pcpanel.web;

import dev.niels.pcpanel.device.ConnectedDeviceService;
import dev.niels.pcpanel.profile.Profile;
import dev.niels.pcpanel.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @Transactional
  @PostMapping("profile/{deviceId}")
  public List<Profile> addProfile(@PathVariable String deviceId, @RequestParam String name) {
    var device = deviceService.getDevice(deviceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    var newProfile = new Profile().setDevice(deviceId).setName(name);
    device.getProfiles().add(profileRepository.save(newProfile));

    return device.getProfiles();
  }
}
