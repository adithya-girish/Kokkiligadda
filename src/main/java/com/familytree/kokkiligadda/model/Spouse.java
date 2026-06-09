package com.familytree.kokkiligadda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Embedded spouse details stored inside a FamilyMember document.
 * spouseId can reference another FamilyMember document if the spouse
 * is also registered in the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spouse {

    private String spouseId;         // Reference to FamilyMember._id (optional)
    private String name;             // Spouse full name
    private String type;             // e.g., FIRST, SECOND (for remarriage cases)
    private boolean isLate;          // true if spouse is deceased
    private LocalDate dod;           // Date of death of spouse
    private String aadhaar;          // Spouse Aadhaar
    private String pan;              // Spouse PAN
    private String gotram;           // Spouse Gotram
}
