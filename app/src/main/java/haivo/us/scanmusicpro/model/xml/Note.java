package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

@Xml
public class Note {
    @Element
    Pitch pitch;

    @PropertyElement
    String duration;

    @PropertyElement
    String voice;

    @PropertyElement
    String type;

    @PropertyElement
    String stem;

    @PropertyElement
    String accidental;

    @PropertyElement
    String unpitched;

    @PropertyElement
    String staff;

    @PropertyElement
    String chord;

    @PropertyElement
    String rest;

    @Element
    Notation notation;

    @Element
    Beam beam;

    public Pitch getPitch() {
        return pitch;
    }

    public String getDuration() {
        return duration;
    }
    public String getVoice() {
        return voice;
    }
    public String getType() {
        return type;
    }
    public String getStem() {
        return stem;
    }
    public String getAccidental() {
        return accidental;
    }
    public String getUnpitched() {
        return unpitched;
    }
    public String getStaff() {
        return staff;
    }
    public String getChord() {
        return chord;
    }
    public String getRest() {
        return rest;
    }
    public Notation getNotation() {
        return notation;
    }
    public Beam getBeam() {
        return beam;
    }
}
