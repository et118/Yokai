package com.github.et118.Yokai;
import mjson.Json;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends JFrame {

    public static String selectedVersion = "";
    public static JButton button;
    public static JComboBox comboBox;

    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException, InterruptedException {
        Path MinecraftFolder;
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            MinecraftFolder = Path.of(System.getenv("APPDATA")).resolve(".minecraft");
        } else {
            MinecraftFolder = Path.of(System.getProperty("user.home")).resolve(".minecraft");
        }

        JFrame frame = new JFrame("Yokai Installer");
        frame.setLayout(new FlowLayout());
        String versions[] = Git.lsRemoteRepository().setRemote("https://github.com/et118/Yokai.git").call().stream().filter(ref -> ref.getName().startsWith("refs/heads/") && !ref.getName().contains("main") && !ref.getName().contains("mods")).map(ref -> ref.getName().substring(11)).toList().toArray(new String[0]);
        Arrays.sort(versions, (o1, o2) -> Integer.parseInt(o1.split("\\.")[1]) <= Integer.parseInt(o2.split("\\.")[1]) ? 1 : -1);
        comboBox = new JComboBox(versions);
        JLabel label = new JLabel("Select version");
        button = new JButton("Install");
        button.addActionListener(e -> selectedVersion = (String)comboBox.getSelectedItem());
        JPanel panel = new JPanel();
        panel.add(label);
        panel.add(comboBox);
        panel.add(button);
        frame.add(panel);
        frame.setSize(400,300);
        frame.show();
        while(Objects.equals(selectedVersion, "")) {
            Thread.sleep(1);
        }
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));

        if(!Files.exists(MinecraftFolder.resolve("launcher_profiles.json"))) {System.err.println("Launcher_profiles.json not found. " + MinecraftFolder + "launcher_profiles.json");System.exit(0);}
        Optional<Path> fabricPath = Files.list(MinecraftFolder.resolve("versions")).filter(path -> path.getFileName().toString().endsWith("-" + selectedVersion)).filter(path -> path.getFileName().toString().startsWith("fabric-loader-")).findFirst();
        if(fabricPath.isEmpty()) {System.err.println("No fabric for version " + selectedVersion + " could be found"); System.exit(0);}
        Path VersionDir = MinecraftFolder.resolve("versions/Yokai-" + selectedVersion);
        Path LibraryDir = MinecraftFolder.resolve("libraries/com/github/et118/Yokai/1.1");
        Path GameDir = MinecraftFolder.resolve("Profiles/Yokai-" + selectedVersion);
        String fabricVersion = fabricPath.get().getFileName().toString();

        if(Files.exists(VersionDir)) {Files.walk(VersionDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);}
        Files.createDirectory(VersionDir);
        Files.createDirectories(GameDir);
        Files.createDirectories(LibraryDir);
        Files.copy(MinecraftFolder.resolve("versions/" + fabricVersion + "/" + fabricVersion + ".jar"),VersionDir.resolve("Yokai-"+selectedVersion+".jar"));
        Files.copy(MinecraftFolder.resolve("versions/" + fabricVersion + "/" + fabricVersion + ".json"),VersionDir.resolve("Yokai-"+selectedVersion+".json"));
        Files.copy(Path.of(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName()),LibraryDir.resolve("Yokai-1.1.jar"), StandardCopyOption.REPLACE_EXISTING);

        Json yokaiVersion = Json.read(Files.readString(VersionDir.resolve("Yokai-"+selectedVersion+".json")));
        yokaiVersion.set("id","Yokai-" + selectedVersion);
        yokaiVersion.set("mainClass","com.github.et118.Yokai.UpdateClient");
        yokaiVersion.at("libraries").add(Json.read("{\"name\": \"com.github.et118:Yokai:1.1\",\"url\": \"\"}"));
        yokaiVersion.at("arguments").at("jvm").add(Json.read("\"-DYokaiVersion= " + selectedVersion + "\""));
        Files.writeString(VersionDir.resolve("Yokai-"+selectedVersion+".json"), yokaiVersion.toString());

        Json launcherProfiles = Json.read(Files.readString(MinecraftFolder.resolve("launcher_profiles.json")));
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setCalendar(Calendar.getInstance());
        Json profile = Json.object();
        profile.set("name","Yokai " + selectedVersion);
        profile.set("type","custom");
        profile.set("created",df.format(new Date()));
        profile.set("lastUsed",df.format(new Date()));
        profile.set("icon","Glass");
        profile.set("gameDir",GameDir.toString());
        profile.set("lastVersionId","Yokai-" + selectedVersion);
        profile.set("javaArgs","-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M");
        launcherProfiles.at("profiles").set("Yokai-" + selectedVersion,profile);
        Files.writeString(MinecraftFolder.resolve("launcher_profiles.json"), launcherProfiles.toString());
        System.out.println("Successfully installed Yokai Launcher");
    }
}
