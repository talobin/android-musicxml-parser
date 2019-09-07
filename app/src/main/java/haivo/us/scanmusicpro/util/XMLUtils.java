package haivo.us.scanmusicpro.util;

import android.os.Environment;
import android.util.Log;
import com.tickaroo.tikxml.TikXml;
import com.tickaroo.tikxml.XmlReader;
import haivo.us.scanmusicpro.model.xml.Pitch;
import haivo.us.scanmusicpro.model.xml.ScorePartWise;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class XMLUtils {
    private static TikXml tikXmll;

    public static void parseTestXML() {
        tikXmll = new TikXml.Builder().exceptionOnUnreadXml(false).build();
        try {

            BufferedSource bufferedSource = sourceForFile("text.xml");
            ScorePartWise data = tikXmll.read(bufferedSource, ScorePartWise.class);
            List<Pitch> pitches = extractPitchList(0, data);
            Log.d("Hai", "Got data" + pitches);
        } catch (Exception e) {
            Log.e("HAI", "Ex ception:" + e.getMessage());
        }
    }
    public static void parseXML(File filePath) {
        tikXmll = new TikXml.Builder().exceptionOnUnreadXml(true).build();
        Source fileSource;
        try {
            fileSource = Okio.source(filePath);
            BufferedSource bufferedSource = Okio.buffer(fileSource);
            ScorePartWise data = tikXmll.read(bufferedSource, ScorePartWise.class);
            Log.d("Hai", "Got data" + data);
        } catch (Exception e) {
            Log.e("HAI", "Exception:" + e.getMessage());
        }

        //try {Source fileSource = Okio.source(filePath);
        //     BufferedSource bufferedSource = Okio.buffer(fileSource);
        //
        //    while (true) {
        //        String line = bufferedSource.readUtf8Line();
        //        if (line == null) break;
        //
        //        if (line.contains("square")) {
        //            System.out.println(line);
        //        }
        //    }
        //
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    public static XmlReader readerFrom(String xml) {
        return XmlReader.of(new Buffer().writeUtf8(xml));
    }

    public static XmlReader readerFromFile(String filePath) throws IOException {
        return XmlReader.of(Okio.buffer(Okio.source(new File(getResourcePath(filePath)))));
    }

    public static BufferedSource sourceForFile(String filePath) throws IOException {
        return Okio.buffer(Okio.source(new File(getResourcePath(filePath))));
    }

    /**
     * Get the resource path
     */
    private static String getResourcePath(String resPath) {

        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                          .getAbsolutePath()
                          .concat("/Camera/Scores" + "/" + "test.xml");
    }

    /**
     * Converts the buffers content to a String
     */
    public static String bufferToString(Buffer buffer) {
        return buffer.readString(Charset.defaultCharset());
    }

    public static List<Pitch> extractPitchList(int partIndex, ScorePartWise scorePartWise) {
        List<Pitch> result = new ArrayList<>();
        if (scorePartWise.getParts()!=null && scorePartWise.getParts().size() > partIndex) {
            final Part selectedPart = scorePartWise.getParts().get(partIndex);
            final List<Measure> measureList = selectedPart.getMeasureList();
            if (measureList != null && measureList.size() > 0) {
                for (int i = 0; i < measureList.size(); i++) {
                    final Measure eachMesure = selectedPart.getMeasureList().get(i);
                    if (eachMesure != null) {
                        final List<Note> noteList = eachMesure.getNoteList();
                        if (noteList != null && noteList.size() > 0) {
                            for (int j = 0; j < noteList.size(); j++) {
                                final Note eachNote = noteList.get(j);
                                if (eachNote.getPitch() != null) {
                                    result.add(eachNote.getPitch());
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
