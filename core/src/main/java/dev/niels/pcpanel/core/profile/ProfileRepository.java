package dev.niels.pcpanel.core.profile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProfileRepository extends CrudRepository<Profile, Long> {
  List<Profile> findByDevice(String device);
}
