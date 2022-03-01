package model;

import com.mongodb.MongoClient;
import java.util.List;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import org.bson.Document;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.regex;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * A mock implementation of the BooksDBInterface interface to demonstrate how to
 * use it together with the user interface.
 *
 * Your implementation should access a real database.
 *
 * @author Umar A & Rabi S
 */
public class BooksDb implements BooksDbInterface {

    private MongoClientURI clientURI;
    private MongoDatabase mongoBooksDb;
    private MongoClient mongoClient;
    private MongoCollection<Document> booksCollection;
    private MongoCollection<Document> usersCollection;

    private User currentUser;

    private final String connectionString;
    private final String databaseString;

    private final SimpleDateFormat formattedDate;
    private final DecimalFormat formattedRating;

    public BooksDb() {
        connectionString = "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        databaseString = "MongoBooksDB";
        formattedDate = new SimpleDateFormat("yyyy-MM-dd");
        formattedRating = new DecimalFormat("#0.0");
        currentUser = null;
    }

    @Override
    public void loginAsGuest() {
        clientURI = new MongoClientURI(connectionString);
        mongoClient = new MongoClient(clientURI);
        mongoBooksDb = mongoClient.getDatabase(databaseString);
        booksCollection = mongoBooksDb.getCollection("books");
        usersCollection = mongoBooksDb.getCollection("users");

        currentUser = null;
    }

    @Override
    public User loginAsUser(User user) {
        FindIterable<Document> username = usersCollection.find(eq("username", user.getUsername()));
        FindIterable<Document> password = usersCollection.find(eq("password", user.getPassword()));

        MongoCursor<Document> usernameCursor = username.iterator();
        MongoCursor<Document> passwordCursor = password.iterator();

        Document usernameDoc = usernameCursor.next();
        Document passwordDoc = passwordCursor.next();

        currentUser = new User(usernameDoc.getString("username"), passwordDoc.getString("password"));

        mongoClient.close();

        clientURI = new MongoClientURI(connectionString);
        mongoClient = new MongoClient(clientURI);
        mongoBooksDb = mongoClient.getDatabase(databaseString);
        booksCollection = mongoBooksDb.getCollection("books");
        usersCollection = mongoBooksDb.getCollection("users");

        return currentUser;
    }

    @Override
    public void disconnect() {
        mongoClient.close();
    }

    @Override
    public List<Book> getTheBooks() {
        FindIterable<Document> bookIt = booksCollection.find();

        List<Book> list = new ArrayList();
        for (MongoCursor<Document> cursor = bookIt.iterator(); cursor.hasNext();) {
            Document bookDoc = cursor.next();

            ObjectId _id = bookDoc.getObjectId("_id");
            String title = bookDoc.getString("title");
            String isbn = bookDoc.getString("isbn");
            String published = formattedDate.format(bookDoc.getDate("published"));
            String genre = bookDoc.getString("genre");
            double rating = Double.parseDouble(formattedRating.format(bookDoc.getDouble("rating")).replace(",", "."));

            Book book = new Book(_id, title, isbn, published, genre, rating, new User(bookDoc.getString("username")));

            List<Document> authors = bookDoc.getList("authors", Document.class);

            for (int i = 0; i < authors.size(); i++) {
                String name = authors.get(i).getString("name");
                String birthdate = formattedDate.format(authors.get(i).getDate("birthDate"));

                book.addAuthor(new Author(name, birthdate, new User(authors.get(i).getString("addedByUser"))));
            }

            List<Document> reviews = bookDoc.getList("reviews", Document.class);

            if (reviews != null) {
                for (int i = 0; i < reviews.size(); i++) {
                    double rRating = reviews.get(i).getDouble("rating");
                    String text = reviews.get(i).getString("text");
                    String dateAdded = formattedDate.format(reviews.get(i).getDate("dateAdded"));

                    book.addReview(new Review(rRating, text, dateAdded, new User(reviews.get(i).getString("addedByUser"))));
                }
            }
            list.add(book);
        }
        return list;
    }

    @Override
    public Book addBookToDb(Book bookToBeAdded) {
        Document bookDoc = new Document();
        bookDoc.append("title", bookToBeAdded.getTitle());
        bookDoc.append("isbn", bookToBeAdded.getIsbn());
        bookDoc.append("published", bookToBeAdded.getPublished());
        bookDoc.append("genre", bookToBeAdded.getGenre().name());
        bookDoc.append("rating", bookToBeAdded.getRating());
        bookDoc.append("username", currentUser.getUsername());

        ArrayList<Document> authors = new ArrayList();
        for (int i = 0; i < bookToBeAdded.getAuthors().size(); i++) {
            Document author = new Document();
            author.append("name", bookToBeAdded.getAuthors().get(i).getName());
            author.append("birthDate", bookToBeAdded.getAuthors().get(i).getDateOfBirth());
            author.append("addedByUser", currentUser.getUsername());
            authors.add(author);
        }

        bookDoc.append("authors", authors);

        booksCollection.insertOne(bookDoc);
        bookDoc = booksCollection.find(bookDoc).first();

        if (bookDoc != null) {
            ObjectId _id = bookDoc.getObjectId("_id");
            String title = bookDoc.getString("title");
            String isbn = bookDoc.getString("isbn");
            String published = formattedDate.format(bookDoc.getDate("published"));
            String genre = bookDoc.getString("genre");
            double rating = bookDoc.getDouble("rating");

            bookToBeAdded = new Book(_id, title, isbn, published, genre, rating, new User(bookDoc.getString("username")));
            return bookToBeAdded;
        }
        return null;
    }

    @Override
    public Book deleteBookFromDb(Book bookToBeDeleted) {
        if (bookToBeDeleted.getUser().getUsername().equals(currentUser.getUsername())) {

            Document book = new Document();
            book.append("_id", bookToBeDeleted.getBId());

            book = booksCollection.findOneAndDelete(book);

            if (book != null) {
                ObjectId _id = book.getObjectId("_id");
                String title = book.getString("title");
                String isbn = book.getString("isbn");
                String published = formattedDate.format(book.getDate("published"));
                String genre = book.getString("genre");
                double rating = book.getDouble("rating");

                bookToBeDeleted = new Book(_id, title, isbn, published, genre, rating, new User(book.getString("username")));

                return bookToBeDeleted;
            }
        }
        return null;
    }

    @Override
    public Review addReviewToDb(Book reviewToBeAdded) {
        Document bookDoc = new Document();
        bookDoc.append("_id", reviewToBeAdded.getBId());

        Document revDoc = new Document();
        revDoc.append("title", reviewToBeAdded.getTitle());
        revDoc.append("isbn", reviewToBeAdded.getIsbn());
        revDoc.append("published", reviewToBeAdded.getPublished());
        revDoc.append("genre", reviewToBeAdded.getGenre().name());

        ArrayList<Document> authors = new ArrayList();
        for (int i = 0; i < reviewToBeAdded.getAuthors().size(); i++) {
            Document author = new Document();
            author.append("name", reviewToBeAdded.getAuthors().get(i).getName());
            author.append("birthDate", reviewToBeAdded.getAuthors().get(i).getDateOfBirth());
            author.append("addedByUser", reviewToBeAdded.getUser().getUsername());
            authors.add(author);
        }

        int nrOfRatings = reviewToBeAdded.getReviews().size();
        double totalRating = 0.0;

        ArrayList<Document> reviews = new ArrayList();
        for (int i = 0; i < reviewToBeAdded.getReviews().size(); i++) {
            if (i == reviewToBeAdded.getReviews().size() - 1) {
                Document review = new Document();
                review.append("rating", reviewToBeAdded.getReviews().get(i).getbRating());
                review.append("text", reviewToBeAdded.getReviews().get(i).getRevString());
                review.append("dateAdded", reviewToBeAdded.getReviews().get(i).getDateAdded());
                review.append("addedByUser", currentUser.getUsername());
                reviews.add(review);
                totalRating += reviewToBeAdded.getReviews().get(i).getbRating();
            } else {
                Document review = new Document();
                review.append("rating", reviewToBeAdded.getReviews().get(i).getbRating());
                review.append("text", reviewToBeAdded.getReviews().get(i).getRevString());
                review.append("dateAdded", reviewToBeAdded.getReviews().get(i).getDateAdded());
                review.append("addedByUser", reviewToBeAdded.getReviews().get(i).getUser().getUsername());
                reviews.add(review);
                totalRating += reviewToBeAdded.getReviews().get(i).getbRating();
            }
        }

        reviewToBeAdded.setRating(totalRating / nrOfRatings);

        revDoc.append("rating", reviewToBeAdded.getRating());
        revDoc.append("username", reviewToBeAdded.getUser().getUsername());

        revDoc.append("authors", authors);
        revDoc.append("reviews", reviews);

        booksCollection.findOneAndReplace(bookDoc, revDoc);

        revDoc = booksCollection.find(revDoc).first();

        int lastBook = reviewToBeAdded.getReviews().size() - 1;

        if (revDoc != null) {
            List<Document> reviewsFromDb = revDoc.getList("reviews", Document.class);

            double rating = reviewsFromDb.get(lastBook).getDouble("rating");
            String text = reviewsFromDb.get(lastBook).getString("text");
            String dateAdded = formattedDate.format(reviewsFromDb.get(lastBook).getDate("dateAdded"));
            String addedByUser = reviewsFromDb.get(lastBook).getString("addedByUser");

            Review revToReturn = new Review(rating, text, dateAdded, new User(addedByUser));
            return revToReturn;
        }
        return null;
    }

    @Override
    public User createAccToDb(User userToBeAdded) {
        Document user = new Document();

        user.append("username", userToBeAdded.getUsername().toLowerCase());
        user.append("password", userToBeAdded.getPassword());

        usersCollection.insertOne(user);

        return userToBeAdded;
    }

    @Override
    public boolean isUsernameAvailable(User user) {
        Document username = usersCollection.find(eq("username", user.getUsername())).first();

        return username == null;
    }

    @Override
    public boolean isUser(User user) {
        Document userDoc = new Document("username", user.getUsername());
        userDoc.append("password", user.getPassword());

        userDoc = usersCollection.find(userDoc).first();

        return userDoc != null;

    }

    @Override
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    @Override
    public boolean isBookReviewedByUser(Book bookToBeReviewed) {
        Document userDoc = new Document("_id", bookToBeReviewed.getBId());

        userDoc.append("reviews.addedByUser", currentUser.getUsername());

        userDoc = booksCollection.find(userDoc).first();

        return userDoc != null;

    }

    @Override
    public List<Book> searchBooksByTitle(String titleIn) {
        Document query = new Document("title", titleIn);
        String pattern = ".*" + query.getString("title") + ".*";

        FindIterable<Document> bookIt = booksCollection.find(regex("title", pattern, "i"));

        ArrayList<Book> list = new ArrayList();
        for (MongoCursor<Document> cursor = bookIt.iterator(); cursor.hasNext();) {
            Document bookDoc = cursor.next();

            ObjectId _id = bookDoc.getObjectId("_id");
            String title = bookDoc.getString("title");
            String isbn = bookDoc.getString("isbn");
            String published = formattedDate.format(bookDoc.getDate("published"));
            String genre = bookDoc.getString("genre");
            double rating = Double.parseDouble(formattedRating.format(bookDoc.getDouble("rating")).replace(",", "."));

            Book book = new Book(_id, title, isbn, published, genre, rating, new User(bookDoc.getString("username")));

            List<Document> authors = bookDoc.getList("authors", Document.class);

            for (int i = 0; i < authors.size(); i++) {
                String name = authors.get(i).getString("name");
                String birthdate = formattedDate.format(authors.get(i).getDate("birthDate"));

                book.addAuthor(new Author(name, birthdate, new User(authors.get(i).getString("addedByUser"))));
            }

            List<Document> reviews = bookDoc.getList("reviews", Document.class);

            if (reviews != null) {
                for (int i = 0; i < reviews.size(); i++) {
                    double rRating = reviews.get(i).getDouble("rating");
                    String text = reviews.get(i).getString("text");
                    String dateAdded = formattedDate.format(reviews.get(i).getDate("dateAdded"));

                    book.addReview(new Review(rRating, text, dateAdded, new User(reviews.get(i).getString("addedByUser"))));
                }
            }
            list.add(book);
        }
        return list;
    }

    @Override
    public List<Book> searchBooksByISBN(String isbnIn) {
        Document query = new Document("isbn", isbnIn);
        String pattern = query.getString("isbn") + ".*";

        FindIterable<Document> bookIt = booksCollection.find(regex("isbn", pattern, "i"));

        ArrayList<Book> list = new ArrayList();
        for (MongoCursor<Document> cursor = bookIt.iterator(); cursor.hasNext();) {
            Document bookDoc = cursor.next();

            ObjectId _id = bookDoc.getObjectId("_id");
            String title = bookDoc.getString("title");
            String isbn = bookDoc.getString("isbn");
            String published = formattedDate.format(bookDoc.getDate("published"));
            String genre = bookDoc.getString("genre");
            double rating = Double.parseDouble(formattedRating.format(bookDoc.getDouble("rating")).replace(",", "."));

            Book book = new Book(_id, title, isbn, published, genre, rating, new User(bookDoc.getString("username")));

            List<Document> authors = bookDoc.getList("authors", Document.class);

            for (int i = 0; i < authors.size(); i++) {
                String name = authors.get(i).getString("name");
                String birthdate = formattedDate.format(authors.get(i).getDate("birthDate"));

                book.addAuthor(new Author(name, birthdate, new User(authors.get(i).getString("addedByUser"))));
            }

            List<Document> reviews = bookDoc.getList("reviews", Document.class);

            if (reviews != null) {
                for (int i = 0; i < reviews.size(); i++) {
                    double rRating = reviews.get(i).getDouble("rating");
                    String text = reviews.get(i).getString("text");
                    String dateAdded = formattedDate.format(reviews.get(i).getDate("dateAdded"));

                    book.addReview(new Review(rRating, text, dateAdded, new User(reviews.get(i).getString("addedByUser"))));
                }
            }
            list.add(book);
        }
        return list;
    }

    @Override
    public List<Book> searchBooksByAuthor(String author) {
        Document query = new Document("authors.name", author);
        String pattern = ".*" + query.getString("authors.name") + ".*";

        FindIterable<Document> bookIt = booksCollection.find(regex("authors.name", pattern, "i"));

        ArrayList<Book> list = new ArrayList();
        for (MongoCursor<Document> cursor = bookIt.iterator(); cursor.hasNext();) {
            Document bookDoc = cursor.next();

            ObjectId _id = bookDoc.getObjectId("_id");
            String title = bookDoc.getString("title");
            String isbn = bookDoc.getString("isbn");
            String published = formattedDate.format(bookDoc.getDate("published"));
            String genre = bookDoc.getString("genre");
            double rating = Double.parseDouble(formattedRating.format(bookDoc.getDouble("rating")).replace(",", "."));

            Book book = new Book(_id, title, isbn, published, genre, rating, new User(bookDoc.getString("username")));

            List<Document> authors = bookDoc.getList("authors", Document.class);

            for (int i = 0; i < authors.size(); i++) {
                String name = authors.get(i).getString("name");
                String birthdate = formattedDate.format(authors.get(i).getDate("birthDate"));

                book.addAuthor(new Author(name, birthdate, new User(authors.get(i).getString("addedByUser"))));
            }

            List<Document> reviews = bookDoc.getList("reviews", Document.class);

            if (reviews != null) {
                for (int i = 0; i < reviews.size(); i++) {
                    double rRating = reviews.get(i).getDouble("rating");
                    String text = reviews.get(i).getString("text");
                    String dateAdded = formattedDate.format(reviews.get(i).getDate("dateAdded"));

                    book.addReview(new Review(rRating, text, dateAdded, new User(reviews.get(i).getString("addedByUser"))));
                }
            }
            list.add(book);
        }
        return list;
    }

    @Override
    public List<Book> searchBooksByRating(double min, double max) {
        Bson filter = and(gte("rating", min), lte("rating", max));

        FindIterable<Document> ratingIt = booksCollection.find(filter);

        ArrayList<Book> list = new ArrayList();
        for (MongoCursor<Document> cursor = ratingIt.iterator(); cursor.hasNext();) {
            Document bookDoc = cursor.next();

            ObjectId _id = bookDoc.getObjectId("_id");
            String title = bookDoc.getString("title");
            String isbn = bookDoc.getString("isbn");
            String published = formattedDate.format(bookDoc.getDate("published"));
            String genre = bookDoc.getString("genre");
            double rating = Double.parseDouble(formattedRating.format(bookDoc.getDouble("rating")).replace(",", "."));

            Book book = new Book(_id, title, isbn, published, genre, rating, new User(bookDoc.getString("username")));

            List<Document> authors = bookDoc.getList("authors", Document.class);

            for (int i = 0; i < authors.size(); i++) {
                String name = authors.get(i).getString("name");
                String birthdate = formattedDate.format(authors.get(i).getDate("birthDate"));

                book.addAuthor(new Author(name, birthdate, new User(authors.get(i).getString("addedByUser"))));
            }

            List<Document> reviews = bookDoc.getList("reviews", Document.class);

            if (reviews != null) {
                for (int i = 0; i < reviews.size(); i++) {
                    double rRating = reviews.get(i).getDouble("rating");
                    String text = reviews.get(i).getString("text");
                    String dateAdded = formattedDate.format(reviews.get(i).getDate("dateAdded"));

                    book.addReview(new Review(rRating, text, dateAdded, new User(reviews.get(i).getString("addedByUser"))));
                }
            }
            list.add(book);
        }
        return list;
    }

    @Override
    public List<Book> searchBooksByGenre(String genreIn) {
        FindIterable<Document> ratingIt = booksCollection.find(eq("genre", genreIn));

        ArrayList<Book> list = new ArrayList();
        for (MongoCursor<Document> cursor = ratingIt.iterator(); cursor.hasNext();) {
            Document bookDoc = cursor.next();

            ObjectId _id = bookDoc.getObjectId("_id");
            String title = bookDoc.getString("title");
            String isbn = bookDoc.getString("isbn");
            String published = formattedDate.format(bookDoc.getDate("published"));
            String genre = bookDoc.getString("genre");
            double rating = Double.parseDouble(formattedRating.format(bookDoc.getDouble("rating")).replace(",", "."));

            Book book = new Book(_id, title, isbn, published, genre, rating, new User(bookDoc.getString("username")));

            List<Document> authors = bookDoc.getList("authors", Document.class);

            for (int i = 0; i < authors.size(); i++) {
                String name = authors.get(i).getString("name");
                String birthdate = formattedDate.format(authors.get(i).getDate("birthDate"));

                book.addAuthor(new Author(name, birthdate, new User(authors.get(i).getString("addedByUser"))));
            }

            List<Document> reviews = bookDoc.getList("reviews", Document.class);

            if (reviews != null) {
                for (int i = 0; i < reviews.size(); i++) {
                    double rRating = reviews.get(i).getDouble("rating");
                    String text = reviews.get(i).getString("text");
                    String dateAdded = formattedDate.format(reviews.get(i).getDate("dateAdded"));

                    book.addReview(new Review(rRating, text, dateAdded, new User(reviews.get(i).getString("addedByUser"))));
                }
            }
            list.add(book);
        }
        return list;
    }
}
