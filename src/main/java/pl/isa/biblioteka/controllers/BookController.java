package pl.isa.biblioteka.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.isa.biblioteka.model.Book;
import pl.isa.biblioteka.model.User;
import pl.isa.biblioteka.repositories.BookRepository;
import pl.isa.biblioteka.repositories.UserRepository;
import pl.isa.biblioteka.servises.BookService;
import pl.isa.biblioteka.servises.UserService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static pl.isa.biblioteka.servises.BookService.extracted;

@Controller
public class BookController {

    private final BookService bookService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
//    private final MailService mailService;

    List<Book> bookListByAuthor;
    List<Book> searchCategoryBook;
    List<Book> searchBook;
    List<Book> localSearchBook;
    List<Book> availableBooks;
    List<Book> borrowedBooks;

    public BookController(BookService bookService, UserService userService, UserRepository userRepository, BookRepository bookRepository) {
        this.bookService = bookService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
//        this.mailService = mailService;
    }

    @GetMapping("/bookList")
    public String listBooks(Model model,
                            @RequestParam("page") Optional<Integer> page,
                            @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        extracted(model, currentPage, pageSize, bookService.getBooks());

        return "list";

    }


    @GetMapping("/top/{category}")
    public String top(@PathVariable("category") String category, Model model, Book book) {
        List<Book> books = bookRepository.findAllByCategoryOrderByCounterDesc(category);
        List<Book> limitedBooks = books.subList(0, Math.min(books.size(), 10));
        model.addAttribute("books", limitedBooks);
        model.addAttribute("category", category);
        return "top";
    }

    @GetMapping("/search")
    String search(Principal principal, Model model, Book book) {
        model.addAttribute("book", book);
        model.addAttribute("category", bookService.availableCategory());

        return "search";

    }

    @PostMapping("/filterAuthor")
    String showByAuthor(@ModelAttribute("book") Book book) {
        bookListByAuthor = bookService.filterByAuthor(book.getAuthor());
        return "redirect:bookAuthorList";
    }


    @RequestMapping(value = "/bookAuthorList", method = RequestMethod.GET)
    public String listBooksAuthor(Model model,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        extracted(model, currentPage, pageSize, bookListByAuthor);

        return "listToBorrow";

    }

    @PostMapping("/filterTitle")
    String showByTitle(@ModelAttribute("book") Book book) {
        searchBook = bookService.findBookByTitle(book.getTitle());
        return "redirect:bookByTitle";
    }

    @RequestMapping(value = "/bookByTitle", method = RequestMethod.GET)
    public String listBooksTitle(Model model,
                                 @RequestParam("page") Optional<Integer> page,
                                 @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        extracted(model, currentPage, pageSize, searchBook);

        return "listToBorrow";

    }

    @PostMapping("/filterCategory")
    String showByCategory(@ModelAttribute("book") Book book) {
        searchCategoryBook = bookService.sortByCategory(book.getCategory());
        return "redirect:bookByCategory";
    }


    @RequestMapping(value = "/bookByCategory", method = RequestMethod.GET)
    public String listBooksCategory(Model model,
                                    @RequestParam("page") Optional<Integer> page,
                                    @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        extracted(model, currentPage, pageSize, searchCategoryBook);

        return "listToBorrow";

    }

    @GetMapping("/searchText")
    public String searchByText(Model model,
                               @RequestParam("page") Optional<Integer> page,
                               @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(20);
        extracted(model, currentPage, pageSize, localSearchBook);

        return "list";

    }

    // TU
    @PostMapping("/searchByText")
    public String searchByText(@RequestParam("text") String text, Model model) {
        localSearchBook = bookService.searchByText(text);

        return "redirect:searchText";
    }

    @GetMapping("/librarianDay")
    public String librarianDay(Model model) {
        return "librarianDay";
    }

    @GetMapping("/addBook")
    public String addBook(Model model) {

        return "addBook";

    }

    @GetMapping("/myBooksReturnByName")
    public String deleteBook(@RequestParam("id") Long id, Authentication principal) {
        User user = userRepository.findByUsername(principal.getName());
        Book book = bookRepository.getById(id);
        bookService.returnBook(user, book);
        userRepository.save(user);
        bookRepository.save(book);

        return "redirect:/myBooksReturn";
    }

    @GetMapping("/myBooksReturn")
    public String returnMyBook(Principal principal, Model model) {
        List<User> users = userService.getAllPerson();
        User user = userRepository.findByUsername(principal.getName());
        List<Book> personBooks = user.getPersonBooks();
//        MailSender.sendToAssignee();
        model.addAttribute("users", users);
        model.addAttribute("books", personBooks);
        return "returnBook";

    }


    @RequestMapping(value = "/borrowBooksList", method = RequestMethod.GET)
    public String listBooksToBorrow(Model model,
                                    @RequestParam("page") Optional<Integer> page,
                                    @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        extracted(model, currentPage, pageSize, bookService.getBooks());
        return "listToBorrow";

    }

    @GetMapping("/myBookBorrowByName")
    public String borrowBook(@RequestParam("id") Long id, Authentication principal) {
        User user = userRepository.findByUsername(principal.getName());
        Book book = bookRepository.getById(id);
//        mailService.send();

        bookService.setBorrowedAndReturnDate(book);
        bookService.addBookToPerson(user, book);
        userRepository.save(user);
        bookRepository.save(book);
        return "redirect:/borrowBooksList";
    }


    @PostMapping("/addBook")
    public String addBook(@RequestParam String title, @RequestParam String author, @RequestParam String category, Model model) {
        Book book = new Book(title, author, category);
        String str = bookService.addBook(book);
        model.addAttribute("result", book);
        model.addAttribute("mesage", str);

        return "addBook";

    }

    @GetMapping("/availableBooks")
    public String availableBooks(Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        availableBooks = bookService.showAllAvailableBooks();

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(20);
        extracted(model, currentPage, pageSize, availableBooks);

        return "availableBooks";


    }

    @GetMapping("/borrowedBooks")
    public String borrowedBooks(Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        borrowedBooks = bookService.showAllBorrowedBooks();
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(20);
        extracted(model, currentPage, pageSize, borrowedBooks);
        return "borrowedBooks";

    }
}