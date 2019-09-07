package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Pitch {
    @PropertyElement
    String step;

    @PropertyElement
    String alter;

    @PropertyElement
    String octave;

    public String getStep() {
        return step;
    }
    public String getAlter() {
        return alter;
    }
    public String getOctave() {
        return octave;
    }
}
