package ru.practicum.compilations.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.compilations.model.Compilation;

import java.util.Optional;

public interface CompilationsRepository extends JpaRepository<Compilation, Long> {

    @EntityGraph("compilation")
    Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);

    @EntityGraph("compilation")
    Page<Compilation> findAll(Pageable pageable);

    @EntityGraph("compilation")
    Optional<Compilation> findById(long compId);
}
