package com.mtuci.scienceParser.repository;

import com.mtuci.scienceParser.model.AuthorInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorInfoRepository extends JpaRepository<AuthorInfo, Long> {

}
