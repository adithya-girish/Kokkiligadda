package com.familytree.kokkiligadda.service;

import com.familytree.kokkiligadda.dto.FamilyTreeNode;
import com.familytree.kokkiligadda.dto.MemberPatchRequest;
import com.familytree.kokkiligadda.exception.ResourceNotFoundException;
import com.familytree.kokkiligadda.model.FamilyMember;
import com.familytree.kokkiligadda.model.Marriage;
import com.familytree.kokkiligadda.repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyMemberService {

    private final FamilyMemberRepository repository;

    // ── Create ──────────────────────────────────────────────────────────────

    public FamilyMember create(FamilyMember member) {
        if (member.getFirstName() == null || member.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required");
        if (member.getLastName() == null || member.getLastName().isBlank())
            throw new IllegalArgumentException("Last name is required");
        if (member.getName() == null || member.getName().isBlank())
            member.setName(member.getFirstName() + " " + member.getLastName());
        return repository.save(member);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public List<FamilyMember> getAll() { return repository.findAll(); }

    public FamilyMember getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Family member not found with id: " + id));
    }

    public List<FamilyMember> getByLastName(String lastName) {
        return repository.findByLastName(lastName);
    }

    public List<FamilyMember> getChildren(String parentId) {
        FamilyMember parent = getById(parentId);
        return "FEMALE".equalsIgnoreCase(parent.getGender())
                ? repository.findByMotherId(parentId)
                : repository.findByFatherId(parentId);
    }

    // ── Full Update (PUT) ────────────────────────────────────────────────────

    public FamilyMember update(String id, FamilyMember updated) {
        FamilyMember existing = getById(id);
        updated.setId(existing.getId());
        return repository.save(updated);
    }

    // ── Partial Update (PATCH) ───────────────────────────────────────────────

    public FamilyMember patch(String id, MemberPatchRequest req) {
        FamilyMember m = getById(id);
        if (req.getName() != null)         m.setName(req.getName());
        if (req.getType() != null)         m.setType(req.getType());
        if (req.getGotram() != null)       m.setGotram(req.getGotram());
        if (req.getIsLate() != null)       m.setLate(req.getIsLate());
        if (req.getDod() != null)          m.setDod(req.getDod());
        if (req.getFirstName() != null)    m.setFirstName(req.getFirstName());
        if (req.getLastName() != null)     m.setLastName(req.getLastName());
        if (req.getGender() != null)       m.setGender(req.getGender());
        if (req.getBirthDate() != null)    m.setBirthDate(req.getBirthDate());
        if (req.getPlaceOfBirth() != null) m.setPlaceOfBirth(req.getPlaceOfBirth());
        if (req.getOccupation() != null)   m.setOccupation(req.getOccupation());
        if (req.getFatherId() != null)     m.setFatherId(req.getFatherId());
        if (req.getMotherId() != null)     m.setMotherId(req.getMotherId());
        if (req.getContact() != null)      m.setContact(req.getContact());
        if (req.getDocuments() != null)    m.setDocuments(req.getDocuments());
        if (req.getMarriages() != null)    m.setMarriages(req.getMarriages());
        return repository.save(m);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    public void delete(String id) {
        repository.delete(getById(id));
    }

    // ── Children ─────────────────────────────────────────────────────────────

    public FamilyMember addChild(String parentId, String childId) {
        FamilyMember parent = getById(parentId);
        FamilyMember child  = getById(childId);
        if ("FEMALE".equalsIgnoreCase(parent.getGender()))
            child.setMotherId(parentId);
        else
            child.setFatherId(parentId);
        repository.save(child);

        List<String> kids = parent.getChildrenIds();
        if (kids == null) kids = new ArrayList<>();
        if (!kids.contains(childId)) { kids.add(childId); parent.setChildrenIds(kids); repository.save(parent); }
        return repository.findById(parentId).orElseThrow();
    }

    public FamilyMember removeChild(String parentId, String childId) {
        FamilyMember parent = getById(parentId);
        FamilyMember child  = getById(childId);
        if ("FEMALE".equalsIgnoreCase(parent.getGender()))
            child.setMotherId(null);
        else
            child.setFatherId(null);
        repository.save(child);

        List<String> kids = parent.getChildrenIds();
        if (kids != null) { kids.remove(childId); parent.setChildrenIds(kids); repository.save(parent); }
        return repository.findById(parentId).orElseThrow();
    }

    // ── Marriages ─────────────────────────────────────────────────────────────

    public List<Marriage> getMarriages(String memberId) {
        FamilyMember m = getById(memberId);
        return m.getMarriages() != null ? m.getMarriages() : new ArrayList<>();
    }

    public FamilyMember addMarriage(String memberId, Marriage marriage) {
        FamilyMember member = getById(memberId);
        List<Marriage> list = member.getMarriages();
        if (list == null) list = new ArrayList<>();

        // Prevent duplicate partnerId
        if (marriage.getPartnerId() != null) {
            boolean dup = list.stream().anyMatch(m -> marriage.getPartnerId().equals(m.getPartnerId()));
            if (dup) throw new IllegalArgumentException(
                    "Marriage with partnerId " + marriage.getPartnerId() + " already exists");
        }
        list.add(marriage);
        member.setMarriages(list);
        repository.save(member);

        // Set reverse link on partner if registered
        if (marriage.getPartnerId() != null && !marriage.getPartnerId().isBlank()) {
            repository.findById(marriage.getPartnerId()).ifPresent(partner -> {
                List<Marriage> rev = partner.getMarriages();
                if (rev == null) rev = new ArrayList<>();
                boolean revExists = rev.stream().anyMatch(m -> memberId.equals(m.getPartnerId()));
                if (!revExists) {
                    Marriage reverse = Marriage.builder()
                            .partnerId(memberId)
                            .partnerName(member.getName())
                            .relationshipType(marriage.getRelationshipType())
                            .marriageStartDate(marriage.getMarriageStartDate())
                            .marriageEndDate(marriage.getMarriageEndDate())
                            .divorced(marriage.isDivorced())
                            .widowed(marriage.isWidowed())
                            .order(marriage.getOrder())
                            .build();
                    rev.add(reverse);
                    partner.setMarriages(rev);
                    repository.save(partner);
                }
            });
        }
        return repository.findById(memberId).orElseThrow();
    }

    public FamilyMember setAllMarriages(String memberId, List<Marriage> marriages) {
        FamilyMember member = getById(memberId);
        member.setMarriages(marriages != null ? marriages : new ArrayList<>());
        return repository.save(member);
    }

    public FamilyMember updateMarriage(String memberId, int index, Marriage updated) {
        FamilyMember member = getById(memberId);
        List<Marriage> list = member.getMarriages();
        if (list == null || index < 0 || index >= list.size())
            throw new IllegalArgumentException("Marriage at index " + index + " not found");
        list.set(index, updated);
        member.setMarriages(list);
        return repository.save(member);
    }

    public FamilyMember removeMarriage(String memberId, int index) {
        FamilyMember member = getById(memberId);
        List<Marriage> list = member.getMarriages();
        if (list == null || index < 0 || index >= list.size())
            throw new IllegalArgumentException("Marriage at index " + index + " not found");
        Marriage removed = list.remove(index);
        member.setMarriages(list);
        repository.save(member);

        // Clear reverse link
        if (removed.getPartnerId() != null) {
            repository.findById(removed.getPartnerId()).ifPresent(p -> {
                List<Marriage> rev = p.getMarriages();
                if (rev != null) { rev.removeIf(m -> memberId.equals(m.getPartnerId())); p.setMarriages(rev); repository.save(p); }
            });
        }
        return repository.findById(memberId).orElseThrow();
    }

    public FamilyMember removeAllMarriages(String memberId) {
        FamilyMember member = getById(memberId);
        List<Marriage> list = member.getMarriages();
        if (list != null) {
            list.stream().filter(m -> m.getPartnerId() != null).forEach(m ->
                repository.findById(m.getPartnerId()).ifPresent(p -> {
                    List<Marriage> rev = p.getMarriages();
                    if (rev != null) { rev.removeIf(r -> memberId.equals(r.getPartnerId())); p.setMarriages(rev); repository.save(p); }
                })
            );
        }
        member.setMarriages(new ArrayList<>());
        return repository.save(member);
    }

    // ── Parents ───────────────────────────────────────────────────────────────

    public FamilyMember setFather(String memberId, String fatherId) {
        FamilyMember member = getById(memberId);
        FamilyMember father = getById(fatherId);
        member.setFatherId(fatherId);
        repository.save(member);
        List<String> kids = father.getChildrenIds();
        if (kids == null) kids = new ArrayList<>();
        if (!kids.contains(memberId)) { kids.add(memberId); father.setChildrenIds(kids); repository.save(father); }
        return repository.findById(memberId).orElseThrow();
    }

    public FamilyMember setMother(String memberId, String motherId) {
        FamilyMember member = getById(memberId);
        FamilyMember mother = getById(motherId);
        member.setMotherId(motherId);
        repository.save(member);
        List<String> kids = mother.getChildrenIds();
        if (kids == null) kids = new ArrayList<>();
        if (!kids.contains(memberId)) { kids.add(memberId); mother.setChildrenIds(kids); repository.save(mother); }
        return repository.findById(memberId).orElseThrow();
    }

    // ── Remove Fields ─────────────────────────────────────────────────────────

    public FamilyMember removeFields(String id, List<String> fields) {
        FamilyMember m = getById(id);
        Set<String> protect = Set.of("id", "firstName", "lastName", "isRoot", "root");
        List<String> rejected = new ArrayList<>();
        for (String f : fields) {
            if (protect.contains(f)) { rejected.add(f); continue; }
            switch (f.toLowerCase()) {
                case "name"         -> m.setName(null);
                case "type"         -> m.setType(null);
                case "gender"       -> m.setGender(null);
                case "gotram"       -> m.setGotram(null);
                case "birthdate"    -> m.setBirthDate(null);
                case "islate"       -> m.setLate(false);
                case "dod"          -> m.setDod(null);
                case "placeofbirth" -> m.setPlaceOfBirth(null);
                case "occupation"   -> m.setOccupation(null);
                case "contact"      -> m.setContact(null);
                case "documents"    -> m.setDocuments(null);
                case "fatherid"     -> m.setFatherId(null);
                case "motherid"     -> m.setMotherId(null);
                case "marriages"    -> m.setMarriages(null);
                case "childrenids"  -> m.setChildrenIds(null);
                default             -> rejected.add(f);
            }
        }
        if (!rejected.isEmpty())
            throw new IllegalArgumentException("Cannot remove protected or unknown fields: " + rejected);
        return repository.save(m);
    }

    // ── Tree Navigation ───────────────────────────────────────────────────────

    public Optional<FamilyMember> getRootMember() { return repository.findByIsRootTrue(); }

    public Optional<FamilyTreeNode> getRootNode() {
        return repository.findByIsRootTrue().map(r -> buildNode(r, true));
    }

    public FamilyMember createRoot(FamilyMember member) {
        if (repository.findByIsRootTrue().isPresent())
            throw new IllegalArgumentException("A root node already exists. Only one root is allowed.");
        if (member.getFirstName() == null || member.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required");
        if (member.getLastName() == null || member.getLastName().isBlank())
            throw new IllegalArgumentException("Last name is required");
        if (member.getName() == null || member.getName().isBlank())
            member.setName(member.getFirstName() + " " + member.getLastName());
        member.setRoot(true);
        return repository.save(member);
    }

    public FamilyTreeNode getNode(String id) { return buildNode(getById(id), true); }

    public List<FamilyTreeNode> getChildNodes(String parentId) {
        FamilyMember parent = getById(parentId);
        List<FamilyMember> kids = "FEMALE".equalsIgnoreCase(parent.getGender())
                ? repository.findByMotherId(parentId)
                : repository.findByFatherId(parentId);
        return kids.stream().map(c -> buildNode(c, false)).collect(Collectors.toList());
    }

    private FamilyTreeNode buildNode(FamilyMember member, boolean populate) {
        FamilyTreeNode node = FamilyTreeNode.from(member);
        if (populate) {
            List<FamilyMember> kids = "FEMALE".equalsIgnoreCase(member.getGender())
                    ? repository.findByMotherId(member.getId())
                    : repository.findByFatherId(member.getId());
            node.setChildren(kids.stream().map(c -> buildNode(c, false)).collect(Collectors.toList()));
            node.setHasChildren(!kids.isEmpty());
        }
        return node;
    }

    public FamilyTreeNode getFullTree() {
        FamilyMember root = repository.findByIsRootTrue()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No root node found. Please create a root member first."));
        return buildFullNode(root);
    }

    public FamilyTreeNode getSubTree(String nodeId) { return buildFullNode(getById(nodeId)); }

    private FamilyTreeNode buildFullNode(FamilyMember member) {
        FamilyTreeNode node = FamilyTreeNode.from(member);
        List<FamilyMember> kids = "FEMALE".equalsIgnoreCase(member.getGender())
                ? repository.findByMotherId(member.getId())
                : repository.findByFatherId(member.getId());
        node.setChildren(kids.stream().map(this::buildFullNode).collect(Collectors.toList()));
        node.setHasChildren(!kids.isEmpty());
        return node;
    }

    public List<FamilyTreeNode> getAncestors(String nodeId) {
        List<FamilyTreeNode> ancestors = new ArrayList<>();
        FamilyMember current = getById(nodeId);
        while (current != null) {
            ancestors.add(FamilyTreeNode.from(current));
            String parentId = current.getFatherId() != null ? current.getFatherId() : current.getMotherId();
            if (parentId == null) break;
            current = repository.findById(parentId).orElse(null);
        }
        return ancestors;
    }

    public List<FamilyTreeNode> getPathFromRoot(String nodeId) {
        List<FamilyTreeNode> path = getAncestors(nodeId);
        Collections.reverse(path);
        return path;
    }

    public List<FamilyMember> searchByName(String query) {
        return repository.findAll().stream()
                .filter(m -> m.getName() != null &&
                        m.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
