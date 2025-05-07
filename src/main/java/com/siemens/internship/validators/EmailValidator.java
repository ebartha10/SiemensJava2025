package com.siemens.internship.validators;

import com.siemens.internship.Item;

public class EmailValidator {
    public boolean validate(String email) {
        // check if the email is null or empty
        if (email == null || email.isEmpty()) {
            return false;
        }

        // ascii characters email regex
        String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        // return true if the given email matches the regex
        return email.matches(emailRegex);
    }
}
