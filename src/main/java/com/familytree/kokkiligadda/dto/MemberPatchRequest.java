package com.familytree.kokkiligadda.dto;

import com.familytree.kokkiligadda.model.Contact;
import com.familytree.kokkiligadda.model.Documents;
import com.familytree.kokkiligadda.model.Marriage;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Partial update request — only non-null fields will be applied.
 */
@Data
public class MemberPatchRequest {

    private String name;
    private String type;
    private String gotram;
    private Boolean isLate;
    private LocalDate dod;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;        // new schema: birthDate (was dateOfBirth)
    private String placeOfBirth;
    private String occupation;
    private String fatherId;
    private String motherId;

    // Nested contact & documents (new schema)
    private Contact contact;
    private Documents documents;

    // Marriages list (replaces old spouses)
    private List<Marriage> marriages;
}
