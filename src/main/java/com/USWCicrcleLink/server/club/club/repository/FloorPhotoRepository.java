package com.USWCicrcleLink.server.club.club.repository;


import com.USWCicrcleLink.server.club.club.domain.FloorPhoto;
import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FloorPhotoRepository extends JpaRepository<FloorPhoto, Long> {
    Optional<FloorPhoto> findByFloor(FloorPhotoEnum floor);
}
