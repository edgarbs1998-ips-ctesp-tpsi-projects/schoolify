package com.pam.schoolify.util.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.pam.schoolify.util.Util;

import java.util.regex.Pattern;

/**
 * User data structure class
 */
public class User implements Parcelable {

    /**
     * Data fields
     */
    private long id;
    private String email;
    private String name;
    private String password;
    private String token;

    /**
     * Constructor used when retrieving data from database
     *
     * @param id       user id
     * @param email    user email address
     * @param name     user name
     * @param password user password
     * @param token    user token
     */
    public User(long id, String email, String name, String password, String token) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.token = token;
    }

    /**
     * Constructor
     *
     * @param email    user email address
     * @param name     user name
     * @param password user password
     */
    public User(String email, String name, String password) {
        this(-1, email, name, password, null);

        this.password = Util.sha256Hex(password);
    }

    /**
     * Parcel constructor
     *
     * @param in parcel
     */
    public User(Parcel in) {
        id = in.readLong();
        email = in.readString();
        name = in.readString();
        password = in.readString();
        token = in.readString();
    }

    /**
     * Getters and setters
     */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = Util.sha256Hex(password);
    }

    public String getToken() {
        return token;
    }

    public String createToken() {
        String token = Util.sha256Hex(email + name + System.currentTimeMillis());
        this.token = token;
        return this.token;
    }

    /**
     * Check if a password is valid based on the default pattern
     *
     * @param password password to check
     * @return true if valid, false otherwise
     */
    public static boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$";
        return Pattern.compile(passwordPattern).matcher(password).matches();
    }

    @Override
    public String toString() {
        return email;
    }

    /**
     * Parcel handling
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeString(password);
        dest.writeString(token);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
