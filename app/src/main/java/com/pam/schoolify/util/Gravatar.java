package com.pam.schoolify.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle Gravatar images URLs
 */
public class Gravatar {

    private final static int DEFAULT_SIZE = 80;
    private final static String HTTP = "http://";
    private final static String HTTPS = "https://";
    private final static String GRAVATAR_URL = "www.gravatar.com/avatar/";
    private static final GravatarRating DEFAULT_RATING = GravatarRating.GENERAL_AUDIENCES;
    private static final GravatarDefaultImage DEFAULT_IMAGE = GravatarDefaultImage.GRAVATAR_ICON;

    private int size = DEFAULT_SIZE;
    private GravatarRating rating = DEFAULT_RATING;
    private GravatarDefaultImage defaultImage = DEFAULT_IMAGE;
    private boolean useHttps;

    /**
     * Return a gravatar url prefixed with https instead of default http
     *
     * @return {@link Gravatar}
     */
    public Gravatar setUseHttps(boolean useHttps) {
        this.useHttps = useHttps;
        return this;
    }

    /**
     * Specify a gravatar size between 1 and 2048 pixels. If you omit this, a
     * default size of 80 pixels is used.
     *
     * @return {@link Gravatar}
     */
    public Gravatar setSize(int sizeInPixels) {
        if (sizeInPixels < 1 || sizeInPixels > 2048) {
            throw new IllegalArgumentException("sizeInPixels needs to be between 1 and 2048");
        }

        this.size = sizeInPixels;
        return this;
    }

    /**
     * Specify a rating to ban gravatar images with explicit content.
     *
     * @return {@link Gravatar
     */
    public Gravatar setRating(GravatarRating rating) {
        if (rating == null) {
            throw new NullPointerException("rating");
        }

        this.rating = rating;
        return this;
    }

    /**
     * Specify the default image to be produced if no gravatar image was found.
     *
     * @return {@link Gravatar}
     */
    public Gravatar setDefaultImage(GravatarDefaultImage defaultImage) {
        if (defaultImage == null) {
            throw new NullPointerException("defaultImage");
        }

        this.defaultImage = defaultImage;
        return this;
    }

    /**
     * Returns the Gravatar URL for the given email address.
     *
     * @return {@link String}
     */
    public String getUrl(String email) {
        if (email == null) {
            throw new NullPointerException("email");
        }

        // hexadecimal MD5 hash of the requested user's lowercased email address
        // with all whitespace trimmed
        String emailHash = Util.md5Hex(email.toLowerCase().trim());
        String params = formatUrlParameters();
        return (useHttps ? HTTPS : HTTP) + GRAVATAR_URL + emailHash + ".jpg" + params;
    }

    /**
     * Formats Gravatar URL parameters
     *
     * @return {@link String}
     */
    private String formatUrlParameters() {
        List<String> params = new ArrayList<>();

        if (size != DEFAULT_SIZE)
            params.add("s=" + size);
        if (rating != DEFAULT_RATING)
            params.add("r=" + rating.getCode());
        if (defaultImage != GravatarDefaultImage.GRAVATAR_ICON)
            params.add("d=" + defaultImage.getCode());

        if (params.isEmpty())
            return "";
        else
            return "?" + TextUtils.join("&", params);
    }

    /**
     * Enum of Gravatar ratings
     */
    public enum GravatarRating {

        GENERAL_AUDIENCES("g"),

        PARENTAL_GUIDANCE_SUGGESTED("pg"),

        RESTRICTED("r"),

        XPLICIT("x");

        private String code;

        GravatarRating(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

    }

    /**
     * Enum of Gravatar default images
     */
    public enum GravatarDefaultImage {

        GRAVATAR_ICON(""),

        MYSTER_MAN("mm"),

        IDENTICON("identicon"),

        MONSTERID("monsterid"),

        WAVATAR("wavatar"),

        RETRO("retro"),

        ROBOHASH("robohash"),

        BLANK("blank");

        private String code;

        GravatarDefaultImage(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
