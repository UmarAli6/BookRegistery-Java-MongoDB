package model;

import java.time.LocalDate;

/**
 * Representation of an author.
 *
 * @author Umar A & Rabi S
 */
public class Author {

    private final String name;
    private final LocalDate dateOfBirth;
    private final User user;

    public Author(String name, String dateOfBirth, User user) {
        this.name = name;
        this.dateOfBirth = LocalDate.parse(dateOfBirth);
        this.user = user;
    }
    
    public Author(String name, String dateOfBirth) {
        this.name = name;
        this.dateOfBirth = LocalDate.parse(dateOfBirth);
        this.user = null;
    }

    /**
     * Get name of the author
     *
     * @return a {@code String} of the authors name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the author date of birth
     *
     * @return a {@code LocalDate} of the authors date of birth
     */
    public LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    /**
     * Get the user that added the book
     *
     * @return the {@code User} that added the book 
     */
    public User getUser() {
        return this.user;
    }

    @Override
    public String toString() {
        return "Author{" + "name=" + name + ", dateOfBirth=" + dateOfBirth + ", user=" + user + '}';
    }
}
