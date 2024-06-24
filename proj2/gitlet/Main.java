package gitlet;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init();
                break;
            case "add":
                verifyLength(args.length, 2);
                add(args[1]);
                break;
            case "commit":
                if (args.length == 1 || args[1].equals("")){
                    exit("Please enter a commit message.");
                }
                commit(args[1]);
                break;
            case "rm":
                verifyLength(args.length, 2);
                rm(args[1]);
                break;
            case "log":
                log();
                break;
            case "global-log":
                global_log();
                break;
            case "find":
                verifyLength(args.length, 2);
                find(args[1]);
                break;
            case "status":
                status();
                break;
            default:
                exit("No command with that name exists.");
        }
    }

    public static void verifyLength(int argsLength, int expectedLength){
        if (argsLength != expectedLength){
            exit("No command with that name exists.");
        }
    }
}
