package model;

import java.time.LocalDate;

/**
 * Representation of a review of a book.
 *
 * @author Umar A & Rabi S
 */
public class Review {

    private final double bRating;
    private final String revString;
    private final LocalDate dateAdded;
    private final User user;

    public Review(double bRating, String revString, String dateAdded, User user) {
        this.bRating = bRating;
        this.revString = revString;
        this.dateAdded = LocalDate.parse(dateAdded);
        this.user = user;
    }

    public Review(double bRating, String revString, String dateAdded) {
        this.bRating = bRating;
        this.revString = revString;
        this.dateAdded = LocalDate.parse(dateAdded);
        this.user = null;
    }

    /**
     * Get the rating of the book in the reivew
     *
     * @return an {@code int} of the review's book rating
     */
    public double getbRating() {
        return this.bRating;
    }

    /**
     * Get the rating string
     *
     * @return a {@code String} of the review string
     */
    public String getRevString() {
        return this.revString;
    }

    /**
     * Get the date the review was added
     *
     * @return an {@code LocalDate} the review was added
     */
    public LocalDate getDateAdded() {
        return this.dateAdded;
    }

    /**
     * Get the user that wrote the review
     *
     * @return an {@code User} that wrote the reviews
     */
    public User getUser() {
        return this.user;
    }

    @Override
    public String toString() {
        return "Review{" + ", bRating=" + bRating + ", revString=" + revString + ", dateAdded=" + dateAdded + ", user=" + user + '}';
    }

}
