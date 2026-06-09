package com.familytree.kokkiligadda.controller;

import com.familytree.kokkiligadda.dto.FamilyTreeNode;
import com.familytree.kokkiligadda.dto.MemberPatchRequest;
import com.familytree.kokkiligadda.model.Contact;
import com.familytree.kokkiligadda.model.Documents;
import com.familytree.kokkiligadda.model.FamilyMember;
import com.familytree.kokkiligadda.model.Marriage;
import com.familytree.kokkiligadda.service.FamilyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService service;

    // ════════════════════════════════════════════════════════════════
    // CRUD
    // ════════════════════════════════════════════════════════════════

    @PostMapping
    public ResponseEntity<FamilyMember> create(@RequestBody FamilyMember member) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(member));
    }

    @GetMapping
    public ResponseEntity<List<FamilyMember>> getAll(
            @RequestParam(required = false) String lastName) {
        if (lastName != null && !lastName.isBlank())
            return ResponseEntity.ok(service.getByLastName(lastName));
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FamilyMember> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FamilyMember> update(@PathVariable String id, @RequestBody FamilyMember member) {
        return ResponseEntity.ok(service.update(id, member));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FamilyMember> patch(@PathVariable String id, @RequestBody MemberPatchRequest req) {
        return ResponseEntity.ok(service.patch(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════════
    // CHILDREN
    // ════════════════════════════════════════════════════════════════

    @GetMapping("/{id}/children")
    public ResponseEntity<List<FamilyMember>> getChildren(@PathVariable String id) {
        return ResponseEntity.ok(service.getChildren(id));
    }

    @PostMapping("/{parentId}/children/{childId}")
    public ResponseEntity<FamilyMember> addChild(@PathVariable String parentId, @PathVariable String childId) {
        return ResponseEntity.ok(service.addChild(parentId, childId));
    }

    @DeleteMapping("/{parentId}/children/{childId}")
    public ResponseEntity<FamilyMember> removeChild(@PathVariable String parentId, @PathVariable String childId) {
        return ResponseEntity.ok(service.removeChild(parentId, childId));
    }

    // ════════════════════════════════════════════════════════════════
    // MARRIAGES  (replaces old /spouses)
    // ════════════════════════════════════════════════════════════════

    /** GET /api/family-members/{id}/marriages — get all marriages */
    @GetMapping("/{id}/marriages")
    public ResponseEntity<List<Marriage>> getMarriages(@PathVariable String id) {
        return ResponseEntity.ok(service.getMarriages(id));
    }

    /**
     * POST /api/family-members/{id}/marriages — add a new marriage
     * Body: { partnerId, partnerName, relationshipType, marriageStartDate,
     *         marriageEndDate, divorced, widowed, order, isLate, dod, gotram,
     *         documents: { aadhaar, pan } }
     */
    @PostMapping("/{id}/marriages")
    public ResponseEntity<FamilyMember> addMarriage(@PathVariable String id, @RequestBody Marriage marriage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMarriage(id, marriage));
    }

    /** PUT /api/family-members/{id}/marriages — replace entire marriages list */
    @PutMapping("/{id}/marriages")
    public ResponseEntity<FamilyMember> setAllMarriages(@PathVariable String id, @RequestBody List<Marriage> marriages) {
        return ResponseEntity.ok(service.setAllMarriages(id, marriages));
    }

    /** PUT /api/family-members/{id}/marriages/{index} — update by 0-based index */
    @PutMapping("/{id}/marriages/{index}")
    public ResponseEntity<FamilyMember> updateMarriage(
            @PathVariable String id, @PathVariable int index, @RequestBody Marriage marriage) {
        return ResponseEntity.ok(service.updateMarriage(id, index, marriage));
    }

    /** DELETE /api/family-members/{id}/marriages/{index} — remove by index */
    @DeleteMapping("/{id}/marriages/{index}")
    public ResponseEntity<FamilyMember> removeMarriage(@PathVariable String id, @PathVariable int index) {
        return ResponseEntity.ok(service.removeMarriage(id, index));
    }

    /** DELETE /api/family-members/{id}/marriages — remove all */
    @DeleteMapping("/{id}/marriages")
    public ResponseEntity<FamilyMember> removeAllMarriages(@PathVariable String id) {
        return ResponseEntity.ok(service.removeAllMarriages(id));
    }

    // ════════════════════════════════════════════════════════════════
    // PARENTS
    // ════════════════════════════════════════════════════════════════

    @PutMapping("/{memberId}/father/{fatherId}")
    public ResponseEntity<FamilyMember> setFather(@PathVariable String memberId, @PathVariable String fatherId) {
        return ResponseEntity.ok(service.setFather(memberId, fatherId));
    }

    @PutMapping("/{memberId}/mother/{motherId}")
    public ResponseEntity<FamilyMember> setMother(@PathVariable String memberId, @PathVariable String motherId) {
        return ResponseEntity.ok(service.setMother(memberId, motherId));
    }

    // ════════════════════════════════════════════════════════════════
    // REMOVE FIELDS
    // ════════════════════════════════════════════════════════════════

    /**
     * DELETE /api/family-members/{id}/fields
     * Body: ["gotram", "contact", "documents", "birthDate"]
     * Removable: name, type, gender, gotram, birthDate, isLate, dod,
     *            placeOfBirth, occupation, contact, documents,
     *            fatherId, motherId, marriages, childrenIds
     */
    @DeleteMapping("/{id}/fields")
    public ResponseEntity<FamilyMember> removeFields(@PathVariable String id, @RequestBody List<String> fields) {
        return ResponseEntity.ok(service.removeFields(id, fields));
    }

    // ════════════════════════════════════════════════════════════════
    // TREE NAVIGATION
    // ════════════════════════════════════════════════════════════════

    /** GET /tree/root — 204 if no root exists */
    @GetMapping("/tree/root")
    public ResponseEntity<?> getRoot() {
        return service.getRootNode()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** POST /tree/root — create root node */
    @PostMapping("/tree/root")
    public ResponseEntity<FamilyMember> createRoot(@RequestBody FamilyMember member) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRoot(member));
    }

    /** GET /tree/node/{id} — node + immediate children */
    @GetMapping("/tree/node/{id}")
    public ResponseEntity<FamilyTreeNode> getNode(@PathVariable String id) {
        return ResponseEntity.ok(service.getNode(id));
    }

    /** GET /tree/node/{id}/children — lazy-load children */
    @GetMapping("/tree/node/{id}/children")
    public ResponseEntity<List<FamilyTreeNode>> getChildNodes(@PathVariable String id) {
        return ResponseEntity.ok(service.getChildNodes(id));
    }

    /** GET /tree/full — full recursive tree from root */
    @GetMapping("/tree/full")
    public ResponseEntity<FamilyTreeNode> getFullTree() {
        return ResponseEntity.ok(service.getFullTree());
    }

    /** GET /tree/subtree/{id} — recursive subtree from any node */
    @GetMapping("/tree/subtree/{id}")
    public ResponseEntity<FamilyTreeNode> getSubTree(@PathVariable String id) {
        return ResponseEntity.ok(service.getSubTree(id));
    }

    /** GET /tree/ancestors/{id} — node → ... → root */
    @GetMapping("/tree/ancestors/{id}")
    public ResponseEntity<List<FamilyTreeNode>> getAncestors(@PathVariable String id) {
        return ResponseEntity.ok(service.getAncestors(id));
    }

    /** GET /tree/path/{id} — root → ... → node */
    @GetMapping("/tree/path/{id}")
    public ResponseEntity<List<FamilyTreeNode>> getPathFromRoot(@PathVariable String id) {
        return ResponseEntity.ok(service.getPathFromRoot(id));
    }

    /** GET /tree/search?q= — name search */
    @GetMapping("/tree/search")
    public ResponseEntity<List<FamilyMember>> searchByName(@RequestParam String q) {
        return ResponseEntity.ok(service.searchByName(q));
    }

    // ════════════════════════════════════════════════════════════════
    // DETAILS — Contact, Documents, Personal
    // ════════════════════════════════════════════════════════════════

    /**
     * PATCH /api/family-members/{id}/contact
     * Body: { "phoneNumber": "9876543210", "email": "ravi@example.com" }
     */
    @PatchMapping("/{id}/contact")
    public ResponseEntity<FamilyMember> updateContact(
            @PathVariable String id, @RequestBody Map<String, String> body) {
        MemberPatchRequest req = new MemberPatchRequest();
        req.setContact(Contact.builder()
                .phoneNumber(body.get("phoneNumber"))
                .email(body.get("email"))
                .build());
        return ResponseEntity.ok(service.patch(id, req));
    }

    /**
     * PATCH /api/family-members/{id}/documents
     * Body: { "aadhaar": "1234-5678-9012", "pan": "ABCDE1234F" }
     */
    @PatchMapping("/{id}/documents")
    public ResponseEntity<FamilyMember> updateDocuments(
            @PathVariable String id, @RequestBody Map<String, String> body) {
        MemberPatchRequest req = new MemberPatchRequest();
        req.setDocuments(Documents.builder()
                .aadhaar(body.get("aadhaar"))
                .pan(body.get("pan"))
                .build());
        return ResponseEntity.ok(service.patch(id, req));
    }

    /**
     * PATCH /api/family-members/{id}/personal
     * Body: { gotram, occupation, placeOfBirth, birthDate, isLate, dod, type, name }
     */
    @PatchMapping("/{id}/personal")
    public ResponseEntity<FamilyMember> updatePersonal(
            @PathVariable String id, @RequestBody MemberPatchRequest request) {
        return ResponseEntity.ok(service.patch(id, request));
    }
}
