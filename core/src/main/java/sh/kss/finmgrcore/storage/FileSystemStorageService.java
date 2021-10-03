/*
    finmgr - A financial transaction framework
    Copyright (C) 2021 Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package sh.kss.finmgrcore.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
@Singleton
public class FileSystemStorageService implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemStorageService.class);

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        LOG.info("Calling store on filename: " + filename);

        try {

            if (file.isEmpty()) {

                LOG.warn("Failed to store empty file " + filename);
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {

                // This is a security check
                LOG.warn("Cannot store file with relative path outside current directory " + filename);
                throw new StorageException("Cannot store file with relative path outside current directory " + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {

                LOG.debug("Storing file in: " + this.rootLocation.resolve(filename));
                Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {

            LOG.error("IOException trying to store " + filename);
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {

        LOG.debug("Calling loadAll");

        try {

            return Files.walk(this.rootLocation, 1)
                .filter(path -> !path.equals(this.rootLocation))
                .map(this.rootLocation::relativize);
        }
        catch (IOException e) {

            LOG.error("IOException trying to loadAll on " + e.getLocalizedMessage());
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {

        LOG.debug("load on filename: " + filename);

        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {

        LOG.debug("loadAsResource on filename: " + filename);

        try {

            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {

                return resource;
            }
            else {

                LOG.warn("Could not read file " + filename);
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {

            LOG.error("MalformedURLException trying to loadAsResource " + filename);
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {

        LOG.info("Calling deleteAll");
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {

        LOG.info("Calling init");

        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {

            LOG.error("IOException trying to init");
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
