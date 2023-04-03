
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class CommitData {
    private static final String REPO_PATH = "C:/Users/Nishtha Sahu/outr/.git";
    private static final String CSV_PATH = "C:/Users/Nishtha Sahu/outr/data.csv";

    public static void main(String[] args) throws IOException, GitAPIException {
        try (Git git = Git.open(new java.io.File(REPO_PATH))) {
            Repository repo = git.getRepository();
            RevWalk walk = new RevWalk(repo);
            List<RevCommit> commits = new ArrayList<>();
            ObjectId head = repo.resolve("HEAD");
            walk.markStart(walk.parseCommit(head));
            for (RevCommit commit : walk) {
                commits.add(commit);
            }
            walk.dispose();
            TreeMap<Long, ArrayList<String>> dataMap = new TreeMap<>();
            for (int i = commits.size() - 1; i >= 0; i--) {
                RevCommit commit = commits.get(i);
                String commitTime = Long.toString(commit.getCommitTime());
                System.out.println("Commit " + commit.name() + " at " + commitTime);
                ObjectId treeId = commit.getTree().getId();
                TreeWalk treeWalk = new TreeWalk(repo);
                treeWalk.addTree(treeId);
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                    if (treeWalk.getPathString().equals(CSV_PATH)) {
                        ObjectId objectId = treeWalk.getObjectId(0);
                        try (ObjectReader reader = repo.newObjectReader()) {
                            ObjectLoader loader = reader.open(objectId);
                            BufferedReader br = new BufferedReader(new InputStreamReader(loader.openStream()));
                            String line;
                            while ((line = br.readLine()) != null) {
                                String[] parts = line.split(",");
                                String city = parts[0];
                                int temperature = Integer.parseInt(parts[1]);
                                String row = city + "," + temperature + "," + commitTime;
                                ArrayList<String> dataList = dataMap.getOrDefault(commit.getCommitTime(),
                                        new ArrayList<>());
                                dataList.add(row);
                                dataMap.put((long) commit.getCommitTime(), dataList);
                            }
                            br.close();
                        }
                    }
                }
                // CanonicalTreeParser treeParser = new CanonicalTreeParser();
                // try (org.eclipse.jgit.lib.ObjectReader reader = repo.newObjectReader()) {
                // treeParser.reset(reader, treeId);
                // }
                // Map<String, String> csvData = CsvParser.parse(treeParser, CSV_PATH);
                // for (Map.Entry<String, String> entry : csvData.entrySet()) {
                // String row = entry.getKey() + "," + entry.getValue() + "," + commitTime;
                // List<String> dataList = dataMap.getOrDefault(commit.getCommitTime(), new
                // ArrayList<>());
                // dataList.add(row);
                // dataMap.put(commit.getCommitTime(), dataList);
                // }
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Long, ArrayList<String>> entry : dataMap.entrySet()) {
                for (String row : entry.getValue()) {
                    sb.append(row).append("\n");
                }
            }
            System.out.println(sb.toString());
        }
    }
}
