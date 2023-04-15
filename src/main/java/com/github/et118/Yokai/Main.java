package com.github.et118.Yokai;
import mjson.Json;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Path MinecraftFolder;
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            MinecraftFolder = Path.of(System.getenv("APPDATA")).resolve(".minecraft");
        } else {
            MinecraftFolder = Path.of(System.getProperty("user.home")).resolve(".minecraft");
        }

        if(!Files.exists(MinecraftFolder.resolve("launcher_profiles.json"))) {System.err.println("Launcher_profiles.json not found. " + MinecraftFolder + "launcher_profiles.json");System.exit(0);}
        Optional<Path> fabricPath = Files.list(MinecraftFolder.resolve("versions")).filter(path -> path.getFileName().toString().endsWith("-1.19.4")).filter(path -> path.getFileName().toString().startsWith("fabric-loader-")).findFirst();
        if(fabricPath.isEmpty()) {System.err.println("No fabric for version 1.19.4 could be found"); System.exit(0);}
        Path VersionDir = MinecraftFolder.resolve("versions/Yokai");
        Path LibraryDir = MinecraftFolder.resolve("libraries/com/github/et118/Yokai/1.0");
        Path GameDir = MinecraftFolder.resolve("Profiles/Yokai");
        String fabricVersion = fabricPath.get().getFileName().toString();

        if(Files.exists(VersionDir)) {Files.walk(VersionDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);}
        Files.createDirectory(VersionDir);
        Files.createDirectories(GameDir);
        Files.createDirectories(LibraryDir);
        Files.copy(MinecraftFolder.resolve("versions/" + fabricVersion + "/" + fabricVersion + ".jar"),VersionDir.resolve("Yokai.jar"));
        Files.copy(MinecraftFolder.resolve("versions/" + fabricVersion + "/" + fabricVersion + ".json"),VersionDir.resolve("Yokai.json"));
        Files.copy(Path.of(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName()),LibraryDir.resolve("Yokai-1.0.jar"), StandardCopyOption.REPLACE_EXISTING);

        Json yokaiVersion = Json.read(Files.readString(VersionDir.resolve("Yokai.json")));
        yokaiVersion.set("id","Yokai");
        yokaiVersion.set("mainClass","com.github.et118.Yokai.UpdateClient");
        yokaiVersion.at("libraries").add(Json.read("{\"name\": \"com.github.et118:Yokai:1.0\",\"url\": \"\"}"));
        Files.writeString(VersionDir.resolve("Yokai.json"), yokaiVersion.toString());

        Json launcherProfiles = Json.read(Files.readString(MinecraftFolder.resolve("launcher_profiles.json")));
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setCalendar(Calendar.getInstance());
        Json profile = Json.object();
        profile.set("name","Yokai");
        profile.set("type","custom");
        profile.set("created",df.format(new Date()));
        profile.set("lastUsed",df.format(new Date()));
        profile.set("icon","Glass");
        profile.set("gameDir",GameDir.toString());
        profile.set("lastVersionId","Yokai");
        profile.set("javaArgs","-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M");
        launcherProfiles.at("profiles").set("Yokai",profile);
        Files.writeString(MinecraftFolder.resolve("launcher_profiles.json"), launcherProfiles.toString());
        System.out.println("Successfully installed Yokai Launcher");
    }
}
