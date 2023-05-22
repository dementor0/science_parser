package com.mtuci.scienceParser.repository;

import com.mtuci.scienceParser.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

}
