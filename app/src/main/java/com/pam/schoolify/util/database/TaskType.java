package com.pam.schoolify.util.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * TaskType data structure class
 */
public class TaskType implements Parcelable {

    /**
     * Data fields
     */
    private long id;
    private long user;
    private String type;
    private String color;

    /**
     * Constructor used when retrieving data from database
     *
     * @param id    task type id
     * @param user  task type user
     * @param type  task type name
     * @param color task type color
     */
    public TaskType(long id, long user, String type, String color) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.color = color;
    }

    /**
     * Constructor
     *
     * @param user  task type user
     * @param type  task type name
     * @param color task type color
     */
    public TaskType(long user, String type, String color) {
        this(-1, user, type, color);
    }

    /**
     * Parcel constructor
     *
     * @param in parcel
     */
    public TaskType(Parcel in) {
        id = in.readLong();
        user = in.readLong();
        type = in.readString();
        color = in.readString();
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

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getColor() {
        return Integer.parseInt(color);
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return type;
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
        dest.writeLong(user);
        dest.writeString(type);
        dest.writeString(color);
    }

    public static final Parcelable.Creator<TaskType> CREATOR = new Parcelable.Creator<TaskType>() {
        @Override
        public TaskType createFromParcel(Parcel in) {
            return new TaskType(in);
        }

        @Override
        public TaskType[] newArray(int size) {
            return new TaskType[size];
        }
    };
}
