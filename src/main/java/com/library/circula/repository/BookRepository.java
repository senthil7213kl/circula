package com.library.circula.repository;

import com.library.circula.model.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    @Query("select count(b) > 0 from Book b where b.isbn = :isbn and (b.title <> :title or b.author <> :author)")
    boolean existsByIsbnWithDifferentMetadata(@Param("isbn") String isbn, @Param("title") String title, @Param("author") String author);
}
