package com.macaronsteam.amethysttoolsmod.compat;

import static org.junit.Assert.*;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DefaultCompatPackTest {
  @Rule public TemporaryFolder temporary = new TemporaryFolder();

  @Test
  public void installsCompletePackOnlyWhenDirectoryIsMissing() throws Exception {
    Path directory = temporary.getRoot().toPath().resolve("amethyst_tools");
    DefaultCompatPack.installIfMissing(directory);
    Path config = directory.resolve("simple_weapons.json");
    assertEquals(24, JsonParser.parseString(Files.readString(config)).getAsJsonObject()
        .getAsJsonArray("items").size());
    try (var files = Files.walk(directory.resolve("assets/models/item"))) {
      assertEquals(24, files.filter(Files::isRegularFile).count());
    }
    try (var files = Files.walk(directory.resolve("assets/textures/items"))) {
      assertEquals(24, files.filter(Files::isRegularFile).count());
    }
    Files.writeString(config, "user config");
    DefaultCompatPack.installIfMissing(directory);
    assertEquals("user config", Files.readString(config));
  }

  @Test
  public void leavesExistingEmptyDirectoryUntouched() throws Exception {
    Path directory = Files.createDirectory(temporary.getRoot().toPath().resolve("amethyst_tools"));
    DefaultCompatPack.installIfMissing(directory);
    try (var files = Files.list(directory)) {
      assertEquals(0, files.count());
    }
  }
}
