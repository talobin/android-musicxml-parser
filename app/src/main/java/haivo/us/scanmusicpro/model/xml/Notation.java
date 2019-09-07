package haivo.us.scanmusicpro.model.xml;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;
import java.util.List;

@Xml
public class Notation {
    @Element(name = "tied")
    List<Tied> tiedList;

    @Element(name = "slur")
    List<Slur> slurList;

    @Element
    //Articulations articulations;

    public List<Tied> getTiedList() {
        return tiedList;
    }
    public List<Slur> getSlurList() {
        return slurList;
    }
    //public Articulations getArticulations() {
    //    return articulations;
    //}
}
