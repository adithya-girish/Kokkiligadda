package com.familytree.kokkiligadda.service;

import com.familytree.kokkiligadda.dto.FamilyTreeNode;
import com.familytree.kokkiligadda.exception.ResourceNotFoundException;
import com.familytree.kokkiligadda.model.FamilyMember;
import com.familytree.kokkiligadda.repository.FamilyMemberRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tree Navigation Service Tests")
class FamilyMemberServiceTreeTest {

    @Mock  private FamilyMemberRepository repository;
    @InjectMocks private FamilyMemberService service;

    private FamilyMember root, child1, child2;

    @BeforeEach
    void setUp() {
        root   = FamilyMember.builder().id("root-id").firstName("Ravi").lastName("Kokkiligadda")
                .name("Ravi Kokkiligadda").gender("MALE").isRoot(true).build();
        child1 = FamilyMember.builder().id("child1-id").firstName("Suresh").lastName("Kokkiligadda")
                .name("Suresh Kokkiligadda").gender("MALE").fatherId("root-id").build();
        child2 = FamilyMember.builder().id("child2-id").firstName("Ramesh").lastName("Kokkiligadda")
                .name("Ramesh Kokkiligadda").gender("MALE").fatherId("root-id").build();
    }

    @Nested @DisplayName("Get Root")
    class GetRootTests {
        @Test void shouldReturnRoot() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of(child1, child2));
            assertThat(service.getRootNode()).isPresent();
            assertThat(service.getRootNode().get().getId()).isEqualTo("root-id");
        }
        @Test void shouldReturnEmptyWhenNoRoot() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.empty());
            assertThat(service.getRootNode()).isEmpty();
        }
        @Test void shouldPopulateChildren() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of(child1, child2));
            assertThat(service.getRootNode().get().getChildren()).hasSize(2);
            assertThat(service.getRootNode().get().isHasChildren()).isTrue();
        }
        @Test void rootWithNoChildren() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of());
            assertThat(service.getRootNode().get().getChildren()).isEmpty();
            assertThat(service.getRootNode().get().isHasChildren()).isFalse();
        }
        @Test void shouldSetHasChildrenFlag() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of(child1, child2));
            service.getRootNode().get().getChildren()
                    .forEach(c -> assertThat(c.isHasChildren()).isFalse());
        }
    }

    @Nested @DisplayName("Create Root")
    class CreateRootTests {
        @Test void shouldCreateRoot() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            FamilyMember r = FamilyMember.builder().firstName("Ravi").lastName("K").build();
            assertThat(service.createRoot(r).isRoot()).isTrue();
            assertThat(service.createRoot(r).getName()).isEqualTo("Ravi K");
        }
        @Test void shouldThrowWhenRootExists() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.of(root));
            assertThatThrownBy(() -> service.createRoot(FamilyMember.builder()
                    .firstName("X").lastName("Y").build()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("root node already exists");
        }
        @Test void shouldThrowMissingFirstName() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.createRoot(FamilyMember.builder().lastName("K").build()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        @Test void shouldAutoBuildName() {
            when(repository.findByIsRootTrue()).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            assertThat(service.createRoot(FamilyMember.builder()
                    .firstName("A").lastName("B").build()).getName()).isEqualTo("A B");
        }
    }

    @Nested @DisplayName("Get Node")
    class GetNodeTests {
        @Test void shouldGetNodeWithChildren() {
            when(repository.findById("root-id")).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of(child1, child2));
            FamilyTreeNode node = service.getNode("root-id");
            assertThat(node.getChildren()).hasSize(2);
            assertThat(node.isHasChildren()).isTrue();
        }
        @Test void shouldThrowNotFound() {
            when(repository.findById("x")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.getNode("x")).isInstanceOf(ResourceNotFoundException.class);
        }
        @Test void childrenShouldBeOneLevelDeep() {
            when(repository.findById("root-id")).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of(child1));
            assertThat(service.getNode("root-id").getChildren().get(0).getChildren()).isNull();
        }
    }

    @Nested @DisplayName("Get Child Nodes (Lazy Load)")
    class GetChildNodesTests {
        @Test void shouldGetChildrenOfFather() {
            when(repository.findById("root-id")).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of(child1, child2));
            assertThat(service.getChildNodes("root-id")).hasSize(2);
        }
        @Test void shouldGetChildrenOfMother() {
            FamilyMember mom = FamilyMember.builder().id("mom-id").gender("FEMALE")
                    .firstName("L").lastName("K").build();
            FamilyMember baby = FamilyMember.builder().id("baby-id").firstName("P")
                    .lastName("K").motherId("mom-id").build();
            when(repository.findById("mom-id")).thenReturn(Optional.of(mom));
            when(repository.findByMotherId("mom-id")).thenReturn(List.of(baby));
            assertThat(service.getChildNodes("mom-id")).hasSize(1);
        }
        @Test void shouldReturnEmptyWhenNoChildren() {
            when(repository.findById("child1-id")).thenReturn(Optional.of(child1));
            when(repository.findByFatherId("child1-id")).thenReturn(List.of());
            assertThat(service.getChildNodes("child1-id")).isEmpty();
        }
        @Test void shouldSetHasChildrenFlag() {
            child1 = FamilyMember.builder().id("child1-id").firstName("S").lastName("K")
                    .gender("MALE").fatherId("root-id").childrenIds(List.of("gc-id")).build();
            when(repository.findById("root-id")).thenReturn(Optional.of(root));
            when(repository.findByFatherId("root-id")).thenReturn(List.of(child1));
            assertThat(service.getChildNodes("root-id").get(0).isHasChildren()).isTrue();
        }
        @Test void shouldThrowForUnknownParent() {
            when(repository.findById("bad")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.getChildNodes("bad"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
