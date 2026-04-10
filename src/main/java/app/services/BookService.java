package app.services;

import app.Main;
import app.daos.BookDAO;
import app.daos.CollectionDAO;
import app.daos.UserDAO;
import app.dtos.BookDTO;
import app.dtos.OpenLibraryDTOresponse;
import app.entities.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class BookService {
    private static HttpClient client = HttpClient.newHttpClient();
    private static ObjectMapper om = new ObjectMapper();


    /*private static String getDataFromApiJson(String url) {
        String bodyText = null;
        try{
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).version(HttpClient.Version.HTTP_1_1).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if ( response.statusCode()==200) {
                bodyText = response.body();
                return bodyText;
            }
                } catch (
                URISyntaxException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        return bodyText;
    }*/

    private static String getDataFromApiJsonWithHeader(String url) {
        String bodyText = null;
        try{
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).header("accept","application/json").version(HttpClient.Version.HTTP_1_1).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if ( response.statusCode()==200) {
                bodyText = response.body();
                return bodyText;
            }
        } catch (
                URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return bodyText;
    }

    public static OpenLibraryDTOresponse getListOfBooksByKeyword(String keyword){
        OpenLibraryDTOresponse OlDTOresponse = null;
        keyword = keyword.replace(" ", "+");
        String url = "https://openlibrary.org/search.json?q="+keyword+"&limit=10";
        try{
            OlDTOresponse = om.readValue(getDataFromApiJsonWithHeader(url), OpenLibraryDTOresponse.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return OlDTOresponse;
    }

    public static BookDTO runGetBooksByKeyword(String keyword) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<BookDTO> task = () -> {
            OpenLibraryDTOresponse bookDTOlist = BookService.getListOfBooksByKeyword(keyword);
            List<BookDTO> tmpList = bookDTOlist.getDocs();

            if (tmpList == null || tmpList.isEmpty()) {
                System.out.println("No books found.");
                return null;
            }

            for (int i = 0; i < tmpList.size(); i++) {
                System.out.println((i + 1) + ". " + tmpList.get(i).getTitle());
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("Choose a book by number: ");
            int choice = scanner.nextInt();

            if (choice < 1 || choice > tmpList.size()) {
                System.out.println("Invalid choice.");
                return null;
            }

            return tmpList.get(choice - 1);
        };

        try {
            Future<BookDTO> future = executor.submit(task);
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }
    // method is just a placeholder before frontend is implemented (its a quick fix to see if the book information is correctly retrieved and can be saved to the database)
    public static void saveBookChoiceToDatabase(String keyword, int userId){
        BookDTO dto = runGetBooksByKeyword(keyword);
        if (dto == null || dto.getTitle() == null || dto.getPublish_year() == 0 || dto.getAuthor().isEmpty()) {
            System.out.println("Book information is incomplete. Cannot save to database.");
            return;
        }

        //find user
        UserDAO userDAO = new UserDAO(Main.emf);

        User user = userDAO.getByID(userId);

        //scanner object for user input (console as of now)
        Scanner scanner = new Scanner(System.in);

        //create new collection or add to existing collection
        Collection collection;


        CollectionDAO collectionDAO = new CollectionDAO(Main.emf);
        System.out.println("Do you want to add the book to an existing collection or create a new one? (existing/new): ");
        String collectionChoice = scanner.nextLine();
        if (collectionChoice.equalsIgnoreCase("existing")) {

            System.out.println("Enter the name of the existing collection: ");
             collection = collectionDAO.getByName(scanner.nextLine(), user);
            if (collection == null) {
                System.out.println("Collection not found.");
                return;
            }
        }
        else {
        collection = new Collection(user, "My Book Collection", "A collection of my favorite books", LocalDateTime.now());
        collectionDAO.create(collection);
        }




        BookDAO bookDAO = new BookDAO(Main.emf);

        // DESCRIPTION
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        // STATUS
        System.out.println("Choose item status:");
        for (ItemStatus s : ItemStatus.values()) {
            System.out.println((s.ordinal() + 1) + ". " + s);
        }
        int statusChoice = scanner.nextInt();
        scanner.nextLine();
        ItemStatus status = ItemStatus.values()[statusChoice - 1];

        // CONDITION
        System.out.println("Choose item condition:");
        for (ItemCondition c : ItemCondition.values()) {
            System.out.println((c.ordinal() + 1) + ". " + c);
        }

        int conditionChoice = scanner.nextInt();
        ItemCondition condition = ItemCondition.values()[conditionChoice - 1];
        Book book = new Book(dto.getTitle(), description, dto.getAuthor(), dto.getPublish_year(), status, condition, collection);
        bookDAO.create(book);

        System.out.println("Book saved to database successfully.");
    }



}
