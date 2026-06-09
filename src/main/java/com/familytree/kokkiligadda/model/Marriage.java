package com.familytree.kokkiligadda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Embedded marriage/partnership record inside a FamilyMember document.
 * partnerId references another FamilyMember._id if the partner is registered.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Marriage {

    private String partnerId;           // Reference to FamilyMember._id (optional)
    private String partnerName;         // Partner full name

    /**
     * Relationship type:
     * MARRIED   — legally married
     * PARTNER   — unmarried partner / live-in
     * DIVORCED  — marriage ended by divorce
     * WIDOWED   — marriage ended by death of partner
     */
    private String relationshipType;    // MARRIED / PARTNER / DIVORCED / WIDOWED

    private LocalDate marriageStartDate;
    private LocalDate marriageEndDate;  // null if still ongoing

    private boolean divorced;
    private boolean widowed;

    private int order;                  // 1 = first marriage, 2 = second, etc.

    // Partner personal details (for unregistered partners)
    private boolean isLate;
    private LocalDate dod;
    private String gotram;
    private Documents documents;        // partner aadhaar / pan
}
