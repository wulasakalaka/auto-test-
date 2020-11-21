import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class testSelection {
    HashSet<ClassNode> classNodes;
    HashSet<MethodNode> methodNodes;
    HashSet<String> classNames;
    HashSet<String> methodNames;
    HashSet<String> classDotsentence;
    HashSet<String> methodDotsentence;
    HashSet<String> varResultconts;
    String varResultcont;
    HashSet<String> varconts;
    HashSet<ClassNode> varClassNodes;
    HashSet<MethodNode> varMethodNodes;
    String projectName = "";
    String[] args;
    public static void main(String[] args) {
        try {
            new testSelection(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void classvarcont() {
        MethodNode varMethod;
        ClassNode varClass;
        for (String varcont : this.varconts) {
            varMethod = getMethodNode(varcont);
            varClass = varMethod.classNode;
            this.getvarClass(varClass, this.varClassNodes, this.varMethodNodes);
        }
    }
    public void methodvarcont() {
        MethodNode varMethod;
        for (String varcont : this.varconts) {
            varMethod = getMethodNode(varcont);
            this.getvarMethod(varMethod, this.varMethodNodes);
        }
    }

    public ClassNode getClassNode(String className) {
        if (this.classNames.contains(className)) {
            for (ClassNode node : this.classNodes) {
                if (node.name.equals(className)) {
                    return node;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    public MethodNode getMethodNode(String methodName) {
        if (this.methodNames.contains(methodName)) {
            for (MethodNode node : this.methodNodes) {
                if (node.name.equals(methodName)) {
                    return node;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    public ClassNode getCurrentClassNode(ShrikeBTMethod method) {
        String classInnerName = method.getDeclaringClass().getName().toString();
        String signature = method.getSignature();
        String methodInnerName = classInnerName + " " + signature;
        ClassNode classNode = getClassNode(classInnerName);
        MethodNode methodNode = getMethodNode(methodInnerName);
        if (classNode == null) {
            classNode = new ClassNode(classInnerName);
            this.classNames.add(classInnerName);
            this.classNodes.add(classNode);
        }
        if (methodNode == null) {
            methodNode = new MethodNode(methodInnerName);
            this.methodNames.add(methodInnerName);
            this.methodNodes.add(methodNode);
        }
        methodNode.setClassName(classNode);
        classNode.addMethod(methodNode);
        classNode.setNode(methodNode);
        return classNode;
    }


    public HashSet<String> getvarcont() throws IOException {
        if(this.args[2].equals("novar")){return null;}
        HashSet<String> varconts;
        BufferedReader fileReader;
        String varcont;

        varconts = new HashSet<>();
        fileReader = new BufferedReader(new FileReader(this.args[2]));

        while ((varcont = fileReader.readLine()) != null) {
            varconts.add(varcont);
        }
        return varconts;
    }

    private void getvarClass(ClassNode classNode, HashSet<ClassNode> varClassNodes, HashSet<MethodNode> varMethodNodes) {
        for (ClassNode node : classNode.predClassNodes) {
            if (!varClassNodes.contains(node) && !node.equals(classNode)) {
                for (MethodNode methodNode : node.methods) {
                    if (!varMethodNodes.contains(methodNode)) {
                        varMethodNodes.add(methodNode);
                    }
                }
                varClassNodes.add(node);
                this.getvarClass(node, varClassNodes, varMethodNodes);
            }
        }
    }


    private void getvarMethod(MethodNode methodNode, HashSet<MethodNode> varMethodNodes) {
        for (MethodNode node : methodNode.predMethodNodes) {
            if (!varMethodNodes.contains(node) && !node.equals(methodNode)) {
                varMethodNodes.add(node);
                this.getvarMethod(node, varMethodNodes);
            }
        }
    }

    public testSelection(String[] strings) throws IOException {
        this.classNodes = new HashSet<>();
        this.methodNodes = new HashSet<>();
        this.classNames = new HashSet<>();
        this.methodNames = new HashSet<>();
        this.classDotsentence = new HashSet<>();
        this.classDotsentence.add("digraph cmd_class {");
        this.methodDotsentence = new HashSet<>();
        this.methodDotsentence.add("digraph cmd_method {");
        this.varResultconts = new HashSet<>();
        this.varconts = this.getvarcont();
        this.varClassNodes = new HashSet<>();
        this.varMethodNodes = new HashSet<>();
        this.varResultcont = "";
        this.args = strings;
        String[] temp = this.args[1].split("\\\\");
        this.projectName = temp[temp.length - 2];
        try {
            AnalysisScope scope;
            ClassHierarchy classHierarchy;
            AllApplicationEntrypoints entrypoints;
            AnalysisOptions options;
            SSAPropagationCallGraphBuilder builder;
            CallGraph callGraph;
            scope = new Scope(this.args[1]).getScope();
            classHierarchy = ClassHierarchyFactory.makeWithRoot(scope);
            entrypoints = new AllApplicationEntrypoints(scope, classHierarchy);
            options = new AnalysisOptions(scope, entrypoints);
            builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), classHierarchy, scope);
            callGraph = builder.makeCallGraph(options);
            for (CGNode cgNode : callGraph) {
                if (cgNode.getMethod() instanceof ShrikeBTMethod) {
                    ShrikeBTMethod method = (ShrikeBTMethod) cgNode.getMethod();
                    if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                        ClassNode classNode = getCurrentClassNode(method);
                        MethodNode methodNode = classNode.Node;
                        Iterator<CGNode> iterator = callGraph.getPredNodes(cgNode);
                        while (iterator.hasNext()) {
                            CGNode predCGNode = iterator.next();
                            if (predCGNode.getMethod() instanceof ShrikeBTMethod) {
                                ShrikeBTMethod predMethod = (ShrikeBTMethod) predCGNode.getMethod();
                                if ("Application".equals(predMethod.getDeclaringClass().getClassLoader().toString())) {
                                    ClassNode predClassNode = getCurrentClassNode(predMethod);
                                    MethodNode predMethodNode = predClassNode.Node;
                                    classNode.addPredClass(predClassNode);
                                    methodNode.addPredMethod(predMethodNode);
                                    String classReference = "    \"" + classNode.name + "\" -> \"" + predClassNode.name + "\";";
                                    String methodReference = "    \"" + methodNode.name + "\" -> \"" + predMethodNode.name + "\";";
                                    if (!this.classDotsentence.contains(classReference)) {
                                        this.classDotsentence.add(classReference);
                                    }
                                    if (!this.methodDotsentence.contains(methodReference)) {
                                        this.methodDotsentence.add(methodReference);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.classDotsentence.add("}");
            this.methodDotsentence.add("}");
            if(this.args[2].equals("novar")){
                return;
            }
            if (this.args[0].equals("-c")) {
                this.classvarcont();
            } else if (this.args[0].equals("-m")) {
                this.methodvarcont();
            }
            this.collectvarResultcont();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void collectvarResultcont() {
        for (MethodNode node : this.varMethodNodes) {
            if (node.name.contains("Test") && !node.name.contains("init")) {
                this.varResultconts.add(node.name);
            }
        }
        List<String> tempList = new ArrayList<String>(this.varResultconts);
        tempList.sort(Comparator.naturalOrder());
        this.varResultconts=new HashSet<>(tempList);
        for (String cont : this.varResultconts) {
            this.varResultcont += cont;
            this.varResultcont += "\n";
        }
        this.varResultcont += "\n";

        try {
            FileWriter fileWriter = null;
            if(this.args[0].equals("-c")){
                fileWriter = new FileWriter("selection-class.txt");
            }else if(this.args[0].equals("-m")){
                fileWriter = new FileWriter("selection-method.txt");
            }
            fileWriter.write(this.varResultcont);
            fileWriter.flush();
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
}
    }
}
