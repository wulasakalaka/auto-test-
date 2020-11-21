import java.util.HashSet;
class ClassNode {
    String name;
    HashSet<MethodNode> methods;
    HashSet<ClassNode> predClassNodes;
    MethodNode Node;

    public  ClassNode(String name){
        this.name = name;
        methods = new HashSet<>();
        predClassNodes = new HashSet<>();
        Node = null;
    }
    public void setNode(MethodNode Node) {
        this.Node = Node;
    }
    public void addMethod(MethodNode method){
        if(!this.methods.contains(method)) {
            this.methods.add(method);
        }
        method.setClassName(this);
    }

    public void addPredClass(ClassNode classNode){
        if(!this.predClassNodes.contains(classNode)){
            this.predClassNodes.add(classNode);
        }
    }
}
class MethodNode {
    String name;
    ClassNode classNode;
    HashSet<MethodNode> predMethodNodes;

    public MethodNode(String name){
        this.name = name;
        this.predMethodNodes = new HashSet<>();
        classNode = null;
    }

    public void setClassName(ClassNode classNode) {
        this.classNode = classNode;
    }

    public void addPredMethod(MethodNode methodNode){
        if(!this.predMethodNodes.contains(methodNode)) {
            this.predMethodNodes.add(methodNode);
        }
    }
}
