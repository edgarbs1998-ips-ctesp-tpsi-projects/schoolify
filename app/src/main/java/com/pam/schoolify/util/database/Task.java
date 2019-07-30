package com.pam.schoolify.util.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Task data structure class
 */
public class Task implements Parcelable {

    /**
     * Data fields
     */
    private long id;
    private long user;
    private String title;
    private long type;
    private long datetime;
    private String description;

    /**
     * Constructor used when retrieving data from database
     *
     * @param id          task id
     * @param user        task user
     * @param title       task title
     * @param type        task type
     * @param datetime    task datetime
     * @param description task description
     */
    public Task(long id, long user, String title, long type, long datetime, String description) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.type = type;
        this.datetime = datetime;
        this.description = description;
    }

    /**
     * Constructor
     *
     * @param user        task user
     * @param title       task title
     * @param type        task type
     * @param datetime    task datetime
     * @param description task description
     */
    public Task(long user, String title, long type, long datetime, String description) {
        this(-1, user, title, type, datetime, description);
    }

    /**
     * Parcel constructor
     *
     * @param in parcel
     */
    public Task(Parcel in) {
        id = in.readLong();
        user = in.readLong();
        title = in.readString();
        type = in.readLong();
        datetime = in.readLong();
        description = in.readString();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public long getDateTime() {
        return datetime;
    }

    public void setDateTime(long datetime) {
        this.datetime = datetime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return title;
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
        dest.writeString(title);
        dest.writeLong(type);
        dest.writeLong(datetime);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
