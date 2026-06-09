package com.familytree.kokkiligadda.repository;

import com.familytree.kokkiligadda.model.FamilyMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends MongoRepository<FamilyMember, String> {

    List<FamilyMember> findByLastName(String lastName);

    List<FamilyMember> findByFirstNameAndLastName(String firstName, String lastName);

    List<FamilyMember> findByFatherId(String fatherId);

    List<FamilyMember> findByMotherId(String motherId);

    // Root node — only one should exist with isRoot = true
    Optional<FamilyMember> findByIsRootTrue();

    // Members with no father and no mother (potential roots)
    List<FamilyMember> findByFatherIdIsNullAndMotherIdIsNull();
}
