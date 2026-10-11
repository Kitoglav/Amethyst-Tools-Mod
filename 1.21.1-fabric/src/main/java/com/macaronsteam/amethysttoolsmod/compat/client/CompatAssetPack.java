package com.macaronsteam.amethysttoolsmod.compat.client;

import com.macaronsteam.amethysttoolsmod.compat.CompatModules;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;

/** Exposes config/amethyst_tools/assets/models and textures as amethysttoolsmod assets. */
final class CompatAssetPack extends AbstractPackResources {
  private static final byte[] METADATA = ("{\"pack\":{\"pack_format\":34,"
      + "\"description\":\"Amethyst Tools compatibility assets\"}}")
      .getBytes(StandardCharsets.UTF_8);
  private final Path root;

  CompatAssetPack(PackLocationInfo location) {
    super(location);
    root = CompatModules.directory().resolve("assets").toAbsolutePath().normalize();
  }

  @Override
  public IoSupplier<InputStream> getRootResource(String... paths) {
    return paths.length == 1 && paths[0].equals("pack.mcmeta")
        ? () -> new ByteArrayInputStream(METADATA) : null;
  }

  @Override
  public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
    if (type != PackType.CLIENT_RESOURCES || !location.getNamespace().equals("amethysttoolsmod")) {
      return null;
    }
    Path file = resolve(location.getPath());
    return Files.isRegularFile(file) ? () -> Files.newInputStream(file) : null;
  }

  private Path resolve(String path) {
    Path result = root.resolve(path).normalize();
    return result.startsWith(root) ? result : root;
  }

  @Override
  public void listResources(PackType type, String namespace, String folder,
      PackResources.ResourceOutput output) {
    if (type != PackType.CLIENT_RESOURCES || !namespace.equals("amethysttoolsmod")) {
      return;
    }
    Path start = root.resolve(folder).normalize();
    if (!start.startsWith(root) || !Files.isDirectory(start)) {
      return;
    }
    try (Stream<Path> files = Files.walk(start)) {
      files.filter(Files::isRegularFile)
          .forEach(path -> output.accept(ResourceLocation.fromNamespaceAndPath(namespace,
              root.relativize(path).toString().replace('\\', '/')), () -> Files.newInputStream(path)));
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot reload Amethyst Tools compat assets from " + start, exception);
    }
  }

  @Override
  public Set<String> getNamespaces(PackType type) {
    return type == PackType.CLIENT_RESOURCES ? Set.of("amethysttoolsmod") : Set.of();
  }

  @Override
  public void close() {}
}
