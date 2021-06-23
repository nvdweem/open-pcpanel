package dev.niels.pcpanel.core.plugin;

import dev.niels.pcpanel.core.profile.Profile;
import dev.niels.pcpanel.core.profile.ProfileRepository;
import dev.niels.pcpanel.plugins.config.DropDownOption;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("core")
@RequiredArgsConstructor
public class ProfilesController {
  private final ProfileRepository profileRepository;

  @GetMapping("profiles")
  public List<DropDownOption> getProfiles() {
    return StreamEx.of(profileRepository.findAll().spliterator())
      .mapToEntry(p -> p.getId().toString(), Profile::getName)
      .mapKeyValue(DropDownOption::new)
      .sorted(Comparator.comparing(DropDownOption::getDisplay, String.CASE_INSENSITIVE_ORDER))
      .toList();
  }
}
