package util;

import misc.Format;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility that extracts all Path elements from an SVG File
 * */
public class SvgPathParser {

    public record PathElement(@Nullable String id, @Nullable String title, String data) {
    }


    private static final String SVG_ELEMENT_PATH = "path";
    private static final String SVG_PATH_ATTR_ID = "id";
    private static final String SVG_PATH_ATTR_TITLE = "title";
    private static final String SVG_PATH_ATTR_DATA = "d";

    @NotNull
    public final String lineCommentToken;

    @NotNull
    public final String pathDataDelimiter;

    @NotNull
    public final String pathDataDelimiterRegex;

    @NotNull
    public final Charset encoding;

    public SvgPathParser(@NotNull String lineCommentToken, @NotNull String pathDataDelimiter, @NotNull String pathDataDelimiterRegex, @NotNull Charset encoding) {
        this.lineCommentToken = lineCommentToken;
        this.pathDataDelimiter = pathDataDelimiter;
        this.pathDataDelimiterRegex = pathDataDelimiterRegex;
        this.encoding = encoding;
    }

    /* Loading */

    public static boolean isValidPathData(@Nullable String pathData) {
        return !(pathData == null || pathData.isBlank());
    }

    @NotNull
    public List<PathElement> extractSvgPathElements(@NotNull Path svgFile) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = docFactory.newDocumentBuilder();

        final Document doc = builder.parse(svgFile.toFile());
        final NodeList pathElements = doc.getElementsByTagName(SVG_ELEMENT_PATH);
        if (pathElements.getLength() == 0)
            return Collections.emptyList();

        final List<PathElement> elements = new ArrayList<>(pathElements.getLength() + 2);
        for (int i=0; i < pathElements.getLength(); i++) {
            final Node node = pathElements.item(i);

            if (!(node instanceof Element element))
                continue;

            final String data = element.getAttribute(SVG_PATH_ATTR_DATA);
            if (isValidPathData(data)) {
                elements.add(new PathElement(element.getAttribute(SVG_PATH_ATTR_ID), element.getAttribute(SVG_PATH_ATTR_TITLE), data));
            }
        }

        return elements;
    }

    @NotNull
    public List<String> extractPathsFromSvg(@NotNull Path svgFile) throws ParserConfigurationException, IOException, SAXException {
        final List<PathElement> pes = extractSvgPathElements(svgFile);
        return pes.stream().map(PathElement::data).collect(Collectors.toList());
    }

    @NotNull
    public List<String> extractPathsFromPathDataFile(@NotNull String pathData) {
        if (Format.isEmpty(pathData))
            return Collections.emptyList();

        pathData = Format.removeAllLinedComments(pathData, lineCommentToken, true);
        if (Format.isEmpty(pathData))
            return Collections.emptyList();

        final String[] paths = pathData.split(pathDataDelimiterRegex);
        return Stream.of(paths).filter(SvgPathParser::isValidPathData).collect(Collectors.toList());
    }

    @NotNull
    public List<String> extractPathsFromPathDataFile(@NotNull Path pathDataFile) throws IOException {
        return extractPathsFromPathDataFile(Files.readString(pathDataFile, encoding));
    }


    /* Writing */

    public void writeSvgPaths(@NotNull Path svgFile, boolean pretty, @NotNull Appendable out) throws ParserConfigurationException, IOException, SAXException {
        final List<PathElement> paths = extractSvgPathElements(svgFile);
        if (paths.isEmpty())
            return;

        // Header
        if (pretty) {
            final String titleTag = "Source <" + svgFile.getFileName() + "> Parsed by RC SVG Parser";
            out.append(lineCommentToken)
                    .append(' ')
                    .append(titleTag)
                    .append('\n');
        }

        final Iterator<PathElement> itr = paths.iterator();
        while (itr.hasNext()) {
            final PathElement pe = itr.next();

            if (pretty) {
                out.append('\n')
                        .append(lineCommentToken)
                        .append(" Path ID: ")
                        .append(pe.id())
                        .append(", Title: ")
                        .append(pe.title())
                        .append('\n');
            }

            out.append(pe.data());

            // except last element
            if (itr.hasNext()) {
                out.append(pathDataDelimiter);

                if (pretty) {
                    out.append('\n');
                }
            }
        }
    }

    public void writeSvgPaths(@NotNull Path svgFile, boolean pretty, @NotNull Path outFile) throws ParserConfigurationException, IOException, SAXException {
        try (final Writer writer = Files.newBufferedWriter(outFile, encoding)) {
            writeSvgPaths(svgFile, pretty, writer);
        }
    }

    @NotNull
    public CharSequence writeSvgPathsToString(@NotNull Path svgFile, boolean pretty) throws ParserConfigurationException, IOException, SAXException {
        final StringBuilder sb = new StringBuilder();
        writeSvgPaths(svgFile, pretty, sb);
        return sb;
    }
}
