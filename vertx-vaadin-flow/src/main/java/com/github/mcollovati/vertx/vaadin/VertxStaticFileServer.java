/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.mcollovati.vertx.vaadin;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.HttpStatusCode;
import com.vaadin.flow.server.StaticFileServer;
import com.vaadin.flow.server.VaadinService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.impl.FileResolverImpl;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.spi.file.FileResolver;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.VAADIN_BUILD_FILES_PATH;
import static com.vaadin.flow.server.Constants.VAADIN_WEBAPP_RESOURCES;
import static com.vaadin.flow.shared.ApplicationConstants.VAADIN_STATIC_FILES_PATH;

/**
 * Adapted from Vaadin StaticFileServer
 */
class VertxStaticFileServer implements Handler<RoutingContext> {

    static final String PROPERTY_FIX_INCORRECT_WEBJAR_PATHS = Constants.VAADIN_PREFIX + "fixIncorrectWebjarPaths";
    private static final Pattern INCORRECT_WEBJAR_PATH_REGEX = Pattern.compile("^/frontend[-\\w/]*/webjars/");
    protected static final Pattern APP_THEME_PATTERN = Pattern.compile("^\\/themes\\/[\\s\\S]+?\\/");

    private final VertxVaadinService vaadinService;
    private final DeploymentConfiguration deploymentConfiguration;
    private final DevModeHandler devModeHandler;
    private final StaticFileServer vaadinStaticFileServer;
    private ResponseWriter responseWriter;

    // Mapped uri is for the jar file
    static final Map<URI, Integer> openFileSystems = new HashMap<>();
    private static final Object fileSystemLock = new Object();

    /**
     * Constructs a file server.
     *
     * @param vaadinService vaadin service for the deployment, not <code>null</code>
     */
    public VertxStaticFileServer(VertxVaadinService vaadinService) {
        this.vaadinService = vaadinService;
        vaadinStaticFileServer = new StaticFileServer(vaadinService);
        deploymentConfiguration = vaadinService.getDeploymentConfiguration();
        responseWriter = new ResponseWriter(deploymentConfiguration);
        this.devModeHandler =
                DevModeHandlerManager.getDevModeHandler(vaadinService).orElse(null);
    }

    public boolean serveStaticResource(RoutingContext routingContext) throws IOException {
        String filenameWithPath = getRequestFilename(routingContext);
        if (filenameWithPath.endsWith("/")) {
            // Directories are not static resources although
            // servletContext.getResource will return a URL for them, at
            // least with Jetty
            return false;
        }

        if (HandlerHelper.isPathUnsafe(filenameWithPath)) {
            getLogger().info("Blocked attempt to access file: {}", filenameWithPath);
            routingContext.response().setStatusCode(HttpStatusCode.BAD_REQUEST.getCode());
            routingContext.end();
            return true;
        }

        URL resourceUrl = null;
        if (APP_THEME_PATTERN.matcher(filenameWithPath).find()) {
            resourceUrl = vaadinService
                    .getClassLoader()
                    .getResource(VAADIN_WEBAPP_RESOURCES + "VAADIN/static/" + filenameWithPath.replaceFirst("^/", ""));

        } else if (!"/index.html".equals(filenameWithPath)) {
            // index.html needs to be handled by IndexHtmlRequestHandler
            resourceUrl = vaadinService
                    .getClassLoader()
                    .getResource(VAADIN_WEBAPP_RESOURCES + filenameWithPath.replaceFirst("^/", ""));
        }

        if (resourceUrl == null) {
            resourceUrl = getStaticResource(filenameWithPath);
        }
        if (resourceUrl == null && shouldFixIncorrectWebjarPaths() && isIncorrectWebjarPath(filenameWithPath)) {
            // Flow issue #4601
            resourceUrl = getStaticResource(fixIncorrectWebjarPath(filenameWithPath));
        }

        if (resourceUrl == null) {
            // Not found in webcontent or in META-INF/resources in some JAR
            return false;
        }

        if (resourceIsDirectory(resourceUrl)) {
            // Directories are not static resources although
            // servletContext.getResource will return a URL for them, at
            // least with Jetty
            return false;
        }

        // There is a resource!

        // Intentionally writing cache headers also for 304 responses
        HttpServerResponse response = routingContext.response();
        writeCacheHeaders(filenameWithPath, response);

        long timestamp = writeModificationTimestamp(resourceUrl, routingContext);
        if (browserHasNewestVersion(routingContext.request(), timestamp)) {
            // Browser is up to date, nothing further to do than set the
            // response code
            response.setStatusCode(HttpServletResponse.SC_NOT_MODIFIED);
            routingContext.end();
            return true;
        }
        responseWriter.writeResponseContents(filenameWithPath, resourceUrl, routingContext);
        routingContext.end();
        return true;
    }

    static final String UNSAFE_PATH_ERROR_MESSAGE_PATTERN = "Blocked attempt to access file: {}";

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            if (!serveStaticResource(routingContext)) {
                routingContext.next();
            }
        } catch (IOException e) {
            routingContext.fail(e);
        }
    }

    /**
     * Returns the (decoded) requested file name, relative to the context path.
     * <p>
     * Package private for testing purposes.
     *
     * @param routingContext the routing context
     * @return the requested file name, starting with a {@literal /}
     */
    String getRequestFilename(RoutingContext routingContext) {
        // http://localhost:8888/context/servlet/folder/file.js
        // ->
        // /servlet/folder/file.js
        //
        // http://localhost:8888/context/servlet/VAADIN/folder/file.js
        // ->
        // /VAADIN/folder/file.js
        return HttpUtils.pathInfo(routingContext);
        /*
        if (request.getPathInfo() == null) {
            return request.getServletPath();
        } else if (request.getPathInfo().startsWith("/" + VAADIN_MAPPING)
                || APP_THEME_PATTERN.matcher(request.getPathInfo()).find()) {
            return request.getPathInfo();
        }
        return request.getServletPath() + request.getPathInfo();
        */
    }

    /**
     * Writes the modification timestamp info for the file into the response.
     *
     * @param resourceUrl    the internal URL of the file
     * @param routingContext routing context
     * @return the written timestamp or -1 if no timestamp was written
     */
    protected long writeModificationTimestamp(URL resourceUrl, RoutingContext routingContext) {
        // Find the modification timestamp
        long lastModifiedTime;
        URLConnection connection = null;
        try {
            connection = resourceUrl.openConnection();
            lastModifiedTime = connection.getLastModified();
            // Remove milliseconds to avoid comparison problems (milliseconds
            // are not returned by the browser in the "If-Modified-Since"
            // header).
            lastModifiedTime = lastModifiedTime - lastModifiedTime % 1000;
            routingContext
                    .response()
                    .putHeader(HttpHeaders.LAST_MODIFIED, HttpUtils.formatDateHeader(lastModifiedTime));
            return lastModifiedTime;
        } catch (Exception e) {
            getLogger().trace("Failed to find out last modified timestamp. Continuing without it.", e);
        } finally {
            try {
                // Explicitly close the input stream to prevent it
                // from remaining hanging
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4257700
                if (connection != null) {
                    InputStream is = connection.getInputStream();
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {
                getLogger().warn("Error closing URLConnection input stream", e);
            }
        }
        return -1L;
    }

    /**
     * Checks if the browser has an up to date cached version of requested
     * resource using the "If-Modified-Since" header.
     *
     * @param request                       The HttpServletRequest from the browser.
     * @param resourceLastModifiedTimestamp The timestamp when the resource was last modified. -1 if the
     *                                      last modification time is unknown.
     * @return true if the If-Modified-Since header tells the cached version in
     * the browser is up to date, false otherwise
     */
    protected boolean browserHasNewestVersion(HttpServerRequest request, long resourceLastModifiedTimestamp) {
        assert resourceLastModifiedTimestamp >= -1L;

        if (resourceLastModifiedTimestamp == -1L) {
            // We do not know when it was modified so the browser cannot have an
            // up-to-date version
            return false;
        }
        /*
         * The browser can request the resource conditionally using an
         * If-Modified-Since header. Check this against the last modification
         * time.
         */
        try {
            // If-Modified-Since represents the timestamp of the version cached
            // in the browser
            long headerIfModifiedSince = HttpUtils.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE, request);

            if (headerIfModifiedSince >= resourceLastModifiedTimestamp) {
                // Browser has this an up-to-date version of the resource
                return true;
            }
        } catch (Exception e) {
            // Failed to parse header.
            getLogger().trace("Unable to parse If-Modified-Since", e);
        }
        return false;
    }

    /**
     * Writes cache headers for the file into the response.
     *
     * @param filenameWithPath the name and path of the file being sent
     * @param response         the response object
     */
    protected void writeCacheHeaders(String filenameWithPath, HttpServerResponse response) {
        int resourceCacheTime = getCacheTime(filenameWithPath);
        String cacheControl;
        if (!deploymentConfiguration.isProductionMode()) {
            cacheControl = "no-cache";
        } else if (resourceCacheTime > 0) {
            cacheControl = "max-age=" + resourceCacheTime;
        } else {
            cacheControl = "public, max-age=0, must-revalidate";
        }
        response.putHeader(HttpHeaders.CACHE_CONTROL, cacheControl);
    }

    /**
     * Calculates the cache lifetime for the given filename in seconds.
     * <p>
     * By default filenames containing ".nocache." return 0, filenames
     * containing ".cache." return one year and all other files return 1 hour.
     *
     * @param filenameWithPath the name of the file being sent
     * @return cache lifetime for the given filename in seconds
     */
    protected int getCacheTime(String filenameWithPath) {
        /*
         * GWT conventions:
         *
         * - files containing .nocache. will not be cached.
         *
         * - files containing .cache. will be cached for one year.
         *
         * https://developers.google.com/web-toolkit/doc/latest/
         * DevGuideCompilingAndDebugging#perfect_caching
         */
        if (filenameWithPath.contains(".nocache.")) {
            return 0;
        }
        if (filenameWithPath.contains(".cache.")) {
            return 60 * 60 * 24 * 365;
        }
        /*
         * For all other files, the browser is allowed to cache for 1 hour
         * without checking if the file has changed.
         */
        return 3600;
    }

    /**
     * Returns a URL to the static Web resource at the given URI or null if no
     * file found.
     * <p>
     * The resource will be exposed via HTTP (available as a static web
     * resource). The {@code null} return value means that the resource won't be
     * exposed as a Web resource even if it's a resource available via
     * {@link ServletContext}.
     *
     * @param path the path for the resource
     * @return the resource located at the named path to expose it via Web, or
     * {@code null} if there is no resource at that path or it should
     * not be exposed
     * @see VaadinService#getStaticResource(String)
     */
    protected URL getStaticResource(String path) {
        return vaadinService.getStaticResource(path);
    }

    /**
     * Check if it is ok to serve the requested file from the classpath.
     * <p>
     * ClassLoader is applicable for use when we are in NPM mode and are serving
     * from the VAADIN/build folder with no folder changes in path.
     *
     * @param filenameWithPath requested filename containing path
     * @return true if we are ok to try serving the file
     */
    private boolean isAllowedVAADINBuildOrStaticUrl(String filenameWithPath) {
        // Check that we target VAADIN/build | VAADIN/static | themes/theme-name
        return filenameWithPath.startsWith("/" + VAADIN_BUILD_FILES_PATH)
                || filenameWithPath.startsWith("/" + VAADIN_STATIC_FILES_PATH)
                || APP_THEME_PATTERN.matcher(filenameWithPath).find();
    }

    private boolean resourceIsDirectory(URL resource) {
        if (resource.getPath().endsWith("/")) {
            return true;
        }
        URI resourceURI = null;
        try {
            resourceURI = resource.toURI();
        } catch (URISyntaxException e) {
            getLogger().debug("Syntax error in uri from getStaticResource", e);
            // Return false as we couldn't determine if the resource is a
            // directory.
            return false;
        }

        if ("jar".equals(resource.getProtocol())) {
            // Get the file path in jar
            final String pathInJar =
                    resource.getPath().substring(resource.getPath().indexOf('!') + 1);
            try {
                FileSystem fileSystem = getFileSystem(resourceURI);
                // Get the file path inside the jar.
                final Path path = fileSystem.getPath(pathInJar);

                return Files.isDirectory(path);
            } catch (IOException e) {
                getLogger().debug("failed to read jar file contents", e);
            } finally {
                closeFileSystem(resourceURI);
            }
        }

        // If not a jar check if a file path directory.
        return "file".equals(resource.getProtocol()) && Files.isDirectory(Paths.get(resourceURI));
    }

    /**
     * Get the file URI for the resource jar file. Returns give URI if
     * URI.scheme is not of type jar.
     * <p>
     * The URI for a file inside a jar is composed as
     * 'jar:file://...pathToJar.../jarFile.jar!/pathToFile'
     * <p>
     * the first step strips away the initial scheme 'jar:' leaving us with
     * 'file://...pathToJar.../jarFile.jar!/pathToFile' from which we remove the
     * inside jar path giving the end result
     * 'file://...pathToJar.../jarFile.jar'
     *
     * @param resourceURI resource URI to get file URI for
     * @return file URI for resource jar or given resource if not a jar schemed
     * URI
     */
    private URI getFileURI(URI resourceURI) {
        if (!"jar".equals(resourceURI.getScheme())) {
            return resourceURI;
        }
        try {
            String scheme = resourceURI.getRawSchemeSpecificPart();
            int jarPartIndex = scheme.indexOf("!/");
            if (jarPartIndex != -1) {
                scheme = scheme.substring(0, jarPartIndex);
            }
            return new URI(scheme);
        } catch (URISyntaxException syntaxException) {
            throw new IllegalArgumentException(syntaxException.getMessage(), syntaxException);
        }
    }

    // Package protected for feature verification purpose
    FileSystem getFileSystem(URI resourceURI) throws IOException {
        synchronized (fileSystemLock) {
            URI fileURI = getFileURI(resourceURI);
            if (!fileURI.getScheme().equals("file")) {
                throw new IOException("Can not read scheme '"
                        + fileURI.getScheme() + "' for resource " + resourceURI
                        + " and will determine this as not a folder");
            }

            if (openFileSystems.computeIfPresent(fileURI, (key, value) -> value + 1) != null) {
                // Get filesystem is for the file to get the correct provider
                return FileSystems.getFileSystem(resourceURI);
            }
            // Opened filesystem is for the file to get the correct provider
            FileSystem fileSystem = getNewOrExistingFileSystem(resourceURI);
            openFileSystems.put(fileURI, 1);
            return fileSystem;
        }
    }

    private FileSystem getNewOrExistingFileSystem(URI resourceURI) throws IOException {
        try {
            return FileSystems.newFileSystem(resourceURI, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException fsaee) {
            getLogger().trace("Tried to get new filesystem, but it already existed for target uri.", fsaee);
            return FileSystems.getFileSystem(resourceURI);
        }
    }

    // Package protected for feature verification purpose
    void closeFileSystem(URI resourceURI) {
        synchronized (fileSystemLock) {
            try {
                URI fileURI = getFileURI(resourceURI);
                final Integer locks = openFileSystems.computeIfPresent(fileURI, (key, value) -> value - 1);
                if (locks != null && locks == 0) {
                    openFileSystems.remove(fileURI);
                    // Get filesystem is for the file to get the correct
                    // provider
                    FileSystems.getFileSystem(resourceURI).close();
                }
            } catch (IOException ioe) {
                getLogger().error("Failed to close FileSystem for '{}'", resourceURI);
                getLogger().debug("Exception closing FileSystem", ioe);
            }
        }
    }

    // When referring to webjar resources from application stylesheets (loaded
    // using @StyleSheet) using relative paths, the paths will be different in
    // development mode and in production mode. The reason is that in production
    // mode, the CSS is incorporated into the bundle and when this happens,
    // the relative paths are changed so that they end up pointing to paths like
    // 'frontend-es6/webjars' instead of just 'webjars'.

    // There is a similar problem when referring to webjar resources from
    // application stylesheets inside HTML custom styles (loaded using
    // @HtmlImport). In this case, the paths will also be changed in production.
    // For example, if the HTML file resides in 'frontend/styles' and refers to
    // 'webjars/foo', the path will be changed to refer to
    // 'frontend/styles/webjars/foo', which is incorrect. You could add '../../'
    // to the path in the HTML file but then it would not work in development
    // mode.

    // These paths are changed deep inside the Polymer build chain. It was
    // easier to fix the StaticFileServer to take the incorrect path names
    // into account than fixing the Polymer build chain to generate correct
    // paths. Hence, these methods:

    private boolean shouldFixIncorrectWebjarPaths() {
        return deploymentConfiguration.isProductionMode()
                && deploymentConfiguration.getBooleanProperty(PROPERTY_FIX_INCORRECT_WEBJAR_PATHS, false);
    }

    private boolean isIncorrectWebjarPath(String requestFilename) {
        return INCORRECT_WEBJAR_PATH_REGEX.matcher(requestFilename).lookingAt();
    }

    private String fixIncorrectWebjarPath(String requestFilename) {
        return INCORRECT_WEBJAR_PATH_REGEX.matcher(requestFilename).replaceAll("/webjars/");
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VertxStaticFileServer.class.getName());
    }
}

class ResponseWriter implements Serializable {
    private static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("^bytes=((\\d*-\\d*\\s*,\\s*)*\\d*-\\d*\\s*)$");
    private static final Pattern BYTE_RANGE_PATTERN = Pattern.compile("(\\d*)-(\\d*)");

    /**
     * Maximum number of ranges accepted in a single Range header. Remaining
     * ranges will be ignored.
     */
    private static final int MAX_RANGE_COUNT = 16;

    /**
     * Maximum number of overlapping ranges allowed. The request will be denied
     * if above this threshold.
     */
    private static final int MAX_OVERLAPPING_RANGE_COUNT = 2;

    private final int bufferSize;
    private final boolean brotliEnabled;

    /**
     * Create a response writer with the given deployment configuration.
     *
     * @param deploymentConfiguration the deployment configuration to use, not <code>null</code>
     */
    public ResponseWriter(DeploymentConfiguration deploymentConfiguration) {
        this(DEFAULT_BUFFER_SIZE, deploymentConfiguration.isBrotli());
    }

    private ResponseWriter(int bufferSize, boolean brotliEnabled) {
        this.brotliEnabled = brotliEnabled;
        this.bufferSize = bufferSize;
    }

    /**
     * Writes the contents and content type (if available) of the given
     * resourceUrl to the response.
     * <p>
     * WARNING: note that this should not be used for a {@code resourceUrl} that
     * represents a directory! For security reasons, the directory contents
     * should not be ever written into the {@code response}, and the
     * implementation which is used for setting the content length relies on
     * {@link URLConnection#getContentLengthLong()} method which returns
     * incorrect values for directories.
     *
     * @param filenameWithPath the name of the file being sent
     * @param resourceUrl      the URL to the file, reported by the servlet container
     * @param routingContext   the routing context
     * @throws IOException if the servlet container threw an exception while locating
     *                     the resource
     */
    public void writeResponseContents(String filenameWithPath, URL resourceUrl, RoutingContext routingContext)
            throws IOException {
        writeContentType(filenameWithPath, routingContext);

        URL url = null;
        URLConnection connection = null;
        InputStream dataStream = null;

        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        if (brotliEnabled && acceptsBrotliResource(request)) {
            String brotliFilenameWithPath = filenameWithPath + ".br";
            try {
                url = getResource(brotliFilenameWithPath);
                if (url != null) {
                    connection = url.openConnection();
                    dataStream = connection.getInputStream();
                    response.putHeader(HttpHeaders.CONTENT_ENCODING, "br");
                }
            } catch (Exception e) {
                getLogger().debug("Unexpected exception looking for Brotli resource {}", brotliFilenameWithPath, e);
            }
        }

        if (dataStream == null && acceptsGzippedResource(request)) {
            // try to serve a gzipped version if available
            String gzippedFilenameWithPath = filenameWithPath + ".gz";
            try {
                url = getResource(gzippedFilenameWithPath);
                if (url != null) {
                    connection = url.openConnection();
                    dataStream = connection.getInputStream();
                    response.putHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                }
            } catch (Exception e) {
                getLogger().debug("Unexpected exception looking for gzipped resource {}", gzippedFilenameWithPath, e);
            }
        }

        if (dataStream == null) {
            // compressed resource not available, get non compressed
            url = resourceUrl;
            connection = resourceUrl.openConnection();
            dataStream = connection.getInputStream();
        } else {
            response.putHeader(HttpHeaders.VARY, "Accept-Encoding");
        }

        try {
            String range = request.getHeader("Range");
            if (range != null) {
                closeStream(dataStream);
                dataStream = null;
                writeRangeContents(range, response, url);
            } else {
                final long contentLength = connection.getContentLengthLong();
                if (0 <= contentLength) {
                    setContentLength(response, contentLength);
                }
                writeStream(response, dataStream, Long.MAX_VALUE);
            }
        } catch (IOException e) {
            getLogger().debug("Error writing static file to user", e);
        } finally {
            if (dataStream != null) {
                closeStream(dataStream);
            }
        }
    }

    private void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            getLogger().debug("Error closing input stream for resource", e);
        }
    }

    /**
     * Handle a "Header:" request. The handling logic is splits on single or
     * multiple ranges: for a single range, send a regular response with
     * Content-Length; for multiple ranges, send a "Content-Type:
     * multipart/byteranges" response. If the byte ranges are satisfiable, the
     * response code is 206, otherwise it is 416. See e.g.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests for
     * protocol details.
     */
    private void writeRangeContents(String range, HttpServerResponse response, URL resourceURL) throws IOException {
        response.putHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

        URLConnection connection = resourceURL.openConnection();

        Matcher headerMatcher = RANGE_HEADER_PATTERN.matcher(range);
        if (!headerMatcher.matches()) {
            response.putHeader(HttpHeaders.CONTENT_LENGTH, "0");
            response.setStatusCode(416); // Range Not Satisfiable
            return;
        }
        String byteRanges = headerMatcher.group(1);

        long resourceLength = connection.getContentLengthLong();
        Matcher rangeMatcher = BYTE_RANGE_PATTERN.matcher(byteRanges);

        Stack<Pair<Long, Long>> ranges = new Stack<>();
        while (rangeMatcher.find() && ranges.size() < MAX_RANGE_COUNT) {
            String startGroup = rangeMatcher.group(1);
            String endGroup = rangeMatcher.group(2);
            if (startGroup.isEmpty() && endGroup.isEmpty()) {
                response.putHeader(HttpHeaders.CONTENT_LENGTH, "0");
                response.setStatusCode(416); // Range Not Satisfiable
                getLogger().info("received a malformed range: '{}'", rangeMatcher.group());
                return;
            }
            long start = startGroup.isEmpty() ? 0L : Long.parseLong(startGroup);
            long end = endGroup.isEmpty() ? Long.MAX_VALUE : Long.parseLong(endGroup);
            if (end < start || (resourceLength >= 0 && start >= resourceLength)) {
                // illegal range -> 416
                getLogger().info("received an illegal range '{}' for resource '{}'", rangeMatcher.group(), resourceURL);
                response.putHeader(HttpHeaders.CONTENT_LENGTH, "0");
                response.setStatusCode(416);
                return;
            }
            ranges.push(new Pair<>(start, end));

            if (!verifyRangeLimits(ranges)) {
                ranges.pop();
                getLogger()
                        .info(
                                "serving only {} ranges for resource '{}' even though more were requested",
                                ranges.size(),
                                resourceURL);
                break;
            }
        }

        response.setStatusCode(206);

        if (ranges.size() == 1) {
            // single range: calculate Content-Length
            long start = ranges.get(0).getFirst();
            long end = ranges.get(0).getSecond();
            if (resourceLength >= 0) {
                end = Math.min(end, resourceLength - 1);
            }
            setContentLength(response, end - start + 1);
            response.putHeader(HttpHeaders.CONTENT_RANGE, createContentRangeHeader(start, end, resourceLength));

            final InputStream dataStream = connection.getInputStream();
            try {
                long skipped = dataStream.skip(start);
                assert (skipped == start);
                writeStream(response, dataStream, end - start + 1);
            } finally {
                closeStream(dataStream);
            }
        } else {
            writeMultipartRangeContents(ranges, connection, response, resourceURL);
        }
    }

    /**
     * Write a multi-part request with MIME type "multipart/byteranges",
     * separated by boundaries and use "Transfer-Encoding: chunked" mode to
     * avoid computing "Content-Length".
     */
    private void writeMultipartRangeContents(
            List<Pair<Long, Long>> ranges, URLConnection connection, HttpServerResponse response, URL resourceURL)
            throws IOException {
        String partBoundary = UUID.randomUUID().toString();
        response.putHeader(HttpHeaders.CONTENT_TYPE, String.format("multipart/byteranges; boundary=%s", partBoundary));
        response.putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked");

        long position = 0L;
        String mimeType = response.headers().get(HttpHeaders.CONTENT_TYPE);
        InputStream dataStream = connection.getInputStream();
        try {
            for (Pair<Long, Long> rangePair : ranges) {
                response.write(String.format("\r\n--%s\r\n", partBoundary));
                long start = rangePair.getFirst();
                long end = rangePair.getSecond();
                if (mimeType != null) {
                    response.write(String.format("Content-Type: %s\r\n", mimeType));
                }
                response.write(String.format(
                        "Content-Range: %s\r\n\r\n",
                        createContentRangeHeader(start, end, connection.getContentLengthLong())));

                if (position > start) {
                    // out-of-sequence range -> open new stream to the file
                    // alternative: use single stream with mark / reset
                    closeStream(connection.getInputStream());
                    connection = resourceURL.openConnection();
                    dataStream = connection.getInputStream();
                    position = 0L;
                }
                long skipped = dataStream.skip(start - position);
                assert (skipped == start - position);
                writeStream(response, dataStream, end - start + 1);
                position = end + 1;
            }
        } finally {
            closeStream(dataStream);
        }
        response.write(String.format("\r\n--%s", partBoundary));
    }

    private String createContentRangeHeader(long start, long end, long size) {
        String lengthString = size >= 0 ? Long.toString(size) : "*";
        return String.format("bytes %d-%d/%s", start, end, lengthString);
    }

    private void setContentLength(HttpServerResponse response, long contentLength) {
        try {
            response.putHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(contentLength));
        } catch (Exception e) {
            getLogger().debug("Error setting the content length", e);
        }
    }

    /**
     * Returns true if the number of ranges in <code>ranges</code> is less than
     * the upper limit and the number that overlap (= have at least one byte in
     * common) with the range <code>[start, end]</code> are less than the upper
     * limit.
     */
    private boolean verifyRangeLimits(List<Pair<Long, Long>> ranges) {
        if (ranges.size() > MAX_RANGE_COUNT) {
            getLogger().info("more than {} ranges requested", MAX_RANGE_COUNT);
            return false;
        }
        int count = 0;
        for (int i = 0; i < ranges.size(); i++) {
            for (int j = i + 1; j < ranges.size(); j++) {
                if (ranges.get(i).getFirst() <= ranges.get(j).getSecond()
                        && ranges.get(j).getFirst() <= ranges.get(i).getSecond()) {
                    count++;
                }
            }
        }
        if (count > MAX_OVERLAPPING_RANGE_COUNT) {
            getLogger().info("more than {} overlapping ranges requested", MAX_OVERLAPPING_RANGE_COUNT);
            return false;
        }
        return true;
    }

    private URL getResource(String resource) throws MalformedURLException {
        FileResolver fileResolver = new FileResolverImpl();
        File file = fileResolver.resolveFile(resource);
        if (!file.exists() && resource.startsWith("/" + VAADIN_BUILD_FILES_PATH) && isAllowedVAADINBuildUrl(resource)) {
            file = fileResolver.resolveFile(VAADIN_WEBAPP_RESOURCES + resource.replaceFirst("^/", ""));
        }
        return file.exists() ? file.toURI().toURL() : null;
    }

    /**
     * Check if it is ok to serve the requested file from the classpath.
     * <p>
     * ClassLoader is applicable for use when we are in NPM mode and are serving
     * from the VAADIN/build folder with no folder changes in path.
     *
     * @param filenameWithPath requested filename containing path
     * @return true if we are ok to try serving the file
     */
    private boolean isAllowedVAADINBuildUrl(String filenameWithPath) {
        // Check that we target VAADIN/build and do not have '/../'
        if (!filenameWithPath.startsWith("/" + VAADIN_BUILD_FILES_PATH) || filenameWithPath.contains("/../")) {
            getLogger().info("Blocked attempt to access file: {}", filenameWithPath);
            return false;
        }

        return true;
    }

    private void writeStream(HttpServerResponse response, InputStream dataStream, long count) throws IOException {

        final byte[] buffer = new byte[bufferSize];

        long bytesTotal = 0L;
        int bytes;
        while (bytesTotal < count
                && (bytes = dataStream.read(buffer, 0, (int) Long.min(bufferSize, count - bytesTotal))) >= 0) {
            Buffer bf = Buffer.buffer(bufferSize);
            bf.appendBytes(buffer, 0, bytes);
            response.write(bf);
            bytesTotal += bytes;
        }
    }

    /**
     * Returns whether it is ok to serve a gzipped version of the given
     * resource.
     * <p>
     * If this method returns true, the browser is ok with receiving a gzipped
     * version of the resource. In other cases, an uncompressed file must be
     * sent.
     *
     * @param request the request for the resource
     * @return true if the servlet should attempt to serve a gzipped version of
     * the resource, false otherwise
     */
    protected boolean acceptsGzippedResource(HttpServerRequest request) {
        return acceptsEncoding(request, "gzip");
    }

    /**
     * Returns whether it is ok to serve a Brotli version of the given resource.
     * <p>
     * If this method returns true, the browser is ok with receiving a Brotli
     * version of the resource. In other cases, an uncompressed or gzipped file
     * must be sent.
     *
     * @param request the request for the resource
     * @return true if the servlet should attempt to serve a Brotli version of
     * the resource, false otherwise
     */
    protected boolean acceptsBrotliResource(HttpServerRequest request) {
        return acceptsEncoding(request, "br");
    }

    private static boolean acceptsEncoding(HttpServerRequest request, String encodingName) {
        String accept = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (accept == null) {
            return false;
        }

        accept = accept.replace(" ", "");
        // Browser denies gzip compression if it reports
        // gzip;q=0
        //
        // Browser accepts gzip compression if it reports
        // "gzip"
        // "gzip;q=[notzero]"
        // "*"
        // "*;q=[not zero]"
        if (accept.contains(encodingName)) {
            return !isQualityValueZero(accept, encodingName);
        }
        return accept.contains("*") && !isQualityValueZero(accept, "*");
    }

    void writeContentType(String filenameWithPath, RoutingContext routingContext) {
        // Set type mime type if we can determine it based on the filename
        String mimetype = HttpUtils.getMimeType(filenameWithPath);
        if (mimetype != null) {
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, mimetype);
        }
    }

    /**
     * Check the quality value of the encoding. If the value is zero the
     * encoding is disabled and not accepted.
     *
     * @param acceptEncoding Accept-Encoding header from request
     * @param encoding       encoding to check
     * @return true if quality value is Zero
     */
    private static boolean isQualityValueZero(String acceptEncoding, String encoding) {
        String qPrefix = encoding + ";q=";
        int qValueIndex = acceptEncoding.indexOf(qPrefix);
        if (qValueIndex == -1) {
            return false;
        }

        // gzip;q=0.123 or gzip;q=0.123,compress...
        String qValue = acceptEncoding.substring(qValueIndex + qPrefix.length());
        int endOfQValue = qValue.indexOf(',');
        if (endOfQValue != -1) {
            qValue = qValue.substring(0, endOfQValue);
        }

        return Double.valueOf(0.000).equals(Double.valueOf(qValue));
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass().getName());
    }
}
