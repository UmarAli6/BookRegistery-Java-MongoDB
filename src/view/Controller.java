package view;

import model.SearchMode;
import model.Book;
import model.BooksDbInterface;
import java.util.List;
import javafx.application.Platform;
import static javafx.scene.control.Alert.AlertType.*;
import model.User;

/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 *
 * @author Umar A & Rabi S
 */
public final class Controller {

    private final DbView booksView;
    private final BooksDbInterface booksDb;

    public Controller(BooksDbInterface booksDb, DbView booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
        initLogin();
    }

    protected void initLogin() {
        new Thread() {
            @Override
            public void run() {
                booksDb.loginAsGuest();
            }
        }.start();
    }

    protected void refreshBooksInView() {
        new Thread() {
            @Override
            public void run() {
                List<Book> list = booksDb.getTheBooks();
                Platform.runLater(() -> booksView.displayBooks(list));
            }
        }.start();
    }

    protected void handleLoginAsGuestEvent() {
        new Thread() {
            @Override
            public void run() {
                if (booksDb.isLoggedIn()) {
                    booksDb.loginAsGuest();
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("You are logged out", INFORMATION, "LOG OUT"));
                }
            }
        }.start();
    }

    

    protected void handleDisconnectEvent() {
        new Thread() {
            @Override
            public void run() {
                booksDb.disconnect();
            }
        }.start();
    }

    protected void handleAddBookDialogEvent(Book book, boolean loggedCheck) {
        new Thread() {
            @Override
            public void run() {
                if (booksDb.isLoggedIn() && !loggedCheck) {
                    Platform.runLater(() -> booksView.showAddBookDialog());
                } else if (booksDb.isLoggedIn() && loggedCheck) {
                    booksDb.addBookToDb(book);
                    List<Book> list = booksDb.getTheBooks();
                    Platform.runLater(() -> booksView.displayBooks(list));
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("You need to log in to add a book", INFORMATION, "NOT LOGGED IN"));
                }
            }
        }.start();
    }

    protected void handleRemoveBookDialogEvent(Book book, boolean loggedCheck) {
        new Thread() {
            @Override
            public void run() {
                if (booksDb.isLoggedIn() && !loggedCheck) {
                    Platform.runLater(() -> booksView.showRemoveBookDialog());
                } else if (booksDb.isLoggedIn() && loggedCheck) {
                    if (booksDb.deleteBookFromDb(book) != null) {
                        List<Book> list = booksDb.getTheBooks();
                        Platform.runLater(() -> booksView.displayBooks(list));
                    } else {
                        Platform.runLater(() -> booksView.showAddReviewDialog());
                        Platform.runLater(() -> booksView.showAlertAndWait("This is not your book.", INFORMATION, "NOT YOUR BOOK"));
                    }
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("You need to log in to remove a book", INFORMATION, "NOT LOGGED IN"));
                }
            }
        }.start();
    }

    protected void handleAddReviewDialogEvent(Book review, boolean loggedCheck) {
        new Thread() {
            @Override
            public void run() {
                if (booksDb.isLoggedIn() && !loggedCheck) {
                    Platform.runLater(() -> booksView.showAddReviewDialog());
                } else if (booksDb.isLoggedIn() && loggedCheck) {
                    booksDb.addReviewToDb(review);
                    List<Book> list = booksDb.getTheBooks();
                    Platform.runLater(() -> booksView.displayBooks(list));
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("You need to log in to review a book", INFORMATION, "NOT LOGGED IN"));
                }
            }
        }.start();
    }
    
    protected void onSearchSelected(String searchFor, SearchMode mode) {
        new Thread() {
            @Override
            public void run() {
                if (searchFor != null && searchFor.length() > 0) {
                    List<Book> result;
                    switch (mode) {
                        case Title:
                            result = booksDb.searchBooksByTitle(searchFor);
                            break;
                        case ISBN:
                            result = booksDb.searchBooksByISBN(searchFor);
                            break;
                        case Author:
                            result = booksDb.searchBooksByAuthor(searchFor);
                            break;
                        case Genre:
                            result = booksDb.searchBooksByGenre(searchFor);
                            break;
                        default:
                            result = null;
                    }
                    if (result == null || result.isEmpty()) {
                        Platform.runLater(() -> booksView.showAlertAndWait("No results found", INFORMATION, "INFORMATION"));
                    } else {
                        Platform.runLater(() -> booksView.displayBooks(result));
                    }
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("Enter a search string", WARNING, "WARNING"));
                }
            }
        }.start();
    }

    protected void onSearchRatingSelected(String min, String max) {
        new Thread() {
            @Override
            public void run() {
                if (min != null && max != null) {
                    {
                        double minD = Double.parseDouble((min.replace(",", ".")));
                        double maxD = Double.parseDouble((max.replace(",", ".")));
                        if (minD >= 0.0 && maxD <= 5.0) {
                            List<Book> result = booksDb.searchBooksByRating(minD, maxD);

                            if (result == null || result.isEmpty()) {
                                Platform.runLater(() -> booksView.showAlertAndWait("No results found", INFORMATION, "INFORMATION"));
                            } else {
                                Platform.runLater(() -> booksView.displayBooks(result));
                            }
                        }
                    }
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("Enter a search string", WARNING, "WARNING"));
                }
            }
        }.start();
    }

    protected void handleInvalidInput(String invalidMsg) {
        booksView.showAlertAndWait(invalidMsg, ERROR, "INVALID INPUT");
    }
    
     protected boolean isBookReviewed(Book book) {
        if (book.getReviews().size() > 0) {
            return true;
        } else {
            booksView.showAlertAndWait("This book has no reviews", INFORMATION, "NO REVIEWS");
            return false;
        }
    }

    protected void isUsernameAvailable(User newUser, boolean loggedCheck) {
        new Thread() {
            @Override
            public void run() {
                if (!booksDb.isLoggedIn() && !loggedCheck) {
                    Platform.runLater(() -> booksView.showCreateAccDialog());
                } else if (!booksDb.isLoggedIn() && loggedCheck) {
                    if (booksDb.isUsernameAvailable(newUser)) {
                        booksDb.createAccToDb(newUser);
                    } else {
                        Platform.runLater(() -> booksView.showCreateAccDialog());
                        Platform.runLater(() -> booksView.showAlertAndWait("Username Taken", INFORMATION, ""));
                    }
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("You are already logged in", INFORMATION, "ALREADY LOGGED IN"));
                }
            }
        }.start();
    }

    protected void isUser(User user, boolean loggedCheck) {
        new Thread() {
            @Override
            public void run() {
                if (!booksDb.isLoggedIn() && !loggedCheck) {
                    Platform.runLater(() -> booksView.showLoginDialog());
                } else if (!booksDb.isLoggedIn() && loggedCheck) {
                    if (booksDb.isUser(user)) {
                        User current = booksDb.loginAsUser(user);
                        Platform.runLater(() -> booksView.setCurrentUsername(current.getUsername()));
                    } else {
                        Platform.runLater(() -> booksView.showLoginDialog());
                        Platform.runLater(() -> booksView.showAlertAndWait("Wrong username or password", ERROR, "ERROR"));
                    }
                } else {
                    Platform.runLater(() -> booksView.showAlertAndWait("You are already logged in", INFORMATION, "ALREADY LOGGED IN"));
                }
            }
        }.start();
    }

    protected void isBookReviewedByUser(Book bookToBeReviewed) {
        new Thread() {
            @Override
            public void run() {
                if (booksDb.isBookReviewedByUser(bookToBeReviewed)) {
                    Platform.runLater(() -> booksView.showAddReviewDialog());
                    Platform.runLater(() -> booksView.showAlertAndWait("You have already reviewed this book", INFORMATION, "CHOOSE A DIFFERENT BOOK"));
                } else {
                    Platform.runLater(() -> booksView.showWriteReviewDialog(bookToBeReviewed));
                }
            }
        }.start();
    }

   
}
