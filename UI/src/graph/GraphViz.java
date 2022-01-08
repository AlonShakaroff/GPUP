package graph;

import javafx.scene.image.Image;
import target.Target;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class GraphViz {
    private String dotText = "";
    /**-----------------------------------saved phrases---------------------------------------*/
    private final String tempPath;
    private final String rootColor;
    private final String middleColor;
    private final String leafColor;
    private final String independentColor;
    private static final String fileNameDOT = "GeneratedGraph.dot";
    private static final String fileNamePNG = "GeneratedGraph.png";
    private static final String createPNGFromDOT = "dot -Tpng "+ fileNameDOT + " -o " + fileNamePNG;
    private static final String nodeProperties = "node[style = filled fontsize=42 width=1.5 shape=circle fillcolor=white]\n";
    private static final String graphProperties = "graph[truecolor=true bgcolor = transparent nodesep = 1.3 ranksep = 1.3]\n";
    private static final String edgeProperties = "edge[color=black arrowsize=3.0 penwidth =3.0]\n";

    /**-------------------------------------constructor---------------------------------------*/
    GraphViz(String tempPath, String rootColor, String middleColor, String leafColor, String independentColor){
        this.tempPath = tempPath;
        this.rootColor = rootColor;
        this.middleColor = middleColor;
        this.leafColor = leafColor;
        this.independentColor = independentColor;
    }

    /**-------------------------------------dotText writer---------------------------------------*/
    public void openGraph(){dotText = "digraph G {\n" + nodeProperties + graphProperties + edgeProperties;}
    public void closeGraph(){dotText += "}\n";}
    public void addRoot(String rootName){dotText += rootName + "[fillcolor =" + rootColor +"]\n";}
    public void addLeaf(String rootName){dotText += rootName + "[fillcolor =" + leafColor +"]\n";}
    public void addMiddle(String rootName){dotText += rootName + "[fillcolor =" + middleColor +"]\n";}
    public void addIndependent(String rootName){dotText += rootName + "[fillcolor =" + independentColor +"]\n";}
    public void addNode(Target target) {
        switch (target.getNodeType()){
            case LEAF: addLeaf(target.getName());
                break;
            case MIDDLE:addMiddle(target.getName());
                break;
            case ROOT:addRoot(target.getName());
                break;
            case INDEPENDENT:addIndependent(target.getName());
                break;
        }
    }
    public void addConnections(Target from, String arrowColor){
        if(from.getDependsOnSet().isEmpty())
            return;
        dotText += from.getName() + "-> {";
        for (Target target : from.getDependsOnSet())
            dotText += target.getName() + " ";
        dotText += "} [color = " +arrowColor +"]\n";
    }

    /**-------------------------------------generate javaFX Image from dotText---------------------------------------*/
    public Image generateImage(){
        byte[] img_stream = null;
        FileInputStream in = null;

        File dotFile = new File(tempPath + "/" +fileNameDOT);
        if (createDotFile(dotFile)) return null;

        if (createImageCMD()) return null;

        if (dotFile.exists())
                dotFile.delete();

        File pngFile = new File(tempPath + "/" +fileNamePNG);
        img_stream = getBytes(pngFile);
        if (img_stream == null) return null;

        return new Image(new ByteArrayInputStream(img_stream));
    }

    private byte[] getBytes(File pngFile) {
        FileInputStream in;
        byte[] img_stream;
        try {
            in = new FileInputStream(pngFile.getAbsolutePath());
            img_stream = new byte[in.available()];
            in.read(img_stream);
            if( in != null ) in.close();
            if (pngFile.exists())
                pngFile.delete();
        } catch (Exception ex) {
            System.out.println("could not generate png from graph");
            return null;
        }
        return img_stream;
    }

    private boolean createImageCMD() {
        try {
            Process process = Runtime.getRuntime().exec(
                    "cmd /c start /wait cmd.exe /K \"cd \\ && cd " + tempPath + " && " + createPNGFromDOT + "&& exit");
                process.waitFor();
            }catch (Exception exception) {
                System.out.println("could not generate png from graph - problem with GraphViz in cmd");
            return true;
            }
        return false;
    }

    private boolean createDotFile(File dotFile) {
        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(dotFile), "UTF-8"))) {
            out.write(dotText + "\r\n");
        } catch (Exception e) {
            System.out.println("could not generate png from graph - problem with saving dot file");
            return true;
        }
        return false;
    }


}
