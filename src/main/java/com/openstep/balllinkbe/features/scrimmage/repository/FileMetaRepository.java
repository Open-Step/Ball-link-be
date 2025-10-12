package com.openstep.balllinkbe.features.scrimmage.repository;

import com.openstep.balllinkbe.domain.file.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetaRepository extends JpaRepository<FileMeta, String> {
}
