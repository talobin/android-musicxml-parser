package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Time {
    @PropertyElement
    String beats;

    @PropertyElement(name = "beat-type")
    String beatType;

    @PropertyElement
    String symbol;

    public String getBeats() {
        return beats;
    }
    public String getBeatType() {
        return beatType;
    }
    public String getSymbol() {
        return symbol;
    }
}
