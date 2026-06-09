package com.familytree.kokkiligadda.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "family_members")
public class FamilyMember {

    @Id
    private String id;

    // ── Identity ──────────────────────────────────────────────────────────────
    private String name;                // Full display name
    private String firstName;
    private String lastName;

    /** Relationship type to the family tree: BLOOD, ADOPTED, STEP, IN_LAW */
    private String type;

    private String gender;              // MALE / FEMALE / OTHER
    private String gotram;              // Family lineage / Gotram

    // ── Life Details ─────────────────────────────────────────────────────────
    @Field("birthDate")
    @JsonAlias("dateOfBirth")           // accept old field name from existing data
    private LocalDate birthDate;

    private boolean isLate;             // true if deceased
    private LocalDate dod;              // Date of Death
    private String placeOfBirth;
    private String occupation;

    // ── Contact (nested object) ───────────────────────────────────────────────
    private Contact contact;

    // ── Identity Documents (nested object) ───────────────────────────────────
    private Documents documents;

    // ── Family Relationships ─────────────────────────────────────────────────
    @com.fasterxml.jackson.annotation.JsonProperty("isRoot")
    private boolean isRoot;             // true for the root ancestor of the tree
    private String fatherId;
    private String motherId;

    /**
     * Embedded marriages / partnerships (replaces old `spouses` list).
     * Supports multiple marriages, same-sex partners, divorced/widowed status.
     */
    private List<Marriage> marriages;

    private List<String> childrenIds;
}
