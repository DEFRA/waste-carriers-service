package uk.gov.ea.wastecarrier.services.support;

import uk.gov.ea.wastecarrier.services.core.User;

/**
 * A class which implements the builder pattern for generating test
 * entities. Java does not have default parameters but instead utilises
 * method overloading to achieve the same effect. To make calling code more
 * explicit we implement the builder pattern e.g.
 *
 * User usr1 = new UserBuilder()
 *                          .build();
 * User usr2 = new UserBuilder()
 *                          .email("jason.isaacs@example.com")
 *                          .build();
 *
 * Note: This builder does not create all the fields in the user object at this
 * time, only those needed for the unit tests.
 */
public class UserBuilder {

    private String email = "jason.isaacs@example.com";

    public UserBuilder() { }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public User build() {
        User user = new User();

        user.setEmail(this.email);

        return user;
    }
}
