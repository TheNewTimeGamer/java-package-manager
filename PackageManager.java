import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PackageManager {

    public static void main(String[] args) throws Exception {
        if (args.length <= 0) {
            PackageManager.showHelp();
            return;
        }
        switch (args[0]) {
            case "init":
                PackageManager.init();
                break;
            case "add":
                PackageManager.addPackage(args);
                break;
            case "remove":
                PackageManager.removePackage(args);
                break;
            case "install":
                PackageManager.installPackages();
                break;
        }
    }

    public static void showHelp() {
        System.out.println("init - Create an empty \"packages.json\" file.");
        System.out.println("add - Add a package to the \"packages.json\" file.");
        System.out.println("remove - Remove a package from the \"packages.json\" file.");
        System.out.println("install - Download all packages in the \"packages.json\" file.");
    }

    public static void init() throws IOException {
        if (PackageManager.doesPackagesFileExist()) {
            System.err.println("\"packages.json\" already exists.");
            return;
        }
        Packages packages = new Packages(new String[0]);
        packages.write();
    }

    public static void addPackage(String[] args) {
        if (!PackageManager.doesPackagesFileExist()) {
            System.err.println("\"packages.json\" doesn't exist.");
            return;
        }
        try {
            Packages packages = Packages.read();
            packages.add(args[1]);
            packages.write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removePackage(String[] args) {
        if (!PackageManager.doesPackagesFileExist()) {
            System.err.println("\"packages.json\" doesn't exist.");
            return;
        }
        try {
            Packages packages = Packages.read();
            packages.remove(args[1]);
            packages.write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void installPackages() {
        if (!PackageManager.doesPackagesFileExist()) {
            System.err.println("\"packages.json\" doesn't exist.");
            return;
        }
        File dir = new File("lib");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        try {
            Packages.read().download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean doesPackagesFileExist() {
        return new File("packages.json").exists();
    }

}

class Packages {

    public final String version = "1.0";
    public final String[] sources = new String[] { "https://repo1.maven.org/maven2" };
    public ArrayList<String> packages;

    public Packages(String[] packages) {
        this.packages = new ArrayList<String>();
        this.packages.addAll(Arrays.asList(packages));
    }

    public void write() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        FileOutputStream out = new FileOutputStream(new File("packages.json"));
        out.write(json.getBytes());
        out.flush();
        out.close();
    }

    public boolean add(String name) {
        if (this.packages.contains(name)) {
            System.err.println("Package with name: \"" + name + "\" already exists.");
            return false;
        }
        this.packages.add(name);
        return true;
    }

    public boolean remove(String name) {
        if (this.packages.remove(name)) {
            System.out.println("Removed package: \"" + name + "\".");
            return true;
        }

        System.err.println("No package found with name: \"" + name + "\".");
        return false;
    }

    public void download() {
        String source = this.sources[0];
        for(int i = 0; i < this.packages.size(); i++) {
            try {
                String[] rawPackage = this.packages.get(i).split(":");
                String groupId = rawPackage[0].replace(".", "/");
                String artifactId = rawPackage[1];
                String version = rawPackage[2];
                String jarName = artifactId + "-" + version + ".jar";
                if(Files.notExists(Paths.get("lib/" + jarName))){
                    String fullPath = source + "/" + groupId + "/" + artifactId + "/" + version + "/" + jarName;
                    URL url = new URL(fullPath);
                    System.out.println("Downloading from: " + fullPath);
                    InputStream in = url.openStream();
                    Files.copy(in, Paths.get("lib/" + jarName), StandardCopyOption.REPLACE_EXISTING);
                }
            }catch(Exception e){
                System.err.println("Could not download package: " + this.packages.get(i));
            }
        }
    }

    public static Packages read() throws IOException {
        File file = new File("packages.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        in.read(buffer);
        in.close();
        return gson.fromJson(new String(buffer), Packages.class);
    }

}