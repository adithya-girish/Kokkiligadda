package com.familytree.kokkiligadda.dto;

import com.familytree.kokkiligadda.model.Contact;
import com.familytree.kokkiligadda.model.Documents;
import com.familytree.kokkiligadda.model.FamilyMember;
import com.familytree.kokkiligadda.model.Marriage;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Lightweight tree node — carries full member data plus populated children.
 */
@Data
public class FamilyTreeNode {

    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private String type;
    private String gotram;
    private LocalDate birthDate;
    private boolean isLate;
    private LocalDate dod;
    private String placeOfBirth;
    private String occupation;
    private boolean isRoot;

    // Parent references
    private String fatherId;
    private String motherId;

    // Nested contact & documents
    private Contact contact;
    private Documents documents;

    // Marriages / partnerships
    private List<Marriage> marriages;

    // Direct children IDs (for lazy loading)
    private List<String> childrenIds;

    // Populated children nodes (one level deep when lazy, full when recursive)
    private List<FamilyTreeNode> children;

    // Whether this node has further children (for UI expand/collapse)
    private boolean hasChildren;

    public static FamilyTreeNode from(FamilyMember m) {
        FamilyTreeNode node = new FamilyTreeNode();
        node.setId(m.getId());
        node.setName(m.getName());
        node.setFirstName(m.getFirstName());
        node.setLastName(m.getLastName());
        node.setGender(m.getGender());
        node.setType(m.getType());
        node.setGotram(m.getGotram());
        node.setBirthDate(m.getBirthDate());
        node.setLate(m.isLate());
        node.setDod(m.getDod());
        node.setPlaceOfBirth(m.getPlaceOfBirth());
        node.setOccupation(m.getOccupation());
        node.setRoot(m.isRoot());
        node.setFatherId(m.getFatherId());
        node.setMotherId(m.getMotherId());
        node.setContact(m.getContact());
        node.setDocuments(m.getDocuments());
        node.setMarriages(m.getMarriages());
        node.setChildrenIds(m.getChildrenIds());
        node.setHasChildren(m.getChildrenIds() != null && !m.getChildrenIds().isEmpty());
        return node;
    }
}
