package gitlet;

import edu.princeton.cs.algs4.ST;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /* The current working directory. */
    /*
     *   .gitlet
     *      |--objects
     *      |     |--commit(an object of Commit)
     *      |     |--blob(only the copy of the file)
     *      |--refs
     *      |    |--heads
     *      |         |--master(commitid[SHA-1])
     *      |--HEAD(branchname:refs/heads/master)
     *      |--stage
     *      |     |--addstage(TreeMap)
     *      |     |--removestage(TreeMap)
     *
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /* The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /* the .gitlet/objects folder*/
    public static final File OBJECTS_FOLDER = join(GITLET_DIR, "objects");
    public static final File COMMIT_FOLDER = join(OBJECTS_FOLDER, "commit");
    public static final File BLOB_FOLDER = join(OBJECTS_FOLDER, "blob");
    /* the .gitlet/refs folder*/
    public static final File REFS_FOLDER = join(GITLET_DIR, "refs");
    public static final File HEADS_FOLDER = join(REFS_FOLDER, "heads");
    /* the .gitlet/HEAD file*/
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    /* the ,gitlet/stage folder*/
    public static final File STAGE_FOLDER = join(GITLET_DIR, "stage");
    public static final File ADDSTAGE_FILE = join(STAGE_FOLDER, "addstage");
    public static final File REMOVESTAGE_FILE = join(STAGE_FOLDER, "removestage");

    /* TODO: fill in the rest of this class. */

    /**
     * helper method: get the head commit
     * @return HEAD commit
     */
    public static File getHeadBranchFile() {
        return Utils.join(GITLET_DIR, Utils.readContentsAsString(HEAD_FILE).split("/"));
    }

    /**
     * helper method: get the head commit
     * @return HEAD commit
     */
    public static Commit getHeadCommit() {
        String[] temp = Utils.readContentsAsString(HEAD_FILE).split("/");
        return Utils.readObject(Utils.join(COMMIT_FOLDER, readContentsAsString(Utils.join(GITLET_DIR, temp))), Commit.class);
    }

    /**
     * get the working directory untracked file list
     * @return untrackedFileList
     */
    public static List<String> getUntrackedFileList() {
        List<String> workDirectoryFileList = Utils.plainFilenamesIn(CWD);
        Commit headCommit = getHeadCommit();
        List<String> untrackedFileList = new LinkedList<String>();
        for (int i = 0; i < workDirectoryFileList.size(); i++) {
            String tempFileName = workDirectoryFileList.get(i);
            if ((!Utils.readObject(ADDSTAGE_FILE, TreeMap.class).containsKey(tempFileName) &&
                    !headCommit.blobSHA1IdMap.containsKey(tempFileName))
                    || Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).containsKey(tempFileName)) {
                untrackedFileList.add(tempFileName);
            }
        }
        return untrackedFileList;
    }

    /**
     * helper method: checkout the givenCommit file to the working directory
     */
    public static void checkoutFile(Commit givenCommit) {
        Commit headCommit = getHeadCommit();
        List<String> workDirectoryFileList = Utils.plainFilenamesIn(CWD);
        List<String> untrackedFileList = getUntrackedFileList();
        /* If a working file is untracked in the current branch and would be overwritten by the checkout*/
        for (int i = 0; i < untrackedFileList.size(); i++) {
            String untrackedFileName = untrackedFileList.get(i);
            if (!givenCommit.blobSHA1IdMap.containsKey(untrackedFileName)
                    || !givenCommit.blobSHA1IdMap.get(untrackedFileName).equals(Utils.sha1(untrackedFileName, Utils.readContentsAsString(Utils.join(CWD, untrackedFileName))))){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        /* delete all the files and copy all files in that branch*/
        for (int i = 0; i < workDirectoryFileList.size(); i++) {
            String tempFileName = workDirectoryFileList.get(i);
            Utils.join(CWD, tempFileName).delete();
        }

        givenCommit.blobSHA1IdMap.forEach((key, value) -> {
            File checkoutFile = Utils.join(CWD, key);
            try {
                checkoutFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Utils.writeContents(checkoutFile, Utils.readContentsAsString(Utils.join(BLOB_FOLDER, value)));
        });
    }

    /**
     * helper method: bfs the commit graph
     * @param curCommit
     * @param currentBranchFlowMap
     */
    public static void bfs(Commit curCommit, TreeMap<String, String> currentBranchFlowMap) {
        //use queue (non-recursion)
        Queue<Commit> que = new LinkedList<>();
        if(curCommit != null)
            que.add(curCommit);
        while(!que.isEmpty()){
            //Note: the Size is key
            int size = que.size();
            for(int i = 0; i < size; i++){
                Commit cur = que.peek();
                currentBranchFlowMap.put(cur.toSHA1(), cur.toSHA1());
                que.poll();
                for (int j = 0; j < cur.parentsList.size(); j++) {
                    que.add(Utils.readObject(Utils.join(COMMIT_FOLDER, cur.parentsList.get(i)), Commit.class));
                }
            }
        }
    }

    /**
     * helper method: return the split point
     * @param givenBranchCommit
     * @param currentBranchFlowMap
     * @return Split point commit
     */
    public static Commit getSplitPoint(Commit givenBranchCommit, TreeMap<String, String> currentBranchFlowMap) {
        //use queue (non-recursion)
        Queue<Commit> que = new LinkedList<>();
        if(givenBranchCommit != null)
            que.add(givenBranchCommit);
        while(!que.isEmpty()){
            //Note: the Size is key
            int size = que.size();
            for(int i = 0; i < size; i++){
                Commit cur = que.peek();
                /* find the split point*/
                if (currentBranchFlowMap.containsKey(cur.toSHA1())) {
                    return cur;
                }
                que.poll();
                for (int j = 0; j < cur.parentsList.size(); j++) {
                    que.add(Utils.readObject(Utils.join(COMMIT_FOLDER, cur.parentsList.get(i)), Commit.class));
                }
            }
        }
        return null;
    }

    /**
     * help write content to the conflict file in your working directory
     * @param currentBranchFile
     * @param givenBranchFile
     */
    public static void mergeConflictChangeFileContent(File currentBranchFile, File givenBranchFile) {
        try {
            currentBranchFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String conflictFileContentString = "<<<<<<< HEAD\n";
        conflictFileContentString += Utils.readContentsAsString(currentBranchFile);
        conflictFileContentString += "\n=======\n";
        if (givenBranchFile.exists()) {
            conflictFileContentString += Utils.readContentsAsString(givenBranchFile);
            conflictFileContentString += "\n";
        }
        conflictFileContentString += ">>>>>>>";
        Utils.writeContents(currentBranchFile, conflictFileContentString);
        gitletAdd(currentBranchFile.getName());
    }

    /* init the .git*/
    public static void gitletInit() {
        /* if the directory has already had .git*/
        if (!GITLET_DIR.mkdir()) {
            System.out.println("A Gitlet version-control" +
                    " system already exists in the current directory.");
            return;
        }
        /* create the skeleton of the .git directory*/
        OBJECTS_FOLDER.mkdir();
        COMMIT_FOLDER.mkdir();
        BLOB_FOLDER.mkdir();
        REFS_FOLDER.mkdir();
        STAGE_FOLDER.mkdir();
        try {
            ADDSTAGE_FILE.createNewFile();
            REMOVESTAGE_FILE.createNewFile();
            HEAD_FILE.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /* create init commit*/
        Commit initCommit = new Commit("initial commit");
        initCommit.setInitCommitDate();
        /* create master branch*/
        HEADS_FOLDER.mkdir();
        File MASTER_FILE = join(HEADS_FOLDER, "master");
        try {
            MASTER_FILE.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /* write SHA-1 of commit into master branch*/
        /* here is a meaningful skill. the sha1 can not after commit serializable to byte.
         * because serializable file need first sha1 as file name. hahaha
         * so we cannot use the whole commit as sha1
         */
        String commitSHAId = initCommit.toSHA1();
        Utils.writeContents(MASTER_FILE, commitSHAId);
        File commitSerializeFile = join(COMMIT_FOLDER, commitSHAId);
        Utils.writeObject(commitSerializeFile, initCommit);
        Utils.writeContents(HEAD_FILE, "refs/heads/master");
        /* init an initial map in addstage*/
        TreeMap<String, String> addStageBlodSHAMap = new TreeMap<>();
        Utils.writeObject(ADDSTAGE_FILE, addStageBlodSHAMap);
        /* init an initial map in removestage*/
        TreeMap<String, String> removeStageBlodSHAMap = new TreeMap<>();
        Utils.writeObject(REMOVESTAGE_FILE, removeStageBlodSHAMap);
    }

    public static void gitletAdd(String addFileName) {
        File addFile= Utils.join(CWD, addFileName);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        /* get Head commit blobSHA1IdMap*/
        Commit headCommit = getHeadCommit();
        TreeMap<String, String> headCommitBlobSHA1IdMap = headCommit.blobSHA1IdMap;
        /* each add need to compare addFile content to HeadCommitBlobSHA1IdMap content*/
        String blobSHAId = Utils.sha1(addFileName, Utils.readContents(addFile));
        /* if equal the current branch commit blob. then rm the addstage relevant blob*/
        if (blobSHAId.equals(headCommitBlobSHA1IdMap.get(addFileName))) {
            TreeMap<String, String> addStageBlodSHAMap = Utils.readObject(ADDSTAGE_FILE, TreeMap.class);
            if (addStageBlodSHAMap.containsKey(addFileName)) {
                addStageBlodSHAMap.remove(addFileName);
            }
            return;
        }
        /* if the file has been in remove stage, then we delete it in remove stage */
        TreeMap<String, String> removeStageBlodSHAMap = Utils.readObject(REMOVESTAGE_FILE, TreeMap.class);
        if (removeStageBlodSHAMap.containsKey(addFileName)) {
            removeStageBlodSHAMap.remove(addFileName);
            Utils.writeObject(REMOVESTAGE_FILE, removeStageBlodSHAMap);
        }
        /* create relevant blob*/
        File addBlobFile = Utils.join(BLOB_FOLDER, blobSHAId);
        try {
            addBlobFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeContents(addBlobFile, Utils.readContents(addFile));
        /* map the new blob in addstage*/
        TreeMap<String, String> addStageBlodSHAMap = Utils.readObject(ADDSTAGE_FILE, TreeMap.class);
        addStageBlodSHAMap.put(addFileName, blobSHAId);
        Utils.writeObject(ADDSTAGE_FILE, addStageBlodSHAMap);
    }

    public static void gitletCommit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (Utils.readObject(ADDSTAGE_FILE, TreeMap.class).isEmpty() && Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        /*  use addstage map to update parent Commit blobSHA1IdMap */
        Commit newCommit = new Commit(message);
        Commit headCommit = getHeadCommit();
        newCommit.blobSHA1IdMap.putAll(headCommit.blobSHA1IdMap);
        newCommit.blobSHA1IdMap.putAll(Utils.readObject(ADDSTAGE_FILE, TreeMap.class));
        /* use removestage map to update parent Commit blobSHA1IdMap*/
        Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).keySet().forEach(newCommit.blobSHA1IdMap::remove);
        /* update the parentsList*/
        newCommit.parentsList.add(headCommit.toSHA1());
        /* create relevant commit*/
        File addCommitFile = Utils.join(COMMIT_FOLDER, newCommit.toSHA1());
        try {
            addCommitFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeObject(addCommitFile, newCommit);
        /* update the current branch*/
        Utils.writeContents(Utils.join(GITLET_DIR, Utils.readContentsAsString(HEAD_FILE).split("/")), newCommit.toSHA1());
        /* the staging area is cleared after a commit*/
        Utils.writeObject(ADDSTAGE_FILE, new TreeMap<>());
        Utils.writeObject(REMOVESTAGE_FILE, new TreeMap<>());
    }

    public static void gitletRm(String rmFileName) {
        /* judge this file if is staged or tracked by the head commit*/
        Commit headCommit = getHeadCommit();
        TreeMap<String, String> addStageBlodSHAMap = Utils.readObject(ADDSTAGE_FILE, TreeMap.class);
        if (!headCommit.blobSHA1IdMap.containsKey(rmFileName) &&  !addStageBlodSHAMap.containsKey(rmFileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (addStageBlodSHAMap.containsKey(rmFileName)) {
            /* Unstage the file if it is currently staged for addition*/
            addStageBlodSHAMap.remove(rmFileName);
            /* !!!need to update ADDSTAGE_FILE*/
            Utils.writeObject(ADDSTAGE_FILE, addStageBlodSHAMap);
        }
        /* update the removestage*/
        if (headCommit.blobSHA1IdMap.containsKey(rmFileName)) {
            TreeMap<String, String> removeStageBlodSHAMap = Utils.readObject(REMOVESTAGE_FILE, TreeMap.class);
            if (headCommit.blobSHA1IdMap.containsKey(rmFileName)) {
                removeStageBlodSHAMap.put(rmFileName, headCommit.blobSHA1IdMap.get(rmFileName));
            } else {
                removeStageBlodSHAMap.put(rmFileName, addStageBlodSHAMap.get(rmFileName));
            }
            /* !!!need to update REMOVESTAGE_FILE*/
            Utils.writeObject(REMOVESTAGE_FILE, removeStageBlodSHAMap);
            /* delete the file from the working directory*/
            File rmFile = Utils.join(CWD, rmFileName);
            if (rmFile.exists()) {
                rmFile.delete();
            }
        }
    }

    public static void gitletLog() {
        /* get the parent commit SHA1-id*/
        Commit curCommit = getHeadCommit();
       while (true) {
           System.out.println("===");
           System.out.println("commit " + curCommit.toSHA1());
           if (curCommit.parentsList.size() == 2) {
               System.out.println("Merge: " + curCommit.parentsList.get(0).substring(0, 7)
               + " " + curCommit.parentsList.get(1).substring(0, 7));
           }
           System.out.println("Date: " + curCommit.dateToTimeStamp());
           System.out.println(curCommit.getMessage());
           System.out.println();
           if (curCommit.parentsList.isEmpty()) {
               break;
           } else {
               curCommit = Utils.readObject(Utils.join(COMMIT_FOLDER, curCommit.parentsList.get(0)), Commit.class);
           }
       }
    }

    public static void gitletGlobalLog() {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT_FOLDER);
        for (String commitSHA1Name : commitList) {
            Commit curCommit = Utils.readObject(Utils.join(COMMIT_FOLDER, commitSHA1Name), Commit.class);
            System.out.println("===");
            System.out.println("commit " + curCommit.toSHA1());
            if (curCommit.parentsList.size() == 2) {
                System.out.println("Merge: " + curCommit.parentsList.get(0).substring(0, 7)
                        + " " + curCommit.parentsList.get(1).substring(0, 7));
            }
            System.out.println("Date: " + curCommit.dateToTimeStamp());
            System.out.println(curCommit.getMessage());
            System.out.println();
        }
    }

    public static void gitletFind(String message) {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT_FOLDER);
        boolean hasThisMessage = false;
        for (String commitSHA1Name : commitList) {
            Commit tempCommit = Utils.readObject(Utils.join(COMMIT_FOLDER, commitSHA1Name), Commit.class);
            if (tempCommit.getMessage().equals(message)) {
                hasThisMessage = true;
                System.out.println(commitSHA1Name);
            }
        }
        if (!hasThisMessage) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void gitletStatus() {
        List<String> branchList = Utils.plainFilenamesIn(HEADS_FOLDER);
        /* print "Branches"*/
        System.out.println("=== Branches ===");
        for (String branchName : branchList) {

            /* find if equal HEAD commit*/
            if (branchName.equals(Utils.readContentsAsString(HEAD_FILE).split("/")[2])) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();
        /* print "Staged Files"*/
        System.out.println("=== Staged Files ===");
        Utils.readObject(ADDSTAGE_FILE, TreeMap.class).forEach((key, value) -> {
            System.out.println(key);
        });
        System.out.println();
        /* print "Removed Files"*/
        System.out.println("=== Removed Files ===");
        Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).forEach((key, value) -> {
            System.out.println(key);
        });
        System.out.println();
        /* print "Modifications Not Staged For Commit"*/
        System.out.println("=== Modifications Not Staged For Commit ===");
        // are extra credit, worth 32 points. Feel free to leave them blank (leaving just the headers).
        List<String> workDirectoryFileList = Utils.plainFilenamesIn(CWD);
        /* Staged for addition, but with different contents than in the working directory; or
           Staged for addition, but deleted in the working directory;
         */
        Utils.readObject(ADDSTAGE_FILE, TreeMap.class).forEach((key, value) -> {
            if (!workDirectoryFileList.contains(key) || !value.equals(Utils.sha1(key, Utils.readContentsAsString(Utils.join(CWD, (String) key))))) {
                System.out.print(key);
                if (!workDirectoryFileList.contains(key)) {
                    System.out.println(" (deleted)");
                } else {
                    System.out.println(" (modified)");
                }
            }
        });
        /* Tracked in the current commit, changed in the working directory, but not staged; or
         *  Not staged for removal, but tracked in the current commit and deleted from the working directory.
         */
        Commit headCommit = getHeadCommit();
        headCommit.blobSHA1IdMap.forEach((key, value) -> {
            if ((!workDirectoryFileList.contains(key) && !Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).containsKey(key))
                    || (workDirectoryFileList.contains(key) && !value.equals(Utils.sha1(key, Utils.readContentsAsString(Utils.join(CWD, key)))))) {
                System.out.print(key);
                if (!workDirectoryFileList.contains(key) && !Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).containsKey(key)) {
                    System.out.println(" (deleted)");
                } else {
                    System.out.println(" (modified)");
                }
            }
        });
        System.out.println();
        /* print "Untracked Files"*/
        System.out.println("=== Untracked Files ===");
        // are extra credit, worth 32 points. Feel free to leave them blank (leaving just the headers).
        List<String> untrackedFileList = getUntrackedFileList();
        for (int i = 0; i < untrackedFileList.size(); i++) {
            System.out.println(untrackedFileList.get(i));
        }
        System.out.println();
    }

    public static void gitletCheckout(String[] args) {
        /* java gitlet.Main checkout -- [file name].
         * "--" is only one argument
         */
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            /* copy the blob to the file*/
            String fileName = args[2];
            Commit headCommit = getHeadCommit();
            /* Failure cases*/
            if (!headCommit.blobSHA1IdMap.containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            File checkoutFile = Utils.join(CWD, fileName);
            try {
                checkoutFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Utils.writeContents(checkoutFile, Utils.readContentsAsString(Utils.join(BLOB_FOLDER, headCommit.blobSHA1IdMap.get(fileName))));
        } else if (args.length == 4) {
            /* java gitlet.Main checkout [commit id] -- [file name]*/
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            String checkoutCommitId = args[1];
            String fileName = args[3];
            List<String> commitIdList = Utils.plainFilenamesIn(COMMIT_FOLDER);
            /* Failure cases*/
            if (!commitIdList.contains(checkoutCommitId)) {
                System.out.println("No commit with that id exists.");
                return;
            }
            Commit checkoutCommit = Utils.readObject(Utils.join(COMMIT_FOLDER, checkoutCommitId), Commit.class);
            if (!checkoutCommit.blobSHA1IdMap.containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            /* exist this file. so we copy the blob to file*/
            File checkoutFile = Utils.join(CWD, fileName);
            try {
                checkoutFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Utils.writeContents(checkoutFile, Utils.readContentsAsString(Utils.join(BLOB_FOLDER, checkoutCommit.blobSHA1IdMap.get(fileName))));
        } else if (args.length == 2) {
            /* java gitlet.Main checkout [branch name]*/
            String branchName = args[1];
            /* Failure cases*/
            if (!Utils.join(HEADS_FOLDER, branchName).exists()) {
                System.out.println("No such branch exists.");
                return;
            }
            if (readContentsAsString(HEAD_FILE).split("/")[2].equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            Commit branchCommit = Utils.readObject(Utils.join(COMMIT_FOLDER, Utils.readContentsAsString(Utils.join(HEADS_FOLDER, branchName))), Commit.class);
            checkoutFile(branchCommit);
            /* clear the staging area and update the current branch*/
            Utils.writeContents(HEAD_FILE, "refs/heads/" + branchName);
            Utils.writeObject(ADDSTAGE_FILE, new TreeMap<>());
            Utils.writeObject(REMOVESTAGE_FILE, new TreeMap<>());
        } else {
            throw new RuntimeException("Invalid syntax of input argument");
        }
    }

    public static void gitletBranch(String branchName) {
        if (Utils.join(HEADS_FOLDER, branchName).exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        /* create a new branch*/
        File newBranchFile = Utils.join(HEADS_FOLDER, branchName);
        try {
            newBranchFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeContents(newBranchFile, getHeadCommit().toSHA1());
    }

    public static void gitletRmbranch(String rmbranchName) {
        if (!Utils.join(HEADS_FOLDER, rmbranchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        /* judge if the rmbranch is the current branch*/
        if (Utils.readContentsAsString(HEAD_FILE).split("/")[2].equals(rmbranchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        /* delete the branch with the given name*/
        Utils.join(HEADS_FOLDER, rmbranchName).delete();
    }

    public static void gitletReset(String commitId) {
        /* Failure cases*/
        if (!Utils.join(COMMIT_FOLDER, commitId).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (getHeadCommit().toSHA1().equals(commitId)) {
            return;
        }
        Commit resetCommit = Utils.readObject(Utils.join(COMMIT_FOLDER, commitId), Commit.class);
        checkoutFile(resetCommit);
        /* clear the staging area and update the current branch*/
        Utils.writeContents(getHeadBranchFile(), commitId);
        Utils.writeObject(ADDSTAGE_FILE, new TreeMap<>());
        Utils.writeObject(REMOVESTAGE_FILE, new TreeMap<>());

    }

    public static void gitletMerge(String givenBranchName) {
        /* failure cases*/
        if (!Utils.readObject(ADDSTAGE_FILE, TreeMap.class).isEmpty() || !Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!Utils.join(HEADS_FOLDER, givenBranchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (Utils.readContentsAsString(HEAD_FILE).split("/")[2].equals(givenBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
        }
        /* how to get the split point is an algorithm task
         * =>Tree how to find the common ancestor of two nodes?
         */
        Commit curCommit = getHeadCommit();
        Commit givenBranchCommit = Utils.readObject(Utils.join(COMMIT_FOLDER, readContentsAsString(Utils.join(HEADS_FOLDER, givenBranchName))), Commit.class);
        TreeMap<String, String> currentBranchFlowMap = new TreeMap<String, String>();
        bfs(curCommit, currentBranchFlowMap);
        Commit splitPointCommit = getSplitPoint(givenBranchCommit, currentBranchFlowMap);

        if (splitPointCommit.toSHA1().equals(givenBranchCommit.toSHA1())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPointCommit.toSHA1().equals(curCommit.toSHA1())) {
            String[] args = new String[2];
            args[0] = "checkout";
            args[1] = givenBranchName;
            gitletCheckout(args);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        /**
         * only when the working directory need to be changed. Here will come up with this error
         * here has two situations when the working directory need to be changed
         */
        curCommit.blobSHA1IdMap.forEach((key, value) -> {
            /* current branch file = splitCommit file != given branch file*/
            if (splitPointCommit.blobSHA1IdMap.containsKey(key) && splitPointCommit.blobSHA1IdMap.get(key).equals(value)) {
                if (getUntrackedFileList().contains(key) &&
                        (!givenBranchCommit.blobSHA1IdMap.containsKey(key) || !givenBranchCommit.blobSHA1IdMap.get(key).equals(value))) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return;
                }
            }
            /* current branch file != splitCommit file != given branch file*/
            if (splitPointCommit.blobSHA1IdMap.containsKey(key) && !splitPointCommit.blobSHA1IdMap.get(key).equals(value)) {
                if (!givenBranchCommit.blobSHA1IdMap.containsKey(key) || !value.equals(givenBranchCommit.blobSHA1IdMap.get(key))) {
                    if (getUntrackedFileList().contains(key)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                }
            }
        });


        /* common cases*/
        boolean whetherHasMergeConflict = false;
        /* Any files that have been modified in the given branch since the split point, but not modified in the current branch
         * should be changed to their versions in the given branch
         * These files should then all be automatically staged
         */
        for (String key : curCommit.blobSHA1IdMap.keySet()) {
            String value = curCommit.blobSHA1IdMap.get(key);
            if (splitPointCommit.blobSHA1IdMap.containsKey(key) && splitPointCommit.blobSHA1IdMap.get(key).equals(value)) {
                /* if only deleted in the given branch*/
                if (!givenBranchCommit.blobSHA1IdMap.containsKey(key)) {
                    gitletRm(key);
                /* if only changed in the given branch*/
                } else if(!givenBranchCommit.blobSHA1IdMap.get(key).equals(value)) {
                    Utils.writeContents(Utils.join(CWD, key), Utils.readContentsAsString(Utils.join(BLOB_FOLDER, givenBranchCommit.blobSHA1IdMap.get(key))));
                    gitletAdd(key);
                }
            /* merge conflict*/
            } else if (splitPointCommit.blobSHA1IdMap.containsKey(key) && !splitPointCommit.blobSHA1IdMap.get(key).equals(value)) {
                if (!givenBranchCommit.blobSHA1IdMap.containsKey(key)
                        || (givenBranchCommit.blobSHA1IdMap.containsKey(key) && !givenBranchCommit.blobSHA1IdMap.get(key).equals(splitPointCommit.blobSHA1IdMap.get(key)) && !value.equals(givenBranchCommit.blobSHA1IdMap.get(key)))) {
                    mergeConflictChangeFileContent(Utils.join(CWD, key), Utils.join(BLOB_FOLDER, givenBranchCommit.blobSHA1IdMap.get(key)));
                    System.out.println("Encountered a merge conflict.");
                    whetherHasMergeConflict = true;
                }
            }
            else if (!splitPointCommit.blobSHA1IdMap.containsKey(key) && givenBranchCommit.blobSHA1IdMap.containsKey(key)
                    && !value.equals(givenBranchCommit.blobSHA1IdMap.get(key))) {
                mergeConflictChangeFileContent(Utils.join(CWD, key), Utils.join(BLOB_FOLDER, givenBranchCommit.blobSHA1IdMap.get(key)));
                System.out.println("Encountered a merge conflict.");
                whetherHasMergeConflict = true;
            }
        }
        /* Any files that were not present at the split point and are present only in the given branch should be checked out and staged.*/
        for (String key : curCommit.blobSHA1IdMap.keySet()) {
            String value = curCommit.blobSHA1IdMap.get(key);
            if (!splitPointCommit.blobSHA1IdMap.containsKey(key) && !curCommit.blobSHA1IdMap.containsKey(key)) {
                File tempFile = Utils.join(CWD, key);
                try {
                    tempFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Utils.writeContents(tempFile, Utils.readContentsAsString(Utils.join(BLOB_FOLDER, value)));
                gitletAdd(key);
            }
            if (splitPointCommit.blobSHA1IdMap.containsKey(key) && !value.equals(splitPointCommit.blobSHA1IdMap.get(key))
                    && !curCommit.blobSHA1IdMap.containsKey(key)) {
                mergeConflictChangeFileContent(Utils.join(CWD, key), Utils.join(BLOB_FOLDER, givenBranchCommit.blobSHA1IdMap.get(key)));
                System.out.println("Encountered a merge conflict.");
                whetherHasMergeConflict = true;
            }
        }
        /* if not have conflict. commit directly*/
        if (!whetherHasMergeConflict) {
            /*  use addstage map to update parent Commit blobSHA1IdMap */
            Commit newCommit = new Commit("Merged " + givenBranchName +" into " + getHeadBranchFile().getName() +".");
            Commit headCommit = getHeadCommit();
            newCommit.blobSHA1IdMap.putAll(headCommit.blobSHA1IdMap);
            newCommit.blobSHA1IdMap.putAll(Utils.readObject(ADDSTAGE_FILE, TreeMap.class));
            /* use removestage map to update parent Commit blobSHA1IdMap*/
            Utils.readObject(REMOVESTAGE_FILE, TreeMap.class).keySet().forEach(newCommit.blobSHA1IdMap::remove);
            /* update the parentsList*/
            newCommit.parentsList.add(headCommit.toSHA1());
            newCommit.parentsList.add(givenBranchCommit.toSHA1());
            /* create relevant commit*/
            File addCommitFile = Utils.join(COMMIT_FOLDER, newCommit.toSHA1());
            try {
                addCommitFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Utils.writeObject(addCommitFile, newCommit);
            /* update the current branch*/
            Utils.writeContents(Utils.join(GITLET_DIR, Utils.readContentsAsString(HEAD_FILE).split("/")), newCommit.toSHA1());
            /* the staging area is cleared after a commit*/
            Utils.writeObject(ADDSTAGE_FILE, new TreeMap<>());
            Utils.writeObject(REMOVESTAGE_FILE, new TreeMap<>());
        }
    }

    public static void gitletHelp() {
        System.out.println("Options:");
        System.out.println("--init:\n    -java gitlet.Main init");
        System.out.println("--add:\n    -java gitlet.Main add [file name]");
        System.out.println("--commit:\n    -java gitlet.Main commit [message]");
        System.out.println("--rm:\n    -java gitlet.Main rm [file name]");
        System.out.println("--log:\n    -java gitlet.Main log");
        System.out.println("--global-log:\n    -java gitlet.Main global-log");
        System.out.println("--find:\n    -java gitlet.Main find [commit message]");
        System.out.println("--status:\n    -java gitlet.Main status");
        System.out.println("--checkout:\n    -java gitlet.Main checkout -- [file name]");
        System.out.println("    -java gitlet.Main checkout [commit id] -- [file name]");
        System.out.println("    -java gitlet.Main checkout [branch name]");
        System.out.println("--branch:\n    -java gitlet.Main branch [branch name]");
        System.out.println("--rm-branch:\n    -java gitlet.Main rm-branch [branch name]");
        System.out.println("--reset:\n    -java gitlet.Main reset [commit id]");
        System.out.println("--merge:\n    -java gitlet.Main merge [branch name]");
    }

}
