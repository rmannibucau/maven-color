package com.github.rmannibucau.maven.color;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Installer {
    private Installer() {
        // no-op
    }

    public static void main(final String[] args) throws MalformedURLException {
        final String home;
        if (args == null || args.length == 0) {
            home = System.getenv("MAVEN_HOME");
            if (home == null) {
                System.err.println("Usage: java -jar maven-color.jar $MAVEN_HOME");
                return;
            }
        } else {
            home = args[0];
        }

        final File homeFile = new File(home);
        if (!homeFile.isDirectory()) {
            System.err.println(home + " is a folder. Expected maven home.");
            return;
        }

        final File targetFolder = new File(homeFile, "lib");
        if (!targetFolder.isDirectory()) {
            throw new IllegalArgumentException(targetFolder + " doesnt exist");
        }

        // copy jansi
        final String[] jansis = targetFolder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("jansi") && name.endsWith(".jar");
            }
        });
        if (jansis == null || jansis.length == 0) {
            final String jansiVersion = "1.11";
            final URL url = new URL("https://repo1.maven.org/maven2/org/fusesource/jansi/jansi/" + jansiVersion + "/jansi-" + jansiVersion + ".jar");
            HttpURLConnection connection = null;
            try {
                connection = HttpURLConnection.class.cast(url.openConnection());
                try (final FileOutputStream fos = new FileOutputStream(new File(targetFolder, "jansi-" + jansiVersion + ".jar"))) {
                    System.out.printf("Copying jansi.jar to " + targetFolder);
                    copy(fos, connection.getInputStream());
                }
            } catch (final IOException e) {
                System.err.println("Jansi can't be downloaded from " + url.toExternalForm() + " add it in " + targetFolder.getAbsolutePath() + " and relaunch please.");
                return;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }


        // copy this jar
        try (final FileOutputStream fos = new FileOutputStream(new File(targetFolder, "maven-color.jar"));
             final FileInputStream fis = new FileInputStream(findJar(Thread.currentThread().getContextClassLoader(), Installer.class.getName().replace('.', '/') + ".class"))) {
            System.out.printf("Copying maven-color.jar to " + targetFolder);
            copy(fos, fis);
        } catch (final IOException e) {
            System.err.println("Can't copy this jar in " + targetFolder.getAbsolutePath() + ".");
            return;
        }

        // rename slf4j-simple
        final File[] slf4jSimple = targetFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.startsWith("slf4j-simple") && name.endsWith(".jar");
            }
        });
        if (slf4jSimple != null) {
            for (final File sl4j : slf4jSimple) {
                final File target = new File(targetFolder, sl4j.getName() + "_");
                try (final FileOutputStream fos = new FileOutputStream(target);
                     final FileInputStream fis = new FileInputStream(sl4j)) {
                    System.out.printf("Renaming " + sl4j + " to " + target);
                    copy(fos, fis);
                } catch (final IOException e) {
                    System.err.println("Can't copy this jar in " + targetFolder.getAbsolutePath() + ".");
                    return;
                }
            }
        }
    }

    private static void copy(FileOutputStream fos, InputStream stream) throws IOException {
        int count;
        final byte[] buffer = new byte[1024 * 1024];
        while ((count = stream.read(buffer)) >= 0) {
            fos.write(buffer, 0, count);
        }
    }

    private static File findJar(final ClassLoader loader, final String resourceName) {
        try {
            URL url = loader.getResource(resourceName);
            if (url == null) {
                throw new IllegalStateException("Didnt find " + resourceName);
            }

            if ("jar".equals(url.getProtocol())) {
                final String spec = url.getFile();
                url = new URL(spec.substring(0, spec.indexOf('!')));
                return new File(decode(url.getFile()));

            } else if ("file".equals(url.getProtocol())) {
                return toFile(resourceName, url);
            } else {
                throw new IllegalArgumentException("Unsupported URL scheme: " + url.toExternalForm());
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static File toFile(final String classFileName, final URL url) {
        String path = url.getFile();
        path = path.substring(0, path.length() - classFileName.length());
        return new File(decode(path));
    }

    private static String decode(final String fileName) {
        if (fileName.indexOf('%') == -1) {
            return fileName;
        }

        final StringBuilder result = new StringBuilder(fileName.length());
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < fileName.length(); ) {
            final char c = fileName.charAt(i);

            if (c == '%') {
                out.reset();
                do {
                    if (i + 2 >= fileName.length()) {
                        throw new IllegalArgumentException("Incomplete % sequence at: " + i);
                    }

                    final int d1 = Character.digit(fileName.charAt(i + 1), 16);
                    final int d2 = Character.digit(fileName.charAt(i + 2), 16);

                    if (d1 == -1 || d2 == -1) {
                        throw new IllegalArgumentException("Invalid % sequence (" + fileName.substring(i, i + 3) + ") at: " + String.valueOf(i));
                    }

                    out.write((byte) ((d1 << 4) + d2));

                    i += 3;

                } while (i < fileName.length() && fileName.charAt(i) == '%');


                result.append(out.toString());

                continue;
            } else {
                result.append(c);
            }

            i++;
        }
        return result.toString();
    }
}
