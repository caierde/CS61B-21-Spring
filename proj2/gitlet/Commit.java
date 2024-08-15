package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /* The message of this Commit. */
    private String message;
    /* the time of this commit*/
    private Date commitDate;
    /* the author of this commit*/
    private String commitAuthor;
    /* parent commit of this commit. when Merge this is two parents*/
    List<String> parentsList;
    /* record blods name Map SHA-1 id*/
    TreeMap<String, String> blobSHA1IdMap;
    /* TODO: fill in the rest of this class. */

    public Commit(String message) {
        this.message = message;
        this.commitAuthor = "God Me";
        this.commitDate = new Date();
        this.parentsList = new ArrayList<>();
        this.blobSHA1IdMap = new TreeMap<>();
    }

    public String getMessage() {
        return message;
    }

    public String dateToTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(commitDate);
    }

    public void setInitCommitDate() {
        commitDate = new Date(0);
    }

    public String toSHA1() {
        return Utils.sha1(message, commitAuthor, dateToTimeStamp(), parentsList.toString(), blobSHA1IdMap.toString());
    }

}
