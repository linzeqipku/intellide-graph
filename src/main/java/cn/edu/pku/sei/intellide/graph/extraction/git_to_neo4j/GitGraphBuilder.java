package cn.edu.pku.sei.intellide.graph.extraction.git_to_neo4j;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GitGraphBuilder {

    public static final RelationshipType PARENT = RelationshipType.withName("parent");
    public static final String NAME = "name";
    public static final String MESSAGE = "message";
    public static final String COMMIT_TIME = "commitTime";
    public static final String DIFF_SUMMARY = "diffSummary";
    public static final Label COMMIT = Label.label("Commit");
    public static final String EMAIL_ADDRESS = "emailAddress";
    public static final Label GIT_USER = Label.label("GitUser");
    public static final RelationshipType CREATOR = RelationshipType.withName("creator");
    public static final RelationshipType COMMITTER = RelationshipType.withName("committer");

    private String gitDirPath = null;
    private BatchInserter inserter = null;
    private Map<String, Long> commitMap = new HashMap<>();
    private Map<String, Long> personMap = new HashMap<>();
    private Map<String, Set<String>> parentsMap = new HashMap<>();

    public static void process(String graphDirPath, String gitDirPath) throws IOException, GitAPIException {
        new GitGraphBuilder(graphDirPath,gitDirPath).process();
        System.out.println("git ok !");
    }

    private GitGraphBuilder(String graphDirPath, String gitDirPath) throws IOException {
        this.gitDirPath = gitDirPath;
        inserter = BatchInserters.inserter(new File(graphDirPath));
    }

    private void process() throws IOException, GitAPIException {
        Repository repository;
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.setMustExist(true);
        repositoryBuilder.setGitDir(new File(gitDirPath));
        repository = repositoryBuilder.build();
        if (repository.getObjectDatabase().exists()) {
            Git git = new Git(repository);
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits)
                parseCommit(commit, repository, git);
        }
        parentsMap.entrySet().forEach(entry -> {
            long commitNodeId = commitMap.get(entry.getKey());
            entry.getValue().forEach(parentName -> {
                if (commitMap.containsKey(parentName))
                    inserter.createRelationship(commitNodeId, commitMap.get(parentName), PARENT, new HashMap<>());
            });
        });
        inserter.shutdown();
    }

    private void parseCommit(RevCommit commit, Repository repository, Git git) throws IOException, GitAPIException {
        //System.out.println(commit.getName());
        Map<String, Object> map = new HashMap<>();
        map.put(NAME, commit.getName());
        String message = commit.getFullMessage();
        map.put(MESSAGE, message != null ? message : "");
        map.put(COMMIT_TIME, commit.getCommitTime());
        List<String> diffStrs = new ArrayList<>();
        Set<String> parentNames = new HashSet<>();
        for (int i = 0; i < commit.getParentCount(); i++) {
            parentNames.add(commit.getParent(i).getName());
            ObjectId head = repository.resolve(commit.getName() + "^{tree}");
            ObjectId old = repository.resolve(commit.getParent(i).getName() + "^{tree}");
            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, old);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
            for (int k = 0; k < diffs.size(); k++) {
                diffStrs.add(diffs.get(k).getChangeType().name() + " " + diffs.get(k).getOldPath() + " to " + diffs.get(k).getNewPath());
            }
        }
        map.put(DIFF_SUMMARY, String.join("\n", diffStrs));
        long commitNodeId = inserter.createNode(map, COMMIT);
        commitMap.put(commit.getName(), commitNodeId);
        parentsMap.put(commit.getName(), parentNames);
        PersonIdent author = commit.getAuthorIdent();
        String personStr = author.getName() + ": " + author.getEmailAddress();
        if (!personMap.containsKey(personStr)) {
            Map<String, Object> pMap = new HashMap<>();
            String name = author.getName();
            String email = author.getEmailAddress();
            pMap.put(NAME, name != null ? name : "");
            pMap.put(EMAIL_ADDRESS, email != null ? email : "");
            long personNodeId = inserter.createNode(pMap, GIT_USER);
            personMap.put(personStr, personNodeId);
            inserter.createRelationship(commitNodeId, personNodeId, CREATOR, new HashMap<>());
        } else
            inserter.createRelationship(commitNodeId, personMap.get(personStr), CREATOR, new HashMap<>());
        PersonIdent committer = commit.getCommitterIdent();
        personStr = committer.getName() + ": " + committer.getEmailAddress();
        if (!personMap.containsKey(personStr)) {
            Map<String, Object> pMap = new HashMap<>();
            String name = committer.getName();
            String email = committer.getEmailAddress();
            pMap.put(NAME, name != null ? name : "");
            pMap.put(EMAIL_ADDRESS, email != null ? email : "");
            long personNodeId = inserter.createNode(pMap, GIT_USER);
            personMap.put(personStr, personNodeId);
            inserter.createRelationship(commitNodeId, personNodeId, COMMITTER, new HashMap<>());
        } else
            inserter.createRelationship(commitNodeId, personMap.get(personStr), COMMITTER, new HashMap<>());
    }

    public  static void main(String[] args) throws IOException, GitAPIException {

        GitGraphBuilder.process("F:\\graph-tsr3","F:\\TSR2\\git\\TSR\\.git");


        System.out.println("git OK");

    }

}
