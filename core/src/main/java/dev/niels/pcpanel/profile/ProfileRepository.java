package dev.niels.pcpanel.profile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProfileRepository extends CrudRepository<Profile, Long> {
  List<Profile> findByDevice(String device);
}
