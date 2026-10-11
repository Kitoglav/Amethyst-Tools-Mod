package com.macaronsteam.amethysttoolsmod.compat;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;

/** Installs the bundled Simple Weapons example once, without touching an existing config directory. */
public final class DefaultCompatPack {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final String RESOURCE = "/compat/simple_weapons.bin";

  private DefaultCompatPack() {}

  public static void installIfMissing() {
    installIfMissing(CompatModules.directory());
  }

  static void installIfMissing(Path directory) {
    if (Files.exists(directory)) {
      return;
    }

    Path staging = null;
    try {
      Files.createDirectories(directory.getParent());
      staging = Files.createTempDirectory(directory.getParent(), "amethyst_tools-install-");
      unpack(staging);
      Files.move(staging, directory);
      staging = null;
      LOGGER.info("Amethyst Tools compat: installed default Simple Weapons module from {} into {}",
          RESOURCE, directory);
    } catch (FileAlreadyExistsException exception) {
      LOGGER.info("Amethyst Tools compat: config directory {} already exists; default pack was not installed",
          directory);
    } catch (IOException exception) {
      LOGGER.error("Amethyst Tools compat: failed to install default Simple Weapons module from {} into {}",
          RESOURCE, directory, exception);
    } finally {
      if (staging != null) {
        deleteStaging(staging);
      }
    }
  }

  private static void unpack(Path staging) throws IOException {
    InputStream resource = DefaultCompatPack.class.getResourceAsStream(RESOURCE);
    if (resource == null) {
      throw new IOException("Missing bundled resource " + RESOURCE);
    }
    try (InputStream input = resource;
         GZIPInputStream gzip = new GZIPInputStream(input);
         ZipInputStream zip = new ZipInputStream(gzip)) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        String name = entry.getName();
        if (!name.equals("simple_weapons.json") && !name.startsWith("assets/")) {
          throw new IOException("Unexpected bundled path " + name);
        }
        Path file = staging.resolve(name).normalize();
        if (!file.startsWith(staging)) {
          throw new IOException("Unsafe bundled path " + name);
        }
        if (entry.isDirectory()) {
          Files.createDirectories(file);
        } else {
          Files.createDirectories(file.getParent());
          Files.copy(zip, file);
        }
        zip.closeEntry();
      }
    }
    if (!Files.isRegularFile(staging.resolve("simple_weapons.json"))) {
      throw new IOException("Bundled Simple Weapons module has no simple_weapons.json");
    }
  }

  private static void deleteStaging(Path staging) {
    try (Stream<Path> paths = Files.walk(staging)) {
      paths.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException exception) {
          LOGGER.warn("Amethyst Tools compat: could not clean temporary install file {}", path, exception);
        }
      });
    } catch (IOException exception) {
      LOGGER.warn("Amethyst Tools compat: could not clean temporary install directory {}", staging,
          exception);
    }
  }
}
