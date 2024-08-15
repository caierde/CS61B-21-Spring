package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.gitletInit();
                break;
            case "add":
                validateNumArgs(args, 2);
                Repository.gitletAdd(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    break;
                }
                validateNumArgs(args, 2);
                Repository.gitletCommit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                Repository.gitletRm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                Repository.gitletLog();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                Repository.gitletGlobalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                Repository.gitletFind(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                Repository.gitletStatus();
                break;
            case "checkout":
                /* arguments situation is many. so judge them in the method*/
                Repository.gitletCheckout(args);
                break;
            case "branch":
                validateNumArgs(args, 2);
                Repository.gitletBranch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                Repository.gitletRmbranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                Repository.gitletReset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                Repository.gitletMerge(args[1]);
                break;
            case "help":
//                l think a completed gitlet should have this method. so l add this help method
                validateNumArgs(args, 1);
                Repository.gitletHelp();
                break;
            default:
                System.out.println("No command with that name exists.");
        }
        return;
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException("Incorrect operands.");
        }
    }
}
