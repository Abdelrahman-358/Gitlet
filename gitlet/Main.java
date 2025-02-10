package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Abdelarahman Mostafa
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Repository.errorMessage("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                if (args.length == 2) {
                    Repository.add(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "commit":
                if (args.length == 2 && !args[1].equals("")) {
                    Repository.commit(args[1]);
                } else {
                    Repository.errorMessage("Please enter a commit message.");
                }
                break;
            case "rm":
                if (args.length == 2) {
                    Repository.rm(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "rm-branch":
                if (args.length == 2) {
                    Repository.rmBranch(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.global_log();
                break;
            case "find":
                if (args.length == 2) {
                    Repository.find(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkout(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkout(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    Repository.errorMessage("Incorrect operands.");
                }
                break;
            case "branch":
                if (args.length == 2) {
                    Repository.branch(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "reset":
                if (args.length == 2) {
                    Repository.reset(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "merge":
                if (args.length == 2) {
                    Repository.merge(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "get":
                if (args.length == 2) {
                    Repository.get(args[1]);
                } else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "add-remote":
                if (args.length == 3) {
                    Repository.addRemote(args[1],args[2]);
                }else{
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "rm-remote":
                if (args.length == 2) {
                    Repository.removeRemote(args[1]);
                }else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "push":
                if (args.length == 3) {
                    Repository.push(args[1],args[2]);
                }else{
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "fetch":
                if (args.length == 3) {
                    Repository.fetch(args[1],args[2]);
                }else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;
            case "pull":
                if (args.length == 3) {
                    Repository.pull(args[1],args[2]);
                }else {
                    Repository.errorMessage("Not enough arguments.");
                }
                break;

            default:
                Repository.errorMessage("No command with that name exists.");
                break;
        }
    }
}