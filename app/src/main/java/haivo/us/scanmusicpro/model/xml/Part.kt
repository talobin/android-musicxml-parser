package haivo.us.scanmusicpro.model.xml

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml
data class Part(

        @Attribute
        val id: String?,

        @Element(name = "measure")
        val measureList: List<Measure>?
)
