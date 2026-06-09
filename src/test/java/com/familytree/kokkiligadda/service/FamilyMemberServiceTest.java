package com.familytree.kokkiligadda.service;

import com.familytree.kokkiligadda.dto.MemberPatchRequest;
import com.familytree.kokkiligadda.exception.ResourceNotFoundException;
import com.familytree.kokkiligadda.model.FamilyMember;
import com.familytree.kokkiligadda.model.Marriage;
import com.familytree.kokkiligadda.repository.FamilyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyMemberServiceTest {

    @Mock  private FamilyMemberRepository repository;
    @InjectMocks private FamilyMemberService service;

    private FamilyMember father;
    private FamilyMember mother;
    private FamilyMember child;

    @BeforeEach
    void setUp() {
        father = FamilyMember.builder().id("father-id").firstName("Ravi")
                .lastName("Kokkiligadda").name("Ravi Kokkiligadda")
                .gender("MALE").type("BLOOD").gotram("Kasyapa").build();
        mother = FamilyMember.builder().id("mother-id").firstName("Lakshmi")
                .lastName("Kokkiligadda").name("Lakshmi Kokkiligadda")
                .gender("FEMALE").build();
        child = FamilyMember.builder().id("child-id").firstName("Suresh")
                .lastName("Kokkiligadda").name("Suresh Kokkiligadda")
                .gender("MALE").build();
    }

    @Nested @DisplayName("Create Member")
    class CreateTests {
        @Test @DisplayName("Should create member successfully")
        void shouldCreateMember() {
            when(repository.save(any())).thenReturn(father);
            assertThat(service.create(father).getFirstName()).isEqualTo("Ravi");
        }
        @Test @DisplayName("Should auto-build name if not provided")
        void shouldAutoBuildName() {
            FamilyMember m = FamilyMember.builder().firstName("Test").lastName("User").build();
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            assertThat(service.create(m).getName()).isEqualTo("Test User");
        }
        @Test @DisplayName("Should throw when firstName missing")
        void shouldThrowMissingFirst() {
            assertThatThrownBy(() -> service.create(FamilyMember.builder().lastName("K").build()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        @Test @DisplayName("Should throw when lastName missing")
        void shouldThrowMissingLast() {
            assertThatThrownBy(() -> service.create(FamilyMember.builder().firstName("R").build()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested @DisplayName("Read Member")
    class ReadTests {
        @Test void shouldGetById() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            assertThat(service.getById("father-id").getId()).isEqualTo("father-id");
        }
        @Test void shouldThrowForUnknownId() {
            when(repository.findById("x")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.getById("x")).isInstanceOf(ResourceNotFoundException.class);
        }
        @Test void shouldGetAll() {
            when(repository.findAll()).thenReturn(List.of(father, mother, child));
            assertThat(service.getAll()).hasSize(3);
        }
        @Test void shouldGetByLastName() {
            when(repository.findByLastName("K")).thenReturn(List.of(father, mother));
            assertThat(service.getByLastName("K")).hasSize(2);
        }
    }

    @Nested @DisplayName("Patch Member")
    class PatchTests {
        @Test void shouldPatchFields() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            MemberPatchRequest req = new MemberPatchRequest();
            req.setGotram("Bharadwaja");
            assertThat(service.patch("father-id", req).getGotram()).isEqualTo("Bharadwaja");
            assertThat(service.patch("father-id", req).getFirstName()).isEqualTo("Ravi");
        }
        @Test void shouldNotOverwriteWithNull() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            assertThat(service.patch("father-id", new MemberPatchRequest()).getGotram()).isEqualTo("Kasyapa");
        }
    }

    @Nested @DisplayName("Delete Member")
    class DeleteTests {
        @Test void shouldDelete() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            service.delete("father-id");
            verify(repository).delete(father);
        }
        @Test void shouldThrowForUnknown() {
            when(repository.findById("x")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.delete("x")).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("Children")
    class ChildrenTests {
        @Test void shouldAddChildSetFatherId() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.findById("child-id")).thenReturn(Optional.of(child));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.addChild("father-id", "child-id");
            assertThat(child.getFatherId()).isEqualTo("father-id");
        }
        @Test void shouldAddChildSetMotherId() {
            when(repository.findById("mother-id")).thenReturn(Optional.of(mother));
            when(repository.findById("child-id")).thenReturn(Optional.of(child));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.addChild("mother-id", "child-id");
            assertThat(child.getMotherId()).isEqualTo("mother-id");
        }
        @Test void shouldUpdateParentChildrenIds() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.findById("child-id")).thenReturn(Optional.of(child));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.addChild("father-id", "child-id");
            assertThat(father.getChildrenIds()).contains("child-id");
        }
        @Test void shouldNotAddDuplicateChild() {
            father.setChildrenIds(new ArrayList<>(List.of("child-id")));
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.findById("child-id")).thenReturn(Optional.of(child));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.addChild("father-id", "child-id");
            assertThat(father.getChildrenIds()).hasSize(1);
        }
        @Test void shouldRemoveChild() {
            father.setChildrenIds(new ArrayList<>(List.of("child-id")));
            child.setFatherId("father-id");
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.findById("child-id")).thenReturn(Optional.of(child));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.removeChild("father-id", "child-id");
            assertThat(child.getFatherId()).isNull();
            assertThat(father.getChildrenIds()).doesNotContain("child-id");
        }
        @Test void shouldGetChildren() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.findByFatherId("father-id")).thenReturn(List.of(child));
            assertThat(service.getChildren("father-id")).hasSize(1);
        }
    }

    @Nested @DisplayName("Marriages")
    class MarriageTests {
        @Test void shouldAddMarriage() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            Marriage m = Marriage.builder().partnerName("Lakshmi").relationshipType("MARRIED").order(1).build();
            service.addMarriage("father-id", m);
            assertThat(father.getMarriages()).hasSize(1);
            assertThat(father.getMarriages().get(0).getPartnerName()).isEqualTo("Lakshmi");
        }
        @Test void shouldAddMultipleMarriages() {
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.addMarriage("father-id", Marriage.builder().partnerName("Lakshmi").order(1).build());
            service.addMarriage("father-id", Marriage.builder().partnerName("Saraswathi").order(2).build());
            assertThat(father.getMarriages()).hasSize(2);
        }
        @Test void shouldUpdateMarriage() {
            father.setMarriages(new ArrayList<>(List.of(
                    Marriage.builder().partnerName("Lakshmi").relationshipType("MARRIED").build())));
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            Marriage upd = Marriage.builder().partnerName("Lakshmi Devi").relationshipType("MARRIED").widowed(true).build();
            service.updateMarriage("father-id", 0, upd);
            assertThat(father.getMarriages().get(0).getPartnerName()).isEqualTo("Lakshmi Devi");
            assertThat(father.getMarriages().get(0).isWidowed()).isTrue();
        }
        @Test void shouldThrowForInvalidIndex() {
            father.setMarriages(new ArrayList<>());
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            assertThatThrownBy(() -> service.updateMarriage("father-id", 5,
                    Marriage.builder().build())).isInstanceOf(IllegalArgumentException.class);
        }
        @Test void shouldRemoveMarriage() {
            father.setMarriages(new ArrayList<>(List.of(
                    Marriage.builder().partnerName("Lakshmi").build(),
                    Marriage.builder().partnerName("Saraswathi").build())));
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.removeMarriage("father-id", 0);
            assertThat(father.getMarriages()).hasSize(1);
            assertThat(father.getMarriages().get(0).getPartnerName()).isEqualTo("Saraswathi");
        }
        @Test void shouldRemoveAllMarriages() {
            father.setMarriages(new ArrayList<>(List.of(
                    Marriage.builder().partnerName("Lakshmi").build())));
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.removeAllMarriages("father-id");
            assertThat(father.getMarriages()).isEmpty();
        }
        @Test void shouldGetMarriages() {
            father.setMarriages(List.of(Marriage.builder().partnerName("Lakshmi").build()));
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            assertThat(service.getMarriages("father-id")).hasSize(1);
        }
    }

    @Nested @DisplayName("Parents")
    class ParentTests {
        @Test void shouldSetFather() {
            when(repository.findById("child-id")).thenReturn(Optional.of(child));
            when(repository.findById("father-id")).thenReturn(Optional.of(father));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.setFather("child-id", "father-id");
            assertThat(child.getFatherId()).isEqualTo("father-id");
            assertThat(father.getChildrenIds()).contains("child-id");
        }
        @Test void shouldSetMother() {
            when(repository.findById("child-id")).thenReturn(Optional.of(child));
            when(repository.findById("mother-id")).thenReturn(Optional.of(mother));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            service.setMother("child-id", "mother-id");
            assertThat(child.getMotherId()).isEqualTo("mother-id");
            assertThat(mother.getChildrenIds()).contains("child-id");
        }
    }
}
