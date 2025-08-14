
# Gitlet - A Java-based Version Control System

Gitlet is a lightweight, Java-based version control system  inspired by Git. It mimics some of the core functionalities of Git, such as committing files, branching, merging, and restoring previous versions of files. Gitlet is designed to be simple and efficient, making it an excellent tool for learning how version control systems work under the hood.

---

## Features

Gitlet supports the following features:

1. **Commit Management**:
   - Save snapshots of files in commits.
   - Restore files or entire commits to previous states.
   - View commit history using the `log` command.

2. **Branching**:
   - Create and manage multiple branches.
   - Merge changes from one branch into another.

3. **Staging Area**:
   - Stage files for addition or removal before committing.
   - Clear the staging area after a commit.

4. **File Tracking**:
   - Track changes to files across commits.
   - Detect and handle conflicts during merges.

5. **Metadata Management**:
   - Each commit includes a timestamp, log message, and reference to its parent commit(s).
   - Unique SHA-1 hashes are used to identify commits and files.

6. **Error Handling**:
   - Gracefully handle errors such as invalid commands, missing files, or untracked files.

---

## Installation

To use Gitlet, ensure you have the following installed on your system:

- **Java Development Kit (JDK)**: Version 11 or higher.
- **Git**: To clone the repository.

### Steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/gitlet.git
   ```

2. Navigate to the project directory:
   ```bash
   cd gitlet
   ```

3. Compile the Java files:
   ```bash
   javac gitlet/Main.java
   ```

4. Run Gitlet using the `java` command:
   ```bash
   java gitlet.Main [command] [arguments]
   ```

---

## Usage

Gitlet is a command-line tool. Below are the supported commands and their usage:

### Initialize a Repository
```bash
java gitlet.Main init
```
- Creates a new Gitlet repository in the current directory.
- Initializes with a single commit (`initial commit`) and a `master` branch.

### Stage a File for Addition
```bash
java gitlet.Main add [file name]
```
- Stages the specified file for addition in the next commit.

### Commit Changes
```bash
java gitlet.Main commit [message]
```
- Saves a snapshot of all staged files in a new commit.
- Requires a non-empty commit message.

### Remove a File
```bash
java gitlet.Main rm [file name]
```
- Unstages the file if it is staged for addition.
- Stages the file for removal if it is tracked in the current commit.

### View Commit History
```bash
java gitlet.Main log
```
- Displays the history of commits starting from the current branch's head.

### View Global Commit History
```bash
java gitlet.Main global-log
```
- Displays information about all commits ever made.

### Find Commits by Message
```bash
java gitlet.Main find [commit message]
```
- Prints the IDs of all commits with the specified message.

### Checkout Files or Branches
```bash
java gitlet.Main checkout -- [file name]
java gitlet.Main checkout [commit id] -- [file name]
java gitlet.Main checkout [branch name]
```
- Restores files from a specific commit or branch.

### Create a New Branch
```bash
java gitlet.Main branch [branch name]
```
- Creates a new branch with the specified name.

### Remove a Branch
```bash
java gitlet.Main rm-branch [branch name]
```
- Deletes the specified branch.

### Reset to a Specific Commit
```bash
java gitlet.Main reset [commit id]
```
- Resets the current branch to the specified commit.

### Merge Branches
```bash
java gitlet.Main merge [branch name]
```
- Merges changes from the specified branch into the current branch.

