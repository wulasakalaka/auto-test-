import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class Scope {
    public AnalysisScope scope;
    public Scope(String targetPath) throws IOException, InvalidClassFileException {
        ClassLoader classLoader;
        HashSet<File> srcClasses;
        HashSet<File> testClasses;
        while (targetPath.endsWith("\\")) {
            targetPath = targetPath.substring(0, targetPath.length() - 1);
        }
        while (targetPath.endsWith(".")) {
            targetPath = targetPath.substring(0, targetPath.length() - 1);
        }

//生成分析域
        classLoader = AnalysisScope.class.getClassLoader();
        scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), classLoader);
        srcClasses = getFiles(targetPath + "\\classes");
        testClasses = getFiles(targetPath + "\\test-classes");
        for (File sfile : srcClasses) {
            scope.addClassFileToScope(ClassLoaderReference.Application, sfile);
        }
        for (File tfile : testClasses) {
            scope.addClassFileToScope(ClassLoaderReference.Application, tfile);
        }
    }
    public AnalysisScope getScope() {
        return scope;
    }
    public HashSet<File> getFiles(String root) {
        File target;
        HashSet<File> result;
        String[] paths;

        result = new HashSet<>();
        target = new File(root);
        paths = target.list();
        if (paths == null) {
            return null;
        }

        for (String path : paths) {
            File tempFile = new File(root + "\\" + path);
            if (tempFile.isDirectory()) {
                HashSet<File> temp = getFiles(root + "\\" + path);
                if (temp != null) {
                    result.addAll(temp);
                }
            } else {
                if(path.endsWith(".class")) {
                    result.add(tempFile);
                }
            }
        }

        return result;
    }
}
