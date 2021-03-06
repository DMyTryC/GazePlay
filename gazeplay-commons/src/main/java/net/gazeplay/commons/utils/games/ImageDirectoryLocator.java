package net.gazeplay.commons.utils.games;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class ImageDirectoryLocator {

    public static File locateImagesDirectoryInUnpackedDistDirectory(String parentImagesPackageResourceLocation) {
        log.info("locateImagesDirectoryInUnpackedDistDirectory : parentImagesPackageResourceLocation = {}",
                parentImagesPackageResourceLocation);
        final File workingDirectory = new File(".");
        log.info("locateImagesDirectoryInUnpackedDistDirectory : workingDirectory = {}",
                workingDirectory.getAbsolutePath());
        final String workingDirectoryName;
        try {
            workingDirectoryName = workingDirectory.getCanonicalFile().getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("locateImagesDirectoryInUnpackedDistDirectory : workingDirectoryName = {}", workingDirectoryName);

        log.info("locateImagesDirectoryInUnpackedDistDirectory : parentImagesPackageResourceLocation = {}",
                parentImagesPackageResourceLocation);

        {
            final File imagesDirectory = new File(workingDirectory, parentImagesPackageResourceLocation);
            log.info("locateImagesDirectoryInUnpackedDistDirectory : imagesDirectory = {}",
                    imagesDirectory.getAbsolutePath());
            boolean checked = checkImageDirectory(imagesDirectory);
            if (checked) {
                return imagesDirectory;
            }
        }

        if (workingDirectoryName.equals("bin")) {
            final File imagesDirectory = new File(workingDirectory, "../" + parentImagesPackageResourceLocation);
            log.info("locateImagesDirectoryInUnpackedDistDirectory : imagesDirectory = {}",
                    imagesDirectory.getAbsolutePath());
            boolean checked = checkImageDirectory(imagesDirectory);
            if (checked) {
                return imagesDirectory;
            }
        }

        return null;
    }

    public static File locateImagesDirectoryInExplodedClassPath(String parentImagesPackageResourceLocation) {
        log.info("parentImagesPackageResourceLocation = {}", parentImagesPackageResourceLocation);

        final URL parentImagesPackageResourceUrl;

        final ClassLoader classLoader = ImageDirectoryLocator.class.getClassLoader();
        parentImagesPackageResourceUrl = classLoader.getResource(parentImagesPackageResourceLocation);
        log.info("parentImagesPackageResourceUrl = {}", parentImagesPackageResourceUrl);

        if (parentImagesPackageResourceUrl == null) {
            throw new IllegalStateException("Resource not found : " + parentImagesPackageResourceLocation);
        }

        final File imagesDirectory = new File(parentImagesPackageResourceUrl.getFile());
        log.info("imagesDirectory = {}", imagesDirectory.getAbsolutePath());

        checkImageDirectory(imagesDirectory);
        return imagesDirectory;
    }

    private static boolean checkImageDirectory(File imagesDirectory) {
        if (!imagesDirectory.exists()) {
            log.warn("Directory does not exist : {}", imagesDirectory.getAbsolutePath());
            return false;
        }
        if (!imagesDirectory.isDirectory()) {
            log.warn("File is not a valid Directory : {}", imagesDirectory.getAbsolutePath());
            return false;
        }
        return true;
    }

}
