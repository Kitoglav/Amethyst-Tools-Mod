package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;

/** Exposes config/amethyst_tools/assets/models and textures as amethysttoolsmod assets. */
final class CompatAssetPack extends AbstractPackResources {
  private static final String PREFIX = "assets/amethysttoolsmod/";
  private static final byte[] METADATA = ("{\"pack\":{\"pack_format\":8,"
      + "\"description\":\"Amethyst Tools compatibility assets\"}}")
      .getBytes(StandardCharsets.UTF_8);
  private final Path root;

  CompatAssetPack() {
    super(CompatModules.directory().resolve("assets").toFile());
    root = CompatModules.directory().resolve("assets").toAbsolutePath().normalize();
  }

  @Override
  protected InputStream getResource(String path) throws IOException {
    if (path.equals("pack.mcmeta")) {
      return new ByteArrayInputStream(METADATA);
    }
    Path file = resolve(path);
    if (file == null || !Files.isRegularFile(file)) {
      throw new FileNotFoundException(path);
    }
    return Files.newInputStream(file);
  }

  @Override
  protected boolean hasResource(String path) {
    return path.equals("pack.mcmeta") || (resolve(path) != null && Files.isRegularFile(resolve(path)));
  }

  private Path resolve(String path) {
    if (!path.startsWith(PREFIX)) {
      return null;
    }
    Path result = root.resolve(path.substring(PREFIX.length())).normalize();
    return result.startsWith(root) ? result : null;
  }

  @Override
  public Collection<ResourceLocation> getResources(PackType type, String namespace, String folder,
      int maxDepth, Predicate<String> filter) {
    if (type != PackType.CLIENT_RESOURCES || !namespace.equals("amethysttoolsmod")) {
      return List.of();
    }
    Path start = root.resolve(folder).normalize();
    if (!start.startsWith(root) || !Files.isDirectory(start)) {
      return List.of();
    }
    List<ResourceLocation> found = new ArrayList<>();
    try (Stream<Path> files = Files.walk(start, maxDepth)) {
      files.filter(Files::isRegularFile).filter(path -> filter.test(path.getFileName().toString()))
          .forEach(path -> found.add(new ResourceLocation(namespace,
              root.relativize(path).toString().replace('\\', '/'))));
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot reload Amethyst Tools compat assets from " + start, exception);
    }
    return found;
  }

  @Override
  public Set<String> getNamespaces(PackType type) {
    return type == PackType.CLIENT_RESOURCES ? Set.of("amethysttoolsmod") : Set.of();
  }

  @Override
  public String getName() {
    return "Amethyst Tools compatibility assets";
  }

  @Override
  public void close() {}
}
