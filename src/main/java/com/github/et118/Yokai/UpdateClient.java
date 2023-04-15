package com.github.et118.Yokai;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UpdateClient {
    public static void main(String[] args) throws IOException, GitAPIException {
        Path ModFolder = (System.getProperty("os.name").toLowerCase().contains("win") ? Path.of(System.getenv("APPDATA")) : Path.of(System.getProperty("user.home"))).resolve( ".minecraft/Profiles/Yokai/mods");
        Files.createDirectories(ModFolder);
        if(!Files.exists(ModFolder.resolve(".git"))) {
            Files.walk(ModFolder).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            Files.createDirectories(ModFolder);
            Git.cloneRepository()
                    .setURI("https://github.com/et118/Yokai.git")
                    .setBranch("mods")
                    .setDirectory(ModFolder.toFile())
                    .call();
            Git.open(ModFolder.toFile()).checkout().setName("mods").call();
        }
        Git.open(ModFolder.toFile()).pull().call();
        Git.open(ModFolder.toFile()).reset().setMode(ResetCommand.ResetType.HARD).setRef("origin/mods").call();
        Git.open(ModFolder.toFile()).clean().setCleanDirectories(true).setForce(true).call();
        startMinecraft(args);
    }

    public static void startMinecraft(String[] args) throws IOException {
        List<String> arguments = new ArrayList<>();
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            arguments.add(System.getProperty("java.home") + "\\bin\\java.exe");
        } else {
            arguments.add(System.getProperty("java.home") + "/bin/java");
        }
        arguments.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path"));
        arguments.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
        arguments.addAll(List.of(args));
        ProcessBuilder pb = new ProcessBuilder(arguments.toArray(new String[0]));
        Process p = pb.start();
    }
}