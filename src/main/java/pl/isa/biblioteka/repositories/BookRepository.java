package pl.isa.biblioteka.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.isa.biblioteka.model.Book;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

//    @Query("SELECT b FROM Book b WHERE b.category = :category ORDER BY b.counter DESC")
//    List<Book> findAllByCategoryOrderByCounter(@Param("category") String category);


//    Page<Book> findAllByCategoryOrderByCounterDesc(String category, PageRequest pageable);

    List<Book> findAllByCategoryOrderByCounterDesc(String category);

//    List<Book> findAllByCategoryOrderByCounter();



    Optional<Book> findById(Long id);


//    static void saveBooks() {
//        ObjectMapper mapper = new ObjectMapper();
//        List<Book> booksList = BookService.booksList;
//        try {
//            mapper.writeValue(new File("booksFile.json"), booksList);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    static List<Book> readBooks() {
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get("booksFile.json"));
            ObjectMapper folderBooks = new ObjectMapper();
            return Arrays.asList(folderBooks.readValue(jsonData, Book[].class));
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    static List<Book> sampelReadBooks() {
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get("test.json"));
            ObjectMapper folderBooks = new ObjectMapper();
            return Arrays.asList(folderBooks.readValue(jsonData, Book[].class));
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    @Query("SELECT b from Book b where title like concat(:title, '%')")
    List<Book> findBookByTitleLike(@Param("title") String title);
}