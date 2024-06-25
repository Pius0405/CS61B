package gitlet;

import java.util.Arrays;

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
        if (args.length == 0) {
            exit("Please enter a command");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init();
                break;
            case "add":
                inGitletDir();
                add(args[1]);
                break;
            case "commit":
                inGitletDir();
                if (args.length == 1 || args[1].equals("")){
                    exit("Please enter a commit message.");
                }
                commit(args[1]);
                break;
            case "rm":
                inGitletDir();
                rm(args[1]);
                break;
            case "log":
                inGitletDir();
                log();
                break;
            case "global-log":
                inGitletDir();
                globalLog();
                break;
            case "find":
                inGitletDir();
                find(args[1]);
                break;
            case "status":
                inGitletDir();
                status();
                break;
            case "checkout":
                inGitletDir();
                checkout(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "branch":
                inGitletDir();
                branch(args[1]);
                break;
            case "rm-branch":
                inGitletDir();
                rm_branch(args[1]);
                break;
            case "reset":
                inGitletDir();
                reset(args[1]);
            default:
                exit("No command with that name exists.");
        }
    }

    public static void inGitletDir(){
        if (! GITLET_DIR.exists()){
            exit("Not in an initialized Gitlet directory.");
        }
    }
}
