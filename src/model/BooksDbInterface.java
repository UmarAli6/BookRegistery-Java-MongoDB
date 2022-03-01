package model;

import java.util.List;

/**
 * This interface declares methods for querying a Books database. Different
 * implementations of this interface handles the connection and queries to a
 * specific DBMS and database, for example a MySQL or a MongoDB database.
 *
 * @author Umar A & Rabi S
 */
public interface BooksDbInterface {

    /**
     * This method logs you in as an Guest, anon client.
     */
    public void loginAsGuest();

    /**
     * This method logs you in as the user that was inputted. It will
     * log you in as an user client and you will be able manage the books.
     *
     * @param user
     * 
     * @return a {@code boolean} if the login was successful or not 
     */
    public User loginAsUser(User user);
    
    /**
     * This method closes the connection.
     */
    public void disconnect() ;

    /**
     * This method gets the books from the database returns the data.
     *
     * @return a {@code List<Book>} with the book data.
     */
    public List<Book> getTheBooks() ;

    /**
     * This method inserts a complete book to the database.
     *
     * @param bookToBeAdded
     * 
     * @return the {@code Book} you added to the database.
     */
    public Book addBookToDb(Book bookToBeAdded) ;
    
    /**
     * This method searches and deletes the book in the database.
     *
     * @param bookToBeDeleted
     * 
     * @return the deleted {@code Book}
     */
    public Book deleteBookFromDb(Book bookToBeDeleted) ;

    /**
     * This method inserts a complete review to the database.
     *
     * @param reviewToBeAdded
     * 
     * @return the {@code Review} you added to the database.
     */
    public Review addReviewToDb(Book reviewToBeAdded) ;
    
    /**
     * This creates a new user in the database.
     *
     * @param userToBeAdded
     * 
     * @return the created {@code user}
     */
    public User createAccToDb(User userToBeAdded) ;

    /**
     * This method queries the database to check if an user already exists with 
     * the same username
     *
     * @param user
     * 
     * @return a {@code boolean} if the username is available or not
     */
    public boolean isUsernameAvailable(User user) ;

    /**
     * This method queries the database to check if the user exists
     * in the database and the password is correct.
     *
     * @param user

     * @return a {@code boolean} if the user is an user or not
     */
    public boolean isUser(User user) ;

    /**
     * This method checks if the user is logged in as an userclient or
     * guestclient.
     *
     * @return a {@code boolean} if the user is an userclient
     */
    public boolean isLoggedIn() ;

    /**
     * This method checks if the book already has been reviewed by the
     * current logged in user.
     *
     * @param bookToBeReviewed
     * 
     * @return a {@code boolean} if the book has previously been reviewed or not
     */
    public boolean isBookReviewedByUser(Book bookToBeReviewed) ;
    
     /**
     * This method queries the database by searching books by title.
     *
     * @param title
     * 
     * @return a {@code List<Book>} with the returned book results.
     */
    public List<Book> searchBooksByTitle(String title) ;

    /**
     * This method queries the database by searching books by isbn.
     *
     * @param isbn
     * 
     * @return a {@code List<Book>} with the returned book results.
     */
    public List<Book> searchBooksByISBN(String isbn) ;

    /**
     * This method queries the database by searching books by auhtor.
     *
     * @param author
     * 
     * @return a {@code List<Book>} with the returned book results.
     */
    public List<Book> searchBooksByAuthor(String author) ;

    /**
     * This method queries the database by searching books by the rating.
     * Max and min represent the interval the rating should be between.
     *
     * @param min
     * @param max
     * 
     * @return a {@code List<Book>} with the returned book results.
     */
    public List<Book> searchBooksByRating(double min, double max) ;

    /**
     * This method queries the database by searching books by the genre.
     *
     * @param genre
     * 
     * @return a {@code List<Book>} with the returned book results.
     */
    public List<Book> searchBooksByGenre(String genre) ;
}