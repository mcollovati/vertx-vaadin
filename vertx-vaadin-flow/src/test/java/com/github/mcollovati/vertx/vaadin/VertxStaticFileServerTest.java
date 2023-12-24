/*
 * Adapted from Vaadin source code
 */
package com.github.mcollovati.vertx.vaadin;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.HttpStatusCode;
import com.vaadin.flow.server.VaadinService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;

import static com.vaadin.flow.server.Constants.POLYFILLS_DEFAULT_VALUE;
import static com.vaadin.flow.server.Constants.STATISTICS_JSON_DEFAULT;
import static com.vaadin.flow.server.Constants.VAADIN_SERVLET_RESOURCES;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_STATISTICS_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;

public class VertxStaticFileServerTest implements Serializable {

    private OverrideableStaticFileServer fileServer;
    private RoutingContext routingContext;
    private HttpServerRequest request;
    private HttpServerResponse response;
    private Map<String, String> headers;
    private Map<String, Long> dateHeaders;
    private AtomicInteger responseCode;
    private AtomicLong responseContentLength;
    private VertxVaadinService vaadinService = Mockito.mock(VertxVaadinService.class);
    private DeploymentConfiguration configuration;
    private Buffer responseOutput;

    private static final String WEBAPP_RESOURCE_PREFIX = "META-INF/VAADIN/webapp";

    private static URL createFileURLWithDataAndLength(String name, byte[] data)
            throws MalformedURLException {
        return createFileURLWithDataAndLength(name, data, -1);
    }

    private static URL createFileURLWithDataAndLength(String name, byte[] data,
                                                      long lastModificationTime) throws MalformedURLException {
        return new URL("file", "", -1, name, new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                URLConnection connection = Mockito.mock(URLConnection.class);
                Mockito.when(connection.getInputStream())
                        .thenReturn(new ByteArrayInputStream(data));
                Mockito.when(connection.getContentLengthLong())
                        .thenReturn((long) data.length);
                Mockito.when(connection.getLastModified())
                        .thenReturn(lastModificationTime);
                return connection;
            }
        });

    }

    @BeforeClass
    public static void beforeClass() {
        // must be cleared before running this class
        CurrentInstance.clearAll();
    }

    @Before
    public void setUp() throws IOException {
        Set<String> expectedDateHeaders = Stream.of(HttpHeaders.IF_MODIFIED_SINCE, HttpHeaders.DATE, HttpHeaders.LAST_MODIFIED)
                .map(name -> name.toString().toLowerCase())
                .collect(Collectors.toSet());
        Matcher<CharSequence> isDateHeader = new CustomMatcher<CharSequence>("Date header") {
            @Override
            public boolean matches(Object item) {
                return item instanceof CharSequence && expectedDateHeaders.contains(item.toString().toLowerCase());
            }
        };


        request = Mockito.mock(HttpServerRequest.class);
        response = Mockito.mock(HttpServerResponse.class);
        // No header == getDateHeader returns -1 (Mockito default is 0)
        Mockito.when(request.getHeader(MockitoHamcrest.argThat(isDateHeader))).thenReturn("-1");

        responseCode = new AtomicInteger(-1);
        responseContentLength = new AtomicLong(-1L);
        headers = new HashMap<>();
        dateHeaders = new HashMap<>();


        Mockito.doAnswer(invocation -> {
            headers.put(invocation.getArgument(0, CharSequence.class).toString(),
                    invocation.getArgument(1, CharSequence.class).toString());
            return null;
        }).when(response).putHeader(any(CharSequence.class), any(CharSequence.class));
        Mockito.doAnswer(invocation -> {
            dateHeaders.put(invocation.getArgument(0, CharSequence.class).toString(),
                    HttpUtils.parseDateHeader(invocation.getArgument(1, CharSequence.class).toString()));
            return null;
        }).when(response).putHeader(MockitoHamcrest.argThat(isDateHeader), any(CharSequence.class));
        Mockito.doAnswer(invocation -> {
            responseCode.set((int) invocation.getArguments()[0]);
            return null;
        }).when(response).setStatusCode(anyInt());
        Mockito.doAnswer(invocation -> {
            responseCode.set((int) invocation.getArguments()[0]);
            return null;
        }).when(response).setStatusCode(anyInt());
        Mockito.doAnswer(invocation -> {
            responseContentLength.set((long) invocation.getArguments()[0]);
            return null;
        }).when(response).putHeader(matches("(?i)Content-Length"), matches("\\d+"));

        responseOutput = Buffer.buffer();
        Mockito.doAnswer(i -> {
            responseOutput.appendBuffer(i.getArgument(0, Buffer.class));
            return null;
        }).when(response).write(any(Buffer.class));
        Mockito.doAnswer(i -> {
            responseOutput.appendString(i.getArgument(0, String.class));
            return null;
        }).when(response).write(anyString());
        Mockito.doAnswer(i -> {
            responseOutput.appendString(i.getArgument(0, String.class), i.getArgument(1, String.class));
            return null;
        }).when(response).write(anyString(), anyString());


        configuration = Mockito.mock(DeploymentConfiguration.class);
        Mockito.when(configuration.isProductionMode()).thenReturn(true);

        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenReturn(configuration);

        // Use test class loader to enable reading `manifest.json` resource
        Mockito.doAnswer(invocationOnMock -> getClass().getClassLoader()).when(vaadinService).getClassLoader();

        routingContext = Mockito.mock(RoutingContext.class);
        Mockito.when(routingContext.request()).thenReturn(request);
        Mockito.when(routingContext.response()).thenReturn(response);

        fileServer = new OverrideableStaticFileServer(vaadinService, configuration);

        // Required by the ResponseWriter
        /////servletContext = Mockito.mock(ServletContext.class);
        /////Mockito.when(request.getServletContext()).thenReturn(servletContext);
    }

    @After
    public void tearDown() {
        Assert.assertNull(VaadinService.getCurrent());
    }

    @Test
    public void getRequestFilename() {
        // Context path should not affect the filename in any way
        for (String contextPath : new String[]{"", "/foo", "/foo/bar"}) {
            // /* servlet
            Assert.assertEquals("", getRequestFilename(contextPath, null));
            Assert.assertEquals("/bar.js",
                    getRequestFilename(contextPath, "/bar.js"));
            Assert.assertEquals("/foo/bar.js",
                    getRequestFilename(contextPath, "/foo/bar.js"));

            // /foo servlet
            Assert.assertEquals("/foo",
                    getRequestFilename(contextPath, "/foo"));
            Assert.assertEquals("/foo/bar.js",
                    getRequestFilename(contextPath, "/foo/bar.js"));
            Assert.assertEquals("/foo/bar/baz.js",
                    getRequestFilename(contextPath, "/foo/bar/baz.js"));

            // /foo/bar servlet
            Assert.assertEquals("/foo/bar",
                    getRequestFilename(contextPath, "/foo/bar"));
            Assert.assertEquals("/foo/bar/baz.js",
                    getRequestFilename(contextPath, "/foo/bar/baz.js"));
            Assert.assertEquals("/foo/bar/baz/baz.js",
                    getRequestFilename(contextPath, "/foo/bar/baz/baz.js"));
        }
    }

    private String getRequestFilename(String encodedContextPath, String pathInfo) {
        setupRequestURI(encodedContextPath, pathInfo);
        return fileServer.getRequestFilename(routingContext);
    }

    private void setupRequestURI(String encodedContextPath, String pathInfo) {
        assert !encodedContextPath.equals("/") : "root context is always \"\"";
        assert encodedContextPath.equals("") || encodedContextPath
                .startsWith("/") : "context always starts with /";
        assert !encodedContextPath.endsWith(
                "/") : "context path should start with / but not end with /";
        assert pathInfo == null || pathInfo.startsWith("/");

        String requestURI = "";
        if (!encodedContextPath.isEmpty()) {
            requestURI += encodedContextPath;
        }
        if (pathInfo != null) {
            requestURI += pathInfo;
        }

        Mockito.when(routingContext.mountPoint()).thenReturn(encodedContextPath);
        Mockito.when(request.path()).thenReturn(requestURI);
        Mockito.when(request.uri()).thenReturn(requestURI);
    }

    @Test
    public void isResourceRequest() throws Exception {
        fileServer.writeResponse = false;
        setupRequestURI("", "/static/file.png");
        Mockito.when(vaadinService.getStaticResource("/static/file.png"))
                .thenReturn(new URL("file:///static/file.png"));
        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void isResourceRequestWithContextPath() throws Exception {
        fileServer.writeResponse = false;
        setupRequestURI("/foo", "/static/file.png");
        Mockito.when(vaadinService.getStaticResource("/static/file.png"))
                .thenReturn(new URL("file:///static/file.png"));
        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void isNotResourceRequest() throws Exception {
        setupRequestURI("", null);
        Mockito.when(vaadinService.getStaticResource("/")).thenReturn(null);
        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void directoryIsNotResourceRequest() throws Exception {
        fileServer.writeResponse = false;
        final TemporaryFolder folder = TemporaryFolder.builder().build();
        folder.create();

        setupRequestURI("", "/frontend");
        // generate URL so it is not ending with / so that we test the correct
        // method
        String rootAbsolutePath = folder.getRoot().getAbsolutePath()
                .replaceAll("\\\\", "/");
        if (rootAbsolutePath.endsWith("/")) {
            rootAbsolutePath = rootAbsolutePath.substring(0,
                    rootAbsolutePath.length() - 1);
        }
        final URL folderPath = new URL("file:///" + rootAbsolutePath);

        Mockito.when(vaadinService.getStaticResource("/frontend"))
                .thenReturn(folderPath);
        Assert.assertFalse("Folder on disk should not be a static resource.",
                fileServer.serveStaticResource(routingContext));

        // Test any path ending with / to be seen as a directory
        setupRequestURI("", "/fake");
        Mockito.when(vaadinService.getStaticResource("/fake"))
                .thenReturn(new URL("file:///fake/"));
        Assert.assertFalse(
                "Fake should not check the file system nor be a static resource.",
                fileServer.serveStaticResource(routingContext));

        Path tempArchive = generateZipArchive(folder);

        setupRequestURI("", "/frontend/.");
        Mockito.when(vaadinService.getStaticResource("/frontend/."))
                .thenReturn(new URL("jar:file:///"
                        + tempArchive.toString().replaceAll("\\\\", "/")
                        + "!/frontend"));
        Assert.assertFalse(
                "Folder 'frontend' in jar should not be a static resource.",
                fileServer.serveStaticResource(routingContext));
        setupRequestURI("", "/file.txt");
        Mockito.when(vaadinService.getStaticResource("/file.txt"))
                .thenReturn(new URL("jar:file:///"
                        + tempArchive.toString().replaceAll("\\\\", "/")
                        + "!/file.txt"));
        Assert.assertTrue(
                "File 'file.txt' inside jar should be a static resource.",
                fileServer.serveStaticResource(routingContext));

        folder.delete();
    }

    /* TODO: perhapes we don't need to check jar in jar resolution
    @Test
    public void isStaticResource_jarWarFileScheme_detectsAsStaticResources()
            throws IOException {
        Assert.assertTrue("Can not run concurrently with other test",
                StaticFileServer.openFileSystems.isEmpty());

        final TemporaryFolder folder = TemporaryFolder.builder().build();
        folder.create();

        File archiveFile = new File(folder.getRoot(), "fake.jar");
        archiveFile.createNewFile();
        Path tempArchive = archiveFile.toPath();
        File warFile = new File(folder.getRoot(), "war.jar");
        warFile.createNewFile();
        Path warArchive = warFile.toPath();

        generateJarInJar(archiveFile, tempArchive, warArchive);

        // Instantiate URL stream handler factory to be able to handle war:
        WarURLStreamHandlerFactory.getInstance();

        final URL folderResourceURL = new URL(
                "jar:war:" + warFile.toURI().toURL() + "!/"
                        + archiveFile.getName() + "!/frontend");

        setupRequestURI("", "", "/frontend/.");
        Mockito.when(servletService.getStaticResource("/frontend/."))
                .thenReturn(folderResourceURL);

        Assert.assertTrue(
                "Request should return as static request as we can not determine non file resources in jar files.",
                fileServer.isStaticResourceRequest(request));

        folder.delete();
    }

    @Test
    public void isStaticResource_jarInAJar_detectsAsStaticResources()
            throws IOException {
        Assert.assertTrue("Can not run concurrently with other test",
                VertxStaticFileServer.openFileSystems.isEmpty());

        final TemporaryFolder folder = TemporaryFolder.builder().build();
        folder.create();

        File archiveFile = new File(folder.getRoot(), "fake.jar");
        archiveFile.createNewFile();
        Path tempArchive = archiveFile.toPath();

        File warFile = new File(folder.getRoot(), "war.jar");
        warFile.createNewFile();
        Path warArchive = warFile.toPath();

        generateJarInJar(archiveFile, tempArchive, warArchive);

        setupRequestURI("", "", "/frontend/.");
        Mockito.when(servletService.getStaticResource("/frontend/."))
                .thenReturn(new URL("jar:" + warFile.toURI().toURL() + "!/"
                        + archiveFile.getName() + "!/frontend"));
        Assert.assertTrue(
                "Request should return as static request as we can not determine non file resources in jar files.",
                fileServer.isStaticResourceRequest(request));
        setupRequestURI("", "", "/file.txt");
        Mockito.when(servletService.getStaticResource("/file.txt"))
                .thenReturn(new URL("jar:" + warFile.toURI().toURL() + "!/"
                        + archiveFile.getName() + "!/file.txt"));
        Assert.assertTrue(
                "Request should return as static request as we can not determine non file resources in jar files.",
                fileServer.isStaticResourceRequest(request));

        folder.delete();
    }
    */
    private Path generateZipArchive(TemporaryFolder folder) throws IOException {
        File archiveFile = new File(folder.getRoot(), "fake.jar");
        archiveFile.createNewFile();
        Path tempArchive = archiveFile.toPath();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(
                Files.newOutputStream(tempArchive))) {
            // Create a file to the zip
            zipOutputStream.putNextEntry(new ZipEntry("/file"));
            zipOutputStream.closeEntry();
            // Create a directory to the zip
            zipOutputStream.putNextEntry(new ZipEntry("frontend/"));
            zipOutputStream.closeEntry();
        }
        return tempArchive;
    }

    private void generateJarInJar(File archiveFile, Path tempArchive,
                                  Path warArchive) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(
                Files.newOutputStream(tempArchive))) {
            // Create a file to the zip
            zipOutputStream.putNextEntry(new ZipEntry("/file"));
            zipOutputStream.closeEntry();
            // Create a directory to the zip
            zipOutputStream.putNextEntry(new ZipEntry("frontend/"));
            zipOutputStream.closeEntry();
        }

        try (ZipOutputStream warOutputStream = new ZipOutputStream(
                Files.newOutputStream(warArchive))) {
            // Create a file to the zip
            warOutputStream.putNextEntry(new ZipEntry(archiveFile.getName()));
            warOutputStream.write(Files.readAllBytes(tempArchive));

            warOutputStream.closeEntry();
        }
    }

    // TODO: not sure we need this test
    /*
    @Test
    public void openFileServerExistsForZip_openingNewDoesNotFail()
            throws IOException, URISyntaxException {

        Assert.assertTrue("Can not run concurrently with other test",
                StaticFileServer.openFileSystems.isEmpty());

        final TemporaryFolder folder = TemporaryFolder.builder().build();
        folder.create();

        Path tempArchive = generateZipArchive(folder);

        final FileSystem fileSystem = FileSystems
                .newFileSystem(new URL("jar:file:///"
                                + tempArchive.toString().replaceAll("\\\\", "/") + "!/")
                                .toURI(),
                        Collections.emptyMap());

        final URL folderResourceURL = new URL(
                "jar:file:///" + tempArchive.toString().replaceAll("\\\\", "/")
                        + "!/frontend");

        try {
            fileServer.getFileSystem(folderResourceURL.toURI());
        } finally {
            fileServer.closeFileSystem(folderResourceURL.toURI());
            fileSystem.close();
        }

    }

    @Test
    public void openingJarFileSystemForDifferentFilesInSameJar_existingFileSystemIsUsed()
            throws IOException, URISyntaxException {
        Assert.assertTrue("Can not run concurrently with other test",
                VertxStaticFileServer.openFileSystems.isEmpty());

        final TemporaryFolder folder = TemporaryFolder.builder().build();
        folder.create();

        Path tempArchive = generateZipArchive(folder);

        final URL folderResourceURL = new URL(
                "jar:file:///" + tempArchive.toString().replaceAll("\\\\", "/")
                        + "!/frontend");

        final URL fileResourceURL = new URL(
                "jar:file:///" + tempArchive.toString().replaceAll("\\\\", "/")
                        + "!/file.txt");

        fileServer.getFileSystem(folderResourceURL.toURI());
        fileServer.getFileSystem(fileResourceURL.toURI());

        Assert.assertEquals("Same file should be marked for both resources",
                (Integer) 2, VertxStaticFileServer.openFileSystems.entrySet()
                        .iterator().next().getValue());
        fileServer.closeFileSystem(folderResourceURL.toURI());
        Assert.assertEquals("Closing resource should be removed from jar uri",
                (Integer) 1, VertxStaticFileServer.openFileSystems.entrySet()
                        .iterator().next().getValue());
        fileServer.closeFileSystem(fileResourceURL.toURI());
        Assert.assertTrue("Closing last resource should clear marking",
                VertxStaticFileServer.openFileSystems.isEmpty());

        try {
            FileSystems.getFileSystem(folderResourceURL.toURI());
            Assert.fail("Jar FileSystem should have been closed");
        } catch (FileSystemNotFoundException fsnfe) {
            // This should happen as we should not have an open FileSystem here.
        }
    }

    @Test
    public void concurrentRequestsToJarResources_checksAreCorrect()
            throws IOException, InterruptedException, ExecutionException,
            URISyntaxException {
        Assert.assertTrue("Can not run concurrently with other test",
                VertxStaticFileServer.openFileSystems.isEmpty());

        final TemporaryFolder folder = TemporaryFolder.builder().build();
        folder.create();

        Path tempArchive = generateZipArchive(folder);

        setupRequestURI("", "", "/frontend/.");
        final URL folderResourceURL = new URL(
                "jar:file:///" + tempArchive.toString().replaceAll("\\\\", "/")
                        + "!/frontend");
        Mockito.when(servletService.getStaticResource("/frontend/."))
                .thenReturn(folderResourceURL);

        int THREADS = 5;

        List<Callable<Result>> folderNotResource = IntStream.range(0, THREADS)
                .mapToObj(i -> {
                    Callable<Result> callable = () -> {
                        try {
                            if (fileServer.isStaticResourceRequest(request)) {
                                throw new IllegalArgumentException(
                                        "Folder 'frontend' in jar should not be a static resource.");
                            }
                        } catch (Exception e) {
                            return new Result(e);
                        }
                        return new Result(null);
                    };
                    return callable;
                }).collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Future<Result>> futures = executor.invokeAll(folderNotResource);
        List<String> exceptions = new ArrayList<>();

        executor.shutdown();

        for (Future<Result> resultFuture : futures) {
            Result result = resultFuture.get();
            if (result.exception != null) {
                exceptions.add(result.exception.getMessage());
            }
        }

        Assert.assertTrue("There were exceptions in concurrent requests {"
                + exceptions + "}", exceptions.isEmpty());

        Assert.assertFalse("Folder URI should have been cleared",
                VertxStaticFileServer.openFileSystems
                        .containsKey(folderResourceURL.toURI()));
        try {
            FileSystems.getFileSystem(folderResourceURL.toURI());
            Assert.fail("FileSystem for folder resource should be closed");
        } catch (FileSystemNotFoundException fsnfe) {
            // This should happen as we should not have an open FileSystem here.
        }

        setupRequestURI("", "", "/file.txt");
        final URL fileResourceURL = new URL(
                "jar:file:///" + tempArchive.toString().replaceAll("\\\\", "/")
                        + "!/file.txt");
        Mockito.when(servletService.getStaticResource("/file.txt"))
                .thenReturn(fileResourceURL);

        List<Callable<Result>> fileIsResource = IntStream.range(0, THREADS)
                .mapToObj(i -> {
                    Callable<Result> callable = () -> {
                        try {
                            if (!fileServer.isStaticResourceRequest(request)) {
                                throw new IllegalArgumentException(
                                        "File 'file.txt' inside jar should be a static resource.");
                            }
                        } catch (Exception e) {
                            return new Result(e);
                        }
                        return new Result(null);
                    };
                    return callable;
                }).collect(Collectors.toList());

        executor = Executors.newFixedThreadPool(THREADS);
        futures = executor.invokeAll(fileIsResource);
        exceptions = new ArrayList<>();

        executor.shutdown();

        for (Future<Result> resultFuture : futures) {
            Result result = resultFuture.get();
            if (result.exception != null) {
                exceptions.add(result.exception.getMessage());
            }
        }

        Assert.assertTrue("There were exceptions in concurrent requests {"
                + exceptions + "}", exceptions.isEmpty());

        Assert.assertFalse("URI should have been cleared",
                fileServer.openFileSystems
                        .containsKey(fileResourceURL.toURI()));
        try {
            FileSystems.getFileSystem(fileResourceURL.toURI());
            Assert.fail("FileSystem for file resource should be closed");
        } catch (FileSystemNotFoundException fsnfe) {
            // This should happen as we should not have an open FileSystem here.
        }

        folder.delete();
    }
    */
    private static class Result {
        final Exception exception;

        Result(Exception exception) {
            this.exception = exception;
        }
    }

    @Test
    public void isNotResourceRequestWithContextPath() throws Exception {
        setupRequestURI("/context", "/");
        Mockito.when(vaadinService.getStaticResource("/")).thenReturn(new URL("file",
                "", -1,
                "flow/flow-tests/non-root-context-test/src/main/webapp/",
                new URLStreamHandler() {

                    @Override
                    protected URLConnection openConnection(URL u)
                            throws IOException {
                        URLConnection mock = Mockito.mock(URLConnection.class);
                        return mock;
                    }
                }));

        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void manifestPath_isResourceRequest() throws IOException {
        setupRequestURI("", "/sw.js");
        Mockito.when(vaadinService.getStaticResource("/sw.js"))
                .thenReturn(null);
        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void manifestPath_isResourceRequest_withContextPath() throws IOException {
        setupRequestURI("/foo", "/sw.js");
        Mockito.when(vaadinService.getStaticResource("/sw.js"))
                .thenReturn(null);
        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void manifestPath_indexHtml_isNotResourceRequest() throws IOException {
        setupRequestURI("", "/index.html");
        Mockito.when(vaadinService.getStaticResource("/index.html"))
                .thenReturn(null);
        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void manifestPath_indexHtml_isNotResourceRequest_withContextPath() throws IOException {
        setupRequestURI("/foo", "/index.html");
        Mockito.when(vaadinService.getStaticResource("/index.html"))
                .thenReturn(null);
        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void writeModificationTimestampBrowserHasLatest()
            throws MalformedURLException {
        fileServer.overrideBrowserHasNewestVersion = true;
        Long modificationTimestamp = writeModificationTime();
        Assert.assertEquals(modificationTimestamp,
                dateHeaders.get(HttpHeaders.LAST_MODIFIED.toString()));
    }

    @Test
    public void writeModificationTimestampBrowserDoesNotHaveLatest()
            throws MalformedURLException {
        fileServer.overrideBrowserHasNewestVersion = false;
        Long modificationTimestamp = writeModificationTime();
        Assert.assertEquals(modificationTimestamp,
                dateHeaders.get(HttpHeaders.LAST_MODIFIED.toString()));

    }

    private Long writeModificationTime() throws MalformedURLException {
        LocalDateTime modificationTime = LocalDateTime.of(2016, 2, 2, 0, 0, 0);
        Long modificationTimestamp = modificationTime
                .toEpochSecond(ZoneOffset.UTC) * 1000;
        String filename = "modified-1d-ago.txt";
        URL resourceUrl = new URL("file", "", -1, filename,
                new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL u)
                            throws IOException {
                        URLConnection mock = Mockito.mock(URLConnection.class);
                        Mockito.when(mock.getLastModified())
                                .thenReturn(modificationTimestamp);
                        return mock;
                    }
                });

        fileServer.writeModificationTimestamp(resourceUrl, routingContext);
        return modificationTimestamp;
    }

    @Test
    public void browserHasNewestVersionUnknownModificiationTime() {
        Assert.assertFalse(fileServer.browserHasNewestVersion(request, -1));
    }

    @Test(expected = AssertionError.class)
    public void browserHasNewestVersionInvalidModificiationTime() {
        fileServer.browserHasNewestVersion(request, -2);
    }

    @Test
    public void browserHasNewestVersionNoIfModifiedSinceHeader() {
        long fileModifiedTime = 0;

        Assert.assertFalse(
                fileServer.browserHasNewestVersion(request, fileModifiedTime));
    }

    @Test
    public void browserHasNewestVersionOlderIfModifiedSinceHeader() {
        long browserIfModifiedSince = 123L;
        long fileModifiedTime = 124L;
        Mockito.when(request.getHeader("If-Modified-Since")).thenReturn(HttpUtils.formatDateHeader(browserIfModifiedSince));

        Assert.assertFalse(
                fileServer.browserHasNewestVersion(request, fileModifiedTime));
    }

    @Test
    public void browserHasNewestVersionNewerIfModifiedSinceHeader() {
        long browserIfModifiedSince = 125L * 1000;
        long fileModifiedTime = 124L * 1000;
        Mockito.when(request.getHeader(HttpHeaders.IF_MODIFIED_SINCE.toString()))
                .thenReturn(HttpUtils.formatDateHeader(browserIfModifiedSince));

        Assert.assertTrue(
                fileServer.browserHasNewestVersion(request, fileModifiedTime));
    }

    @Test
    public void writeCacheHeadersCacheResource() {
        fileServer.overrideCacheTime = 12;
        fileServer.writeCacheHeaders("/folder/myfile.txt", response);
        Assert.assertTrue(headers.get(HttpHeaders.CACHE_CONTROL.toString()).contains("max-age=12"));
    }

    @Test
    public void nonProductionMode_writeCacheHeadersCacheResource_noCache() {
        Mockito.when(configuration.isProductionMode()).thenReturn(false);

        fileServer.overrideCacheTime = 12;
        fileServer.writeCacheHeaders("/folder/myfile.txt", response);
        Assert.assertEquals("no-cache", headers.get(HttpHeaders.CACHE_CONTROL.toString()));
    }

    @Test
    public void writeCacheHeadersDoNotCacheResource() {
        fileServer.overrideCacheTime = 0;
        fileServer.writeCacheHeaders("/folder/myfile.txt", response);
        Assert.assertTrue(headers.get(HttpHeaders.CACHE_CONTROL.toString()).contains("max-age=0"));
        Assert.assertTrue(
                headers.get(HttpHeaders.CACHE_CONTROL.toString()).contains("must-revalidate"));
    }

    @Test
    public void nonProductionMode_writeCacheHeadersDoNotCacheResource() {
        Mockito.when(configuration.isProductionMode()).thenReturn(false);

        fileServer.overrideCacheTime = 0;
        fileServer.writeCacheHeaders("/folder/myfile.txt", response);
        Assert.assertEquals("no-cache", headers.get(HttpHeaders.CACHE_CONTROL.toString()));
    }

    @Test
    public void getCacheTime() {
        int oneYear = 60 * 60 * 24 * 365;
        Assert.assertEquals(oneYear,
                fileServer.getCacheTime("somefile.cache.js"));
        Assert.assertEquals(oneYear,
                fileServer.getCacheTime("folder/somefile.cache.js"));
        Assert.assertEquals(0, fileServer.getCacheTime("somefile.nocache.js"));
        Assert.assertEquals(0,
                fileServer.getCacheTime("folder/somefile.nocache.js"));
        Assert.assertEquals(3600, fileServer.getCacheTime("randomfile.js"));
        Assert.assertEquals(3600,
                fileServer.getCacheTime("folder/randomfile.js"));
    }

    @Test
    public void serveNonExistingStaticResource() throws IOException {
        setupRequestURI("", "/nonexisting/file.js");

        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void serveStaticResource() throws IOException {
        setupRequestURI("", "/some/file.js");
        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        Mockito.when(vaadinService.getStaticResource("/some/file.js"))
                .thenReturn(createFileURLWithDataAndLength("/some/file.js",
                        fileData));

        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertArrayEquals(fileData, responseOutput.getBytes());
    }

    @Test
    public void contextPath_serveStaticBundleBuildResource()
            throws IOException {
        String pathInfo = "/VAADIN/build/vaadin-bundle-1234.cache.js";
        setupRequestURI("/context", pathInfo);
        assertBundleBuildResource(pathInfo);
    }

    @Test
    public void serveStaticBundleBuildResource() throws IOException {
        String pathInfo = "/VAADIN/build/vaadin-bundle-1234.cache.js";
        setupRequestURI("", pathInfo);
        assertBundleBuildResource(pathInfo);
    }

    @Test
    public void contextPath_serveStaticFileResource() throws IOException {
        String pathInfo = "/VAADIN/static/img/bg.jpg";
        setupRequestURI("/context", pathInfo);
        assertBundleBuildResource(pathInfo);
    }

    @Test
    public void serveStaticFileResource() throws IOException {
        String pathInfo = "/VAADIN/static/img/bg.jpg";
        setupRequestURI("", pathInfo);
        assertBundleBuildResource(pathInfo);
    }

    public void assertBundleBuildResource(String pathInfo) throws IOException {
        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        ClassLoader mockLoader = Mockito.mock(ClassLoader.class);
        Mockito.when(vaadinService.getClassLoader()).thenReturn(mockLoader);

        Mockito.when(mockLoader.getResource(WEBAPP_RESOURCE_PREFIX + pathInfo))
                .thenReturn(createFileURLWithDataAndLength(
                        "/" + WEBAPP_RESOURCE_PREFIX + pathInfo, fileData));

        mockStatsBundles(mockLoader);
        mockConfigurationPolyfills();

        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertArrayEquals(fileData, responseOutput.getBytes());
    }

    private void staticBuildResourceWithDirectoryChange_nothingServed(
            String pathInfo) throws IOException {
        setupRequestURI("/context",  pathInfo);
        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        ClassLoader mockLoader = Mockito.mock(ClassLoader.class);
        Mockito.when(vaadinService.getClassLoader()).thenReturn(mockLoader);

        Mockito.when(mockLoader.getResource(WEBAPP_RESOURCE_PREFIX + pathInfo))
                .thenReturn(createFileURLWithDataAndLength(
                        "/" + WEBAPP_RESOURCE_PREFIX + pathInfo, fileData));

        // have data available for /VAADIN/vaadin-bundle-1234.cache.js
        Mockito.when(mockLoader.getResource(
                        WEBAPP_RESOURCE_PREFIX + pathInfo.replace("build/../", "")))
                .thenReturn(
                        createFileURLWithDataAndLength(
                                WEBAPP_RESOURCE_PREFIX
                                        + pathInfo.replace("build/../", ""),
                                fileData));

        mockStatsBundles(mockLoader);
        mockConfigurationPolyfills();

        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertEquals(0, responseOutput.length());
        Assert.assertEquals(HttpStatusCode.BAD_REQUEST.getCode(),
                responseCode.get());
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeWithSlash_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/../vaadin-bundle-1234.cache.js");
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeWithBackslash_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/something\\..\\vaadin-bundle-1234.cache.js");
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeWithEncodedBackslashUpperCase_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/something%5C..%5Cvaadin-bundle-1234.cache.js");
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeWithEncodedBackslashLowerCase_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/something%5c..%5cvaadin-bundle-1234.cache.js");
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeInTheEndWithSlash_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/..");
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeInTheEndWithBackslash_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/something\\..");
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeInTheEndWithEncodedBackslashUpperCase_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/something%5C..");
    }

    @Test
    public void serveStaticResource_uriWithDirectoryChangeInTheEndWithEncodedBackslashLowerCase_returnsImmediatelyAndSetsForbiddenStatus()
            throws IOException {
        staticBuildResourceWithDirectoryChange_nothingServed(
                "/VAADIN/build/something%5c..");
    }

    @Test
    public void customStaticBuildResource_isServed() throws IOException {
        String pathInfo = "/VAADIN/build/my-text.txt";
        setupRequestURI("", pathInfo);
        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        ClassLoader mockLoader = Mockito.mock(ClassLoader.class);
        Mockito.when(vaadinService.getClassLoader()).thenReturn(mockLoader);

        Mockito.when(mockLoader.getResource(WEBAPP_RESOURCE_PREFIX + pathInfo))
                .thenReturn(createFileURLWithDataAndLength(
                        "/" + WEBAPP_RESOURCE_PREFIX + pathInfo, fileData));

        mockStatsBundles(mockLoader);
        mockConfigurationPolyfills();

        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertArrayEquals(fileData, responseOutput.getBytes());
    }

    @Test
    public void nonexistingStaticBuildResource_notServed() throws IOException {
        String pathInfo = "/VAADIN/build/my-text.txt";
        setupRequestURI("", pathInfo);
        ClassLoader mockLoader = Mockito.mock(ClassLoader.class);
        Mockito.when(vaadinService.getClassLoader()).thenReturn(mockLoader);

        mockStatsBundles(mockLoader);
        mockConfigurationPolyfills();

        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void staticManifestPathResource_isServed() throws IOException {
        String pathInfo = "/sw.js";
        setupRequestURI("", pathInfo);
        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        ClassLoader mockLoader = Mockito.mock(ClassLoader.class);
        Mockito.when(vaadinService.getClassLoader()).thenReturn(mockLoader);

        Mockito.when(mockLoader.getResource(WEBAPP_RESOURCE_PREFIX + pathInfo))
                .thenReturn(createFileURLWithDataAndLength(
                        "/" + WEBAPP_RESOURCE_PREFIX + pathInfo, fileData));

        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertArrayEquals(fileData, responseOutput.getBytes());
    }

    @Test
    public void staticManifestPathIndexHtmlResource_returnsNotFound()
            throws IOException {
        String pathInfo = "/index.html";
        setupRequestURI("", pathInfo);
        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        ClassLoader mockLoader = Mockito.mock(ClassLoader.class);
        Mockito.when(vaadinService.getClassLoader()).thenReturn(mockLoader);

        Mockito.when(mockLoader.getResource(WEBAPP_RESOURCE_PREFIX + pathInfo))
                .thenReturn(createFileURLWithDataAndLength(
                        "/" + WEBAPP_RESOURCE_PREFIX + pathInfo, fileData));

        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void customStatsJson_isServedFromServlet() throws IOException {

        String pathInfo = "/VAADIN/build/stats.json";
        setupRequestURI("/servlet",  pathInfo);
        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);

        ClassLoader mockLoader = Mockito.mock(ClassLoader.class);
        Mockito.when(vaadinService.getClassLoader()).thenReturn(mockLoader);
        mockStatsBundles(mockLoader);
        mockConfigurationPolyfills();

        Mockito.when(vaadinService.getStaticResource(pathInfo))
                .thenReturn(createFileURLWithDataAndLength(pathInfo, fileData));

        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertArrayEquals(fileData, responseOutput.getBytes());
    }

    public void mockConfigurationPolyfills() {
        Mockito.when(configuration.getPolyfills()).thenReturn(
                Arrays.asList(POLYFILLS_DEFAULT_VALUE.split("[, ]+")));
    }

    public void mockStatsBundles(ClassLoader mockLoader) {
        Mockito.when(configuration.getStringProperty(
                        SERVLET_PARAMETER_STATISTICS_JSON,
                        VAADIN_SERVLET_RESOURCES + STATISTICS_JSON_DEFAULT))
                .thenReturn("META-INF/VAADIN/config/stats.json");
        Mockito.when(mockLoader
                        .getResourceAsStream("META-INF/VAADIN/config/stats.json"))
                .thenReturn(new ByteArrayInputStream(getStatsData()));
    }

    /**
     * Returns a byte array for a valid stats.json containing only chunks
     *
     * @return
     */
    public byte[] getStatsData() {
        return ("{ " + "\"assetsByChunkName\" :{ "
                + "\"index\": \"build/vaadin-bundle-1234.cache.js\"} }")
                .getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void serveStaticResourceBrowserHasLatest() throws IOException {
        long browserLatest = 123L * 1000;
        long fileModified = 123L * 1000;

        setupRequestURI("", "/some/file.js");
        Mockito.when(request.getHeader(HttpHeaders.IF_MODIFIED_SINCE.toString()))
                .thenReturn(HttpUtils.formatDateHeader(browserLatest));

        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        Mockito.when(vaadinService.getStaticResource("/some/file.js"))
                .thenReturn(createFileURLWithDataAndLength("/some/file.js",
                        fileData, fileModified));
        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertEquals(0, responseOutput.length());
        Assert.assertEquals(HttpServletResponse.SC_NOT_MODIFIED,
                responseCode.get());
    }

    @Test
    public void serveStaticResourceFromWebjarWithIncorrectPath()
            throws IOException {
        Mockito.when(configuration.getBooleanProperty(
                        VertxStaticFileServer.PROPERTY_FIX_INCORRECT_WEBJAR_PATHS, false))
                .thenReturn(true);

        byte[] fileData = "function() {eval('foo');};"
                .getBytes(StandardCharsets.UTF_8);
        Mockito.when(vaadinService.getStaticResource("/webjars/foo/bar.js"))
                .thenReturn(createFileURLWithDataAndLength(
                        "/webjars/foo/bar.js", fileData));

        setupRequestURI("", "/frontend/src/webjars/foo/bar.js");

        Assert.assertTrue(fileServer.serveStaticResource(routingContext));
        Assert.assertArrayEquals(fileData, responseOutput.getBytes());
    }

    @Test
    public void serveStaticResourceFromWebjarWithIncorrectPathAndFixingDisabled()
            throws IOException {
        Mockito.when(configuration.getBooleanProperty(
                        VertxStaticFileServer.PROPERTY_FIX_INCORRECT_WEBJAR_PATHS, false))
                .thenReturn(false);

        Mockito.when(vaadinService
                        .getStaticResource("/frontend/src/webjars/foo/bar.js"))
                .thenReturn(null);

        setupRequestURI("", "/frontend/src/webjars/foo/bar.js");

        Assert.assertFalse(fileServer.serveStaticResource(routingContext));
    }

    @Test
    public void getStaticResource_delegateToVaadinService()
            throws MalformedURLException {
        URL url = new URL("http://bar");
        Mockito.when(vaadinService.getStaticResource("foo")).thenReturn(url);
        URL result = fileServer.getStaticResource("foo");

        Mockito.verify(vaadinService).getStaticResource("foo");
        Assert.assertSame(url, result);
    }

    private static class CapturingServletOutputStream
            extends ServletOutputStream {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            baos.write(b);
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public boolean isReady() {
            return true;
        }

        public byte[] getOutput() {
            return baos.toByteArray();
        }
    }

    private static class OverrideableStaticFileServer extends VertxStaticFileServer {
        public boolean writeResponse = true;
        private Boolean overrideBrowserHasNewestVersion;
        private Integer overrideCacheTime;
        private DeploymentConfiguration configuration;

        OverrideableStaticFileServer(VertxVaadinService servletService, DeploymentConfiguration configuration) {
            super(servletService);
            this.configuration = configuration;
        }

        @Override
        protected boolean browserHasNewestVersion(HttpServerRequest request,
                                                  long resourceLastModifiedTimestamp) {
            if (overrideBrowserHasNewestVersion != null) {
                return overrideBrowserHasNewestVersion;
            }

            return super.browserHasNewestVersion(request,
                    resourceLastModifiedTimestamp);
        }

        @Override
        protected int getCacheTime(String filenameWithPath) {
            if (overrideCacheTime != null) {
                return overrideCacheTime;
            }
            return super.getCacheTime(filenameWithPath);
        }

        @Override
        public boolean serveStaticResource(RoutingContext routingContext) throws IOException {
            if (!writeResponse)
                try {
                    {
                        ResponseWriter fakeWriter = new ResponseWriter(
                            configuration) {
                            @Override
                            public void writeResponseContents(
                                String filenameWithPath, URL resourceUrl, RoutingContext routingContext)
                                throws IOException {
                                return;
                            }
                        };
                        Field f = VertxStaticFileServer.class
                            .getDeclaredField("responseWriter");
                        f.setAccessible(true);
                        f.set(this, fakeWriter);
                    }
                } catch (IllegalArgumentException | IllegalAccessException
                    | NoSuchFieldException | SecurityException e) {
                    throw new RuntimeException(e);
                }
            return super.serveStaticResource(routingContext);
        }

    }
}
