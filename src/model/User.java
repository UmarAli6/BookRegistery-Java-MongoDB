package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of an user.
 *
 * @author Umar A & Rabi S
 */
public class User {
    private final String username;
    private final String password;
    
    private final ArrayList<Review> theReviews;
    
    public User(String username){
        this.username = username;
        this.password = "";
        this.theReviews = new ArrayList();
    }
    
    public User(String username, String password){
        this.username = username;
        this.password = password;
        this.theReviews = new ArrayList();
    }

    /**
     * Get the username of the user
     *
     * @return a {@code String} with the usernmae
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the users password
     *
     * @return a {@code String} with the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the review with the specified index
     *
     * @param index
     * @return a {@code Review} with the specified index
     */
    public Review getReview(int index) {
        return this.theReviews.get(index);
    }
    
    /**
     * Get the reviews the user has added
     *
     * @return a {@code Listt<Review>} with the reviews
     */
    public List<Review> getReviews() {
        return theReviews;
    }

    @Override
    public String toString() {
        return "User{" + ", username=" + username + ", password=" + password + ", reviews=" + theReviews + '}';
    }
}
